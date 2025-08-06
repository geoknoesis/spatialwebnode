package com.geoknoesis.kastor.runtime

import com.geoknoesis.kastor.runtime.config.KastorConfig
import com.geoknoesis.kastor.runtime.config.KastorConfigLoader
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class BackendIntegrationTest {

    @Test
    fun `should load Jena backend when available`() {
        val config = KastorConfig(backend = "jena")
        
        // This test will pass when the jena backend is on classpath
        val providers = RdfApiLoader.getAvailableProviders()
        if ("jena" in providers) {
            val api = RdfApiLoader.fromConfig(config)
            assertNotNull(api)
        } else {
            assertThrows<IllegalStateException> {
                RdfApiLoader.fromConfig(config)
            }
        }
    }

    @Test
    fun `should load RDF4J backend when available`() {
        val config = KastorConfig(
            backend = "rdf4j",
            options = mapOf("repositoryType" to "memory")
        )
        
        // This test will pass when the rdf4j backend is on classpath
        val providers = RdfApiLoader.getAvailableProviders()
        if ("rdf4j" in providers) {
            val api = RdfApiLoader.fromConfig(config)
            assertNotNull(api)
        } else {
            assertThrows<IllegalStateException> {
                RdfApiLoader.fromConfig(config)
            }
        }
    }

    @Test
    fun `should load SPARQL backend with endpoint configuration`() {
        val config = KastorConfig(
            backend = "sparql",
            options = mapOf(
                "queryEndpoint" to "http://localhost:8080/sparql",
                "updateEndpoint" to "http://localhost:8080/update"
            )
        )
        
        // This test will pass when the sparql backend is on classpath
        val providers = RdfApiLoader.getAvailableProviders()
        if ("sparql" in providers) {
            val api = RdfApiLoader.fromConfig(config)
            assertNotNull(api)
        } else {
            assertThrows<IllegalStateException> {
                RdfApiLoader.fromConfig(config)
            }
        }
    }

    @Test
    fun `should reject SPARQL backend without endpoint configuration`() {
        val config = KastorConfig(backend = "sparql")
        
        val providers = RdfApiLoader.getAvailableProviders()
        if ("sparql" in providers) {
            assertThrows<IllegalArgumentException> {
                RdfApiLoader.fromConfig(config)
            }
        }
    }

    @Test
    fun `should load configuration for different backends`() {
        val jenaConfig = """
            backend: "jena"
            options:
              validation: true
        """.trimIndent()
        
        val rdf4jConfig = """
            backend: "rdf4j"
            options:
              repositoryType: "memory"
              validation: true
        """.trimIndent()
        
        val sparqlConfig = """
            backend: "sparql"
            options:
              queryEndpoint: "http://localhost:8080/sparql"
              validation: false
        """.trimIndent()
        
        // Test configuration parsing
        val jenaConfigObj = KastorConfigLoader.loadFromString(jenaConfig)
        assertEquals("jena", jenaConfigObj.backend)
        assertEquals(true, jenaConfigObj.options["validation"])
        
        val rdf4jConfigObj = KastorConfigLoader.loadFromString(rdf4jConfig)
        assertEquals("rdf4j", rdf4jConfigObj.backend)
        assertEquals("memory", rdf4jConfigObj.options["repositoryType"])
        
        val sparqlConfigObj = KastorConfigLoader.loadFromString(sparqlConfig)
        assertEquals("sparql", sparqlConfigObj.backend)
        assertEquals("http://localhost:8080/sparql", sparqlConfigObj.options["queryEndpoint"])
    }

    @Test
    fun `should provide helpful error messages for missing backends`() {
        val config = KastorConfig(backend = "non-existent")
        
        val exception = assertThrows<IllegalStateException> {
            RdfApiLoader.fromConfig(config)
        }
        
        assertTrue(exception.message!!.contains("No RDF API provider found"))
        assertTrue(exception.message!!.contains("non-existent"))
        assertTrue(exception.message!!.contains("Available providers:"))
    }
}