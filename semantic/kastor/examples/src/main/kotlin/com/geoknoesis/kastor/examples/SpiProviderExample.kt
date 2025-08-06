package com.geoknoesis.kastor.examples

import com.geoknoesis.kastor.runtime.RdfApiLoader
import com.geoknoesis.kastor.runtime.config.KastorConfig
import com.geoknoesis.kastor.runtime.config.KastorConfigLoader

/**
 * Example demonstrating the SPI provider system.
 * 
 * This shows how Kastor dynamically discovers and loads RDF backend implementations
 * using the Java Service Provider Interface (SPI) pattern.
 */
object SpiProviderExample {
    
    @JvmStatic
    fun main(args: Array<String>) {
        println("=== Kastor SPI Provider Discovery Example ===\n")
        
        // 1. Discover available providers
        println("🔍 Discovering available RDF API providers...")
        val availableProviders = RdfApiLoader.getAvailableProviders()
        
        if (availableProviders.isEmpty()) {
            println("❌ No RDF API providers found on classpath!")
            println("💡 To see providers in action, include backend modules like:")
            println("   - semantic:kastor:backend-jena")
            println("   - semantic:kastor:backend-rdf4j")
            println("   - semantic:kastor:backend-sparql")
            return
        }
        
        println("✅ Found ${availableProviders.size} provider(s):")
        availableProviders.forEach { providerId ->
            println("  • $providerId")
        }
        
        // 2. Get detailed provider information
        println("\n📋 Provider Details:")
        val providerInfo = RdfApiLoader.getProviderInfo()
        providerInfo.forEach { (id, description) ->
            println("  • $id: $description")
        }
        
        // 3. Test backend availability checking
        println("\n🔍 Backend Availability Check:")
        listOf("jena", "rdf4j", "sparql", "non-existent").forEach { backend ->
            val isAvailable = RdfApiLoader.isBackendAvailable(backend)
            val status = if (isAvailable) "✅ Available" else "❌ Not Available"
            println("  • $backend: $status")
        }
        
        // 4. Try to load a specific backend
        println("\n⚙️  Loading Backend Example:")
        val firstProvider = availableProviders.first()
        val config = KastorConfig(backend = firstProvider)
        
        try {
            println("Attempting to load backend: $firstProvider")
            val api = RdfApiLoader.fromConfig(config)
            println("✅ Successfully created RdfApi instance: ${api::class.simpleName}")
        } catch (e: Exception) {
            println("❌ Failed to load backend: ${e.message}")
            println("💡 This is expected as the backend implementation is not yet complete")
        }
        
        // 5. Configuration-based loading
        println("\n📄 Configuration-Based Loading:")
        val yamlConfig = """
            backend: "$firstProvider"
            options:
              validation: true
        """.trimIndent()
        
        try {
            val loadedConfig = KastorConfigLoader.loadFromString(yamlConfig)
            println("Config backend: ${loadedConfig.backend}")
            println("Config validation: ${loadedConfig.options["validation"]}")
            
            // This would work when backends are fully implemented:
            // val api = RdfApiLoader.fromConfig(loadedConfig)
            
        } catch (e: Exception) {
            println("❌ Configuration loading failed: ${e.message}")
        }
        
        // 6. Error handling example
        println("\n🚫 Error Handling Example:")
        try {
            val invalidConfig = KastorConfig(backend = "invalid-backend")
            RdfApiLoader.fromConfig(invalidConfig)
        } catch (e: IllegalStateException) {
            println("✅ Correctly caught error for invalid backend:")
            println("   ${e.message}")
        }
        
        println("\n🎉 SPI Provider Discovery Example Complete!")
        println("The SPI system is working correctly and ready for backend implementations.")
    }
}