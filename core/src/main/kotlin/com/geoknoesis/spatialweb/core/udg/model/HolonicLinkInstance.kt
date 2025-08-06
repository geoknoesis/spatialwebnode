package com.geoknoesis.spatialweb.core.udg.model

import com.geoknoesis.spatialweb.identity.did.Did
import java.time.Instant

/**
 * Represents a holonic link instance in the UDG
 *
 * A holonic link represents a relationship between a whole and its parts
 * in a holonic structure. This is a fundamental concept in the Spatial Web
 * where domains can contain other domains or entities.
 */
data class HolonicLinkInstance(
    /** The DID of the whole entity */
    val whole: Did,
    
    /** The DID of the part entity */
    val part: Did,
    
    /** The property that defines the relationship */
    val link: Property,
    
    /** Optional annotations on the link */
    val annotations: Map<Property, RdfTerm> = emptyMap(),
    
    /** Creation timestamp */
    val createdAt: Instant = Instant.now(),
    
    /** Last modification timestamp */
    val modifiedAt: Instant = Instant.now(),
    
    /** Version of the link */
    val version: String = "1.0",
    
    /** Additional metadata */
    val metadata: Map<String, Any> = emptyMap()
) {
    
    /**
     * Gets an annotation value by property
     */
    fun getAnnotation(property: Property): RdfTerm? = annotations[property]
    
    /**
     * Sets an annotation value
     */
    fun setAnnotation(property: Property, value: RdfTerm): HolonicLinkInstance {
        return copy(
            annotations = annotations + (property to value),
            modifiedAt = Instant.now()
        )
    }
    
    /**
     * Removes an annotation
     */
    fun removeAnnotation(property: Property): HolonicLinkInstance {
        return copy(
            annotations = annotations - property,
            modifiedAt = Instant.now()
        )
    }
    
    /**
     * Checks if the link has a specific annotation
     */
    fun hasAnnotation(property: Property): Boolean = annotations.containsKey(property)
    
    /**
     * Gets all annotation properties
     */
    fun getAnnotationProperties(): Set<Property> = annotations.keys
    
    /**
     * Creates a new version of this link
     */
    fun withVersion(newVersion: String): HolonicLinkInstance {
        return copy(
            version = newVersion,
            modifiedAt = Instant.now()
        )
    }
    
    /**
     * Updates the modification timestamp
     */
    fun touch(): HolonicLinkInstance {
        return copy(modifiedAt = Instant.now())
    }
    
    /**
     * Creates a copy with updated metadata
     */
    fun withMetadata(key: String, value: Any): HolonicLinkInstance {
        return copy(metadata = metadata + (key to value))
    }
    
    companion object {
        /**
         * Creates a simple holonic link
         */
        fun create(whole: Did, part: Did, link: Property): HolonicLinkInstance {
            return HolonicLinkInstance(whole, part, link)
        }
        
        /**
         * Creates a holonic link with annotations
         */
        fun createWithAnnotations(
            whole: Did,
            part: Did,
            link: Property,
            annotations: Map<Property, RdfTerm>
        ): HolonicLinkInstance {
            return HolonicLinkInstance(whole, part, link, annotations)
        }
    }
} 