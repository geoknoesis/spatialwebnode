package com.geoknoesis.kastor.runtime

import com.geoknoesis.rdf.IRI
import com.geoknoesis.rdf.RdfApi

/**
 * Internal Resource Contract
 * 
 * Marker interface for generated Impl classes.
 * This interface provides access to the underlying RDF API and graph context
 * needed for internal operations like validation and type casting.
 */
interface InternalResource : Resource {
    val api: RdfApi
    val graph: IRI
}