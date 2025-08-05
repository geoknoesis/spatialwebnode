package com.geoknoesis.spatialweb.core.hstp.operation

import com.geoknoesis.spatialweb.core.hstp.model.HSTPMessage
import com.geoknoesis.spatialweb.core.hstp.model.HSTPHeader
import com.geoknoesis.spatialweb.identity.did.Did
import kotlinx.coroutines.flow.flowOf
import java.time.Instant
import java.util.UUID

/**
 * Utility class for ping operations.
 * 
 * Provides helper methods for creating ping messages and measuring round-trip times.
 */
object PingUtils {
    
    /**
     * Creates a ping message to the specified destination.
     * 
     * @param source The source DID
     * @param destination The destination DID
     * @param expectResponse Whether to expect a pong response
     * @param customPayload Optional custom payload for the ping
     * @return A new HSTPMessage with ping operation
     */
    fun createPingMessage(
        source: Did,
        destination: Did,
        expectResponse: Boolean = true,
        customPayload: String = "ping"
    ): HSTPMessage {
        val header = HSTPHeader(
            id = UUID.randomUUID().toString(),
            operation = "ping",
            source = source,
            destination = destination,
            expectResponse = expectResponse,
            timestamp = Instant.now()
        )
        
        val payload = flowOf(customPayload.toByteArray())
        
        return HSTPMessage(header, payload)
    }
    
    /**
     * Creates a ping message to a channel (pub/sub).
     * 
     * @param source The source DID
     * @param channel The channel DID
     * @param expectResponse Whether to expect a pong response
     * @param customPayload Optional custom payload for the ping
     * @return A new HSTPMessage with ping operation
     */
    fun createChannelPingMessage(
        source: Did,
        channel: Did,
        expectResponse: Boolean = false,
        customPayload: String = "ping"
    ): HSTPMessage {
        val header = HSTPHeader(
            id = UUID.randomUUID().toString(),
            operation = "ping",
            source = source,
            channel = channel,
            expectResponse = expectResponse,
            timestamp = Instant.now()
        )
        
        val payload = flowOf(customPayload.toByteArray())
        
        return HSTPMessage(header, payload)
    }
    
    /**
     * Calculates the round-trip time between a ping and pong message.
     * 
     * @param pingMessage The original ping message
     * @param pongMessage The pong response message
     * @return The round-trip time in milliseconds, or null if timestamps are invalid
     */
    fun calculateRoundTripTime(pingMessage: HSTPMessage, pongMessage: HSTPMessage): Long? {
        val pingTime = pingMessage.header.timestamp
        val pongTime = pongMessage.header.timestamp
        
        return if (pingTime != null && pongTime != null) {
            java.time.Duration.between(pingTime, pongTime).toMillis()
        } else {
            null
        }
    }
    
    /**
     * Checks if a pong message is a response to a specific ping message.
     * 
     * @param pingMessage The original ping message
     * @param pongMessage The pong response message
     * @return true if the pong is a response to the ping
     */
    fun isPongResponse(pingMessage: HSTPMessage, pongMessage: HSTPMessage): Boolean {
        return pongMessage.header.operation == "pong" &&
               pongMessage.header.inReplyTo == pingMessage.header.id
    }
} 