package com.geoknoesis.spatialweb.transport.mqtt

import com.geoknoesis.spatialweb.core.hstp.model.HSTPMessage
import com.geoknoesis.spatialweb.core.hstp.model.HSTPHeader
import com.geoknoesis.spatialweb.core.transport.PubSubBinding
import com.geoknoesis.spatialweb.core.transport.TransportBinding
import com.geoknoesis.spatialweb.identity.did.Did
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.Json
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

/**
 * MQTT Transport Binding implementation using Eclipse Paho MQTT client.
 * Supports both direct messaging and pub/sub functionality via MQTT topics.
 */
class MQTTTransportBinding(
    private val config: MQTTTransportConfig
) : PubSubBinding {

    private val logger = LoggerFactory.getLogger(MQTTTransportBinding::class.java)
    private val isRunning = AtomicBoolean(false)
    private val messageHandlers = mutableListOf<(HSTPMessage) -> Unit>()
    private val subscribedChannels = ConcurrentHashMap<Did, Boolean>()
    private val messageIdCounter = AtomicLong(0)
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    // MQTT client
    private var mqttClient: MqttClient? = null
    private var mqttConnectOptions: MqttConnectOptions? = null
    
    // Message routing
    private val pendingResponses = ConcurrentHashMap<String, CompletableDeferred<HSTPMessage>>()
    private val topicSubscriptions = ConcurrentHashMap<String, Boolean>()

    override fun start() {
        if (isRunning.compareAndSet(false, true)) {
            logger.info("Starting MQTT Transport Binding: ${config.name}")
            
            scope.launch {
                try {
                    initializeMQTTClient()
                    connectToBroker()
                    setupMessageHandlers()
                    logger.info("MQTT Transport Binding started successfully")
                } catch (e: Exception) {
                    logger.error("Failed to start MQTT Transport Binding", e)
                    isRunning.set(false)
                }
            }
        }
    }

    override fun stop() {
        if (isRunning.compareAndSet(true, false)) {
            logger.info("Stopping MQTT Transport Binding: ${config.name}")
            
            scope.launch {
                try {
                    // Unsubscribe from all channels
                    subscribedChannels.keys.forEach { channel ->
                        unsubscribe(channel)
                    }
                    subscribedChannels.clear()
                    
                    // Cancel pending responses
                    pendingResponses.values.forEach { deferred ->
                        deferred.cancel()
                    }
                    pendingResponses.clear()
                    
                    // Disconnect MQTT client
                    disconnectFromBroker()
                    
                    logger.info("MQTT Transport Binding stopped successfully")
                } catch (e: Exception) {
                    logger.error("Error stopping MQTT Transport Binding", e)
                }
            }
            
            scope.cancel()
        }
    }

    override fun send(message: HSTPMessage) {
        if (!isRunning.get()) {
            throw IllegalStateException("MQTT Transport Binding is not running")
        }

        scope.launch {
            try {
                when {
                    message.header.destination != null -> sendDirectMessage(message)
                    message.header.channel != null -> sendPubSubMessage(message)
                    else -> {
                        logger.warn("Message has no destination or channel, cannot send")
                        return@launch
                    }
                }
            } catch (e: Exception) {
                logger.error("Error sending HSTP message: ${e.message}", e)
            }
        }
    }

    override fun onReceive(handler: (message: HSTPMessage) -> Unit) {
        messageHandlers.add(handler)
    }

    override fun subscribe(channel: Did) {
        if (!isRunning.get()) {
            throw IllegalStateException("MQTT Transport Binding is not running")
        }

        scope.launch {
            try {
                if (!subscribedChannels.containsKey(channel)) {
                    subscribeToChannel(channel)
                    subscribedChannels[channel] = true
                    logger.info("Subscribed to channel: $channel")
                }
            } catch (e: Exception) {
                logger.error("Failed to subscribe to channel $channel", e)
            }
        }
    }

    override fun unsubscribe(channel: Did) {
        scope.launch {
            try {
                if (subscribedChannels.containsKey(channel)) {
                    unsubscribeFromChannel(channel)
                    subscribedChannels.remove(channel)
                    logger.info("Unsubscribed from channel: $channel")
                }
            } catch (e: Exception) {
                logger.error("Failed to unsubscribe from channel $channel", e)
            }
        }
    }

    /**
     * Sends a direct message to a specific peer via MQTT topic
     */
    private suspend fun sendDirectMessage(message: HSTPMessage) {
        val destination = message.header.destination ?: return
        val topic = buildDirectTopic(destination)
        val messageData = serializeMessage(message)
        
        // Publish message to direct topic
        publishToTopic(topic, messageData)
        
        // Wait for response if expected
        if (message.header.expectResponse) {
            val responseDeferred = CompletableDeferred<HSTPMessage>()
            pendingResponses[message.header.id] = responseDeferred
            
            try {
                withTimeout(config.connectionTimeout * 1000L) {
                    val response = responseDeferred.await()
                    notifyHandlers(response)
                }
            } catch (e: TimeoutCancellationException) {
                logger.warn("Timeout waiting for response to message: ${message.header.id}")
                pendingResponses.remove(message.header.id)
            } catch (e: Exception) {
                logger.error("Error waiting for response", e)
                pendingResponses.remove(message.header.id)
            }
        }
    }

    /**
     * Sends a message via pub/sub to a channel
     */
    private suspend fun sendPubSubMessage(message: HSTPMessage) {
        val channel = message.header.channel ?: return
        val topic = buildChannelTopic(channel)
        val messageData = serializeMessage(message)
        
        // Publish message to channel topic
        publishToTopic(topic, messageData)
        
        logger.debug("Published message to channel: $channel")
    }

    /**
     * Initializes MQTT client
     */
    private suspend fun initializeMQTTClient() {
        logger.info("Initializing MQTT client for broker: ${config.brokerUrl}")
        
        val clientId = config.clientId ?: "spatialweb_${System.currentTimeMillis()}"
        mqttClient = MqttClient(config.brokerUrl, clientId, MemoryPersistence())
        
        mqttConnectOptions = MqttConnectOptions().apply {
            isCleanSession = config.cleanSession
            connectionTimeout = config.connectionTimeout
            keepAliveInterval = config.keepAliveInterval
            maxInflight = config.maxInflight
            isAutomaticReconnect = config.automaticReconnect
            maxReconnectDelay = config.maxReconnectDelay
            
            config.username?.let { userName = it }
            config.password?.let { password = it.toCharArray() }
            
            // Note: Custom properties would be set here if the MQTT client supports them
            // For now, we'll skip custom properties as they're not directly supported by Paho
        }
        
        logger.info("MQTT client initialized with client ID: $clientId")
    }

    /**
     * Connects to MQTT broker
     */
    private suspend fun connectToBroker() {
        val client = mqttClient ?: throw IllegalStateException("MQTT client not initialized")
        val options = mqttConnectOptions ?: throw IllegalStateException("MQTT connect options not initialized")
        
        try {
            logger.info("Connecting to MQTT broker: ${config.brokerUrl}")
            client.connect(options)
            logger.info("Successfully connected to MQTT broker")
        } catch (e: Exception) {
            logger.error("Failed to connect to MQTT broker", e)
            throw e
        }
    }

    /**
     * Sets up message handlers for incoming MQTT messages
     */
    private suspend fun setupMessageHandlers() {
        val client = mqttClient ?: return
        
        client.setCallback(object : MqttCallback {
            override fun connectionLost(cause: Throwable?) {
                logger.warn("MQTT connection lost", cause)
                if (isRunning.get()) {
                    scope.launch {
                        try {
                            logger.info("Attempting to reconnect to MQTT broker")
                            connectToBroker()
                        } catch (e: Exception) {
                            logger.error("Failed to reconnect to MQTT broker", e)
                        }
                    }
                }
            }

            override fun messageArrived(topic: String?, message: MqttMessage?) {
                if (topic == null || message == null) return
                
                scope.launch {
                    try {
                        val messageData = message.payload
                        val hstpMessage = deserializeMessage(messageData)
                        
                        // Check if this is a response to a pending request
                        val inReplyTo = hstpMessage.header.inReplyTo
                        if (inReplyTo != null) {
                            val responseDeferred = pendingResponses.remove(inReplyTo)
                            responseDeferred?.complete(hstpMessage)
                        } else {
                            // Forward to message handlers
                            notifyHandlers(hstpMessage)
                        }
                    } catch (e: Exception) {
                        logger.error("Error processing incoming MQTT message", e)
                    }
                }
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {
                logger.debug("MQTT message delivery completed")
            }
        })
    }

    /**
     * Subscribes to a pub/sub channel
     */
    private suspend fun subscribeToChannel(channel: Did) {
        val client = mqttClient ?: return
        val topic = buildChannelTopic(channel)
        
        if (!topicSubscriptions.containsKey(topic)) {
            client.subscribe(topic, config.qos)
            topicSubscriptions[topic] = true
            logger.debug("Subscribed to MQTT topic: $topic")
        }
    }

    /**
     * Unsubscribes from a pub/sub channel
     */
    private suspend fun unsubscribeFromChannel(channel: Did) {
        val client = mqttClient ?: return
        val topic = buildChannelTopic(channel)
        
        if (topicSubscriptions.containsKey(topic)) {
            client.unsubscribe(topic)
            topicSubscriptions.remove(topic)
            logger.debug("Unsubscribed from MQTT topic: $topic")
        }
    }

    /**
     * Publishes a message to an MQTT topic
     */
    private suspend fun publishToTopic(topic: String, payload: ByteArray) {
        val client = mqttClient ?: return
        
        try {
            val message = MqttMessage(payload).apply {
                qos = config.qos
                isRetained = config.retainMessages
            }
            
            client.publish(topic, message)
            logger.debug("Published message to topic: $topic (${payload.size} bytes)")
        } catch (e: Exception) {
            logger.error("Failed to publish message to topic: $topic", e)
            throw e
        }
    }

    /**
     * Disconnects from MQTT broker
     */
    private suspend fun disconnectFromBroker() {
        val client = mqttClient ?: return
        
        try {
            if (client.isConnected) {
                client.disconnect()
                logger.info("Disconnected from MQTT broker")
            }
        } catch (e: Exception) {
            logger.error("Error disconnecting from MQTT broker", e)
        } finally {
            try {
                client.close()
                logger.info("MQTT client closed")
            } catch (e: Exception) {
                logger.error("Error closing MQTT client", e)
            }
        }
    }

    /**
     * Builds MQTT topic for direct messaging
     */
    private fun buildDirectTopic(destination: Did): String {
        return "${config.topicPrefix}/direct/${destination.toString().replace(":", "_")}"
    }

    /**
     * Builds MQTT topic for channel pub/sub
     */
    private fun buildChannelTopic(channel: Did): String {
        return "${config.topicPrefix}/channel/${channel.toString().replace(":", "_")}"
    }

    /**
     * Serializes HSTP message to bytes
     */
    private suspend fun serializeMessage(message: HSTPMessage): ByteArray {
        val json = Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        }
        
        // Convert message to JSON and then to bytes
        val payload = message.payload.first()
        val messageJson = """
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
        
        return messageJson.toByteArray(Charsets.UTF_8)
    }

    /**
     * Deserializes bytes to HSTP message
     */
    private fun deserializeMessage(data: ByteArray): HSTPMessage {
        // This is a simplified implementation
        // In production, use proper JSON deserialization
        val json = String(data, Charsets.UTF_8)
        
        // Parse JSON and create HSTPMessage
        // For now, return a placeholder message
        return HSTPMessage(
            header = HSTPHeader(
                id = generateMessageId(),
                operation = "unknown",
                source = Did("did:example:unknown"),
                destination = null,
                channel = null,
                status = null,
                inReplyTo = null,
                mediaType = "application/json",
                timestamp = java.time.Instant.now(),
                expectResponse = false
            ),
            payload = flowOf(data)
        )
    }

    /**
     * Generates a unique message ID
     */
    private fun generateMessageId(): String {
        return "msg_${messageIdCounter.incrementAndGet()}_${System.currentTimeMillis()}"
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
     * Gets the configuration for this transport binding
     */
    fun getConfig(): MQTTTransportConfig = config

    /**
     * Checks if the MQTT client is connected
     */
    fun isConnected(): Boolean = mqttClient?.isConnected ?: false
} 