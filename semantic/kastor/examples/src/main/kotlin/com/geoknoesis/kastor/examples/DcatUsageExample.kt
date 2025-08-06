package com.geoknoesis.kastor.examples

import com.geoknoesis.kastor.runtime.config.KastorConfigLoader

/**
 * Example demonstrating DCAT-US configuration usage.
 * 
 * This shows how to load and work with a real-world government data catalog
 * configuration using the Data Catalog Vocabulary - US (DCAT-US) standard.
 */
object DcatUsageExample {
    
    @JvmStatic
    fun main(args: Array<String>) {
        println("=== DCAT-US Kastor Configuration Example ===\n")
        
        // Load DCAT-US specific configuration
        val dcatConfig = """
            schemaPaths:
              - src/main/resources/dcat-us/dcat-us-3.0.shacl.ttl

            contextPaths:
              - src/main/resources/dcat-us/dcat.context.jsonld

            interfacePackages:
              - com.example.catalog

            prefixMappings:
              dcat: "http://www.w3.org/ns/dcat#"
              dcterms: "http://purl.org/dc/terms/"

            namespaceMappings:
              dcat: "gov.us.dcat.core"
              dcterms: "gov.us.dcat.terms"

            datatypeMappings:
              "http://www.w3.org/2001/XMLSchema#date": "java.time.LocalDate"

            options:
              validation: true
              generateImpl: true
              extrasProperty: true

            backend: "jena"
        """.trimIndent()
        
        val config = KastorConfigLoader.loadFromString(dcatConfig)
        
        // Display configuration details
        println("📋 Schema Configuration:")
        config.schemaPaths.forEach { path ->
            println("  • $path")
        }
        
        println("\n🌐 Context Files:")
        config.contextPaths.forEach { path ->
            println("  • $path")
        }
        
        println("\n📦 Interface Packages:")
        config.interfacePackages.forEach { pkg ->
            println("  • $pkg")
        }
        
        println("\n🏷️  Prefix Mappings:")
        config.prefixMappings.forEach { (prefix, namespace) ->
            println("  • $prefix: $namespace")
        }
        
        println("\n📂 Namespace Mappings:")
        config.namespaceMappings.forEach { (namespace, javaPackage) ->
            println("  • $namespace -> $javaPackage")
        }
        
        println("\n🔧 Datatype Mappings:")
        config.datatypeMappings.forEach { (rdfType, javaType) ->
            println("  • $rdfType -> $javaType")
        }
        
        println("\n⚙️  Options:")
        config.options.forEach { (key, value) ->
            println("  • $key: $value")
        }
        
        println("\n🔌 Backend: ${config.backend}")
        
        // Example of how this would be used in practice
        println("\n=== Usage Example ===")
        println("1. SHACL shapes from: ${config.schemaPaths.first()}")
        println("2. JSON-LD context from: ${config.contextPaths.first()}")
        println("3. Generate classes in package: ${config.interfacePackages.first()}")
        println("4. With DCAT prefix: ${config.prefixMappings["dcat"]}")
        println("5. Mapped to Java package: ${config.namespaceMappings["dcat"]}")
        
        if (config.options["validation"] == true) {
            println("6. ✅ Validation enabled - objects will be validated against SHACL shapes")
        }
        
        if (config.options["generateImpl"] == true) {
            println("7. ⚡ Implementation generation enabled - Kastor will generate concrete classes")
        }
        
        if (config.options["extrasProperty"] == true) {
            println("8. 🔧 Extras property enabled - unmapped RDF properties will be accessible")
        }
        
        // Note: This would be the actual usage when backends are implemented
        // val api = KastorRuntime.load(config)
        // val catalog = api.loadResource<DcatCatalog>("http://example.gov/catalog/1")
        
        println("\n✅ DCAT-US configuration loaded successfully!")
        println("Ready for government data catalog processing with Kastor!")
    }
}