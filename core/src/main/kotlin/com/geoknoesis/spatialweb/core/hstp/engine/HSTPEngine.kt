package com.geoknoesis.spatialweb.core.hstp.engine

import com.geoknoesis.spatialweb.core.hstp.model.HSTPMessage
import com.geoknoesis.spatialweb.core.hstp.operation.OperationManager
import com.geoknoesis.spatialweb.identity.did.DidDocumentManager
import com.geoknoesis.spatialweb.identity.vc.CredentialVerifier

/**
 * Simplified engine facade for HSTP protocol handling.
 */
interface HSTPEngine {
    val operationManager: OperationManager
    val didDocumentManager: DidDocumentManager
    val credentialVerifier: CredentialVerifier
    suspend fun handleMessage(message: HSTPMessage)
    suspend fun sendResponse(response: HSTPMessage)
}
