package com.geoknoesis.kastor.runtime

import com.geoknoesis.rdf.RdfApi
import com.geoknoesis.kastor.runtime.config.KastorConfig
import com.geoknoesis.kastor.runtime.config.KastorConfigLoader

/**
 * Kastor Runtime Loader
 * 
 * Provides factory methods for loading and configuring RDF API instances
 * using configuration files and the SPI (Service Provider Interface) pattern.
 */
object KastorRuntime {
    
    /**
     * Load an RdfApi instance from a configuration file.
     * This will use the SPI pattern to load the appropriate backend implementation.
     * 
     * @param configPath Path to the YAML configuration file
     * @return Configured RdfApi instance
     */
    fun load(configPath: String = "kastor.yaml"): RdfApi {
        val config = KastorConfigLoader.load(configPath)
        return RdfApiLoader.fromConfig(config)
    }
    
    /**
     * Load an RdfApi instance from a KastorConfig object.
     * 
     * @param config The configuration object
     * @return Configured RdfApi instance
     */
    fun load(config: KastorConfig): RdfApi {
        return RdfApiLoader.fromConfig(config)
    }
    
    /**
     * Load an RdfApi instance with default configuration.
     * This will attempt to auto-detect available backends.
     * 
     * @return RdfApi instance with default configuration
     */
    fun loadDefault(): RdfApi {
        return RdfApiLoader.loadDefault()
    }
    
    /**
     * Load configuration with fallback to defaults.
     * 
     * @param configPath Path to the YAML configuration file
     * @return KastorConfig instance with defaults applied if file doesn't exist
     */
    fun loadConfig(configPath: String = "kastor.yaml"): KastorConfig {
        return KastorConfigLoader.loadOrDefault(configPath)
    }
}

/**
 * SPI loader for RDF API implementations.
 * Uses Java's ServiceLoader to dynamically discover and load backend implementations.
 */
object RdfApiLoader {
    
    private val providers: List<com.geoknoesis.kastor.spi.RdfApiProvider> by lazy {
        java.util.ServiceLoader.load(com.geoknoesis.kastor.spi.RdfApiProvider::class.java)
            .toList()
    }
    
    /**
     * Create an RdfApi instance from configuration.
     * Uses the SPI pattern to load the appropriate backend implementation.
     * 
     * @param config The Kastor configuration
     * @return Configured RdfApi instance
     * @throws IllegalStateException if no suitable provider is found
     * @throws IllegalArgumentException if the configuration is invalid
     */
    fun fromConfig(config: KastorConfig): RdfApi {
        val backendId = config.backend
        
        // Find the provider that matches the requested backend
        val provider = providers.find { it.supports(config) }
            ?: throw IllegalStateException(
                "No RDF API provider found for backend '$backendId'. " +
                "Available providers: ${getAvailableProviders().joinToString(", ")}"
            )
        
        return try {
            provider.create(config)
        } catch (e: Exception) {
            throw IllegalArgumentException(
                "Failed to create RDF API instance using provider '${provider.id()}': ${e.message}", 
                e
            )
        }
    }
    
    /**
     * Load default RdfApi implementation.
     * Attempts to auto-detect available backends and use the first one found.
     * 
     * @return RdfApi instance with default configuration
     * @throws IllegalStateException if no providers are available
     */
    fun loadDefault(): RdfApi {
        if (providers.isEmpty()) {
            throw IllegalStateException(
                "No RDF API providers found on classpath. " +
                "Please ensure at least one backend module (jena, rdf4j, sparql) is included."
            )
        }
        
        // Use the first available provider with default configuration
        val provider = providers.first()
        val defaultConfig = KastorConfig(backend = provider.id())
        
        return provider.create(defaultConfig)
    }
    
    /**
     * Get list of available backend provider IDs.
     * 
     * @return List of provider IDs that can be used in configuration
     */
    fun getAvailableProviders(): List<String> {
        return providers.map { it.id() }
    }
    
    /**
     * Check if a specific backend is available.
     * 
     * @param backendId The backend ID to check
     * @return true if the backend is available, false otherwise
     */
    fun isBackendAvailable(backendId: String): Boolean {
        return providers.any { it.id() == backendId }
    }
    
    /**
     * Get detailed information about all available providers.
     * 
     * @return Map of provider ID to description
     */
    fun getProviderInfo(): Map<String, String> {
        return providers.associate { it.id() to it.description() }
    }
}