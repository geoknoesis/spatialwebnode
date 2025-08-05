package com.geoknoesis.spatialweb.transport.p2p

import com.geoknoesis.spatialweb.core.hstp.model.HSTPMessage
import com.geoknoesis.spatialweb.core.hstp.model.HSTPHeader
import com.geoknoesis.spatialweb.identity.did.Did
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class P2PTransportBindingTest {

    @Test
    fun `should create P2P transport binding with local config`() {
        val config = P2PTransportConfig.local()
        val binding = P2PTransportBinding(config)
        
        assertEquals("p2p-local", config.name)
        assertFalse(config.enableDiscovery)
        assertTrue(config.enablePubSub)
        assertTrue(config.enablePing)
        assertEquals(10, config.maxConnections)
    }

    @Test
    fun `should create P2P transport binding with testnet config`() {
        val config = P2PTransportConfig.testnet()
        val binding = P2PTransportBinding(config)
        
        assertEquals("p2p-testnet", config.name)
        assertTrue(config.enableDiscovery)
        assertTrue(config.enablePubSub)
        assertTrue(config.enablePing)
        assertEquals(200, config.maxConnections)
        assertTrue(config.bootstrapPeers.isNotEmpty())
    }

    @Test
    fun `should start and stop transport binding`() = runTest {
        val config = P2PTransportConfig.local()
        val binding = P2PTransportBinding(config)
        
        // Initially not running
        assertFalse(binding.isActive())
        
        // Start the binding
        binding.start()
        assertTrue(binding.isActive())
        
        // Stop the binding
        binding.stop()
        assertFalse(binding.isActive())
    }

    @Test
    fun `should register message handlers`() {
        val config = P2PTransportConfig.local()
        val binding = P2PTransportBinding(config)
        
        var receivedMessage: HSTPMessage? = null
        
        binding.onReceive { message ->
            receivedMessage = message
        }
        
        // The handler should be registered (we can't easily test the callback without starting the binding)
        // This test just verifies the method doesn't throw an exception
    }

    @Test
    fun `should throw exception when sending message to stopped binding`() = runTest {
        val config = P2PTransportConfig.local()
        val binding = P2PTransportBinding(config)
        
        val message = HSTPMessage(
            header = HSTPHeader(
                id = "test_msg",
                operation = "test",
                source = Did("did:example:test"),
                destination = Did("did:example:target"),
                expectResponse = false
            ),
            payload = flowOf("test".toByteArray())
        )
        
        assertThrows<IllegalStateException> {
            binding.send(message)
        }
    }

    @Test
    fun `should throw exception when subscribing to stopped binding`() = runTest {
        val config = P2PTransportConfig.local()
        val binding = P2PTransportBinding(config)
        
        val channel = Did("did:example:channel")
        
        assertThrows<IllegalStateException> {
            binding.subscribe(channel)
        }
    }

    @Test
    fun `should get correct configuration`() {
        val config = P2PTransportConfig.local()
        val binding = P2PTransportBinding(config)
        
        assertEquals(config, binding.getConfig())
    }
} 