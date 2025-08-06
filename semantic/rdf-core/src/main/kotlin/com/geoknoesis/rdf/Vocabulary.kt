package com.geoknoesis.rdf

/**
 * Base interface for vocabulary implementations
 */
interface Vocabulary {
    val iri: String
    val localName: String get() = iri.substringAfterLast('#', iri.substringAfterLast('/'))
}

/**
 * Base enum for vocabulary implementations
 */
abstract class VocabularyEnum(override val iri: String) : Vocabulary {
    // Note: name property is only available in enum classes
}

/**
 * XSD Datatypes as enum
 */
@Suppress("EnumEntryName", "unused")
enum class XSD(override val iri: String, private val _code: Int) : Vocabulary {
    // String types
    string("http://www.w3.org/2001/XMLSchema#string", 1),
    boolean("http://www.w3.org/2001/XMLSchema#boolean", 9),
    int("http://www.w3.org/2001/XMLSchema#int", 12);

    companion object {
        const val NAMESPACE = "http://www.w3.org/2001/XMLSchema#"
        
        fun fromLocalName(localName: String): XSD? {
            return values().find { it.localName == localName }
        }
        
        fun fromIRI(iri: String): XSD? {
            return values().find { it.iri == iri }
        }
    }
    
    val code: Int get() = _code
    
    override fun toString(): String = "XSD.${name}"
}

/**
 * RDF Core vocabulary as enum
 */
@Suppress("EnumEntryName", "unused")
enum class RDF(override val iri: String) : Vocabulary {
    type("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
    subject("http://www.w3.org/1999/02/22-rdf-syntax-ns#subject"),
    predicate("http://www.w3.org/1999/02/22-rdf-syntax-ns#predicate"),
    object_("http://www.w3.org/1999/02/22-rdf-syntax-ns#object");

    companion object {
        const val NAMESPACE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#"
        
        fun fromLocalName(localName: String): RDF? {
            return values().find { it.localName == localName }
        }
        
        fun fromIRI(iri: String): RDF? {
            return values().find { it.iri == iri }
        }
    }
    
    override fun toString(): String = "RDF.${name}"
} 