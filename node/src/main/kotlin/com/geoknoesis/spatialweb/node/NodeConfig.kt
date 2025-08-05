package com.geoknoesis.spatialweb.node

import java.time.Duration

/**
 * Configuration for the Spatial Web Node.
 */
data class NodeConfig(
    val nodeId: String,
    val name: String,
    val version: String,
    val transports: List<String>,
    val logging: LoggingConfig,
    val metrics: MetricsConfig,
    val security: SecurityConfig
) {
    companion object {
        /**
         * Creates a default node configuration.
         */
        fun getDefault(): NodeConfig {
            return NodeConfig(
                nodeId = "did:spatialweb:node:${java.util.UUID.randomUUID()}",
                name = "Spatial Web Node",
                version = "1.0.0",
                transports = listOf(
                    "config/transports/http.yml",
                    "config/transports/mqtt.yml",
                    "config/transports/p2p.yml"
                ),
                logging = LoggingConfig.getDefault(),
                metrics = MetricsConfig.getDefault(),
                security = SecurityConfig.getDefault()
            )
        }
    }
}

/**
 * Logging configuration.
 */
data class LoggingConfig(
    val level: String,
    val file: String?,
    val console: Boolean,
    val maxFileSize: String,
    val maxFiles: Int
) {
    companion object {
        fun getDefault(): LoggingConfig {
            return LoggingConfig(
                level = "INFO",
                file = "logs/spatialweb-node.log",
                console = true,
                maxFileSize = "10MB",
                maxFiles = 5
            )
        }
    }
}

/**
 * Metrics configuration.
 */
data class MetricsConfig(
    val enabled: Boolean,
    val port: Int,
    val path: String
) {
    companion object {
        fun getDefault(): MetricsConfig {
            return MetricsConfig(
                enabled = false,
                port = 8080,
                path = "/metrics"
            )
        }
    }
}

/**
 * Security configuration.
 */
data class SecurityConfig(
    val tlsEnabled: Boolean,
    val certificatePath: String?,
    val keyPath: String?,
    val allowedOrigins: List<String>
) {
    companion object {
        fun getDefault(): SecurityConfig {
            return SecurityConfig(
                tlsEnabled = false,
                certificatePath = null,
                keyPath = null,
                allowedOrigins = listOf("*")
            )
        }
    }
} 