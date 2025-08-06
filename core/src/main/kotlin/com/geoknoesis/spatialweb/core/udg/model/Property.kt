package com.geoknoesis.spatialweb.core.udg.model

/**
 * Represents a property in the UDG
 *
 * A property defines a relationship or attribute that can be associated
 * with entities. Properties are identified by IRIs and can have various types.
 */
data class Property(
    /** The IRI of the property */
    val iri: IRI,
    
    /** The name of the property */
    val name: String,
    
    /** Optional description of the property */
    val description: String? = null,
    
    /** The type of the property */
    val type: PropertyType = PropertyType.OBJECT_PROPERTY,
    
    /** The domain of the property (what types of entities can have this property) */
    val domain: IRI? = null,
    
    /** The range of the property (what types of values this property can have) */
    val range: IRI? = null,
    
    /** Whether the property is required */
    val required: Boolean = false,
    
    /** Whether the property can have multiple values */
    val multiple: Boolean = false,
    
    /** Additional metadata */
    val metadata: Map<String, Any> = emptyMap()
) {
    
    /**
     * Checks if this property can be applied to an entity of the given type
     */
    fun canApplyTo(entityType: IRI): Boolean {
        return domain == null || domain == entityType
    }
    
    /**
     * Checks if a value is valid for this property
     */
    fun isValidValue(value: RdfTerm): Boolean {
        return when (type) {
            PropertyType.OBJECT_PROPERTY -> value is RdfTerm.IRITerm
            PropertyType.DATATYPE_PROPERTY -> value is RdfTerm.LiteralTerm
            PropertyType.ANNOTATION_PROPERTY -> true
        }
    }
    
    /**
     * Creates a copy with updated metadata
     */
    fun withMetadata(key: String, value: Any): Property {
        return copy(metadata = metadata + (key to value))
    }
    
    override fun toString(): String = iri.toString()
    
    companion object {
        /**
         * Creates a property from an IRI string
         */
        fun fromIRI(iriString: String, name: String): Property? {
            val iri = IRI.fromString(iriString) ?: return null
            return Property(iri, name)
        }
        
        /**
         * Creates an object property
         */
        fun objectProperty(iri: IRI, name: String): Property {
            return Property(iri, name, type = PropertyType.OBJECT_PROPERTY)
        }
        
        /**
         * Creates a datatype property
         */
        fun datatypeProperty(iri: IRI, name: String): Property {
            return Property(iri, name, type = PropertyType.DATATYPE_PROPERTY)
        }
        
        /**
         * Creates an annotation property
         */
        fun annotationProperty(iri: IRI, name: String): Property {
            return Property(iri, name, type = PropertyType.ANNOTATION_PROPERTY)
        }
    }
}

/**
 * Types of properties in the UDG
 */
enum class PropertyType {
    /** Object property - relates entities to other entities */
    OBJECT_PROPERTY,
    
    /** Datatype property - relates entities to literal values */
    DATATYPE_PROPERTY,
    
    /** Annotation property - provides metadata about entities */
    ANNOTATION_PROPERTY
} 