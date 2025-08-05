package com.geoknoesis.spatialweb.node

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import org.slf4j.LoggerFactory
import java.io.InputStream

/**
 * Loader for transport configurations from YAML files.
 */
object TransportConfigLoader {
    
    private val logger = LoggerFactory.getLogger(TransportConfigLoader::class.java)
    private val yamlMapper = ObjectMapper(YAMLFactory()).registerModule(KotlinModule.Builder().build())
    
    /**
     * Loads transport configurations from YAML input stream.
     */
    fun loadFromYaml(inputStream: InputStream): List<TransportConfig> {
        return try {
            val configs: List<TransportConfig> = yamlMapper.readValue(inputStream)
            logger.info("Loaded ${configs.size} transport configurations from YAML")
            configs
        } catch (e: Exception) {
            logger.error("Failed to load transport configurations from YAML", e)
            emptyList()
        }
    }
    
    /**
     * Converts transport configurations to YAML string.
     */
    fun toYamlString(configs: List<TransportConfig>): String {
        return try {
            yamlMapper.writeValueAsString(configs)
        } catch (e: Exception) {
            logger.error("Failed to convert transport configurations to YAML string", e)
            throw e
        }
    }
} 