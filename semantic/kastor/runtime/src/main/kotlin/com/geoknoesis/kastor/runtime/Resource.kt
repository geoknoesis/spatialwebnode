package com.geoknoesis.kastor.runtime

import com.geoknoesis.rdf.IRI

/**
 * Resource API (extends rdf-core)
 * 
 * Represents an RDF resource with an IRI and additional properties.
 * This is the main interface that user-defined classes will implement.
 */
interface Resource {
    val iri: IRI
    
    /**
     * Returns additional properties not mapped to specific fields.
     * This allows access to extra RDF properties that weren't anticipated
     * in the data class definition.
     */
    fun extras(): Map<IRI, List<Any>>
}