package com.geoknoesis.spatialweb.core.hstp.engine

import com.geoknoesis.spatialweb.core.hstp.interceptor.DidResolutionInterceptor
import com.geoknoesis.spatialweb.core.hstp.interceptor.LoggingInterceptor
import com.geoknoesis.spatialweb.core.hstp.interceptor.MessageInterceptor
import com.geoknoesis.spatialweb.core.hstp.model.HSTPMessage
import com.geoknoesis.spatialweb.core.hstp.operation.OperationManager
import com.geoknoesis.spatialweb.identity.did.DidDocumentManager
import com.geoknoesis.spatialweb.identity.vc.CredentialVerifier
import org.slf4j.LoggerFactory

class DefaultHSTPEngine(
    override val operationManager: OperationManager,
    override val didDocumentManager: DidDocumentManager,
    override val credentialVerifier: CredentialVerifier,
    private val transportManager: com.geoknoesis.spatialweb.core.transport.TransportManager? = null
) : HSTPEngine {

    private val logger = LoggerFactory.getLogger(DefaultHSTPEngine::class.java)
    private val pipeline: HSTPPipeline = createPipeline()

    override suspend fun handleMessage(message: HSTPMessage) {
        pipeline.run(message)
    }

    override suspend fun sendResponse(response: HSTPMessage) {
        transportManager?.send(response) ?: run {
            logger.warn("No transport manager available to send response")
        }
    }

    private fun createPipeline(): HSTPPipeline {
        val interceptors = listOf<MessageInterceptor>(
            LoggingInterceptor(),
            DidResolutionInterceptor()
            // Add more interceptors here
        )
        return HSTPPipeline(interceptors, this)
    }
}
