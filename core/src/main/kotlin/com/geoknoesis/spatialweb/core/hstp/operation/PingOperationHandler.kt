package com.geoknoesis.spatialweb.core.hstp.operation

import com.geoknoesis.spatialweb.core.hstp.engine.MessageContext
import com.geoknoesis.spatialweb.core.hstp.model.HSTPMessage
import com.geoknoesis.spatialweb.core.hstp.model.HSTPHeader
import com.geoknoesis.spatialweb.identity.did.Did
import kotlinx.coroutines.flow.flowOf
import org.slf4j.LoggerFactory
import java.time.Instant

/**
 * Operation handler for ping operations.
 * 
 * Handles incoming ping messages and responds with a pong message.
 * The ping operation is used for health checks and connectivity testing.
 */
class PingOperationHandler : OperationHandler {
    
    private val logger = LoggerFactory.getLogger(PingOperationHandler::class.java)
    
    override val operation: String = "ping"
    
    override suspend fun handle(context: MessageContext) {
        val message = context.message
        logger.debug("Handling ping message from ${message.header.source}")
        
        // Create pong response if a response is expected
        if (message.header.expectResponse) {
            val pongMessage = createPongResponse(message)
            sendResponse(context, pongMessage)
        }
        
        logger.debug("Ping operation completed")
    }
    
    /**
     * Creates a pong response message for the given ping message.
     */
    private fun createPongResponse(pingMessage: HSTPMessage): HSTPMessage {
        val pongHeader = HSTPHeader(
            id = java.util.UUID.randomUUID().toString(),
            operation = "pong",
            source = pingMessage.header.destination ?: Did("did:unknown:node"),
            destination = pingMessage.header.source,
            inReplyTo = pingMessage.header.id,
            status = 200,
            timestamp = Instant.now(),
            expectResponse = false
        )
        
        val pongPayload = flowOf("pong".toByteArray())
        
        return HSTPMessage(pongHeader, pongPayload)
    }
    
    /**
     * Sends a response message through the engine's transport binding.
     */
    private suspend fun sendResponse(context: MessageContext, response: HSTPMessage) {
        try {
            context.engine.sendResponse(response)
            logger.debug("Sent pong response: ${response.header.id}")
        } catch (e: Exception) {
            logger.error("Failed to send pong response: ${e.message}", e)
        }
    }
} 