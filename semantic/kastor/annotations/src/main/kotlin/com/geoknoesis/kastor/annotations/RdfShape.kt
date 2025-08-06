package com.geoknoesis.kastor.annotations

/**
 * Marks an interface as mapped from a SHACL NodeShape.
 * 
 * This annotation indicates that the class represents a SHACL shape
 * and should be validated according to the constraints defined in the shape.
 * 
 * @param iri The IRI of the SHACL NodeShape
 * @param targetClass The target RDF class for this shape (optional)
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class RdfShape(
    val iri: String,
    val targetClass: String = ""
)