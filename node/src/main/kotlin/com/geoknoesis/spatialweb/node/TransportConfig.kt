package com.geoknoesis.spatialweb.node

/**
 * Configuration for transport bindings.
 */
data class TransportConfig(
    val name: String,
    val type: String,
    val enabled: Boolean,
    val config: Map<String, Any>
) {
    companion object {
        /**
         * Creates a default HTTP transport configuration.
         */
        fun getDefaultHttp(): TransportConfig {
            return TransportConfig(
                name = "http-local",
                type = "http",
                enabled = true,
                config = mapOf(
                    "baseUrl" to "http://localhost:8080",
                    "timeoutMs" to 30000,
                    "enableWebSockets" to true
                )
            )
        }
        
        /**
         * Creates a default MQTT transport configuration.
         */
        fun getDefaultMqtt(): TransportConfig {
            return TransportConfig(
                name = "mqtt-local",
                type = "mqtt",
                enabled = true,
                config = mapOf(
                    "brokerUrl" to "tcp://localhost:1883",
                    "topicPrefix" to "spatialweb/local",
                    "cleanSession" to true,
                    "connectionTimeout" to 30,
                    "keepAliveInterval" to 60,
                    "qos" to 1,
                    "retainMessages" to false
                )
            )
        }
        
        /**
         * Creates a default P2P transport configuration.
         */
        fun getDefaultP2P(): TransportConfig {
            return TransportConfig(
                name = "p2p-local",
                type = "p2p",
                enabled = true,
                config = mapOf(
                    "port" to 4001,
                    "discoveryEnabled" to true,
                    "bootstrapPeers" to emptyList<String>(),
                    "enableMetrics" to false
                )
            )
        }
    }
} 