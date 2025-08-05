package com.geoknoesis.spatialweb.identity.did

import id.walt.did.dids.DidService
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

/**
 * The DefaultDidDocumentManager class is a concrete implementation of the DidDocumentManager interface.
 * It provides mechanisms for managing Decentralized Identifier (DID) documents, including retrieval,
 * refreshing, and removal of documents. The implementation also features caching with a configurable
 * Time-To-Live (TTL) to enhance performance by avoiding redundant resolutions of the same DID document.
 *
 * @param ttlSeconds The Time-To-Live (TTL) in seconds for caching resolved DID documents. Defaults to 3600 seconds.
 */
class DefaultDidDocumentManager(
    private val ttlSeconds: Long = 3600
) : DidDocumentManager {

    private val cache = ConcurrentHashMap<String, CacheEntry>()
    private val mutexes = ConcurrentHashMap<String, Mutex>()

    override suspend fun getDidDocument(did: String, forceRefresh: Boolean): DidDocumentManager.Result {
        val mutex = mutexes.computeIfAbsent(did) { Mutex() }
        return mutex.withLock {
            getCachedResultIfValid(did, forceRefresh) ?: run {
                val result = resolveDidDocument(did)
                if (!result.isDeactivated) {
                    cache[did] = CacheEntry(result)
                }
                result
            }
        }
    }

    private fun getCachedResultIfValid(did: String, forceRefresh: Boolean): DidDocumentManager.Result? {
        if (forceRefresh) return null
        val cached = cache[did] ?: return null
        return if (isFresh(cached)) cached.result else null
    }

    override suspend fun refreshDidDocument(did: String): DidDocumentManager.Result {
        val result = resolveDidDocument(did)
        if (!result.isDeactivated) {
            cache[did] = CacheEntry(result)
        } else {
            cache.remove(did)
        }
        return result
    }

    override fun removeDidDocument(did: String) {
        cache.remove(did)
    }

    private fun isFresh(entry: CacheEntry): Boolean {
        val ttlExpiry = entry.result.metadata.fetchedAt.plusSeconds(ttlSeconds)
        return Instant.now().isBefore(ttlExpiry)
    }

    private suspend fun resolveDidDocument(did: String): DidDocumentManager.Result {
        return try {
            val jsonElement = DidService.resolve(did).getOrThrow()
            val document = jsonElement.toDidDocument()
            val metadata = jsonElement.extractDidDocumentMetadata()
            DidDocumentManager.Result(
                did = did,
                document = document,
                metadata = metadata,
                isDeactivated = false
            )
        } catch (_: Exception) {
            val metadata = DidDocumentManager.Metadata(
                updated = null,
                versionId = null,
                deactivated = true,
                fetchedAt = Instant.now()
            )
            DidDocumentManager.Result(
                did = did,
                document = null,
                metadata = metadata,
                isDeactivated = true
            )
        }
    }

    private data class CacheEntry(val result: DidDocumentManager.Result)
}
