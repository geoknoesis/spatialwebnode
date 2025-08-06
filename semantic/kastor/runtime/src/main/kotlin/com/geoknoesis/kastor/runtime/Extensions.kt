package com.geoknoesis.kastor.runtime

import com.geoknoesis.rdf.IRI

/**
 * Extension Utilities
 * 
 * Provides type casting functionality for Resource objects.
 * This enables safe casting between different resource types with optional validation.
 */

/**
 * Cast a Resource to a specific type using generated implementation classes.
 * 
 * @param T The target type to cast to
 * @param validate Whether to perform SHACL validation on the result
 * @return The casted instance of type T
 * @throws ValidationException if validation is enabled and fails
 * @throws ClassNotFoundException if the generated implementation class is not found
 * @throws ReflectiveOperationException if the constructor cannot be invoked
 */
inline fun <reified T> Resource.asType(validate: Boolean = false): T {
    val implClass = Class.forName("${T::class.qualifiedName}Impl")
    val ctor = implClass.getConstructor(IRI::class.java, com.geoknoesis.rdf.RdfApi::class.java)
    val instance = ctor.newInstance(this.iri, (this as InternalResource).api) as T

    if (validate) {
        // Note: shapesGraphIRI would typically come from configuration or be inferred
        val shapesGraphIRI = IRI("http://example.org/shapes") // This should be configurable
        val report = (this as InternalResource).api.validate(this.graph, shapesGraphIRI)
        if (!report.conforms) {
            throw ValidationException(report.messages)
        }
    }
    
    return instance
}