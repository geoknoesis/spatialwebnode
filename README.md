# hstpd

**hstpd** is a distributed spatial computing node implementing the Hypermedia Spatial Transport Protocol (HSTP).

## 📚 Documentation

📖 **[Full Documentation](https://geoknoesis.github.io/spatialwebnode/)** - Complete guide to hstpd

## 🚀 Quick Start

```bash
# Clone the repository
git clone https://github.com/geoknoesis/spatialwebnode.git
cd spatialwebnode

# Build the project
./gradlew build

# Run hstpd
./gradlew :node:run
```

## 🌟 Features

- 🌐 **HSTP Protocol Implementation** - Full support for Hypermedia Spatial Transport Protocol
- 🔐 **Decentralized Identity** - DID (Decentralized Identifier) support with credential verification
- 🚀 **Multiple Transport Bindings** - HTTP, MQTT, and P2P transport protocols
- 🏗️ **Modular Architecture** - Pluggable components and extensible design
- 📊 **Built-in Operations** - Ping/Pong, message routing, and custom operations
- 🐳 **Container Ready** - Docker and Kubernetes deployment support
- 📈 **Monitoring & Metrics** - Comprehensive logging and health checks

## 🏗️ Architecture

hstpd is built with a modular architecture:

```
hstpd/
├── core/                    # HSTP engine and operations
├── identity/                # DID and credential management
├── transport/               # Transport layer implementations
│   ├── http/               # HTTP transport binding
│   ├── mqtt/               # MQTT transport binding
│   └── p2p/                # P2P transport binding
├── node/                   # Main application node
└── common-utils/           # Shared utilities
```

## 📦 Installation

### Prerequisites

- Java 17 or later
- Gradle 8.0 or later

### Building from Source

```bash
# Clone the repository
git clone https://github.com/your-org/spatialwebnode.git
cd spatialwebnode

# Build the project
./gradlew build

# Run tests
./gradlew test
```

### Running hstpd

```bash
# Run with default configuration
./gradlew :node:run

# Run with custom configuration
./gradlew :node:run --args="--config config/custom.yml"

# Show help
./gradlew :node:run --args="--help"
```

## 🐳 Docker

```bash
# Build Docker image
docker build -t hstpd:latest .

# Run container
docker run -p 8080:8080 -p 1883:1883 -p 4001:4001 hstpd:latest
```

## ☸️ Kubernetes

```bash
# Deploy to Kubernetes
kubectl apply -f k8s/deployment.yaml

# Check deployment
kubectl get pods -l app=hstpd
```

## 🔧 Configuration

hstpd uses YAML configuration files:

```yaml
nodeId: "did:example:node:1"
name: "My hstpd Node"
version: "1.0.0"
transports:
  - "config/transports/http.yml"
  - "config/transports/mqtt.yml"
  - "config/transports/p2p.yml"
logging:
  level: "INFO"
  file: "logs/hstpd.log"
  console: true
```

## 🧪 Examples

### Ping/Pong Operation

```kotlin
// Send a ping message
val pingMessage = PingUtils.createPingMessage(
    source = Did("did:example:alice"),
    destination = Did("did:example:bob")
)

// Send the message
hstpEngine.sendMessage(pingMessage)
```

### Custom Transport Configuration

```yaml
# HTTP Transport
- name: "http-local"
  type: "http"
  enabled: true
  config:
    baseUrl: "http://localhost:8080"
    timeoutMs: 30000
    enableWebSockets: true

# MQTT Transport
- name: "mqtt-local"
  type: "mqtt"
  enabled: true
  config:
    brokerUrl: "tcp://localhost:1883"
    topicPrefix: "spatialweb/local"
    cleanSession: true
```

## 🤝 Contributing

We welcome contributions! Please see our [Contributing Guide](docs/developer-guide/contributing.md) for details.

### Development Setup

```bash
# Clone the repository
git clone https://github.com/your-org/spatialwebnode.git
cd spatialwebnode

# Build the project
./gradlew build

# Run tests
./gradlew test

# Run specific module tests
./gradlew :core:test
```

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🆘 Support

- 📖 **[Documentation](https://your-org.github.io/spatialwebnode/)**
- 🐛 **[Issues](https://github.com/your-org/spatialwebnode/issues)**
- 💬 **[Discussions](https://github.com/your-org/spatialwebnode/discussions)**

## 🙏 Acknowledgments

- [HSTP Protocol](https://hstp.dev) - Hypermedia Spatial Transport Protocol
- [DID Specification](https://www.w3.org/TR/did-core/) - Decentralized Identifiers
- [Kotlin](https://kotlinlang.org/) - Programming language
- [Gradle](https://gradle.org/) - Build system 