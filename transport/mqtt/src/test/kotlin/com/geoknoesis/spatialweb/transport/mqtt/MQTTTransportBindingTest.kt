package com.geoknoesis.spatialweb.transport.mqtt

import com.geoknoesis.spatialweb.core.hstp.model.HSTPMessage
import com.geoknoesis.spatialweb.core.hstp.model.HSTPHeader
import com.geoknoesis.spatialweb.identity.did.Did
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MQTTTransportBindingTest {

    @Test
    fun `should create MQTT transport binding with local config`() {
        val config = MQTTTransportConfig.local()
        val binding = MQTTTransportBinding(config)
        
        assertEquals("mqtt-local", config.name)
        assertEquals("tcp://localhost:1883", config.brokerUrl)
        assertEquals("spatialweb/local", config.topicPrefix)
        assertTrue(config.cleanSession)
        assertEquals(1, config.qos)
        assertFalse(config.retainMessages)
    }

    @Test
    fun `should create MQTT transport binding with testnet config`() {
        val config = MQTTTransportConfig.testnet()
        val binding = MQTTTransportBinding(config)
        
        assertEquals("mqtt-testnet", config.name)
        assertEquals("tcp://test.mosquitto.org:1883", config.brokerUrl)
        assertEquals("spatialweb/testnet", config.topicPrefix)
        assertTrue(config.automaticReconnect)
        assertEquals(1, config.qos)
    }

    @Test
    fun `should create MQTT transport binding with production config`() {
        val config = MQTTTransportConfig.production()
        val binding = MQTTTransportBinding(config)
        
        assertEquals("mqtt-production", config.name)
        assertEquals("tcp://mqtt.example.com:1883", config.brokerUrl)
        assertEquals("spatialweb", config.username)
        assertEquals("secure_password", config.password)
        assertEquals("spatialweb/prod", config.topicPrefix)
        assertEquals(2, config.qos)
        assertTrue(config.enableTLS)
        assertTrue(config.enableMetrics)
    }

    @Test
    fun `should start and stop transport binding`() = runTest {
        val config = MQTTTransportConfig.local()
        val binding = MQTTTransportBinding(config)
        
        // Initially not running
        assertFalse(binding.isActive())
        
        // Start the binding (this will fail to connect without a broker, but should start)
        binding.start()
        assertTrue(binding.isActive())
        
        // Stop the binding
        binding.stop()
        assertFalse(binding.isActive())
    }

    @Test
    fun `should register message handlers`() {
        val config = MQTTTransportConfig.local()
        val binding = MQTTTransportBinding(config)
        
        var receivedMessage: HSTPMessage? = null
        
        binding.onReceive { message ->
            receivedMessage = message
        }
        
        // The handler should be registered (we can't easily test the callback without starting the binding)
        // This test just verifies the method doesn't throw an exception
    }

    @Test
    fun `should throw exception when sending message to stopped binding`() = runTest {
        val config = MQTTTransportConfig.local()
        val binding = MQTTTransportBinding(config)
        
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
        val config = MQTTTransportConfig.local()
        val binding = MQTTTransportBinding(config)
        
        val channel = Did("did:example:channel")
        
        assertThrows<IllegalStateException> {
            binding.subscribe(channel)
        }
    }

    @Test
    fun `should get correct configuration`() {
        val config = MQTTTransportConfig.local()
        val binding = MQTTTransportBinding(config)
        
        assertEquals(config, binding.getConfig())
    }

    @Test
    fun `should not be connected when not started`() {
        val config = MQTTTransportConfig.local()
        val binding = MQTTTransportBinding(config)
        
        assertFalse(binding.isConnected())
    }
} 