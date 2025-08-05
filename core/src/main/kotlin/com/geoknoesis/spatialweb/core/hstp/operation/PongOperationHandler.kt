package com.geoknoesis.spatialweb.core.hstp.operation

import com.geoknoesis.spatialweb.core.hstp.engine.MessageContext
import org.slf4j.LoggerFactory

/**
 * Operation handler for pong operations.
 * 
 * Handles incoming pong messages which are responses to ping messages.
 * The pong operation is used to confirm connectivity and measure latency.
 */
class PongOperationHandler : OperationHandler {
    
    private val logger = LoggerFactory.getLogger(PongOperationHandler::class.java)
    
    override val operation: String = "pong"
    
    override suspend fun handle(context: MessageContext) {
        val message = context.message
        logger.debug("Received pong response from ${message.header.source}")
        
        // Log the round-trip time if this is a response to our ping
        message.header.inReplyTo?.let { pingId ->
            logger.debug("Pong response for ping: $pingId")
            // TODO: Calculate and log round-trip time if we track ping timestamps
        }
        
        logger.debug("Pong operation completed")
    }
} 