package com.geoknoesis.kastor.backend.sparql

import com.geoknoesis.kastor.runtime.config.KastorConfig
import com.geoknoesis.kastor.spi.RdfApiProvider
import com.geoknoesis.rdf.RdfApi

/**
 * SPARQL endpoint implementation of the RdfApiProvider SPI.
 * 
 * This provider creates RdfApi instances that communicate with remote SPARQL endpoints
 * via HTTP for distributed RDF storage and querying.
 */
class SparqlRdfApiProvider : RdfApiProvider {
    
    override fun id(): String = "sparql"
    
    override fun description(): String = "SPARQL Endpoint RDF API Provider - Remote HTTP-based SPARQL endpoints"
    
    override fun create(config: KastorConfig): RdfApi {
        // Extract SPARQL endpoint configuration
        val queryEndpoint = config.options["queryEndpoint"] as? String
            ?: throw IllegalArgumentException("SPARQL backend requires 'queryEndpoint' configuration")
        
        val updateEndpoint = config.options["updateEndpoint"] as? String ?: queryEndpoint
        
        // Create SPARQL endpoint adapter
        return SparqlEndpointAdapter(
            queryEndpoint = queryEndpoint,
            updateEndpoint = updateEndpoint,
            config = config
        )
    }
    
    override fun supports(config: KastorConfig): Boolean {
        return config.backend == "sparql" && config.options.containsKey("queryEndpoint")
    }
}