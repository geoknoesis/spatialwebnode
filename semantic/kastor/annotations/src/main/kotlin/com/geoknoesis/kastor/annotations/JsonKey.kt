package com.geoknoesis.kastor.annotations

/**
 * Sets the JSON-LD key explicitly for this property.
 * 
 * This annotation allows you to override the default JSON-LD property name
 * that would be generated from the RDF predicate IRI or Kotlin property name.
 * 
 * @param name The JSON-LD key name to use in serialization
 */
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.SOURCE)
annotation class JsonKey(val name: String)