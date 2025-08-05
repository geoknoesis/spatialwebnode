package com.geoknoesis.spatialweb.core.transport

import java.io.InputStream

/**
 * Provider interface for creating TransportBinding instances with configuration.
 * 
 * This interface allows transport binding implementations to be discovered via SPI
 * and create multiple instances based on configuration provided by the spatial node.
 */
interface TransportBindingProvider {
    
    /**
     * Gets the name of this provider.
     */
    fun getProviderName(): String
    
    /**
     * Gets the list of protocols supported by this provider.
     */
    fun getSupportedProtocols(): List<String>
    
    /**
     * Creates multiple transport binding instances from configuration.
     * 
     * @param configStream Input stream containing configuration (typically YAML)
     * @return List of created transport binding instances
     */
    fun createInstances(configStream: InputStream?): List<TransportBinding>
    
    /**
     * Creates a single transport binding instance from configuration map.
     * 
     * @param config Configuration map with key-value pairs
     * @return Created transport binding instance, or null if creation failed
     */
    fun createInstance(config: Map<String, Any>): TransportBinding?
    
    /**
     * Gets a specific instance by name.
     * 
     * @param name Name of the instance
     * @return Transport binding instance, or null if not found
     */
    fun getInstance(name: String): TransportBinding?
    
    /**
     * Gets all instances created by this provider.
     * 
     * @return List of all transport binding instances
     */
    fun getAllInstances(): List<TransportBinding>
    
    /**
     * Shuts down all instances created by this provider.
     */
    fun shutdown()
} 