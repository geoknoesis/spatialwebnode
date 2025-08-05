package com.geoknoesis.spatialweb.core.transport

import com.geoknoesis.spatialweb.core.hstp.model.HSTPMessage
import com.geoknoesis.spatialweb.identity.did.Did


interface TransportBinding {
    fun start()
    fun stop()
    fun send(message: HSTPMessage)
    fun onReceive(handler : (message: HSTPMessage) -> Unit)
}

// 3. Pub/Sub extension for channel management
interface PubSubBinding : TransportBinding {
    /** Subscribe to an HSML Channel (SWID) */
    fun subscribe(channel: Did)

    /** Unsubscribe from an HSML Channel (SWID) */
    fun unsubscribe(channel: Did)
}

/**
 * Extension to indicate if a binding supports direct DID send.
 */
fun TransportBinding.supportsPointToPoint(): Boolean = true
