package com.geoknoesis.spatialweb.core.udg.model

import com.geoknoesis.spatialweb.identity.did.Did
import java.time.Instant

/**
 * Represents a change to an entity in the UDG
 *
 * Entity changes are used for change tracking, audit trails, and
 * rebuilding entities from their history.
 */
data class EntityChange(
    /** Unique identifier for this change */
    val id: String,
    
    /** The DID of the entity that was changed */
    val entityDid: Did,
    
    /** The type of change */
    val changeType: ChangeType,
    
    /** The timestamp when the change occurred */
    val timestamp: Instant = Instant.now(),
    
    /** The user or system that made the change */
    val actor: String? = null,
    
    /** The previous state of the entity (before the change) */
    val previousState: Map<Property, RdfTerm>? = null,
    
    /** The new state of the entity (after the change) */
    val newState: Map<Property, RdfTerm>? = null,
    
    /** Specific changes made to properties */
    val propertyChanges: List<PropertyChange> = emptyList(),
    
    /** The version of the entity after this change */
    val entityVersion: String? = null,
    
    /** Additional metadata about the change */
    val metadata: Map<String, Any> = emptyMap()
) {
    
    /**
     * Gets a metadata value by key
     */
    fun getMetadata(key: String): Any? = metadata[key]
    
    /**
     * Sets a metadata value
     */
    fun setMetadata(key: String, value: Any): EntityChange {
        return copy(metadata = metadata + (key to value))
    }
    
    /**
     * Checks if this change affects a specific property
     */
    fun affectsProperty(property: Property): Boolean {
        return propertyChanges.any { it.property == property }
    }
    
    /**
     * Gets changes for a specific property
     */
    fun getPropertyChanges(property: Property): List<PropertyChange> {
        return propertyChanges.filter { it.property == property }
    }
    
    /**
     * Gets the most recent change for a specific property
     */
    fun getLatestPropertyChange(property: Property): PropertyChange? {
        return propertyChanges.filter { it.property == property }.maxByOrNull { it.timestamp }
    }
    
    companion object {
        /**
         * Creates a change ID
         */
        fun generateChangeId(): String = "change_${System.currentTimeMillis()}_${(0..9999).random()}"
        
        /**
         * Creates a creation change
         */
        fun createCreation(
            entityDid: Did,
            newState: Map<Property, RdfTerm>,
            actor: String? = null
        ): EntityChange {
            return EntityChange(
                id = generateChangeId(),
                entityDid = entityDid,
                changeType = ChangeType.CREATED,
                actor = actor,
                newState = newState
            )
        }
        
        /**
         * Creates an update change
         */
        fun createUpdate(
            entityDid: Did,
            previousState: Map<Property, RdfTerm>,
            newState: Map<Property, RdfTerm>,
            propertyChanges: List<PropertyChange>,
            actor: String? = null
        ): EntityChange {
            return EntityChange(
                id = generateChangeId(),
                entityDid = entityDid,
                changeType = ChangeType.UPDATED,
                actor = actor,
                previousState = previousState,
                newState = newState,
                propertyChanges = propertyChanges
            )
        }
        
        /**
         * Creates a deletion change
         */
        fun createDeletion(
            entityDid: Did,
            previousState: Map<Property, RdfTerm>,
            actor: String? = null
        ): EntityChange {
            return EntityChange(
                id = generateChangeId(),
                entityDid = entityDid,
                changeType = ChangeType.DELETED,
                actor = actor,
                previousState = previousState
            )
        }
    }
}

/**
 * Types of entity changes
 */
enum class ChangeType {
    /** Entity was created */
    CREATED,
    
    /** Entity was updated */
    UPDATED,
    
    /** Entity was deleted */
    DELETED,
    
    /** Entity state was changed */
    STATE_CHANGED,
    
    /** Entity properties were modified */
    PROPERTIES_MODIFIED,
    
    /** Entity was restored */
    RESTORED
}

/**
 * Represents a change to a specific property
 */
data class PropertyChange(
    /** The property that was changed */
    val property: Property,
    
    /** The type of property change */
    val changeType: PropertyChangeType,
    
    /** The previous value */
    val previousValue: RdfTerm? = null,
    
    /** The new value */
    val newValue: RdfTerm? = null,
    
    /** The timestamp when this property change occurred */
    val timestamp: Instant = Instant.now()
) {
    init {
        when (changeType) {
            PropertyChangeType.ADDED -> require(newValue != null) { "New value is required for ADDED change" }
            PropertyChangeType.REMOVED -> require(previousValue != null) { "Previous value is required for REMOVED change" }
            PropertyChangeType.MODIFIED -> {
                require(previousValue != null) { "Previous value is required for MODIFIED change" }
                require(newValue != null) { "New value is required for MODIFIED change" }
            }
        }
    }
    
    companion object {
        /**
         * Creates an added property change
         */
        fun added(property: Property, newValue: RdfTerm): PropertyChange {
            return PropertyChange(property, PropertyChangeType.ADDED, newValue = newValue)
        }
        
        /**
         * Creates a removed property change
         */
        fun removed(property: Property, previousValue: RdfTerm): PropertyChange {
            return PropertyChange(property, PropertyChangeType.REMOVED, previousValue = previousValue)
        }
        
        /**
         * Creates a modified property change
         */
        fun modified(property: Property, previousValue: RdfTerm, newValue: RdfTerm): PropertyChange {
            return PropertyChange(property, PropertyChangeType.MODIFIED, previousValue, newValue)
        }
    }
}

/**
 * Types of property changes
 */
enum class PropertyChangeType {
    /** Property was added */
    ADDED,
    
    /** Property was removed */
    REMOVED,
    
    /** Property value was modified */
    MODIFIED
} 