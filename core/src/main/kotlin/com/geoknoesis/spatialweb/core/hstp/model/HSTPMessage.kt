package com.geoknoesis.spatialweb.core.hstp.model

import com.geoknoesis.spatialweb.identity.did.Did
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.util.UUID


data class HSTPHeader(
    val id: String = UUID.randomUUID().toString(),
    val operation: String,
    val source: Did,
    val destination: Did? = null,
    val channel: Did? = null,
    val status: Int? = null,
    val inReplyTo: String? = null,
    val mediaType: String = "application/ld+json",
    val timestamp: Instant = Instant.now(),
    val expectResponse: Boolean = false
)

data class HSTPMessage(
    val header: HSTPHeader,
    val payload: Flow<ByteArray>
)


