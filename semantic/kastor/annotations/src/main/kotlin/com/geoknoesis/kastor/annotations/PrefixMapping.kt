package com.geoknoesis.kastor.annotations

/**
 * Defines a prefix-to-namespace IRI mapping for RDF serialization.
 * 
 * This annotation allows you to define prefix mappings that will be used
 * in JSON-LD contexts and other RDF serializations. Multiple prefix mappings
 * can be defined by using this annotation multiple times on the same target.
 * 
 * Example usage:
 * ```kotlin
 * @PrefixMapping(prefix = "foaf", namespace = "http://xmlns.com/foaf/0.1/")
 * @PrefixMapping(prefix = "dct", namespace = "http://purl.org/dc/terms/")
 * @RdfClass("foaf:Person")
 * data class Person(
 *     @RdfProperty("foaf:name") val name: String,
 *     @RdfProperty("dct:created") val created: String
 * )
 * ```
 * 
 * @param prefix The short prefix to use (e.g., "foaf", "dct", "schema")
 * @param namespace The full namespace IRI (e.g., "http://xmlns.com/foaf/0.1/")
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FILE, AnnotationTarget.PACKAGE)
@Retention(AnnotationRetention.SOURCE)
@Repeatable
annotation class PrefixMapping(
    val prefix: String,
    val namespace: String
)