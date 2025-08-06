package com.geoknoesis.kastor.runtime.config

import com.geoknoesis.kastor.runtime.KastorRuntime

/**
 * Example usage of Kastor configuration system.
 * 
 * This demonstrates how to load and use configuration in a real application.
 */
object ConfigExample {
    
    @JvmStatic
    fun main(args: Array<String>) {
        // Load configuration from default location
        val config = KastorRuntime.loadConfig("kastor.yaml")
        
        println("=== Kastor Configuration ===")
        println("Backend: ${config.backend}")
        println("Schema paths: ${config.schemaPaths}")
        println("Interface packages: ${config.interfacePackages}")
        println("Prefix mappings: ${config.prefixMappings}")
        
        // Example: Check if validation is enabled
        val validationEnabled = config.options["enableValidation"] ?: false
        println("Validation enabled: $validationEnabled")
        
        // Example: Get default graph
        val defaultGraph = config.defaultGraph ?: "http://example.org/default"
        println("Default graph: $defaultGraph")
        
        // Example: JSON-LD options
        println("\n=== JSON-LD Options ===")
        println("Generate from POJO: ${config.jsonld.generateFromPojo}")
        println("Output path: ${config.jsonld.outputPath}")
        println("Merge with existing: ${config.jsonld.mergeWithExisting}")
        
        // Example: Access shapes graphs
        if (config.shapesGraphs.isNotEmpty()) {
            println("\n=== Shapes Graphs ===")
            config.shapesGraphs.forEach { (dataGraph, shapesGraph) ->
                println("$dataGraph -> $shapesGraph")
            }
        }
        
        // Note: Actual RDF API loading would happen here when backends are implemented
        // val api = KastorRuntime.load(config)
        println("\n✅ Configuration loaded successfully!")
    }
}