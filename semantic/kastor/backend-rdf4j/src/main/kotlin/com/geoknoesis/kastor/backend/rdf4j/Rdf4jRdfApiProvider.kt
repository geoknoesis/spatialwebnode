package com.geoknoesis.kastor.backend.rdf4j

import com.geoknoesis.kastor.runtime.config.KastorConfig
import com.geoknoesis.kastor.spi.RdfApiProvider
import com.geoknoesis.rdf.RdfApi
import com.geoknoesis.rdf4j.Rdf4jAdapter
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.sail.SailRepository
import org.eclipse.rdf4j.sail.memory.MemoryStore

/**
 * Eclipse RDF4J implementation of the RdfApiProvider SPI.
 * 
 * This provider creates RdfApi instances backed by Eclipse RDF4J
 * using the existing rdf4j-adapter module for actual implementation.
 */
class Rdf4jRdfApiProvider : RdfApiProvider {
    
    override fun id(): String = "rdf4j"
    
    override fun description(): String = "Eclipse RDF4J RDF API Provider - Repository-based RDF storage and SPARQL"
    
    override fun create(config: KastorConfig): RdfApi {
        // Create an appropriate RDF4J repository based on configuration
        val repository = createRepository(config)
        
        // Leverage the existing Rdf4jAdapter implementation
        return Rdf4jAdapter(repository)
    }
    
    override fun supports(config: KastorConfig): Boolean {
        return config.backend == "rdf4j"
    }
    
    /**
     * Creates an RDF4J repository based on configuration.
     * For now, creates an in-memory repository by default.
     * TODO: Support different repository types based on configuration
     */
    private fun createRepository(config: KastorConfig): Repository {
        // Check configuration options for repository type
        val repositoryType = config.options["repositoryType"] as? String ?: "memory"
        
        val repository = when (repositoryType.lowercase()) {
            "memory" -> {
                // Create in-memory repository
                SailRepository(MemoryStore())
            }
            // TODO: Add support for other repository types:
            // "native" -> SailRepository(NativeStore(dataDir))
            // "http" -> HTTPRepository(serverUrl, repositoryID)
            // "sparql" -> SPARQLRepository(endpointUrl)
            else -> {
                // Default to memory repository
                SailRepository(MemoryStore())
            }
        }
        
        // Initialize the repository
        try {
            repository.init()
        } catch (e: Exception) {
            throw IllegalStateException("Failed to initialize RDF4J repository", e)
        }
        
        return repository
    }
}