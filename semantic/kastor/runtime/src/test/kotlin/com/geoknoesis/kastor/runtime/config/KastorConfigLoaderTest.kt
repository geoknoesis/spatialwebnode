package com.geoknoesis.kastor.runtime.config

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class KastorConfigLoaderTest {

    @Test
    fun `should load config from YAML string`() {
        val yamlContent = """
            backend: "rdf4j"
            schemaPaths:
              - "test.shacl"
            prefixMappings:
              foaf: "http://xmlns.com/foaf/0.1/"
            options:
              enableValidation: true
        """.trimIndent()
        
        val config = KastorConfigLoader.loadFromString(yamlContent)
        
        assertEquals("rdf4j", config.backend)
        assertEquals(listOf("test.shacl"), config.schemaPaths)
        assertEquals("http://xmlns.com/foaf/0.1/", config.prefixMappings["foaf"])
        assertEquals(true, config.options["enableValidation"])
    }

    @Test
    fun `should return default config when loading from empty YAML`() {
        val yamlContent = "{}" // Valid empty YAML object
        
        val config = KastorConfigLoader.loadFromString(yamlContent)
        
        assertEquals("jena", config.backend) // default backend
        assertTrue(config.schemaPaths.isEmpty())
        assertTrue(config.prefixMappings.isEmpty())
    }

    @Test
    fun `should handle missing file gracefully with loadOrDefault`() {
        val config = KastorConfigLoader.loadOrDefault("non-existent-file.yaml")
        
        assertNotNull(config)
        assertEquals("jena", config.backend)
    }

    @Test
    fun `should throw exception for missing file with load`() {
        assertThrows<IllegalStateException> {
            KastorConfigLoader.load("non-existent-file.yaml")
        }
    }

    @Test
    fun `should handle JSON-LD options`() {
        val yamlContent = """
            jsonld:
              generateFromPojo: true
              outputPath: "custom/path.jsonld"
              mergeWithExisting: false
        """.trimIndent()
        
        val config = KastorConfigLoader.loadFromString(yamlContent)
        
        assertTrue(config.jsonld.generateFromPojo)
        assertEquals("custom/path.jsonld", config.jsonld.outputPath)
        assertEquals(false, config.jsonld.mergeWithExisting)
    }
}