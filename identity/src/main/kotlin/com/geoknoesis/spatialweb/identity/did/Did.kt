package com.geoknoesis.spatialweb.identity.did

import id.walt.did.dids.DidService


/**
 * A Decentralized Identifier (DID) value class that provides type-safe operations
 * for working with DIDs according to the W3C DID specification.
 * 
 * This class encapsulates a DID string and provides validation and utility methods
 * for DID operations. It uses Kotlin's value class for efficient memory usage
 * while maintaining type safety.
 * 
 * ## Features
 * - Automatic validation of DID format
 * - Type-safe DID operations
 * - DID resolution capabilities
 * - DID method extraction
 * - Memory-efficient value class implementation
 * 
 * ## Usage Example
 * ```kotlin
 * val did = Did("did:key:z6MkhaXgBZDvotDkL5257faiztiGiC2QtKLGpbnnEGta2doK")
 * println(did.method()) // "key"
 * val document = did.resolve() // Resolves the DID document
 * ```
 * 
 * ## DID Format Validation
 * The class validates that the DID string follows the format: `did:<method>:<method-specific-id>`
 * 
 * ## Supported DID Methods
 * - `did:key` - Self-contained DIDs using cryptographic keys
 * - `did:web` - DIDs using web domains
 * - `did:jwk` - DIDs using JSON Web Keys
 * - Any other DID method that follows the W3C specification
 * 
 * @see [W3C DID Specification](https://www.w3.org/TR/did-core/)
 * @see [Walt-ID DID Library](https://github.com/walt-id/waltid-identity)
 * 
 * @author SpatialWeb Team
 * @since 1.0.0
 */
@JvmInline
value class Did(val value: String) {
    
    /**
     * Validates that the DID string follows the correct format.
     * 
     * @throws IllegalArgumentException if the DID does not start with "did:"
     */
    init { 
        require(value.startsWith("did:")) { "Not a valid DID: $value" } 
    }
    
    /**
     * Resolves this DID to its corresponding DID Document.
     * 
     * This method delegates to the DidService to perform the actual resolution.
     * The resolution process fetches the DID Document from the appropriate
     * DID method resolver.
     * 
     * @return The resolved DID Document
     * @throws RuntimeException if the DID cannot be resolved
     * @see DidService.resolve
     */
    suspend fun resolve() = DidService.resolve(value)
    
    /**
     * Extracts the DID method from this DID.
     * 
     * The DID method is the second component in the DID string,
     * following the format: `did:<method>:<method-specific-id>`
     * 
     * @return The DID method string (e.g., "key", "web", "jwk")
     * @throws IndexOutOfBoundsException if the DID format is invalid
     */
    fun method() = value.split(":")[1]
    
    /**
     * Returns the string representation of this DID.
     * 
     * @return The DID string value
     */
    override fun toString(): String = value
    
    /**
     * Checks if this DID uses a specific method.
     * 
     * @param method The method to check for
     * @return true if this DID uses the specified method, false otherwise
     */
    fun usesMethod(method: String): Boolean = method() == method
    
    /**
     * Extracts the method-specific identifier from this DID.
     * 
     * @return The method-specific identifier (everything after the method)
     * @throws IndexOutOfBoundsException if the DID format is invalid
     */
    fun methodSpecificId(): String = value.substringAfter("${method()}:")
    
    /**
     * Creates a copy of this DID with a different method-specific identifier.
     * 
     * @param newMethodSpecificId The new method-specific identifier
     * @return A new DID with the updated identifier
     */
    fun withMethodSpecificId(newMethodSpecificId: String): Did = 
        Did("did:${method()}:$newMethodSpecificId")
}
