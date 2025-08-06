package com.geoknoesis.spatialweb.core.hstp.model

import kotlinx.serialization.Serializable

/**
 * HSTP Endpoint configuration
 */
@Serializable
data class HSTPEndpoint(
    val url: String,
    val protocol: String = "hstp",
    val version: String = "1.0"
) 