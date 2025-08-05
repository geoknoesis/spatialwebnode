# Transport Protocol Constants

This document describes the transport protocol constants used by the Spatial Web Node transport binding providers.

## Overview

The `TransportProtocols` object provides centralized constants for all transport protocols supported by the various transport binding providers. This ensures consistency across the codebase and makes it easier to reference protocols without hardcoding strings.

## Protocol Categories

### HTTP Protocols

HTTP-based transport protocols for RESTful communication and WebSocket connections.

```kotlin
TransportProtocols.HTTP.HTTP              // "http"
TransportProtocols.HTTP.HTTPS             // "https"
TransportProtocols.HTTP.WEBSOCKET         // "ws"
TransportProtocols.HTTP.WEBSOCKET_SECURE  // "wss"
TransportProtocols.HTTP.ALL               // List of all HTTP protocols
```

**Supported by:** `HTTPTransportBindingProvider`

### MQTT Protocols

MQTT-based transport protocols for publish/subscribe messaging.

```kotlin
TransportProtocols.MQTT.MQTT      // "mqtt"
TransportProtocols.MQTT.MQTTS     // "mqtts"
TransportProtocols.MQTT.MQTT_WS   // "ws"
TransportProtocols.MQTT.MQTT_WSS  // "wss"
TransportProtocols.MQTT.ALL       // List of all MQTT protocols
```

**Supported by:** `MQTTTransportBindingProvider`

### P2P Protocols

Peer-to-peer transport protocols for decentralized communication.

```kotlin
TransportProtocols.P2P.P2P        // "p2p"
TransportProtocols.P2P.LIBP2P     // "libp2p"
TransportProtocols.P2P.IPFS       // "ipfs"
TransportProtocols.P2P.ALL        // List of all P2P protocols
```

**Supported by:** `P2PTransportBindingProvider`

### GraphQL Protocols

GraphQL-based transport protocols for query-based communication (future).

```kotlin
TransportProtocols.GraphQL.GRAPHQL_HTTP   // "graphql+http"
TransportProtocols.GraphQL.GRAPHQL_HTTPS  // "graphql+https"
TransportProtocols.GraphQL.GRAPHQL_WS     // "graphql+ws"
TransportProtocols.GraphQL.GRAPHQL_WSS    // "graphql+wss"
TransportProtocols.GraphQL.ALL            // List of all GraphQL protocols
```

## Utility Methods

The `TransportProtocols.Utils` object provides helpful methods for working with protocols:

### Protocol Validation

```kotlin
// Check if a protocol is valid
TransportProtocols.Utils.isValidProtocol("http")  // true
TransportProtocols.Utils.isValidProtocol("ftp")   // false

// Get all supported protocols
val allProtocols = TransportProtocols.Utils.getAllProtocols()
```

### Protocol Type Checking

```kotlin
// Check protocol types
TransportProtocols.Utils.isHttpProtocol("https")   // true
TransportProtocols.Utils.isMqttProtocol("mqtt")    // true
TransportProtocols.Utils.isP2PProtocol("p2p")      // true
TransportProtocols.Utils.isGraphQLProtocol("graphql+http") // true
```

### Security and WebSocket Detection

```kotlin
// Check if protocol is secure
TransportProtocols.Utils.isSecureProtocol("https")  // true
TransportProtocols.Utils.isSecureProtocol("http")   // false

// Check if protocol supports WebSocket
TransportProtocols.Utils.isWebSocketProtocol("ws")  // true
TransportProtocols.Utils.isWebSocketProtocol("http") // false

// Extract base protocol from WebSocket
TransportProtocols.Utils.getBaseProtocolFromWebSocket("ws")   // "http"
TransportProtocols.Utils.getBaseProtocolFromWebSocket("wss")  // "https"
```

## Usage Examples

### Basic Protocol Usage

```kotlin
// Use constants instead of hardcoded strings
val httpBinding = HTTPTransportBindingProvider()
val supportedProtocols = httpBinding.getSupportedProtocols()

// Check if a specific protocol is supported
if (supportedProtocols.contains(TransportProtocols.HTTP.HTTPS)) {
    println("HTTPS is supported")
}
```

### Protocol Validation

```kotlin
// Validate user input
val userProtocol = "https"
if (TransportProtocols.Utils.isValidProtocol(userProtocol)) {
    // Process the protocol
    if (TransportProtocols.Utils.isHttpProtocol(userProtocol)) {
        println("Using HTTP transport")
    }
} else {
    println("Unsupported protocol: $userProtocol")
}
```

### Transport Binding Selection

```kotlin
fun selectTransportBinding(protocol: String): TransportBindingProvider? {
    return when {
        TransportProtocols.Utils.isHttpProtocol(protocol) -> HTTPTransportBindingProvider()
        TransportProtocols.Utils.isMqttProtocol(protocol) -> MQTTTransportBindingProvider()
        TransportProtocols.Utils.isP2PProtocol(protocol) -> P2PTransportBindingProvider()
        else -> null
    }
}
```

### Protocol Filtering

```kotlin
val protocols = listOf("http", "https", "mqtt", "p2p", "ws", "wss")

// Filter secure protocols
val secureProtocols = protocols.filter { TransportProtocols.Utils.isSecureProtocol(it) }
// Result: ["https", "wss"]

// Filter WebSocket protocols
val wsProtocols = protocols.filter { TransportProtocols.Utils.isWebSocketProtocol(it) }
// Result: ["ws", "wss"]
```

## Integration with Transport Binding Providers

All transport binding providers now use these constants:

```kotlin
class HTTPTransportBindingProvider : TransportBindingProvider {
    override fun getSupportedProtocols(): List<String> = TransportProtocols.HTTP.ALL
}

class MQTTTransportBindingProvider : TransportBindingProvider {
    override fun getSupportedProtocols(): List<String> = TransportProtocols.MQTT.ALL
}

class P2PTransportBindingProvider : TransportBindingProvider {
    override fun getSupportedProtocols(): List<String> = TransportProtocols.P2P.ALL
}
```

## Benefits

1. **Consistency**: All protocol references use the same constants
2. **Maintainability**: Changes to protocol names only need to be made in one place
3. **Type Safety**: Reduces the risk of typos in protocol strings
4. **Documentation**: Constants serve as self-documenting code
5. **Validation**: Built-in utility methods for protocol validation
6. **Extensibility**: Easy to add new protocols and transport types

## Testing

Run the transport protocol tests:

```bash
./gradlew :core:test --tests "com.geoknoesis.spatialweb.core.transport.TransportProtocolsTest"
```

## Future Enhancements

- Protocol versioning support
- Protocol capability negotiation
- Protocol-specific configuration validation
- Protocol performance metrics
- Protocol fallback mechanisms 