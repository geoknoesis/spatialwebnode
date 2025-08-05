package com.geoknoesis.spatialweb.identity.did

import java.time.Instant

interface DidDocumentManager {
    suspend fun getDidDocument(did: String, forceRefresh: Boolean = false): Result
    suspend fun refreshDidDocument(did: String): Result
    fun removeDidDocument(did: String)

    data class Result(
        val did: String,
        val document: DidDocument?,
        val metadata: Metadata,
        val isDeactivated: Boolean = false
    )

    data class Metadata(
        val updated: Instant? = null,
        val versionId: String? = null,
        val deactivated: Boolean = false,
        val fetchedAt: Instant = Instant.now()
    )
}
