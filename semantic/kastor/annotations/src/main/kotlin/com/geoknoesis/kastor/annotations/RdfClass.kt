package com.geoknoesis.kastor.annotations

/**
 * Marks a Kotlin interface/class as representing an RDF Class.
 * 
 * This annotation is useful for ontology-only mappings where you want
 * to map to an RDF class without necessarily having a SHACL shape.
 * 
 * @param iri The IRI of the RDF Class (e.g., "http://xmlns.com/foaf/0.1/Person")
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class RdfClass(val iri: String)