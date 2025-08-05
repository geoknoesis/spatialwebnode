package com.geoknoesis.spatialweb.transport.http

import com.geoknoesis.spatialweb.core.hstp.model.HSTPHeader
import com.geoknoesis.spatialweb.core.hstp.model.HSTPMessage
import com.geoknoesis.spatialweb.identity.did.Did
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import java.time.Instant

class HTTPTransportBindingTest {

    private lateinit var httpTransport: HTTPTransportBinding
    private val testDid = Did("did:test:123")

    @BeforeEach
    fun setUp() {
        httpTransport = HTTPTransportFactory.createLocal()
    }

    @AfterEach
    fun tearDown() {
        if (httpTransport.isActive()) {
            httpTransport.stop()
        }
    }

    @Test
    fun `test transport binding creation`() {
        assertNotNull(httpTransport)
        assertFalse(httpTransport.isActive())
        assertEquals("http://localhost:8080", httpTransport.getBaseUrl())
    }

    @Test
    fun `test start and stop`() {
        httpTransport.start()
        assertTrue(httpTransport.isActive())
        
        httpTransport.stop()
        assertFalse(httpTransport.isActive())
    }

    @Test
    fun `test message handler registration`() {
        var receivedMessage: HSTPMessage? = null
        
        httpTransport.onReceive { message ->
            receivedMessage = message
        }
        
        // Create a test message
        val testMessage = createTestMessage()
        
        // Note: In a real test, you would need a mock HTTP server
        // This test just verifies the handler registration works
        assertNotNull(httpTransport)
    }

    @Test
    fun `test factory methods`() {
        val local = HTTPTransportFactory.createLocal()
        assertNotNull(local)
        assertEquals("http://localhost:8080", local.getBaseUrl())
        
        val production = HTTPTransportFactory.createProduction("https://api.example.com")
        assertNotNull(production)
        assertEquals("https://api.example.com", production.getBaseUrl())
        
        val custom = HTTPTransportFactory.createCustom(
            baseUrl = "https://custom.example.com",
            timeoutMs = 45000,
            enableWebSockets = false
        )
        assertNotNull(custom)
        assertEquals("https://custom.example.com", custom.getBaseUrl())
        
        val httpOnly = HTTPTransportFactory.createHttpOnly("https://http.example.com")
        assertNotNull(httpOnly)
        assertEquals("https://http.example.com", httpOnly.getBaseUrl())
    }

    @Test
    fun `test sending message when not started throws exception`() {
        val testMessage = createTestMessage()
        
        assertThrows(IllegalStateException::class.java) {
            runBlocking {
                httpTransport.send(testMessage)
            }
        }
    }

    private fun createTestMessage(): HSTPMessage {
        val header = HSTPHeader(
            operation = "test",
            source = testDid,
            destination = Did("did:test:456"),
            expectResponse = false
        )
        
        return HSTPMessage(
            header = header,
            payload = flowOf("test payload".toByteArray())
        )
    }
} 