package com.geoknoesis.spatialweb.core.transport

/**
 * Constants for transport protocols supported by TransportBindingProvider implementations.
 * 
 * These constants provide a centralized way to reference transport protocols
 * and ensure consistency across the codebase.
 */
object TransportProtocols {
    
    // HTTP-based protocols
    object HTTP {
        /** HTTP protocol */
        const val HTTP = "http"
        
        /** HTTPS protocol (HTTP over TLS) */
        const val HTTPS = "https"
        
        /** WebSocket protocol */
        const val WEBSOCKET = "ws"
        
        /** WebSocket Secure protocol (WebSocket over TLS) */
        const val WEBSOCKET_SECURE = "wss"
        
        /** All HTTP-based protocols */
        val ALL = listOf(HTTP, HTTPS, WEBSOCKET, WEBSOCKET_SECURE)
    }
    
    // MQTT-based protocols
    object MQTT {
        /** MQTT protocol */
        const val MQTT = "mqtt"
        
        /** MQTT over TLS */
        const val MQTTS = "mqtts"
        
        /** MQTT over WebSocket */
        const val MQTT_WS = "ws"
        
        /** MQTT over WebSocket Secure */
        const val MQTT_WSS = "wss"
        
        /** All MQTT-based protocols */
        val ALL = listOf(MQTT, MQTTS, MQTT_WS, MQTT_WSS)
    }
    
    // P2P-based protocols
    object P2P {
        /** Generic P2P protocol */
        const val P2P = "p2p"
        
        /** libp2p protocol */
        const val LIBP2P = "libp2p"
        
        /** IPFS protocol */
        const val IPFS = "ipfs"
        
        /** All P2P-based protocols */
        val ALL = listOf(P2P, LIBP2P, IPFS)
    }
    
    // GraphQL-based protocols (future)
    object GraphQL {
        /** GraphQL over HTTP */
        const val GRAPHQL_HTTP = "graphql+http"
        
        /** GraphQL over HTTPS */
        const val GRAPHQL_HTTPS = "graphql+https"
        
        /** GraphQL over WebSocket */
        const val GRAPHQL_WS = "graphql+ws"
        
        /** GraphQL over WebSocket Secure */
        const val GRAPHQL_WSS = "graphql+wss"
        
        /** All GraphQL-based protocols */
        val ALL = listOf(GRAPHQL_HTTP, GRAPHQL_HTTPS, GRAPHQL_WS, GRAPHQL_WSS)
    }
    
    // Utility methods
    object Utils {
        /**
         * Checks if a protocol is HTTP-based.
         */
        fun isHttpProtocol(protocol: String): Boolean = HTTP.ALL.contains(protocol.lowercase())
        
        /**
         * Checks if a protocol is MQTT-based.
         */
        fun isMqttProtocol(protocol: String): Boolean = MQTT.ALL.contains(protocol.lowercase())
        
        /**
         * Checks if a protocol is P2P-based.
         */
        fun isP2PProtocol(protocol: String): Boolean = P2P.ALL.contains(protocol.lowercase())
        
        /**
         * Checks if a protocol is GraphQL-based.
         */
        fun isGraphQLProtocol(protocol: String): Boolean = GraphQL.ALL.contains(protocol.lowercase())
        
        /**
         * Checks if a protocol uses TLS/SSL encryption.
         */
        fun isSecureProtocol(protocol: String): Boolean {
            val lowerProtocol = protocol.lowercase()
            return (lowerProtocol == "https" || lowerProtocol == "wss" || lowerProtocol == "mqtts" || 
                    lowerProtocol == "graphql+https" || lowerProtocol == "graphql+wss") ||
                   lowerProtocol.contains("tls") || 
                   lowerProtocol.contains("ssl")
        }
        
        /**
         * Checks if a protocol supports WebSocket.
         */
        fun isWebSocketProtocol(protocol: String): Boolean {
            val lowerProtocol = protocol.lowercase()
            return lowerProtocol == "ws" || lowerProtocol == "wss" || 
                   lowerProtocol == "graphql+ws" || lowerProtocol == "graphql+wss"
        }
        
        /**
         * Gets the base protocol from a WebSocket protocol.
         * Example: "wss" -> "https", "ws" -> "http"
         */
        fun getBaseProtocolFromWebSocket(wsProtocol: String): String {
            return when (wsProtocol.lowercase()) {
                "ws" -> HTTP.HTTP
                "wss" -> HTTP.HTTPS
                else -> wsProtocol
            }
        }
        
        /**
         * Gets all supported protocols across all transport types.
         */
        fun getAllProtocols(): List<String> {
            return (HTTP.ALL + MQTT.ALL + P2P.ALL + GraphQL.ALL).distinct()
        }
        
        /**
         * Validates if a protocol is supported by any transport binding provider.
         */
        fun isValidProtocol(protocol: String): Boolean {
            return getAllProtocols().contains(protocol.lowercase())
        }
    }
} 