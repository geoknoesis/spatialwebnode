package com.geoknoesis.kastor.spi

import com.geoknoesis.kastor.runtime.config.KastorConfig
import com.geoknoesis.rdf.RdfApi

/**
 * Service Provider Interface for RDF API implementations.
 * 
 * Backend modules implement this interface to provide RDF API instances
 * that can be dynamically loaded at runtime based on configuration.
 */
interface RdfApiProvider {
    
    /**
     * Returns the unique identifier for this backend implementation.
     * This ID is used in configuration files to select the backend.
     * 
     * @return Backend ID (e.g., "jena", "rdf4j", "sparql")
     */
    fun id(): String
    
    /**
     * Creates and configures an RdfApi instance based on the provided configuration.
     * 
     * @param config The Kastor configuration containing backend-specific settings
     * @return Configured RdfApi instance
     * @throws IllegalArgumentException if the configuration is invalid for this backend
     * @throws RuntimeException if the backend cannot be initialized
     */
    fun create(config: KastorConfig): RdfApi
    
    /**
     * Returns whether this provider supports the given configuration.
     * This allows providers to validate configuration before attempting to create an instance.
     * 
     * @param config The configuration to validate
     * @return true if this provider can handle the configuration, false otherwise
     */
    fun supports(config: KastorConfig): Boolean {
        return config.backend == id()
    }
    
    /**
     * Returns a human-readable description of this backend provider.
     * Used for logging and error messages.
     * 
     * @return Description of the backend
     */
    fun description(): String {
        return "RDF API Provider for ${id()}"
    }
}