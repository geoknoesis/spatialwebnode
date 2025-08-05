package com.geoknoesis.spatialweb.transport.http

import com.geoknoesis.spatialweb.core.transport.TransportBinding
import com.geoknoesis.spatialweb.core.transport.TransportBindingProvider
import com.geoknoesis.spatialweb.core.transport.TransportProtocols
import com.geoknoesis.spatialweb.core.hstp.model.HSTPMessage
import org.slf4j.LoggerFactory
import java.io.InputStream

/**
 * SPI Provider for HTTP Transport Binding that supports configuration-driven instantiation.
 * 
 * This provider can create multiple HTTP transport binding instances based on
 * configuration provided by the spatial node (typically from YAML files).
 */
class HTTPTransportBindingProvider : TransportBindingProvider {
    
    private val logger = LoggerFactory.getLogger(HTTPTransportBindingProvider::class.java)
    private val instances = mutableMapOf<String, HTTPTransportBinding>()
    
    override fun getProviderName(): String = "http"
    
    override fun getSupportedProtocols(): List<String> = TransportProtocols.HTTP.ALL
    
    override fun createInstances(configStream: InputStream?): List<TransportBinding> {
        if (configStream == null) {
            // Create a default instance if no configuration is provided
            logger.info("No configuration provided, creating default HTTP transport binding")
            val defaultInstance = HTTPTransportConfigFactory.createFromConfig(HTTPTransportConfig.local())
            instances["default"] = defaultInstance
            return listOf(defaultInstance)
        }
        
        try {
            val configs = HTTPTransportConfigFactory.loadFromYaml(configStream)
            logger.info("Loaded ${configs.size} HTTP transport configurations")
            
            val transportBindings = HTTPTransportConfigFactory.createFromConfigs(configs)
            
            // Store instances with their names for later access
            configs.zip(transportBindings).forEach { (config, binding) ->
                instances[config.name] = binding
                logger.info("Created HTTP transport binding: ${config.name} -> ${config.baseUrl}")
            }
            
            return transportBindings
            
        } catch (e: Exception) {
            logger.error("Failed to create HTTP transport bindings from configuration", e)
            // Fallback to default instance
            val defaultInstance = HTTPTransportConfigFactory.createFromConfig(HTTPTransportConfig.local())
            instances["default"] = defaultInstance
            return listOf(defaultInstance)
        }
    }
    
    override fun createInstance(config: Map<String, Any>): TransportBinding? {
        return try {
            val httpConfig = HTTPTransportConfig(
                name = config["name"] as? String ?: "default",
                baseUrl = config["baseUrl"] as? String ?: "http://localhost:8080",
                timeoutMs = (config["timeoutMs"] as? Number)?.toLong() ?: 30000,
                enableWebSockets = config["enableWebSockets"] as? Boolean ?: true,
                maxConnections = (config["maxConnections"] as? Number)?.toInt() ?: 10,
                retryAttempts = (config["retryAttempts"] as? Number)?.toInt() ?: 3,
                retryDelayMs = (config["retryDelayMs"] as? Number)?.toLong() ?: 1000,
                enableCompression = config["enableCompression"] as? Boolean ?: true,
                enableLogging = config["enableLogging"] as? Boolean ?: true,
                customHeaders = config["customHeaders"] as? Map<String, String> ?: emptyMap()
            )
            
            val instance = HTTPTransportConfigFactory.createFromConfig(httpConfig)
            instances[httpConfig.name] = instance
            instance
            
        } catch (e: Exception) {
            logger.error("Failed to create HTTP transport binding from config map", e)
            null
        }
    }
    
    override fun getInstance(name: String): TransportBinding? {
        return instances[name]
    }
    
    override fun getAllInstances(): List<TransportBinding> {
        return instances.values.toList()
    }
    
    override fun shutdown() {
        logger.info("Shutting down HTTP transport binding provider")
        instances.values.forEach { binding ->
            try {
                if (binding.isActive()) {
                    binding.stop()
                }
            } catch (e: Exception) {
                logger.warn("Error stopping HTTP transport binding: ${e.message}")
            }
        }
        instances.clear()
    }
} 