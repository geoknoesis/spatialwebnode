package com.geoknoesis.kastor.backend.jena

import com.geoknoesis.kastor.runtime.config.KastorConfig
import com.geoknoesis.kastor.spi.RdfApiProvider
import com.geoknoesis.rdf.RdfApi
import com.geoknoesis.jena.JenaAdapter

/**
 * Apache Jena implementation of the RdfApiProvider SPI.
 * 
 * This provider creates RdfApi instances backed by Apache Jena
 * using the existing jena-adapter module for actual implementation.
 */
class JenaRdfApiProvider : RdfApiProvider {
    
    override fun id(): String = "jena"
    
    override fun description(): String = "Apache Jena RDF API Provider - In-memory and persistent RDF storage"
    
    override fun create(config: KastorConfig): RdfApi {
        // Leverage the existing JenaAdapter implementation
        // TODO: Pass Jena-specific configuration when JenaAdapter supports it
        return JenaAdapter()
    }
    
    override fun supports(config: KastorConfig): Boolean {
        return config.backend == "jena"
    }
}