package com.geoknoesis.spatialweb.transport.http

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Configuration for HTTP Transport Binding instances.
 * This can be loaded from YAML configuration files.
 */
data class HTTPTransportConfig(
    val name: String,
    val baseUrl: String = "http://localhost:8080",
    val timeoutMs: Long = 30000,
    val enableWebSockets: Boolean = true,
    val maxConnections: Int = 10,
    val retryAttempts: Int = 3,
    val retryDelayMs: Long = 1000,
    val enableCompression: Boolean = true,
    val enableLogging: Boolean = true,
    val customHeaders: Map<String, String> = emptyMap()
) {
    companion object {
        /**
         * Creates a default configuration for local development.
         */
        fun local(): HTTPTransportConfig = HTTPTransportConfig(
            name = "local",
            baseUrl = "http://localhost:8080"
        )

        /**
         * Creates a production configuration.
         */
        fun production(name: String, baseUrl: String): HTTPTransportConfig = HTTPTransportConfig(
            name = name,
            baseUrl = baseUrl,
            timeoutMs = 60000,
            maxConnections = 50,
            retryAttempts = 5
        )
    }
} 