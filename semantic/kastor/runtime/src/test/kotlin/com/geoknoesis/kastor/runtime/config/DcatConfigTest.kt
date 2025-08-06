package com.geoknoesis.kastor.runtime.config

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DcatConfigTest {

    @Test
    fun `should load DCAT-US configuration correctly`() {
        val yamlContent = """
            schemaPaths:
              - src/main/resources/dcat-us/dcat-us-3.0.shacl.ttl

            contextPaths:
              - src/main/resources/dcat-us/dcat.context.jsonld

            interfacePackages:
              - com.example.catalog

            prefixMappings:
              dcat: "http://www.w3.org/ns/dcat#"
              dcterms: "http://purl.org/dc/terms/"

            namespaceMappings:
              dcat: "gov.us.dcat.core"
              dcterms: "gov.us.dcat.terms"

            datatypeMappings:
              "http://www.w3.org/2001/XMLSchema#date": "java.time.LocalDate"

            options:
              validation: true
              generateImpl: true
              extrasProperty: true

            backend: "jena"
        """.trimIndent()
        
        val config = KastorConfigLoader.loadFromString(yamlContent)
        
        // Verify schema paths
        assertEquals(1, config.schemaPaths.size)
        assertEquals("src/main/resources/dcat-us/dcat-us-3.0.shacl.ttl", config.schemaPaths[0])
        
        // Verify context paths
        assertEquals(1, config.contextPaths.size)
        assertEquals("src/main/resources/dcat-us/dcat.context.jsonld", config.contextPaths[0])
        
        // Verify interface packages
        assertEquals(1, config.interfacePackages.size)
        assertEquals("com.example.catalog", config.interfacePackages[0])
        
        // Verify prefix mappings
        assertEquals("http://www.w3.org/ns/dcat#", config.prefixMappings["dcat"])
        assertEquals("http://purl.org/dc/terms/", config.prefixMappings["dcterms"])
        
        // Verify namespace mappings
        assertEquals("gov.us.dcat.core", config.namespaceMappings["dcat"])
        assertEquals("gov.us.dcat.terms", config.namespaceMappings["dcterms"])
        
        // Verify datatype mappings
        assertEquals("java.time.LocalDate", config.datatypeMappings["http://www.w3.org/2001/XMLSchema#date"])
        
        // Verify options
        assertEquals(true, config.options["validation"])
        assertEquals(true, config.options["generateImpl"])
        assertEquals(true, config.options["extrasProperty"])
        
        // Verify backend
        assertEquals("jena", config.backend)
    }

    @Test
    fun `should handle DCAT configuration with government-specific settings`() {
        val yamlContent = """
            schemaPaths:
              - schemas/dcat-us-3.0.shacl.ttl
              - schemas/pod-1.1.ttl

            interfacePackages:
              - gov.us.dcat.core
              - gov.us.dcat.terms
              - gov.us.pod

            prefixMappings:
              dcat: "http://www.w3.org/ns/dcat#"
              dcterms: "http://purl.org/dc/terms/"
              pod: "https://project-open-data.cio.gov/v1.1/schema#"
              vcard: "http://www.w3.org/2006/vcard/ns#"
              foaf: "http://xmlns.com/foaf/0.1/"

            options:
              validation: true
              strictMode: true
              compliance: "dcat-us-3.0"

            backend: "jena"
        """.trimIndent()
        
        val config = KastorConfigLoader.loadFromString(yamlContent)
        
        // Verify multiple schemas
        assertEquals(2, config.schemaPaths.size)
        assertTrue(config.schemaPaths.contains("schemas/dcat-us-3.0.shacl.ttl"))
        assertTrue(config.schemaPaths.contains("schemas/pod-1.1.ttl"))
        
        // Verify government packages
        assertEquals(3, config.interfacePackages.size)
        assertTrue(config.interfacePackages.contains("gov.us.dcat.core"))
        assertTrue(config.interfacePackages.contains("gov.us.dcat.terms"))
        assertTrue(config.interfacePackages.contains("gov.us.pod"))
        
        // Verify comprehensive prefix mappings
        assertEquals("http://www.w3.org/ns/dcat#", config.prefixMappings["dcat"])
        assertEquals("https://project-open-data.cio.gov/v1.1/schema#", config.prefixMappings["pod"])
        assertEquals("http://xmlns.com/foaf/0.1/", config.prefixMappings["foaf"])
        
        // Verify custom options
        assertEquals(true, config.options["validation"])
        assertEquals(true, config.options["strictMode"])
        assertEquals("dcat-us-3.0", config.options["compliance"])
    }
}