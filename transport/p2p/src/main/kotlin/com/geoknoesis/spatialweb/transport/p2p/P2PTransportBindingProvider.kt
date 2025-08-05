package com.geoknoesis.spatialweb.transport.p2p

import com.geoknoesis.spatialweb.core.transport.TransportBinding
import com.geoknoesis.spatialweb.core.transport.TransportBindingProvider
import com.geoknoesis.spatialweb.core.transport.TransportProtocols
import org.slf4j.LoggerFactory
import java.io.InputStream

/**
 * SPI Provider for P2P Transport Binding that supports configuration-driven instantiation.
 * 
 * This provider can create multiple P2P transport binding instances based on
 * configuration provided by the spatial node (typically from YAML files).
 */
class P2PTransportBindingProvider : TransportBindingProvider {
    
    private val logger = LoggerFactory.getLogger(P2PTransportBindingProvider::class.java)
    private val instances = mutableMapOf<String, P2PTransportBinding>()
    
    override fun getProviderName(): String = "p2p"
    
    override fun getSupportedProtocols(): List<String> = TransportProtocols.P2P.ALL
    
    override fun createInstances(configStream: InputStream?): List<TransportBinding> {
        if (configStream == null) {
            // Create a default instance if no configuration is provided
            logger.info("No configuration provided, creating default P2P transport binding")
            val defaultInstance = P2PTransportConfigFactory.createFromConfig(P2PTransportConfig.local())
            instances["default"] = defaultInstance
            return listOf(defaultInstance)
        }
        
        try {
            val configs = P2PTransportConfigFactory.loadFromYaml(configStream)
            logger.info("Loaded ${configs.size} P2P transport configurations")
            
            val transportBindings = P2PTransportConfigFactory.createFromConfigs(configs)
            
            // Store instances with their names for later access
            configs.zip(transportBindings).forEach { (config, binding) ->
                instances[config.name] = binding as P2PTransportBinding
                logger.info("Created P2P transport binding: ${config.name} -> ${config.listenAddresses}")
            }
            
            return transportBindings
            
        } catch (e: Exception) {
            logger.error("Failed to create P2P transport bindings from configuration", e)
            // Fallback to default instance
            val defaultInstance = P2PTransportConfigFactory.createFromConfig(P2PTransportConfig.local())
            instances["default"] = defaultInstance
            return listOf(defaultInstance)
        }
    }
    
    override fun createInstance(config: Map<String, Any>): TransportBinding? {
        return try {
            val p2pConfig = P2PTransportConfig(
                name = config["name"] as? String ?: "p2p",
                listenAddresses = (config["listenAddresses"] as? List<*>)?.mapNotNull { it as? String } ?: listOf("/ip4/0.0.0.0/tcp/4001"),
                bootstrapPeers = (config["bootstrapPeers"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                enableDiscovery = config["enableDiscovery"] as? Boolean ?: true,
                enablePubSub = config["enablePubSub"] as? Boolean ?: true,
                enablePing = config["enablePing"] as? Boolean ?: true,
                maxConnections = (config["maxConnections"] as? Number)?.toInt() ?: 100,
                connectionTimeoutMs = (config["connectionTimeoutMs"] as? Number)?.toLong() ?: 30000,
                messageTimeoutMs = (config["messageTimeoutMs"] as? Number)?.toLong() ?: 10000,
                enableRelay = config["enableRelay"] as? Boolean ?: false,
                enableNAT = config["enableNAT"] as? Boolean ?: true,
                enableMetrics = config["enableMetrics"] as? Boolean ?: false,
                privateKeyPath = config["privateKeyPath"] as? String,
                enableCompression = config["enableCompression"] as? Boolean ?: true,
                enableLogging = config["enableLogging"] as? Boolean ?: true,
                customProtocols = (config["customProtocols"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
            )
            
            val instance = P2PTransportConfigFactory.createFromConfig(p2pConfig)
            instances[p2pConfig.name] = instance
            instance
            
        } catch (e: Exception) {
            logger.error("Failed to create P2P transport binding from config map", e)
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
        logger.info("Shutting down P2P transport binding provider")
        instances.values.forEach { binding ->
            try {
                if (binding.isActive()) {
                    binding.stop()
                }
            } catch (e: Exception) {
                logger.warn("Error stopping P2P transport binding: ${e.message}")
            }
        }
        instances.clear()
    }
} 