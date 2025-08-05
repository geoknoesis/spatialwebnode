package com.geoknoesis.spatialweb.transport.mqtt

import com.geoknoesis.spatialweb.core.transport.TransportBinding
import com.geoknoesis.spatialweb.core.transport.TransportBindingProvider
import com.geoknoesis.spatialweb.core.transport.TransportProtocols
import org.slf4j.LoggerFactory
import java.io.InputStream

/**
 * SPI Provider for MQTT Transport Binding that supports configuration-driven instantiation.
 * 
 * This provider can create multiple MQTT transport binding instances based on
 * configuration provided by the spatial node (typically from YAML files).
 */
class MQTTTransportBindingProvider : TransportBindingProvider {
    
    private val logger = LoggerFactory.getLogger(MQTTTransportBindingProvider::class.java)
    private val instances = mutableMapOf<String, MQTTTransportBinding>()
    
    override fun getProviderName(): String = "mqtt"
    
    override fun getSupportedProtocols(): List<String> = TransportProtocols.MQTT.ALL
    
    override fun createInstances(configStream: InputStream?): List<TransportBinding> {
        if (configStream == null) {
            // Create a default instance if no configuration is provided
            logger.info("No configuration provided, creating default MQTT transport binding")
            val defaultInstance = MQTTTransportConfigFactory.createFromConfig(MQTTTransportConfig.local())
            instances["default"] = defaultInstance
            return listOf(defaultInstance)
        }
        
        try {
            val configs = MQTTTransportConfigFactory.loadFromYaml(configStream)
            logger.info("Loaded ${configs.size} MQTT transport configurations")
            
            val transportBindings = MQTTTransportConfigFactory.createFromConfigs(configs)
            
            // Store instances with their names for later access
            configs.zip(transportBindings).forEach { (config, binding) ->
                instances[config.name] = binding as MQTTTransportBinding
                logger.info("Created MQTT transport binding: ${config.name} -> ${config.brokerUrl}")
            }
            
            return transportBindings
            
        } catch (e: Exception) {
            logger.error("Failed to create MQTT transport bindings from configuration", e)
            // Fallback to default instance
            val defaultInstance = MQTTTransportConfigFactory.createFromConfig(MQTTTransportConfig.local())
            instances["default"] = defaultInstance
            return listOf(defaultInstance)
        }
    }
    
    override fun createInstance(config: Map<String, Any>): TransportBinding? {
        return try {
            val mqttConfig = MQTTTransportConfig(
                name = config["name"] as? String ?: "mqtt",
                brokerUrl = config["brokerUrl"] as? String ?: "tcp://localhost:1883",
                clientId = config["clientId"] as? String,
                username = config["username"] as? String,
                password = config["password"] as? String,
                cleanSession = config["cleanSession"] as? Boolean ?: true,
                connectionTimeout = (config["connectionTimeout"] as? Number)?.toInt() ?: 30,
                keepAliveInterval = (config["keepAliveInterval"] as? Number)?.toInt() ?: 60,
                maxInflight = (config["maxInflight"] as? Number)?.toInt() ?: 1000,
                automaticReconnect = config["automaticReconnect"] as? Boolean ?: true,
                maxReconnectDelay = (config["maxReconnectDelay"] as? Number)?.toInt() ?: 10000,
                topicPrefix = config["topicPrefix"] as? String ?: "spatialweb",
                qos = (config["qos"] as? Number)?.toInt() ?: 1,
                retainMessages = config["retainMessages"] as? Boolean ?: false,
                enableTLS = config["enableTLS"] as? Boolean ?: false,
                tlsVersion = config["tlsVersion"] as? String ?: "TLSv1.2",
                enableLogging = config["enableLogging"] as? Boolean ?: true,
                enableMetrics = config["enableMetrics"] as? Boolean ?: false,
                customProperties = (config["customProperties"] as? Map<*, *>)?.mapKeys { it.key.toString() }?.mapValues { it.value.toString() } ?: emptyMap()
            )
            
            val instance = MQTTTransportConfigFactory.createFromConfig(mqttConfig)
            instances[mqttConfig.name] = instance
            instance
            
        } catch (e: Exception) {
            logger.error("Failed to create MQTT transport binding from config map", e)
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
        logger.info("Shutting down MQTT transport binding provider")
        instances.values.forEach { binding ->
            try {
                if (binding.isActive()) {
                    binding.stop()
                }
            } catch (e: Exception) {
                logger.warn("Error stopping MQTT transport binding: ${e.message}")
            }
        }
        instances.clear()
    }
} 