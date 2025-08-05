package com.geoknoesis.spatialweb.core.hstp.engine

import com.geoknoesis.spatialweb.core.hstp.model.HSTPMessage
import com.geoknoesis.spatialweb.identity.vc.VerificationResult
import id.walt.did.dids.document.DidDocument


class MessageContext(val message: HSTPMessage, val engine: HSTPEngine) {
    private var sourceDidDocument: DidDocument? = null
    private var destinationDidDocument: DidDocument? = null
    private var verifiedCredentials: List<VerificationResult> = emptyList()

    fun setSourceDidDocument(doc: DidDocument) {
        this.sourceDidDocument = doc
    }

    fun getSourceDidDocument(): DidDocument? = sourceDidDocument

    fun setDestinationDidDocument(doc: DidDocument) {
        this.destinationDidDocument = doc
    }

    fun getDestinationDidDocument(): DidDocument? = destinationDidDocument
}
