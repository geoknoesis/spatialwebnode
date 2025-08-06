package com.geoknoesis.spatialweb.core.udg.model

import com.geoknoesis.spatialweb.identity.did.Did
import java.time.Instant

/**
 * Represents an entity in the Universal Domain Graph (UDG)
 *
 * An entity is a fundamental unit in the Spatial Web that can represent
 * domains, resources, services, or any other conceptual or physical thing.
 */
data class Entity(
    /** The DID of the entity */
    val did: Did,
    
    /** The type of the entity */
    val type: IRI,
    
    /** The name of the entity */
    val name: String,
    
    /** Optional description of the entity */
    val description: String? = null,
    
    /** Properties of the entity */
    val properties: Map<Property, RdfTerm> = emptyMap(),
    
    /** Whether this entity is a domain */
    val isDomain: Boolean = false,
    
    /** Creation timestamp */
    val createdAt: Instant = Instant.now(),
    
    /** Last modification timestamp */
    val modifiedAt: Instant = Instant.now(),
    
    /** Version of the entity */
    val version: String = "1.0",
    
    /** Additional metadata */
    val metadata: Map<String, Any> = emptyMap()
) {
    
    /**
     * Gets a property value by its IRI
     */
    fun getProperty(property: Property): RdfTerm? = properties[property]
    
    /**
     * Sets a property value
     */
    fun setProperty(property: Property, value: RdfTerm): Entity {
        return copy(
            properties = properties + (property to value),
            modifiedAt = Instant.now()
        )
    }
    
    /**
     * Removes a property
     */
    fun removeProperty(property: Property): Entity {
        return copy(
            properties = properties - property,
            modifiedAt = Instant.now()
        )
    }
    
    /**
     * Checks if the entity has a specific property
     */
    fun hasProperty(property: Property): Boolean = properties.containsKey(property)
    
    /**
     * Gets all property IRIs
     */
    fun getPropertyNames(): Set<Property> = properties.keys
    
    /**
     * Creates a new version of this entity
     */
    fun withVersion(newVersion: String): Entity {
        return copy(
            version = newVersion,
            modifiedAt = Instant.now()
        )
    }
    
    /**
     * Updates the modification timestamp
     */
    fun touch(): Entity {
        return copy(modifiedAt = Instant.now())
    }
} 