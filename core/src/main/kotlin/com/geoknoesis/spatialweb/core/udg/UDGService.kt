package com.geoknoesis.spatialweb.core.udg

import com.geoknoesis.spatialweb.core.hstp.model.HSTPEndpoint
import com.geoknoesis.spatialweb.core.udg.model.*
import com.geoknoesis.spatialweb.identity.did.Did
import com.geoknoesis.spatialweb.identity.did.DidDocument
import java.time.Instant

/**
 * Universal Domain Graph (UDG) Service Interface
 *
 * The UDGService provides comprehensive management of entities, domains, and their relationships
 * in the Spatial Web. It handles entity lifecycle, holonic operations, hyperspace binding,
 * state management, validation, federation, and change tracking.
 */
interface UDGService {

    // --- Entity Lifecycle (Domain included) ---
    
    /**
     * Creates a new entity in the UDG
     */
    fun createEntity(entity: Entity): Entity
    
    /**
     * Resolves an entity by its DID
     */
    fun resolveEntity(did: Did): Entity?
    
    /**
     * Updates an existing entity
     */
    fun updateEntity(entity: Entity): Entity
    
    /**
     * Deletes an entity by its DID
     */
    fun deleteEntity(did: Did): Boolean
    
    /**
     * Lists all entities of a specific type
     */
    fun listEntitiesByType(type: IRI): List<Entity>

    // --- Holonic Operations (only valid if entity is a Domain) ---
    
    /**
     * Adds a holonic link between a whole and its part
     */
    fun addHolonicLink(
        whole: Did,
        part: Did,
        link: Property,
        annotations: Map<Property, RdfTerm> = emptyMap()
    ): Boolean

    /**
     * Removes a holonic link between a whole and its part
     */
    fun removeHolonicLink(whole: Did, part: Did, link: Property): Boolean
    
    /**
     * Gets all holonic links for a domain
     */
    fun getHolonicLinks(domain: Did): List<HolonicLinkInstance>
    
    /**
     * Gets all holonic link types for a domain
     */
    fun getHolonicLinkTypes(domain: Did): List<Property>

    // --- Hyperspace Binding (Domains only) ---
    
    /**
     * Gets the hyperspace for a domain
     */
    fun getHyperspace(domain: Did): Hyperspace?
    
    /**
     * Updates the hyperspace for a domain
     */
    fun updateHyperspace(domain: Did, hyperspace: Hyperspace): Boolean

    // --- State & Validation ---
    
    /**
     * Gets the current state of an entity
     */
    fun getEntityState(did: Did): Map<Property, RdfTerm>
    
    /**
     * Updates the state of an entity
     */
    fun updateEntityState(did: Did, newState: Map<Property, RdfTerm>): Boolean
    
    /**
     * Validates an entity against a shape graph
     */
    fun validateEntity(did: Did, shapeGraph: IRI): ValidationReport

    // --- Federation ---
    
    /**
     * Synchronizes an entity from the network
     */
    fun syncEntity(did: Did): Entity?
    
    /**
     * Subscribes to changes for an entity
     */
    fun subscribe(did: Did): Boolean
    
    /**
     * Unsubscribes from changes for an entity
     */
    fun unsubscribe(did: Did): Boolean
    
    /**
     * Lists all current subscriptions
     */
    fun listSubscriptions(): List<Did>

    // --- Identity & Routing ---
    
    /**
     * Resolves the HSTP endpoint for a DID
     */
    fun resolveEndpoint(did: Did): HSTPEndpoint?
    
    /**
     * Resolves the DID document for a DID
     */
    fun resolveDidDocument(did: Did): DidDocument?
    
    /**
     * Gets the local node's DID
     */
    fun getLocalNodeId(): Did

    // --- Change Tracking ---
    
    /**
     * Records a change to an entity
     */
    fun recordChange(change: EntityChange)
    
    /**
     * Gets changes for an entity since a specific time
     */
    fun getChanges(did: Did, since: Instant? = null): List<EntityChange>
    
    /**
     * Rebuilds an entity from its change history
     */
    fun rebuildEntity(did: Did): Entity?
} 