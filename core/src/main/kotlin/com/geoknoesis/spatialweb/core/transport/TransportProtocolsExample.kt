package com.geoknoesis.spatialweb.core.transport

import org.slf4j.LoggerFactory

/**
 * Example demonstrating the usage of TransportProtocols constants.
 * 
 * This example shows how to:
 * 1. Use protocol constants instead of hardcoded strings
 * 2. Validate protocols
 * 3. Check protocol types and characteristics
 * 4. Work with protocol lists
 */
object TransportProtocolsExample {
    
    private val logger = LoggerFactory.getLogger(TransportProtocolsExample::class.java)
    
    @JvmStatic
    fun main(args: Array<String>) {
        logger.info("Transport Protocols Example")
        
        // Example 1: Using protocol constants
        logger.info("=== Protocol Constants ===")
        logger.info("HTTP protocol: ${TransportProtocols.HTTP.HTTP}")
        logger.info("HTTPS protocol: ${TransportProtocols.HTTP.HTTPS}")
        logger.info("MQTT protocol: ${TransportProtocols.MQTT.MQTT}")
        logger.info("P2P protocol: ${TransportProtocols.P2P.P2P}")
        
        // Example 2: Getting all protocols for a transport type
        logger.info("=== Protocol Lists ===")
        logger.info("HTTP protocols: ${TransportProtocols.HTTP.ALL}")
        logger.info("MQTT protocols: ${TransportProtocols.MQTT.ALL}")
        logger.info("P2P protocols: ${TransportProtocols.P2P.ALL}")
        logger.info("GraphQL protocols: ${TransportProtocols.GraphQL.ALL}")
        
        // Example 3: Protocol validation
        logger.info("=== Protocol Validation ===")
        val testProtocols = listOf("http", "https", "mqtt", "p2p", "invalid", "ftp")
        
        testProtocols.forEach { protocol ->
            val isValid = TransportProtocols.Utils.isValidProtocol(protocol)
            logger.info("Protocol '$protocol' is valid: $isValid")
        }
        
        // Example 4: Protocol type checking
        logger.info("=== Protocol Type Checking ===")
        val protocolsToCheck = listOf("http", "mqtt", "p2p", "graphql+http")
        
        protocolsToCheck.forEach { protocol ->
            logger.info("Protocol '$protocol':")
            logger.info("  - Is HTTP: ${TransportProtocols.Utils.isHttpProtocol(protocol)}")
            logger.info("  - Is MQTT: ${TransportProtocols.Utils.isMqttProtocol(protocol)}")
            logger.info("  - Is P2P: ${TransportProtocols.Utils.isP2PProtocol(protocol)}")
            logger.info("  - Is GraphQL: ${TransportProtocols.Utils.isGraphQLProtocol(protocol)}")
        }
        
        // Example 5: Security and WebSocket detection
        logger.info("=== Security and WebSocket Detection ===")
        val securityTestProtocols = listOf("http", "https", "ws", "wss", "mqtt", "mqtts")
        
        securityTestProtocols.forEach { protocol ->
            val isSecure = TransportProtocols.Utils.isSecureProtocol(protocol)
            val isWebSocket = TransportProtocols.Utils.isWebSocketProtocol(protocol)
            logger.info("Protocol '$protocol':")
            logger.info("  - Is secure: $isSecure")
            logger.info("  - Is WebSocket: $isWebSocket")
        }
        
        // Example 6: WebSocket base protocol extraction
        logger.info("=== WebSocket Base Protocol Extraction ===")
        val wsProtocols = listOf("ws", "wss", "graphql+ws", "graphql+wss")
        
        wsProtocols.forEach { wsProtocol ->
            val baseProtocol = TransportProtocols.Utils.getBaseProtocolFromWebSocket(wsProtocol)
            logger.info("WebSocket protocol '$wsProtocol' base protocol: $baseProtocol")
        }
        
        // Example 7: Getting all supported protocols
        logger.info("=== All Supported Protocols ===")
        val allProtocols = TransportProtocols.Utils.getAllProtocols()
        logger.info("Total supported protocols: ${allProtocols.size}")
        logger.info("All protocols: $allProtocols")
        
        // Example 8: Case insensitive validation
        logger.info("=== Case Insensitive Validation ===")
        val caseTestProtocols = listOf("HTTP", "HTTPS", "MQTT", "P2P", "http", "https")
        
        caseTestProtocols.forEach { protocol ->
            val isValid = TransportProtocols.Utils.isValidProtocol(protocol)
            logger.info("Protocol '$protocol' is valid: $isValid")
        }
        
        // Example 9: Practical usage in transport binding selection
        logger.info("=== Transport Binding Selection ===")
        val targetProtocol = "https"
        
        when {
            TransportProtocols.Utils.isHttpProtocol(targetProtocol) -> {
                logger.info("Using HTTP transport binding for protocol: $targetProtocol")
            }
            TransportProtocols.Utils.isMqttProtocol(targetProtocol) -> {
                logger.info("Using MQTT transport binding for protocol: $targetProtocol")
            }
            TransportProtocols.Utils.isP2PProtocol(targetProtocol) -> {
                logger.info("Using P2P transport binding for protocol: $targetProtocol")
            }
            TransportProtocols.Utils.isGraphQLProtocol(targetProtocol) -> {
                logger.info("Using GraphQL transport binding for protocol: $targetProtocol")
            }
            else -> {
                logger.warn("No suitable transport binding found for protocol: $targetProtocol")
            }
        }
        
        // Example 10: Protocol filtering
        logger.info("=== Protocol Filtering ===")
        val mixedProtocols = listOf("http", "https", "mqtt", "p2p", "ws", "wss", "graphql+http")
        
        val secureProtocols = mixedProtocols.filter { TransportProtocols.Utils.isSecureProtocol(it) }
        val webSocketProtocols = mixedProtocols.filter { TransportProtocols.Utils.isWebSocketProtocol(it) }
        val httpProtocols = mixedProtocols.filter { TransportProtocols.Utils.isHttpProtocol(it) }
        
        logger.info("Secure protocols: $secureProtocols")
        logger.info("WebSocket protocols: $webSocketProtocols")
        logger.info("HTTP protocols: $httpProtocols")
        
        logger.info("Transport Protocols Example completed")
    }
} 