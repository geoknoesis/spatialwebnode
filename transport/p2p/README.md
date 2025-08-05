# P2P Transport Binding

This module implements a P2P transport binding for the Spatial Web Node using libp2p. It provides both direct messaging and pub/sub functionality for HSTP (Hypermedia Spatial Transport Protocol) messages.

## Features

- **Direct Messaging**: Send HSTP messages directly to specific peers using their DID
- **Pub/Sub**: Subscribe to channels and publish messages to multiple subscribers
- **Peer Discovery**: Automatic discovery of peers in the network
- **Bootstrap Peers**: Connect to known bootstrap peers for network entry
- **Multiple Protocols**: Support for TCP, UDP, and QUIC transports
- **NAT Traversal**: Automatic NAT traversal for peer-to-peer connectivity
- **Relay Support**: Message relay for peers behind restrictive firewalls
- **Metrics**: Optional metrics collection for monitoring

## Configuration

The P2P transport binding can be configured using YAML files or programmatically. Here's an example configuration:

```yaml
- name: "p2p-local"
  listenAddresses:
    - "/ip4/127.0.0.1/tcp/4001"
  enableDiscovery: false
  enablePubSub: true
  enablePing: true
  maxConnections: 10
  connectionTimeoutMs: 10000
  messageTimeoutMs: 5000
```

### Configuration Options

| Option | Type | Default | Description |
|--------|------|---------|-------------|
| `name` | String | "p2p" | Unique name for this transport binding |
| `listenAddresses` | List<String> | ["/ip4/0.0.0.0/tcp/4001"] | Addresses to listen on |
| `bootstrapPeers` | List<String> | [] | Known peers to connect to |
| `enableDiscovery` | Boolean | true | Enable peer discovery |
| `enablePubSub` | Boolean | true | Enable pub/sub functionality |
| `enablePing` | Boolean | true | Enable ping protocol |
| `maxConnections` | Int | 100 | Maximum number of connections |
| `connectionTimeoutMs` | Long | 30000 | Connection timeout in milliseconds |
| `messageTimeoutMs` | Long | 10000 | Message timeout in milliseconds |
| `enableRelay` | Boolean | false | Enable message relay |
| `enableNAT` | Boolean | true | Enable NAT traversal |
| `enableMetrics` | Boolean | false | Enable metrics collection |
| `privateKeyPath` | String? | null | Path to private key file |
| `enableCompression` | Boolean | true | Enable message compression |
| `enableLogging` | Boolean | true | Enable detailed logging |
| `customProtocols` | List<String> | [] | Custom protocols to support |

## Usage

### Basic Usage

```kotlin
// Create a P2P transport binding
val config = P2PTransportConfig.local()
val p2pBinding = P2PTransportBinding(config)

// Start the transport
p2pBinding.start()

// Register message handler
p2pBinding.onReceive { message ->
    println("Received message: ${message.header.operation}")
}

// Send a direct message
val message = HSTPMessage(
    header = HSTPMessage.Header(
        id = "msg_1",
        operation = "ping",
        source = Did("did:example:alice"),
        destination = Did("did:example:bob"),
        expectResponse = true
    ),
    payload = flowOf("Hello Bob!".toByteArray())
)

p2pBinding.send(message)

// Subscribe to a channel
val channel = Did("did:example:channel")
p2pBinding.subscribe(channel)

// Send a pub/sub message
val pubSubMessage = HSTPMessage(
    header = HSTPMessage.Header(
        id = "msg_2",
        operation = "publish",
        source = Did("did:example:alice"),
        channel = channel
    ),
    payload = flowOf("Channel message".toByteArray())
)

p2pBinding.send(pubSubMessage)
```

### Using the Provider

```kotlin
// Create provider
val provider = P2PTransportBindingProvider()

// Load configuration from file
val configStream = File("p2p-transports.yml").inputStream()
val bindings = provider.createInstances(configStream)

// Get specific binding
val binding = provider.getInstance("p2p-local")

// Start all bindings
bindings.forEach { it.start() }
```

## Address Formats

The P2P transport binding supports various address formats:

- **TCP**: `/ip4/127.0.0.1/tcp/4001`
- **UDP**: `/ip4/127.0.0.1/udp/4001`
- **QUIC**: `/ip4/127.0.0.1/udp/4001/quic`
- **IPv6**: `/ip6/::/tcp/4001`
- **DNS**: `/dnsaddr/example.com/tcp/4001`

## Bootstrap Peers

Bootstrap peers are known peers that help new nodes join the network. The default libp2p bootstrap peers are:

- `/dnsaddr/bootstrap.libp2p.io/p2p/QmNnooDu7bfjPFoTZYxMNLWUQJyrVwtbZg5gBMjTezGAJN`
- `/dnsaddr/bootstrap.libp2p.io/p2p/QmQCU2EcMqAqQPR2i9bChDtGNJchTbq5TbXJJ16u19uLTa`
- `/dnsaddr/bootstrap.libp2p.io/p2p/QmbLHAnMoJPWSCR5Zhtx6BHJX9KiKNN6tpvbUcqanj75Nb`

## Security

The P2P transport binding supports:

- **Private Keys**: Load private keys from files for secure peer identity
- **Encryption**: All messages are encrypted using libp2p's built-in encryption
- **Authentication**: Peer authentication using DIDs
- **Access Control**: Channel-based access control for pub/sub

## Monitoring

When metrics are enabled, the P2P transport binding provides:

- Connection count
- Message throughput
- Peer discovery statistics
- Pub/sub subscription counts
- Error rates

## Troubleshooting

### Common Issues

1. **Connection Timeouts**: Increase `connectionTimeoutMs` for slow networks
2. **Message Loss**: Increase `messageTimeoutMs` for large messages
3. **NAT Issues**: Enable `enableRelay` for peers behind restrictive firewalls
4. **High Memory Usage**: Reduce `maxConnections` for resource-constrained environments

### Logging

Enable detailed logging by setting `enableLogging: true` in the configuration. This will provide detailed information about:

- Peer connections and disconnections
- Message sending and receiving
- Pub/sub operations
- Discovery events
- Error conditions

## Dependencies

The P2P transport binding requires:

- libp2p libraries
- Kotlin coroutines
- Jackson for YAML configuration
- SLF4J for logging

## Future Enhancements

- WebRTC transport support
- Circuit relay optimization
- Advanced peer scoring
- Message encryption customization
- Protocol versioning
- Load balancing across multiple transports 