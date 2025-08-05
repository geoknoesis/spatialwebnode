package com.geoknoesis.spatialweb.transport.http

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.geoknoesis.spatialweb.core.transport.TransportBinding
import org.slf4j.LoggerFactory
import java.io.InputStream

/**
 * Factory for creating HTTP Transport Binding instances from configuration.
 * Supports loading multiple configurations from YAML files.
 */
object HTTPTransportConfigFactory {
    
    private val logger = LoggerFactory.getLogger(HTTPTransportConfigFactory::class.java)
    private val yamlMapper = ObjectMapper(YAMLFactory()).registerModule(KotlinModule.Builder().build())

    /**
     * Creates a single HTTP transport binding from configuration.
     */
    fun createFromConfig(config: HTTPTransportConfig): HTTPTransportBinding {
        return HTTPTransportBinding(
            baseUrl = config.baseUrl,
            timeoutMs = config.timeoutMs,
            enableWebSockets = config.enableWebSockets
        ).apply {
            // Set additional configuration if needed
            // This could be extended to configure more aspects of the transport binding
        }
    }

    /**
     * Creates multiple HTTP transport bindings from a list of configurations.
     */
    fun createFromConfigs(configs: List<HTTPTransportConfig>): List<HTTPTransportBinding> {
        return configs.map { createFromConfig(it) }
    }

    /**
     * Loads HTTP transport configurations from YAML input stream.
     */
    fun loadFromYaml(inputStream: InputStream): List<HTTPTransportConfig> {
        return try {
            val configs: List<HTTPTransportConfig> = yamlMapper.readValue(inputStream)
            logger.info("Loaded ${configs.size} HTTP transport configurations from YAML")
            configs
        } catch (e: Exception) {
            logger.error("Failed to load HTTP transport configurations from YAML", e)
            emptyList()
        }
    }

    /**
     * Creates transport bindings from a YAML file.
     */
    fun createFromYaml(inputStream: InputStream): List<HTTPTransportBinding> {
        val configs = loadFromYaml(inputStream)
        return createFromConfigs(configs)
    }
} 