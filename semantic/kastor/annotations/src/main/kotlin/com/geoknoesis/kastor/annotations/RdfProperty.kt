package com.geoknoesis.kastor.annotations

/**
 * Maps a property to an RDF predicate.
 * 
 * This annotation defines how a Kotlin property should be serialized to
 * and deserialized from RDF triples.
 * 
 * @param iri The IRI of the RDF predicate (e.g., "http://xmlns.com/foaf/0.1/name")
 * @param inverse Whether this property represents an inverse relationship
 */
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.SOURCE)
annotation class RdfProperty(
    val iri: String,
    val inverse: Boolean = false
)