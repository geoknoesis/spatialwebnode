package com.geoknoesis.spatialweb.transport.http

import com.geoknoesis.spatialweb.core.transport.TransportBinding

/**
 * Factory class for creating HTTP Transport Bindings with different configurations.
 */
object HTTPTransportFactory {

    /**
     * Creates a basic HTTP transport binding for local development.
     */
    fun createLocal(): HTTPTransportBinding {
        return HTTPTransportBinding(
            baseUrl = "http://localhost:8080",
            timeoutMs = 30000,
            enableWebSockets = true
        )
    }

    /**
     * Creates an HTTP transport binding for production use.
     */
    fun createProduction(baseUrl: String): HTTPTransportBinding {
        return HTTPTransportBinding(
            baseUrl = baseUrl,
            timeoutMs = 60000,
            enableWebSockets = true
        )
    }

    /**
     * Creates an HTTP transport binding with custom configuration.
     */
    fun createCustom(
        baseUrl: String,
        timeoutMs: Long = 30000,
        enableWebSockets: Boolean = true
    ): HTTPTransportBinding {
        return HTTPTransportBinding(
            baseUrl = baseUrl,
            timeoutMs = timeoutMs,
            enableWebSockets = enableWebSockets
        )
    }

    /**
     * Creates an HTTP-only transport binding (no WebSockets).
     */
    fun createHttpOnly(baseUrl: String): HTTPTransportBinding {
        return HTTPTransportBinding(
            baseUrl = baseUrl,
            timeoutMs = 30000,
            enableWebSockets = false
        )
    }

    /**
     * Creates a WebSocket-only transport binding for real-time communication.
     */
    fun createWebSocketOnly(baseUrl: String): HTTPTransportBinding {
        return HTTPTransportBinding(
            baseUrl = baseUrl,
            timeoutMs = 30000,
            enableWebSockets = true
        )
    }
} 