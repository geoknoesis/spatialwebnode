# Ping Operation Implementation

This document describes the ping operation implementation for the Spatial Web Node HSTP (Hypermedia Spatial Transport Protocol).

## Overview

The ping operation is a fundamental network operation used for:
- Health checks and connectivity testing
- Measuring round-trip time (RTT) between nodes
- Verifying that a destination node is reachable and responsive
- Basic network diagnostics

## Components

### 1. PingOperationHandler

Handles incoming ping messages and responds with pong messages.

**Key Features:**
- Automatically responds to ping messages when `expectResponse` is true
- Creates proper pong response messages with correct headers
- Logs ping operations for debugging and monitoring

**Usage:**
```kotlin
val handler = PingOperationHandler()
operationManager.register(handler)
```

### 2. PongOperationHandler

Handles incoming pong messages (responses to ping messages).

**Key Features:**
- Processes pong responses
- Logs round-trip information
- Can be extended to track latency metrics

**Usage:**
```kotlin
val handler = PongOperationHandler()
operationManager.register(handler)
```

### 3. PingUtils

Utility class providing helper methods for ping operations.

**Key Methods:**
- `createPingMessage()` - Creates ping messages for direct communication
- `createChannelPingMessage()` - Creates ping messages for pub/sub channels
- `calculateRoundTripTime()` - Calculates RTT between ping and pong
- `isPongResponse()` - Validates if a message is a pong response

## Message Flow

### Direct Ping (Point-to-Point)

1. **Ping Request:**
   ```
   Source: did:example:alice
   Destination: did:example:bob
   Operation: ping
   ExpectResponse: true
   ```

2. **Pong Response:**
   ```
   Source: did:example:bob
   Destination: did:example:alice
   Operation: pong
   InReplyTo: [ping-message-id]
   Status: 200
   ```

### Channel Ping (Pub/Sub)

1. **Ping Broadcast:**
   ```
   Source: did:example:alice
   Channel: did:example:channel
   Operation: ping
   ExpectResponse: false
   ```

2. **Multiple Pong Responses:**
   ```
   Source: did:example:bob
   Channel: did:example:channel
   Operation: pong
   InReplyTo: [ping-message-id]
   ```

## Usage Examples

### Basic Ping

```kotlin
// Create a ping message
val pingMessage = PingUtils.createPingMessage(
    source = Did("did:example:alice"),
    destination = Did("did:example:bob")
)

// Send the ping
transportBinding.send(pingMessage)
```

### Channel Ping

```kotlin
// Create a channel ping message
val channelPing = PingUtils.createChannelPingMessage(
    source = Did("did:example:alice"),
    channel = Did("did:example:channel")
)

// Send to channel
pubSubBinding.send(channelPing)
```

### Measuring Round-Trip Time

```kotlin
// Store ping timestamp
val pingMessage = PingUtils.createPingMessage(source, destination)
val pingTime = pingMessage.header.timestamp

// When pong is received
val rtt = PingUtils.calculateRoundTripTime(pingMessage, pongMessage)
println("Round-trip time: ${rtt}ms")
```

## Configuration

### Registering Handlers

```kotlin
val operationManager = OperationManager()

// Register ping handlers
operationManager.register(PingOperationHandler())
operationManager.register(PongOperationHandler())

// Or register all at once
operationManager.registerAll(listOf(
    PingOperationHandler(),
    PongOperationHandler()
))
```

### Engine Setup

```kotlin
val engine = DefaultHSTPEngine(
    operationManager = operationManager,
    didDocumentManager = didManager,
    credentialVerifier = credentialVerifier,
    transportManager = transportManager
)
```

## Transport Support

The ping operation works with all transport bindings:

- **HTTP/HTTPS**: Direct ping via HTTP requests
- **MQTT**: Ping via MQTT topics with QoS support
- **P2P**: Ping via libp2p direct messaging
- **WebSocket**: Real-time ping/pong for low latency

## Error Handling

### Common Scenarios

1. **No Response Received:**
   - Network connectivity issues
   - Destination node offline
   - Firewall blocking communication

2. **Invalid Response:**
   - Malformed pong message
   - Wrong operation type
   - Missing required headers

3. **Timeout:**
   - High network latency
   - Destination node overloaded
   - Network congestion

### Logging

Both handlers provide detailed logging:

```kotlin
// Enable debug logging to see ping/pong operations
logger.debug("Handling ping message from ${message.header.source}")
logger.debug("Sent pong response: ${response.header.id}")
```

## Testing

Run the ping operation tests:

```bash
./gradlew :core:test --tests "com.geoknoesis.spatialweb.core.hstp.operation.PingOperationTest"
```

## Future Enhancements

- **Latency Tracking**: Persistent storage of RTT metrics
- **Health Monitoring**: Automatic ping scheduling for node health
- **Load Balancing**: Ping-based node selection
- **Security**: Signed ping/pong messages
- **Compression**: Efficient payload encoding
- **Batch Operations**: Multiple ping requests in single message 