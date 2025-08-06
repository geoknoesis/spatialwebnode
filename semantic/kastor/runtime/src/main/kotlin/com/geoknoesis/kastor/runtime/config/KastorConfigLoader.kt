package com.geoknoesis.kastor.runtime.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import java.io.File

/**
 * YAML configuration loader for Kastor framework.
 * 
 * This object provides methods to load KastorConfig from YAML files
 * using Jackson with safe defaults and proper error handling.
 */
object KastorConfigLoader {
    
    private val mapper = ObjectMapper(YAMLFactory())
        .registerModule(KotlinModule.Builder().build())

    /**
     * Load configuration from a YAML file.
     * 
     * @param path Path to the YAML configuration file
     * @return Parsed KastorConfig with defaults applied
     * @throws IllegalStateException if the configuration file doesn't exist
     * @throws com.fasterxml.jackson.core.JsonProcessingException if YAML parsing fails
     */
    fun load(path: String = "kastor.yaml"): KastorConfig {
        val file = File(path)
        if (!file.exists()) {
            error("Missing configuration file: $path")
        }

        return try {
            mapper.readValue(file)
        } catch (e: Exception) {
            throw IllegalStateException("Failed to parse configuration file: $path", e)
        }
    }
    
    /**
     * Load configuration from a YAML string.
     * 
     * @param yamlContent YAML content as string
     * @return Parsed KastorConfig with defaults applied
     * @throws com.fasterxml.jackson.core.JsonProcessingException if YAML parsing fails
     */
    fun loadFromString(yamlContent: String): KastorConfig {
        return try {
            mapper.readValue(yamlContent, KastorConfig::class.java)
        } catch (e: Exception) {
            throw IllegalStateException("Failed to parse YAML content", e)
        }
    }
    
    /**
     * Load configuration with fallback to default if file doesn't exist.
     * 
     * @param path Path to the YAML configuration file
     * @return Parsed KastorConfig or default configuration if file doesn't exist
     */
    fun loadOrDefault(path: String = "kastor.yaml"): KastorConfig {
        val file = File(path)
        return if (file.exists()) {
            load(path)
        } else {
            KastorConfig() // Return default configuration
        }
    }
}