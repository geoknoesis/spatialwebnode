package com.geoknoesis.kastor.annotations

/**
 * Represents a SHACL property with sh:uniqueLang true.
 * 
 * This annotation generates a Map<String, String> property where keys are
 * language tags and values are the localized strings. This corresponds to
 * JSON-LD @language container mapping.
 * 
 * @param iri The IRI of the RDF predicate
 * @param defaultLang The default language tag to use when no language is specified
 */
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.SOURCE)
annotation class LangMap(
    val iri: String,
    val defaultLang: String = ""
)