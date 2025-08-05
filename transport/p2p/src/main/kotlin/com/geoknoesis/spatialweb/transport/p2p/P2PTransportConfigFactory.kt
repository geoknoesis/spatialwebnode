package com.geoknoesis.spatialweb.transport.p2p

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.geoknoesis.spatialweb.core.transport.TransportBinding
import org.slf4j.LoggerFactory
import java.io.InputStream

/**
 * Factory for creating P2P transport configurations and bindings.
 */
object P2PTransportConfigFactory {
    
    private val logger = LoggerFactory.getLogger(P2PTransportConfigFactory::class.java)
    private val yamlMapper = ObjectMapper(YAMLFactory()).registerModule(KotlinModule.Builder().build())
    
    /**
     * Loads P2P transport configurations from YAML input stream.
     */
    fun loadFromYaml(inputStream: InputStream): List<P2PTransportConfig> {
        return try {
            val configs: List<P2PTransportConfig> = yamlMapper.readValue(inputStream)
            logger.info("Loaded ${configs.size} P2P transport configurations from YAML")
            configs
        } catch (e: Exception) {
            logger.error("Failed to load P2P transport configurations from YAML", e)
            emptyList()
        }
    }
    
    /**
     * Creates a P2P transport binding from a single configuration.
     */
    fun createFromConfig(config: P2PTransportConfig): P2PTransportBinding {
        logger.info("Creating P2P transport binding: ${config.name}")
        return P2PTransportBinding(config)
    }
    
    /**
     * Creates multiple P2P transport bindings from configurations.
     */
    fun createFromConfigs(configs: List<P2PTransportConfig>): List<TransportBinding> {
        return configs.map { createFromConfig(it) }
    }
    
    /**
     * Creates a P2P transport binding from a configuration map.
     */
    fun createFromMap(configMap: Map<String, Any>): P2PTransportBinding {
        val config = P2PTransportConfig(
            name = configMap["name"] as? String ?: "p2p",
            listenAddresses = (configMap["listenAddresses"] as? List<*>)?.mapNotNull { it as? String } ?: listOf("/ip4/0.0.0.0/tcp/4001"),
            bootstrapPeers = (configMap["bootstrapPeers"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
            enableDiscovery = configMap["enableDiscovery"] as? Boolean ?: true,
            enablePubSub = configMap["enablePubSub"] as? Boolean ?: true,
            enablePing = configMap["enablePing"] as? Boolean ?: true,
            maxConnections = (configMap["maxConnections"] as? Number)?.toInt() ?: 100,
            connectionTimeoutMs = (configMap["connectionTimeoutMs"] as? Number)?.toLong() ?: 30000,
            messageTimeoutMs = (configMap["messageTimeoutMs"] as? Number)?.toLong() ?: 10000,
            enableRelay = configMap["enableRelay"] as? Boolean ?: false,
            enableNAT = configMap["enableNAT"] as? Boolean ?: true,
            enableMetrics = configMap["enableMetrics"] as? Boolean ?: false,
            privateKeyPath = configMap["privateKeyPath"] as? String,
            enableCompression = configMap["enableCompression"] as? Boolean ?: true,
            enableLogging = configMap["enableLogging"] as? Boolean ?: true,
            customProtocols = (configMap["customProtocols"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
        )
        return createFromConfig(config)
    }
} 