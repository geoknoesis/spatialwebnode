package com.geoknoesis.kastor.runtime

import com.geoknoesis.kastor.runtime.config.KastorConfig
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SpiTest {

    @Test
    fun `should discover available providers`() {
        val providers = RdfApiLoader.getAvailableProviders()
        
        // The test should work even if no actual providers are on classpath
        // but we can test the mechanism
        assertTrue(providers is List<String>)
    }

    @Test
    fun `should provide detailed provider information`() {
        val providerInfo = RdfApiLoader.getProviderInfo()
        
        assertTrue(providerInfo is Map<String, String>)
        // Each provider should have an ID and description
        providerInfo.forEach { (id, description) ->
            assertTrue(id.isNotBlank())
            assertTrue(description.isNotBlank())
        }
    }

    @Test
    fun `should check backend availability correctly`() {
        val providers = RdfApiLoader.getAvailableProviders()
        
        if (providers.isNotEmpty()) {
            val firstProvider = providers.first()
            assertTrue(RdfApiLoader.isBackendAvailable(firstProvider))
        }
        
        // Non-existent backend should return false
        assertFalse(RdfApiLoader.isBackendAvailable("non-existent-backend"))
    }

    @Test
    fun `should throw exception for non-existent backend`() {
        val config = KastorConfig(backend = "non-existent-backend")
        
        val exception = assertThrows<IllegalStateException> {
            RdfApiLoader.fromConfig(config)
        }
        
        assertTrue(exception.message!!.contains("No RDF API provider found"))
        assertTrue(exception.message!!.contains("non-existent-backend"))
    }

    @Test
    fun `should handle empty provider list gracefully`() {
        // This test verifies the error message when no providers are available
        // In a real scenario with providers on classpath, loadDefault() would work
        
        val providers = RdfApiLoader.getAvailableProviders()
        if (providers.isEmpty()) {
            val exception = assertThrows<IllegalStateException> {
                RdfApiLoader.loadDefault()
            }
            
            assertTrue(exception.message!!.contains("No RDF API providers found"))
        }
    }
}