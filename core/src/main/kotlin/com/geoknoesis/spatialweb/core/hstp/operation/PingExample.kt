package com.geoknoesis.spatialweb.core.hstp.operation

import com.geoknoesis.spatialweb.core.hstp.engine.DefaultHSTPEngine
import com.geoknoesis.spatialweb.core.hstp.engine.MessageContext
import com.geoknoesis.spatialweb.core.hstp.model.HSTPMessage
import com.geoknoesis.spatialweb.identity.did.Did
import com.geoknoesis.spatialweb.identity.did.DidDocumentManager
import com.geoknoesis.spatialweb.identity.vc.CredentialVerifier
import com.geoknoesis.spatialweb.identity.vc.VerificationResult
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

/**
 * Example application demonstrating ping operation usage.
 * 
 * This example shows how to:
 * 1. Set up the HSTP engine with ping handlers
 * 2. Create and send ping messages
 * 3. Handle ping/pong responses
 * 4. Measure round-trip times
 */
object PingExample {
    
    private val logger = LoggerFactory.getLogger(PingExample::class.java)
    
    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        logger.info("Starting Ping Example")
        
        // Create mock dependencies (in real app, these would be actual implementations)
        val mockDidManager = object : DidDocumentManager {
            override suspend fun getDidDocument(did: String, forceRefresh: Boolean): DidDocumentManager.Result {
                return DidDocumentManager.Result(
                    did = did,
                    document = null,
                    metadata = DidDocumentManager.Metadata()
                )
            }
            
            override suspend fun refreshDidDocument(did: String): DidDocumentManager.Result {
                return DidDocumentManager.Result(
                    did = did,
                    document = null,
                    metadata = DidDocumentManager.Metadata()
                )
            }
            
            override fun removeDidDocument(did: String) {
                // Mock implementation - do nothing
            }
        }
        
        val mockCredentialVerifier = object : CredentialVerifier {
            override suspend fun verify(credential: String): VerificationResult {
                return VerificationResult.Success()
            }
        }
        
        // Create operation manager and register handlers
        val operationManager = com.geoknoesis.spatialweb.core.hstp.operation.OperationManager()
        operationManager.register(PingOperationHandler())
        operationManager.register(PongOperationHandler())
        
        // Create engine (without transport manager for this example)
        val engine = DefaultHSTPEngine(
            operationManager = operationManager,
            didDocumentManager = mockDidManager,
            credentialVerifier = mockCredentialVerifier
        )
        
        // Example 1: Create a ping message
        val source = Did("did:example:alice")
        val destination = Did("did:example:bob")
        
        val pingMessage = PingUtils.createPingMessage(
            source = source,
            destination = destination,
            expectResponse = true,
            customPayload = "Hello from Alice!"
        )
        
        logger.info("Created ping message: ${pingMessage.header.id}")
        logger.info("Ping source: ${pingMessage.header.source}")
        logger.info("Ping destination: ${pingMessage.header.destination}")
        logger.info("Ping timestamp: ${pingMessage.header.timestamp}")
        
        // Example 2: Simulate handling a ping message
        logger.info("Simulating ping message handling...")
        val pingHandler = operationManager.resolve("ping")
        if (pingHandler != null) {
            val context = MessageContext(pingMessage, engine)
            pingHandler.handle(context)
        }
        
        // Example 3: Create a channel ping message
        val channel = Did("did:example:channel")
        val channelPing = PingUtils.createChannelPingMessage(
            source = source,
            channel = channel,
            expectResponse = false,
            customPayload = "Channel ping from Alice"
        )
        
        logger.info("Created channel ping message: ${channelPing.header.id}")
        logger.info("Channel ping channel: ${channelPing.header.channel}")
        
        // Example 4: Demonstrate round-trip time calculation
        val pingTime = pingMessage.header.timestamp
        val pongTime = pingTime.plusSeconds(1) // Simulate 1 second delay
        
        val pongMessage = HSTPMessage(
            header = pingMessage.header.copy(
                id = java.util.UUID.randomUUID().toString(),
                operation = "pong",
                source = destination,
                destination = source,
                inReplyTo = pingMessage.header.id,
                timestamp = pongTime
            ),
            payload = pingMessage.payload
        )
        
        val rtt = PingUtils.calculateRoundTripTime(pingMessage, pongMessage)
        logger.info("Calculated round-trip time: ${rtt}ms")
        
        // Example 5: Validate pong response
        val isValidPong = PingUtils.isPongResponse(pingMessage, pongMessage)
        logger.info("Is valid pong response: $isValidPong")
        
        // Example 6: Simulate handling a pong message
        logger.info("Simulating pong message handling...")
        val pongHandler = operationManager.resolve("pong")
        if (pongHandler != null) {
            val pongContext = MessageContext(pongMessage, engine)
            pongHandler.handle(pongContext)
        }
        
        logger.info("Ping Example completed successfully")
    }
} 