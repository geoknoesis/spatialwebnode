# MQTT Transport Binding

This module implements an MQTT transport binding for the Spatial Web Node using Eclipse Paho MQTT client. It provides both direct messaging and pub/sub functionality for HSTP (Hypermedia Spatial Transport Protocol) messages via MQTT topics.

## Features

- **Direct Messaging**: Send HSTP messages directly to specific peers using MQTT topics
- **Pub/Sub**: Subscribe to channels and publish messages to multiple subscribers
- **Multiple QoS Levels**: Support for QoS 0, 1, and 2 for different reliability requirements
- **Automatic Reconnection**: Built-in reconnection logic for network resilience
- **TLS Support**: Secure connections with TLS/SSL encryption
- **WebSocket Support**: Connect via WebSocket for browser-based clients
- **Retained Messages**: Optional message retention for late-joining subscribers
- **Custom Properties**: Support for broker-specific configuration options
- **Metrics**: Optional metrics collection for monitoring

## Configuration

The MQTT transport binding can be configured using YAML files or programmatically. Here's an example configuration:

```yaml
- name: "mqtt-local"
  brokerUrl: "tcp://localhost:1883"
  topicPrefix: "spatialweb/local"
  cleanSession: true
  connectionTimeout: 30
  keepAliveInterval: 60
  qos: 1
  retainMessages: false
```

### Configuration Options

| Option | Type | Default | Description |
|--------|------|---------|-------------|
| `name` | String | "mqtt" | Unique name for this transport binding |
| `brokerUrl` | String | "tcp://localhost:1883" | MQTT broker URL |
| `clientId` | String? | null | MQTT client ID (auto-generated if null) |
| `username` | String? | null | MQTT username for authentication |
| `password` | String? | null | MQTT password for authentication |
| `cleanSession` | Boolean | true | Whether to use clean session |
| `connectionTimeout` | Int | 30 | Connection timeout in seconds |
| `keepAliveInterval` | Int | 60 | Keep alive interval in seconds |
| `maxInflight` | Int | 1000 | Maximum number of in-flight messages |
| `automaticReconnect` | Boolean | true | Enable automatic reconnection |
| `maxReconnectDelay` | Int | 10000 | Maximum reconnection delay in milliseconds |
| `topicPrefix` | String | "spatialweb" | Prefix for all MQTT topics |
| `qos` | Int | 1 | Quality of Service level (0, 1, or 2) |
| `retainMessages` | Boolean | false | Whether to retain messages |
| `enableTLS` | Boolean | false | Enable TLS/SSL encryption |
| `tlsVersion` | String | "TLSv1.2" | TLS version to use |
| `enableLogging` | Boolean | true | Enable detailed logging |
| `enableMetrics` | Boolean | false | Enable metrics collection |
| `customProperties` | Map<String, String> | {} | Custom MQTT properties |

## Usage

### Basic Usage

```kotlin
// Create an MQTT transport binding
val config = MQTTTransportConfig.local()
val mqttBinding = MQTTTransportBinding(config)

// Start the transport
mqttBinding.start()

// Register message handler
mqttBinding.onReceive { message ->
    println("Received message: ${message.header.operation}")
}

// Send a direct message
val message = HSTPMessage(
    header = HSTPHeader(
        id = "msg_1",
        operation = "ping",
        source = Did("did:example:alice"),
        destination = Did("did:example:bob"),
        expectResponse = true
    ),
    payload = flowOf("Hello Bob!".toByteArray())
)

mqttBinding.send(message)

// Subscribe to a channel
val channel = Did("did:example:channel")
mqttBinding.subscribe(channel)

// Send a pub/sub message
val pubSubMessage = HSTPMessage(
    header = HSTPHeader(
        id = "msg_2",
        operation = "publish",
        source = Did("did:example:alice"),
        channel = channel
    ),
    payload = flowOf("Channel message".toByteArray())
)

mqttBinding.send(pubSubMessage)
```

### Using the Provider

```kotlin
// Create provider
val provider = MQTTTransportBindingProvider()

// Load configuration from file
val configStream = File("mqtt-transports.yml").inputStream()
val bindings = provider.createInstances(configStream)

// Get specific binding
val binding = provider.getInstance("mqtt-local")

// Start all bindings
bindings.forEach { it.start() }
```

## Topic Structure

The MQTT transport binding uses a structured topic hierarchy:

### Direct Messaging
- **Format**: `{topicPrefix}/direct/{did}`
- **Example**: `spatialweb/direct/did_example_alice`

### Channel Pub/Sub
- **Format**: `{topicPrefix}/channel/{did}`
- **Example**: `spatialweb/channel/did_example_channel`

### Topic Naming Rules
- DIDs are converted to topic-safe strings by replacing `:` with `_`
- All topics are prefixed with the configured `topicPrefix`
- Topics are case-sensitive

## QoS Levels

The MQTT transport binding supports all three QoS levels:

- **QoS 0 (At most once)**: Fire and forget, no acknowledgment
- **QoS 1 (At least once)**: Guaranteed delivery, may have duplicates
- **QoS 2 (Exactly once)**: Guaranteed delivery, no duplicates

## Security

The MQTT transport binding supports:

- **Username/Password Authentication**: Basic MQTT authentication
- **TLS/SSL Encryption**: Secure connections with certificate validation
- **Custom Properties**: Broker-specific security configurations
- **Client Certificates**: Mutual TLS authentication (via custom properties)

## Broker Compatibility

The MQTT transport binding is compatible with:

- **Eclipse Mosquitto**: Open-source MQTT broker
- **HiveMQ**: Enterprise MQTT broker
- **AWS IoT Core**: Cloud MQTT service
- **Azure IoT Hub**: Cloud MQTT service
- **Google Cloud IoT Core**: Cloud MQTT service
- **IBM Watson IoT**: Cloud MQTT service

## Monitoring

When metrics are enabled, the MQTT transport binding provides:

- Connection status
- Message throughput
- Topic subscription counts
- Error rates
- Reconnection attempts

## Troubleshooting

### Common Issues

1. **Connection Failures**: Check broker URL, network connectivity, and authentication
2. **Message Loss**: Increase QoS level or enable retained messages
3. **High Latency**: Reduce keep-alive interval or connection timeout
4. **Memory Issues**: Reduce maxInflight for resource-constrained environments

### Logging

Enable detailed logging by setting `enableLogging: true` in the configuration. This will provide detailed information about:

- Connection attempts and failures
- Message publishing and delivery
- Topic subscriptions
- Reconnection events
- Error conditions

## Dependencies

The MQTT transport binding requires:

- Eclipse Paho MQTT client
- Kotlin coroutines
- Jackson for YAML configuration
- SLF4J for logging

## Future Enhancements

- Message persistence
- Last Will and Testament (LWT)
- Shared subscriptions
- Message compression
- Advanced authentication methods
- Load balancing across multiple brokers
- Message queuing and buffering 