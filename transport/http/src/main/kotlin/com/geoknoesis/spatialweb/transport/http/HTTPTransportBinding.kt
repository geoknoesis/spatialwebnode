package com.geoknoesis.spatialweb.transport.http

import com.geoknoesis.spatialweb.core.hstp.model.HSTPMessage
import com.geoknoesis.spatialweb.core.transport.TransportBinding
import com.geoknoesis.spatialweb.identity.did.Did
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import java.net.URL
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * HTTP Transport Binding implementation using Ktor HTTP client.
 * Supports both HTTP/HTTPS and WebSocket connections for HSTP message transport.
 */
class HTTPTransportBinding(
    private val baseUrl: String = "http://localhost:8080",
    private val timeoutMs: Long = 30000,
    private val enableWebSockets: Boolean = true
) : TransportBinding {

    private val logger = LoggerFactory.getLogger(HTTPTransportBinding::class.java)
    private val isRunning = AtomicBoolean(false)
    private val messageHandlers = mutableListOf<(HSTPMessage) -> Unit>()
    private val webSocketSessions = ConcurrentHashMap<String, WebSocketSession>()
    private val httpClient: HttpClient
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    init {
        httpClient = HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
            
            if (enableWebSockets) {
                install(WebSockets) {
                    maxFrameSize = Long.MAX_VALUE
                }
            }
            
            expectSuccess = false
        }
    }

    override fun start() {
        if (isRunning.compareAndSet(false, true)) {
            logger.info("Starting HTTP Transport Binding on base URL: $baseUrl")
            // HTTP transport doesn't need persistent connection setup
            // WebSocket connections will be established as needed
        }
    }

    override fun stop() {
        if (isRunning.compareAndSet(true, false)) {
            logger.info("Stopping HTTP Transport Binding")
            
            // Close all WebSocket sessions
            webSocketSessions.values.forEach { session ->
                runBlocking {
                    try {
                        session.close()
                    } catch (e: Exception) {
                        logger.warn("Error closing WebSocket session: ${e.message}")
                    }
                }
            }
            webSocketSessions.clear()
            
            // Close HTTP client
            runBlocking {
                httpClient.close()
            }
            
            scope.cancel()
        }
    }

    override fun send(message: HSTPMessage) {
        if (!isRunning.get()) {
            throw IllegalStateException("HTTP Transport Binding is not running")
        }

        scope.launch {
            try {
                when {
                    shouldUseWebSocket(message) -> sendViaWebSocket(message)
                    else -> sendViaHttp(message)
                }
            } catch (e: Exception) {
                logger.error("Error sending HSTP message: ${e.message}", e)
                // Could implement retry logic here
            }
        }
    }

    override fun onReceive(handler: (message: HSTPMessage) -> Unit) {
        messageHandlers.add(handler)
    }

    /**
     * Determines if WebSocket should be used for this message
     */
    private fun shouldUseWebSocket(message: HSTPMessage): Boolean {
        return enableWebSockets && 
               (message.header.expectResponse || 
                message.header.channel != null ||
                message.header.operation.startsWith("subscribe") ||
                message.header.operation.startsWith("publish"))
    }

    /**
     * Sends HSTP message via HTTP/HTTPS
     */
    private suspend fun sendViaHttp(message: HSTPMessage) {
        val url = buildUrl(message)
        val payload = message.payload.first() // Get first chunk for HTTP
        
        val response = httpClient.post(url) {
            contentType(ContentType.Application.Json)
            setBody(payload)
            
            // Add HSTP headers as HTTP headers
            headers {
                append("X-HSTP-Operation", message.header.operation)
                append("X-HSTP-Source", message.header.source.toString())
                message.header.destination?.let { append("X-HSTP-Destination", it.toString()) }
                message.header.channel?.let { append("X-HSTP-Channel", it.toString()) }
                message.header.inReplyTo?.let { append("X-HSTP-InReplyTo", it) }
                append("X-HSTP-ExpectResponse", message.header.expectResponse.toString())
                append("X-HSTP-Timestamp", message.header.timestamp.toString())
            }
        }

        if (response.status.isSuccess()) {
            logger.debug("HSTP message sent successfully via HTTP to $url")
            
            // Handle response if expected
            if (message.header.expectResponse) {
                val responseBody = response.body<ByteArray>()
                val responseMessage = createResponseMessage(message, responseBody)
                notifyHandlers(responseMessage)
            }
        } else {
            logger.warn("HTTP request failed with status: ${response.status}")
        }
    }

    /**
     * Sends HSTP message via WebSocket
     */
    private suspend fun sendViaWebSocket(message: HSTPMessage) {
        val wsUrl = buildWebSocketUrl(message)
        val sessionKey = wsUrl.toString()
        
        val session = webSocketSessions.getOrPut(sessionKey) {
            httpClient.webSocketSession(wsUrl)
        }
        
        try {
            // Send the message as JSON
            val messageJson = serializeMessage(message)
            session.send(Frame.Text(messageJson))
            
            // Listen for responses if expected
            if (message.header.expectResponse) {
                listenForWebSocketResponses(session, message)
            }
            
            logger.debug("HSTP message sent successfully via WebSocket to $wsUrl")
        } catch (e: Exception) {
            logger.error("WebSocket send failed: ${e.message}", e)
            webSocketSessions.remove(sessionKey)
            throw e
        }
    }

    /**
     * Listens for WebSocket responses
     */
    private suspend fun listenForWebSocketResponses(session: WebSocketSession, originalMessage: HSTPMessage) {
        try {
            for (frame in session.incoming) {
                when (frame) {
                    is Frame.Text -> {
                        val responseMessage = deserializeMessage(frame.readText(), originalMessage)
                        notifyHandlers(responseMessage)
                        break // Exit after first response
                    }
                    is Frame.Close -> {
                        logger.debug("WebSocket connection closed")
                        break
                    }
                    else -> {
                        // Ignore other frame types
                    }
                }
            }
        } catch (e: Exception) {
            logger.error("Error listening for WebSocket responses: ${e.message}", e)
        }
    }

    /**
     * Builds the HTTP URL for the message
     */
    private fun buildUrl(message: HSTPMessage): String {
        val base = URL(baseUrl)
        val path = when {
            message.header.destination != null -> "/hstp/direct"
            message.header.channel != null -> "/hstp/channel/${message.header.channel}"
            else -> "/hstp"
        }
        return "$baseUrl$path"
    }

    /**
     * Builds the WebSocket URL for the message
     */
    private fun buildWebSocketUrl(message: HSTPMessage): String {
        val base = URL(baseUrl)
        val wsProtocol = if (base.protocol == "https") "wss" else "ws"
        val path = when {
            message.header.destination != null -> "/hstp/ws/direct"
            message.header.channel != null -> "/hstp/ws/channel/${message.header.channel}"
            else -> "/hstp/ws"
        }
        return "$wsProtocol://${base.host}:${base.port}$path"
    }

    /**
     * Serializes HSTP message to JSON
     */
    private suspend fun serializeMessage(message: HSTPMessage): String {
        // Simple JSON serialization - in production, use proper serialization library
        val payload = message.payload.first()
        return """
        {
            "header": {
                "id": "${message.header.id}",
                "operation": "${message.header.operation}",
                "source": "${message.header.source}",
                "destination": ${message.header.destination?.let { "\"$it\"" } ?: "null"},
                "channel": ${message.header.channel?.let { "\"$it\"" } ?: "null"},
                "status": ${message.header.status ?: "null"},
                "inReplyTo": ${message.header.inReplyTo?.let { "\"$it\"" } ?: "null"},
                "mediaType": "${message.header.mediaType}",
                "timestamp": "${message.header.timestamp}",
                "expectResponse": ${message.header.expectResponse}
            },
            "payload": "${payload.toString(Charsets.UTF_8)}"
        }
        """.trimIndent()
    }

    /**
     * Deserializes JSON to HSTP message
     */
    private fun deserializeMessage(json: String, originalMessage: HSTPMessage): HSTPMessage {
        // Simple JSON deserialization - in production, use proper serialization library
        // This is a simplified implementation
        return originalMessage.copy(
            header = originalMessage.header.copy(
                status = 200,
                inReplyTo = originalMessage.header.id
            ),
            payload = flowOf(json.toByteArray())
        )
    }

    /**
     * Creates a response message from the original message
     */
    private fun createResponseMessage(originalMessage: HSTPMessage, responseBody: ByteArray): HSTPMessage {
        return HSTPMessage(
            header = originalMessage.header.copy(
                status = 200,
                inReplyTo = originalMessage.header.id,
                source = originalMessage.header.destination ?: originalMessage.header.source,
                destination = originalMessage.header.source
            ),
            payload = flowOf(responseBody)
        )
    }

    /**
     * Notifies all registered message handlers
     */
    private fun notifyHandlers(message: HSTPMessage) {
        messageHandlers.forEach { handler ->
            try {
                handler(message)
            } catch (e: Exception) {
                logger.error("Error in message handler: ${e.message}", e)
            }
        }
    }

    /**
     * Checks if the transport binding is currently running
     */
    fun isActive(): Boolean = isRunning.get()

    /**
     * Gets the base URL for this transport binding
     */
    fun getBaseUrl(): String = baseUrl
} 