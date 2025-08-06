package com.geoknoesis.spatialweb.core.udg

import com.geoknoesis.spatialweb.core.udg.model.*
import com.geoknoesis.spatialweb.identity.did.Did
import java.time.Instant

/**
 * Universal Domain Graph (UDG) Store Interface
 *
 * The UDGStore provides the underlying storage and retrieval operations for the UDG.
 * It handles the management of entities, holonic links, hyperspaces, states, and changes.
 */
interface UDGStore {

    // --- Entity Storage ---
    
    /**
     * Stores an entity
     */
    suspend fun storeEntity(entity: Entity): Boolean
    
    /**
     * Retrieves an entity by its DID
     */
    suspend fun getEntity(did: Did): Entity?
    
    /**
     * Updates an existing entity
     */
    suspend fun updateEntity(entity: Entity): Boolean
    
    /**
     * Removes an entity by its DID
     */
    suspend fun removeEntity(did: Did): Boolean
    
    /**
     * Lists all entities of a specific type
     */
    suspend fun listEntitiesByType(type: IRI): List<Entity>
    
    /**
     * Checks if an entity exists
     */
    suspend fun entityExists(did: Did): Boolean

    // --- Holonic Link Storage ---
    
    /**
     * Stores a holonic link
     */
    suspend fun storeHolonicLink(link: HolonicLinkInstance): Boolean
    
    /**
     * Removes a holonic link
     */
    suspend fun removeHolonicLink(whole: Did, part: Did, link: Property): Boolean
    
    /**
     * Gets all holonic links for a domain
     */
    suspend fun getHolonicLinks(domain: Did): List<HolonicLinkInstance>
    
    /**
     * Gets all holonic link types for a domain
     */
    suspend fun getHolonicLinkTypes(domain: Did): List<Property>
    
    /**
     * Checks if a holonic link exists
     */
    suspend fun holonicLinkExists(whole: Did, part: Did, link: Property): Boolean

    // --- Hyperspace Storage ---
    
    /**
     * Stores a hyperspace for a domain
     */
    suspend fun storeHyperspace(domain: Did, hyperspace: Hyperspace): Boolean
    
    /**
     * Gets the hyperspace for a domain
     */
    suspend fun getHyperspace(domain: Did): Hyperspace?
    
    /**
     * Removes a hyperspace for a domain
     */
    suspend fun removeHyperspace(domain: Did): Boolean

    // --- State Storage ---
    
    /**
     * Stores the state of an entity
     */
    suspend fun storeEntityState(did: Did, state: Map<Property, RdfTerm>): Boolean
    
    /**
     * Gets the state of an entity
     */
    suspend fun getEntityState(did: Did): Map<Property, RdfTerm>?
    
    /**
     * Updates the state of an entity
     */
    suspend fun updateEntityState(did: Did, newState: Map<Property, RdfTerm>): Boolean
    
    /**
     * Removes the state of an entity
     */
    suspend fun removeEntityState(did: Did): Boolean

    // --- Change Storage ---
    
    /**
     * Stores an entity change
     */
    suspend fun storeChange(change: EntityChange): Boolean
    
    /**
     * Gets changes for an entity since a specific time
     */
    suspend fun getChanges(did: Did, since: Instant? = null): List<EntityChange>
    
    /**
     * Gets all changes since a specific time
     */
    suspend fun getAllChanges(since: Instant? = null): List<EntityChange>
    
    /**
     * Removes changes older than a specific time
     */
    suspend fun cleanupChanges(before: Instant): Int

    // --- Subscription Storage ---
    
    /**
     * Stores a subscription
     */
    suspend fun storeSubscription(did: Did): Boolean
    
    /**
     * Removes a subscription
     */
    suspend fun removeSubscription(did: Did): Boolean
    
    /**
     * Gets all subscriptions
     */
    suspend fun getSubscriptions(): List<Did>
    
    /**
     * Checks if a subscription exists
     */
    suspend fun subscriptionExists(did: Did): Boolean

    // --- Store Management ---
    
    /**
     * Initializes the store
     */
    suspend fun initialize(): Boolean
    
    /**
     * Closes the store
     */
    suspend fun close()
    
    /**
     * Gets metadata about the store
     */
    fun getMetadata(): StoreMetadata
    
    /**
     * Performs a backup of the store
     */
    suspend fun backup(backupPath: String): Boolean
    
    /**
     * Restores the store from a backup
     */
    suspend fun restore(backupPath: String): Boolean
    
    /**
     * Performs maintenance operations
     */
    suspend fun performMaintenance(): MaintenanceReport
}

/**
 * Metadata about the UDG store
 */
data class StoreMetadata(
    /** Store type */
    val type: String,
    
    /** Store version */
    val version: String,
    
    /** Whether the store is connected */
    val connected: Boolean,
    
    /** Number of entities stored */
    val entityCount: Long,
    
    /** Number of holonic links stored */
    val holonicLinkCount: Long,
    
    /** Number of changes stored */
    val changeCount: Long,
    
    /** Number of subscriptions */
    val subscriptionCount: Long,
    
    /** Store size in bytes */
    val sizeBytes: Long,
    
    /** Last backup time */
    val lastBackup: Instant?,
    
    /** Additional metadata */
    val properties: Map<String, Any> = emptyMap()
)

/**
 * Report from maintenance operations
 */
data class MaintenanceReport(
    /** Whether maintenance was successful */
    val successful: Boolean,
    
    /** Number of entities processed */
    val entitiesProcessed: Long,
    
    /** Number of links processed */
    val linksProcessed: Long,
    
    /** Number of changes processed */
    val changesProcessed: Long,
    
    /** Number of orphaned records cleaned up */
    val orphanedRecordsCleaned: Long,
    
    /** Time taken for maintenance */
    val durationMs: Long,
    
    /** Any errors encountered */
    val errors: List<String> = emptyList(),
    
    /** Additional details */
    val details: Map<String, Any> = emptyMap()
) 