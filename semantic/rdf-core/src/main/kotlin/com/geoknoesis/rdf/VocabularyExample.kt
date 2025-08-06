package com.geoknoesis.rdf

/**
 * Example demonstrating the benefits of enum-based vocabularies
 */
object VocabularyExample {
    
    fun demonstrateEnumBenefits() {
        println("=== Enum-based Vocabulary Benefits ===\n")
        
        // 1. Type Safety
        demonstrateTypeSafety()
        
        // 2. IDE Support
        demonstrateIDESupport()
        
        // 3. Performance
        demonstratePerformance()
        
        // 4. Additional Features
        demonstrateAdditionalFeatures()
        
        // 5. Comparison with Object Approach
        demonstrateComparison()
    }
    
    private fun demonstrateTypeSafety() {
        println("1. TYPE SAFETY:")
        
        // ✅ Enum approach - compile-time safety
        val xsdType: XSD = XSD.string
        val rdfType: RDF = RDF.type
        
        // ✅ Direct IRI access
        val stringIRI: IRI = IRI(XSD.string.iri)
        val typeIRI: IRI = IRI(RDF.type.iri)
        
        println("   ✅ XSD.string = ${XSD.string}")
        println("   ✅ RDF.type = ${RDF.type}")
        println("   ✅ String IRI = $stringIRI")
        println("   ✅ Type IRI = $typeIRI")
        println()
    }
    
    private fun demonstrateIDESupport() {
        println("2. IDE SUPPORT:")
        
        // ✅ Auto-completion shows all available values
        println("   ✅ XSD.values() = ${XSD.values().joinToString()}")
        println("   ✅ RDF.values() = ${RDF.values().joinToString()}")
        
        // ✅ Easy to find all usages
        println("   ✅ All XSD types: ${XSD.values().size} types")
        println("   ✅ All RDF properties: ${RDF.values().size} properties")
        println()
    }
    
    private fun demonstratePerformance() {
        println("3. PERFORMANCE:")
        
        // ✅ Enum constants are optimized by JVM
        val xsdString = XSD.string
        val rdfType = RDF.type
        
        // ✅ Direct access, no reflection
        println("   ✅ XSD.string.iri = ${xsdString.iri}")
        println("   ✅ RDF.type.iri = ${rdfType.iri}")
        
        // ✅ Memory efficient - single instances
        println("   ✅ XSD.string === XSD.string: ${XSD.string === XSD.string}")
        println("   ✅ RDF.type === RDF.type: ${RDF.type === RDF.type}")
        println()
    }
    
    private fun demonstrateAdditionalFeatures() {
        println("4. ADDITIONAL FEATURES:")
        
        // ✅ Lookup by local name
        val foundXSD = XSD.fromLocalName("string")
        val foundRDF = RDF.fromLocalName("type")
        
        println("   ✅ XSD.fromLocalName(\"string\") = $foundXSD")
        println("   ✅ RDF.fromLocalName(\"type\") = $foundRDF")
        
        // ✅ Lookup by IRI
        val foundByIRI = XSD.fromIRI("http://www.w3.org/2001/XMLSchema#string")
        println("   ✅ XSD.fromIRI(\"http://www.w3.org/2001/XMLSchema#string\") = $foundByIRI")
        
        // ✅ Namespace constants
        println("   ✅ XSD.NAMESPACE = ${XSD.NAMESPACE}")
        println("   ✅ RDF.NAMESPACE = ${RDF.NAMESPACE}")
        println()
    }
    
    private fun demonstrateComparison() {
        println("5. COMPARISON WITH OBJECT APPROACH:")
        
        // ✅ Enum approach - type safe, IDE support, performance
        val enumXSD = XSD.string
        val enumRDF = RDF.type
        
        // ❌ Object approach - strings, no type safety, no IDE support
        val objectXSD = "http://www.w3.org/2001/XMLSchema#string"
        val objectRDF = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type"
        
        println("   ✅ Enum approach:")
        println("      - Type: ${enumXSD::class.simpleName}")
        println("      - Value: $enumXSD")
        println("      - IRI: ${enumXSD.iri}")
        println("      - Local name: ${enumXSD.localName}")
        
        println("   ❌ Object approach:")
        println("      - Type: ${objectXSD::class.simpleName}")
        println("      - Value: $objectXSD")
        println("      - No IRI property")
        println("      - No local name property")
        println()
    }
    
    fun demonstrateUsage() {
        println("=== PRACTICAL USAGE EXAMPLES ===\n")
        
        // Creating RDF terms with enum-based vocabularies
        val subject = IRI("http://example.org/person/1")
        val predicate = IRI(RDF.type.iri)
        val objectIRI = IRI("http://example.org/Person")
        
        val triple = Triple(subject, predicate, objectIRI)
        println("RDF Triple: $triple")
        
        // Creating literals with XSD types
        val stringLiteral = Literal("Hello World", IRI(XSD.string.iri))
        val intLiteral = Literal("42", IRI(XSD.int.iri))
        val booleanLiteral = Literal("true", IRI(XSD.boolean.iri))
        
        println("String literal: $stringLiteral")
        println("Integer literal: $intLiteral")
        println("Boolean literal: $booleanLiteral")
        
        // Validation using enums
        val validXSDType = XSD.fromLocalName("string")
        if (validXSDType != null) {
            println("Valid XSD type found: $validXSDType")
        }
        
        val invalidXSDType = XSD.fromLocalName("invalid")
        if (invalidXSDType == null) {
            println("Invalid XSD type correctly rejected")
        }
    }
    
    fun main() {
        demonstrateEnumBenefits()
        demonstrateUsage()
    }
} 