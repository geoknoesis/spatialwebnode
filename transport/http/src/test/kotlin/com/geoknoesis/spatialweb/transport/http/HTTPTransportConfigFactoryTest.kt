package com.geoknoesis.spatialweb.transport.http

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.io.ByteArrayInputStream

class HTTPTransportConfigFactoryTest {

    @Test
    fun `test create from single config`() {
        val config = HTTPTransportConfig(
            name = "test",
            baseUrl = "https://test.example.com",
            timeoutMs = 45000,
            enableWebSockets = false
        )
        
        val binding = HTTPTransportConfigFactory.createFromConfig(config)
        assertNotNull(binding)
        assertTrue(binding is HTTPTransportBinding)
    }

    @Test
    fun `test create from multiple configs`() {
        val configs = listOf(
            HTTPTransportConfig(name = "local", baseUrl = "http://localhost:8080"),
            HTTPTransportConfig(name = "prod", baseUrl = "https://api.example.com")
        )
        
        val bindings = HTTPTransportConfigFactory.createFromConfigs(configs)
        assertEquals(2, bindings.size)
        assertTrue(bindings.all { it is HTTPTransportBinding })
    }

    @Test
    fun `test load from YAML string`() {
        val yamlString = """
            - name: "test"
              baseUrl: "https://test.example.com"
              timeoutMs: 30000
              enableWebSockets: true
        """.trimIndent()
        
        val inputStream = ByteArrayInputStream(yamlString.toByteArray())
        val configs = HTTPTransportConfigFactory.loadFromYaml(inputStream)
        assertEquals(1, configs.size)
        assertEquals("test", configs[0].name)
        assertEquals("https://test.example.com", configs[0].baseUrl)
    }

    @Test
    fun `test load from YAML input stream`() {
        val yamlString = """
            - name: "stream-test"
              baseUrl: "https://stream.example.com"
              timeoutMs: 45000
              enableWebSockets: false
        """.trimIndent()
        
        val inputStream = ByteArrayInputStream(yamlString.toByteArray())
        val configs = HTTPTransportConfigFactory.loadFromYaml(inputStream)
        assertEquals(1, configs.size)
        assertEquals("stream-test", configs[0].name)
        assertEquals("https://stream.example.com", configs[0].baseUrl)
    }

    @Test
    fun `test create from YAML string`() {
        val yamlString = """
            - name: "yaml-test"
              baseUrl: "https://yaml.example.com"
              timeoutMs: 60000
              enableWebSockets: true
        """.trimIndent()
        
        val inputStream = ByteArrayInputStream(yamlString.toByteArray())
        val bindings = HTTPTransportConfigFactory.createFromYaml(inputStream)
        assertEquals(1, bindings.size)
        assertTrue(bindings[0] is HTTPTransportBinding)
    }

    @Test
    fun `test empty YAML returns empty list`() {
        val yamlString = "[]"
        val inputStream = ByteArrayInputStream(yamlString.toByteArray())
        val configs = HTTPTransportConfigFactory.loadFromYaml(inputStream)
        assertTrue(configs.isEmpty())
    }

    @Test
    fun `test YAML with invalid format`() {
        val yamlString = "otherKey: value"
        val inputStream = ByteArrayInputStream(yamlString.toByteArray())
        val configs = HTTPTransportConfigFactory.loadFromYaml(inputStream)
        assertTrue(configs.isEmpty())
    }
} 