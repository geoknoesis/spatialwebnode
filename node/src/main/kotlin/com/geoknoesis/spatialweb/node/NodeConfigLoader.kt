package com.geoknoesis.spatialweb.node

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import org.slf4j.LoggerFactory
import java.io.File
import java.io.InputStream

/**
 * Loader for node configuration from YAML files.
 */
object NodeConfigLoader {
    
    private val logger = LoggerFactory.getLogger(NodeConfigLoader::class.java)
    private val yamlMapper = ObjectMapper(YAMLFactory()).registerModule(KotlinModule.Builder().build())
    
    /**
     * Loads node configuration from YAML input stream.
     */
    fun loadFromYaml(inputStream: InputStream): NodeConfig {
        return try {
            val config: NodeConfig = yamlMapper.readValue(inputStream)
            logger.info("Node configuration loaded successfully")
            config
        } catch (e: Exception) {
            logger.error("Failed to load node configuration from YAML", e)
            throw e
        }
    }
    
    /**
     * Loads node configuration from YAML file.
     */
    fun loadFromYamlFile(file: File): NodeConfig {
        return try {
            val config: NodeConfig = yamlMapper.readValue(file)
            logger.info("Node configuration loaded from file: ${file.absolutePath}")
            config
        } catch (e: Exception) {
            logger.error("Failed to load node configuration from file: ${file.absolutePath}", e)
            throw e
        }
    }
    
    /**
     * Saves node configuration to YAML file.
     */
    fun saveToYaml(config: NodeConfig, file: File) {
        try {
            // Ensure parent directory exists
            file.parentFile?.mkdirs()
            
            yamlMapper.writeValue(file, config)
            logger.info("Node configuration saved to file: ${file.absolutePath}")
        } catch (e: Exception) {
            logger.error("Failed to save node configuration to file: ${file.absolutePath}", e)
            throw e
        }
    }
    
    /**
     * Converts node configuration to YAML string.
     */
    fun toYamlString(config: NodeConfig): String {
        return try {
            yamlMapper.writeValueAsString(config)
        } catch (e: Exception) {
            logger.error("Failed to convert node configuration to YAML string", e)
            throw e
        }
    }
} 