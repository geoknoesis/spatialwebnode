package com.geoknoesis.spatialweb.core.hstp.operation

import com.geoknoesis.spatialweb.core.hstp.engine.DefaultHSTPEngine
import com.geoknoesis.spatialweb.core.hstp.engine.MessageContext
import com.geoknoesis.spatialweb.core.hstp.model.HSTPMessage
import com.geoknoesis.spatialweb.identity.did.Did
import com.geoknoesis.spatialweb.identity.did.DidDocumentManager
import com.geoknoesis.spatialweb.identity.vc.CredentialVerifier
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.mockito.kotlin.mock
import java.time.Instant

class PingOperationTest {
    
    @Test
    fun `test ping operation handler creation`() {
        val handler = PingOperationHandler()
        assertEquals("ping", handler.operation)
    }
    
    @Test
    fun `test pong operation handler creation`() {
        val handler = PongOperationHandler()
        assertEquals("pong", handler.operation)
    }
    
    @Test
    fun `test ping utils create ping message`() {
        val source = Did("did:example:alice")
        val destination = Did("did:example:bob")
        
        val pingMessage = PingUtils.createPingMessage(source, destination)
        
        assertEquals("ping", pingMessage.header.operation)
        assertEquals(source, pingMessage.header.source)
        assertEquals(destination, pingMessage.header.destination)
        assertTrue(pingMessage.header.expectResponse)
        assertNotNull(pingMessage.header.id)
        assertNotNull(pingMessage.header.timestamp)
    }
    
    @Test
    fun `test ping utils create channel ping message`() {
        val source = Did("did:example:alice")
        val channel = Did("did:example:channel")
        
        val pingMessage = PingUtils.createChannelPingMessage(source, channel)
        
        assertEquals("ping", pingMessage.header.operation)
        assertEquals(source, pingMessage.header.source)
        assertEquals(channel, pingMessage.header.channel)
        assertFalse(pingMessage.header.expectResponse)
        assertNull(pingMessage.header.destination)
    }
    
    @Test
    fun `test ping utils calculate round trip time`() {
        val pingTime = Instant.now()
        val pongTime = pingTime.plusSeconds(1) // 1 second later
        
        val pingMessage = HSTPMessage(
            header = com.geoknoesis.spatialweb.core.hstp.model.HSTPHeader(
                id = "ping_1",
                operation = "ping",
                source = Did("did:example:alice"),
                destination = Did("did:example:bob"),
                timestamp = pingTime
            ),
            payload = flowOf("ping".toByteArray())
        )
        
        val pongMessage = HSTPMessage(
            header = com.geoknoesis.spatialweb.core.hstp.model.HSTPHeader(
                id = "pong_1",
                operation = "pong",
                source = Did("did:example:bob"),
                destination = Did("did:example:alice"),
                inReplyTo = "ping_1",
                timestamp = pongTime
            ),
            payload = flowOf("pong".toByteArray())
        )
        
        val rtt = PingUtils.calculateRoundTripTime(pingMessage, pongMessage)
        assertEquals(1000L, rtt) // 1000ms = 1 second
    }
    
    @Test
    fun `test ping utils is pong response`() {
        val pingMessage = HSTPMessage(
            header = com.geoknoesis.spatialweb.core.hstp.model.HSTPHeader(
                id = "ping_1",
                operation = "ping",
                source = Did("did:example:alice"),
                destination = Did("did:example:bob")
            ),
            payload = flowOf("ping".toByteArray())
        )
        
        val pongMessage = HSTPMessage(
            header = com.geoknoesis.spatialweb.core.hstp.model.HSTPHeader(
                id = "pong_1",
                operation = "pong",
                source = Did("did:example:bob"),
                destination = Did("did:example:alice"),
                inReplyTo = "ping_1"
            ),
            payload = flowOf("pong".toByteArray())
        )
        
        assertTrue(PingUtils.isPongResponse(pingMessage, pongMessage))
    }
    
    @Test
    fun `test ping operation handler with mock engine`() = runBlocking {
        val mockDidManager = mock<DidDocumentManager>()
        val mockCredentialVerifier = mock<CredentialVerifier>()
        val mockTransportManager = mock<com.geoknoesis.spatialweb.core.transport.TransportManager>()
        
        val operationManager = com.geoknoesis.spatialweb.core.hstp.operation.OperationManager()
        val engine = DefaultHSTPEngine(operationManager, mockDidManager, mockCredentialVerifier, mockTransportManager)
        
        val pingHandler = PingOperationHandler()
        operationManager.register(pingHandler)
        
        val pingMessage = PingUtils.createPingMessage(
            Did("did:example:alice"),
            Did("did:example:bob")
        )
        
        val context = MessageContext(pingMessage, engine)
        
        // This should not throw an exception
        pingHandler.handle(context)
    }
} 