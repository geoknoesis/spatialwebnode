package com.geoknoesis.spatialweb.core.transport

import com.geoknoesis.spatialweb.core.hstp.model.HSTPMessage
import com.geoknoesis.spatialweb.identity.did.Did

interface ChannelManager {
    fun join(channel: Did)
    fun leave(channel: Did)
    fun onChannelMessage(channel: Did, handler: (HSTPMessage) -> Unit)
}