package com.geoknoesis.spatialweb.transport.mqtt

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class MQTTTransportConfigFactoryTest {

    @Test
    fun `should create transport binding from config`() {
        val config = MQTTTransportConfig.local()
        val binding = MQTTTransportConfigFactory.createFromConfig(config)
        
        assertNotNull(binding)
        assertEquals(config, binding.getConfig())
    }

    @Test
    fun `should create multiple transport bindings from configs`() {
        val configs = listOf(
            MQTTTransportConfig.local(),
            MQTTTransportConfig.testnet()
        )
        
        val bindings = MQTTTransportConfigFactory.createFromConfigs(configs)
        
        assertEquals(2, bindings.size)
        assertEquals("mqtt-local", (bindings[0] as MQTTTransportBinding).getConfig().name)
        assertEquals("mqtt-testnet", (bindings[1] as MQTTTransportBinding).getConfig().name)
    }

    @Test
    fun `should create transport binding from map`() {
        val configMap = mapOf(
            "name" to "test-mqtt",
            "brokerUrl" to "tcp://test.example.com:1883",
            "topicPrefix" to "spatialweb/test",
            "qos" to 2,
            "enableTLS" to true
        )
        
        val binding = MQTTTransportConfigFactory.createFromMap(configMap)
        
        assertNotNull(binding)
        assertEquals("test-mqtt", binding.getConfig().name)
        assertEquals("tcp://test.example.com:1883", binding.getConfig().brokerUrl)
        assertEquals("spatialweb/test", binding.getConfig().topicPrefix)
        assertEquals(2, binding.getConfig().qos)
        assertTrue(binding.getConfig().enableTLS)
    }

    @Test
    fun `should use default values when map is missing fields`() {
        val configMap = mapOf<String, Any>(
            "name" to "minimal-mqtt"
        )
        
        val binding = MQTTTransportConfigFactory.createFromMap(configMap)
        
        assertNotNull(binding)
        assertEquals("minimal-mqtt", binding.getConfig().name)
        assertEquals("tcp://localhost:1883", binding.getConfig().brokerUrl)
        assertEquals("spatialweb", binding.getConfig().topicPrefix)
        assertEquals(1, binding.getConfig().qos)
        assertTrue(binding.getConfig().cleanSession)
        assertTrue(binding.getConfig().automaticReconnect)
    }
} 