package com.geoknoesis.kastor.runtime.config

/**
 * Main configuration class for Kastor framework.
 * 
 * This data class represents the complete configuration structure
 * loaded from kastor.yaml files.
 */
data class KastorConfig(
    /**
     * Paths to schema files (SHACL, JSON Schema, etc.)
     */
    val schemaPaths: List<String> = emptyList(),
    
    /**
     * Paths to JSON-LD context files
     */
    val contextPaths: List<String> = emptyList(),
    
    /**
     * Packages containing interfaces to process
     */
    val interfacePackages: List<String> = emptyList(),

    /**
     * Prefix mappings for URIs (e.g., "foaf" -> "http://xmlns.com/foaf/0.1/")
     */
    val prefixMappings: Map<String, String> = emptyMap(),
    
    /**
     * Namespace mappings for package structure
     */
    val namespaceMappings: Map<String, String> = emptyMap(),
    
    /**
     * Custom datatype mappings for RDF literals
     */
    val datatypeMappings: Map<String, String> = emptyMap(),

    /**
     * SHACL shapes graphs configuration
     */
    val shapesGraphs: Map<String, String> = emptyMap(),
    
    /**
     * Default graph IRI to use for operations
     */
    val defaultGraph: String? = null,

    /**
     * JSON-LD specific options
     */
    val jsonld: JsonLdOptions = JsonLdOptions(),
    
    /**
     * General framework options (can be Boolean, String, or other types)
     */
    val options: Map<String, Any> = emptyMap(),
    
    /**
     * Backend implementation to use (jena, rdf4j, sparql)
     */
    val backend: String = "jena"
)

/**
 * JSON-LD specific configuration options.
 */
data class JsonLdOptions(
    /**
     * Whether to generate JSON-LD context from POJO annotations
     */
    val generateFromPojo: Boolean = false,
    
    /**
     * Output path for generated JSON-LD context
     */
    val outputPath: String = "build/generated/kastor/context.jsonld",
    
    /**
     * Whether to merge with existing context files
     */
    val mergeWithExisting: Boolean = true
)