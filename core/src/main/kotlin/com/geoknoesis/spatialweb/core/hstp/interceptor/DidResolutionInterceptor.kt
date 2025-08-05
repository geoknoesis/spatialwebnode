package com.geoknoesis.spatialweb.core.hstp.interceptor


import com.geoknoesis.spatialweb.core.hstp.engine.MessageContext
import com.geoknoesis.spatialweb.identity.did.DidDocumentManager
import org.slf4j.LoggerFactory

class DidResolutionInterceptor : MessageInterceptor {

    private val logger = LoggerFactory.getLogger(DidResolutionInterceptor::class.java)

    override suspend fun intercept(
        context: MessageContext,
        next: suspend (MessageContext) -> Unit
    ) {
        val didManager: DidDocumentManager = context.engine.didDocumentManager

        val sourceDid = context.message.header.source
        val destinationDid = context.message.header.destination

        try {
            logger.debug("🔍 Resolving source DID: $sourceDid")
            val sourceResult = didManager.getDidDocument(sourceDid.toString())
            // TODO: Convert between DidDocument types when needed
            // For now, skip setting the document to avoid type mismatch
        } catch (e: Exception) {
            logger.error("❌ Failed to resolve source DID: $sourceDid", e)
            throw IllegalStateException("Failed to resolve source DID: $sourceDid", e)
        }
        if (destinationDid!=null) {
            try {
                logger.debug("🔍 Resolving destination DID: $destinationDid")
                val destResult = didManager.getDidDocument(destinationDid.toString())
                // TODO: Convert between DidDocument types when needed
                // For now, skip setting the document to avoid type mismatch
            } catch (e: Exception) {
                logger.error("❌ Failed to resolve destination DID: $destinationDid", e)
                throw IllegalStateException("Failed to resolve destination DID: $destinationDid", e)
            }
        }

        next(context)
    }
}