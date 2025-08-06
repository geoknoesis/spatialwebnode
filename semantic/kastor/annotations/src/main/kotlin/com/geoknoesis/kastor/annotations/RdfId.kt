package com.geoknoesis.kastor.annotations

/**
 * Marks the RDF subject IRI property.
 * 
 * This annotation identifies which property represents the subject IRI
 * of the RDF resource. This is optional if you always use Resource.iri,
 * but useful when you want a custom ID field.
 * 
 * @param generate Whether to auto-generate the IRI if not provided
 * @param pattern Pattern for IRI generation (e.g., "http://example.org/person/{id}")
 */
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.SOURCE)
annotation class RdfId(
    val generate: Boolean = false,
    val pattern: String = ""
)