package com.geoknoesis.spatialweb.transport.mqtt

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.geoknoesis.spatialweb.core.transport.TransportBinding
import org.slf4j.LoggerFactory
import java.io.InputStream

/**
 * Factory for creating MQTT transport configurations and bindings.
 */
object MQTTTransportConfigFactory {
    
    private val logger = LoggerFactory.getLogger(MQTTTransportConfigFactory::class.java)
    private val yamlMapper = ObjectMapper(YAMLFactory()).registerModule(KotlinModule.Builder().build())
    
    /**
     * Loads MQTT transport configurations from YAML input stream.
     */
    fun loadFromYaml(inputStream: InputStream): List<MQTTTransportConfig> {
        return try {
            val configs: List<MQTTTransportConfig> = yamlMapper.readValue(inputStream)
            logger.info("Loaded ${configs.size} MQTT transport configurations from YAML")
            configs
        } catch (e: Exception) {
            logger.error("Failed to load MQTT transport configurations from YAML", e)
            emptyList()
        }
    }
    
    /**
     * Creates an MQTT transport binding from a single configuration.
     */
    fun createFromConfig(config: MQTTTransportConfig): MQTTTransportBinding {
        logger.info("Creating MQTT transport binding: ${config.name}")
        return MQTTTransportBinding(config)
    }
    
    /**
     * Creates multiple MQTT transport bindings from configurations.
     */
    fun createFromConfigs(configs: List<MQTTTransportConfig>): List<TransportBinding> {
        return configs.map { createFromConfig(it) }
    }
    
    /**
     * Creates an MQTT transport binding from a configuration map.
     */
    fun createFromMap(configMap: Map<String, Any>): MQTTTransportBinding {
        val config = MQTTTransportConfig(
            name = configMap["name"] as? String ?: "mqtt",
            brokerUrl = configMap["brokerUrl"] as? String ?: "tcp://localhost:1883",
            clientId = configMap["clientId"] as? String,
            username = configMap["username"] as? String,
            password = configMap["password"] as? String,
            cleanSession = configMap["cleanSession"] as? Boolean ?: true,
            connectionTimeout = (configMap["connectionTimeout"] as? Number)?.toInt() ?: 30,
            keepAliveInterval = (configMap["keepAliveInterval"] as? Number)?.toInt() ?: 60,
            maxInflight = (configMap["maxInflight"] as? Number)?.toInt() ?: 1000,
            automaticReconnect = configMap["automaticReconnect"] as? Boolean ?: true,
            maxReconnectDelay = (configMap["maxReconnectDelay"] as? Number)?.toInt() ?: 10000,
            topicPrefix = configMap["topicPrefix"] as? String ?: "spatialweb",
            qos = (configMap["qos"] as? Number)?.toInt() ?: 1,
            retainMessages = configMap["retainMessages"] as? Boolean ?: false,
            enableTLS = configMap["enableTLS"] as? Boolean ?: false,
            tlsVersion = configMap["tlsVersion"] as? String ?: "TLSv1.2",
            enableLogging = configMap["enableLogging"] as? Boolean ?: true,
            enableMetrics = configMap["enableMetrics"] as? Boolean ?: false,
            customProperties = (configMap["customProperties"] as? Map<*, *>)?.mapKeys { it.key.toString() }?.mapValues { it.value.toString() } ?: emptyMap()
        )
        return createFromConfig(config)
    }
} 