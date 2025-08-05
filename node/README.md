# Spatial Web Node

A distributed spatial computing node that implements the Hypermedia Spatial Transport Protocol (HSTP) with support for multiple transport bindings.

## Features

- **Multi-Transport Support**: HTTP, MQTT, and P2P transport bindings
- **HSTP Engine**: Full implementation of the Hypermedia Spatial Transport Protocol
- **DID Integration**: Decentralized identifier support
- **YAML Configuration**: Flexible configuration management
- **Container Ready**: Docker and Kubernetes deployment support
- **Health Monitoring**: Built-in health checks and metrics
- **Graceful Shutdown**: Proper cleanup and resource management

## Quick Start

### Prerequisites

- Java 17 or higher
- Gradle 8.0 or higher

### Building

```bash
# Build the entire project
./gradlew build

# Build just the node
./gradlew :node:build
```

### Running Locally

```bash
# Run with default configuration
./gradlew :node:run

# Run with custom configuration
./gradlew :node:run --args="--config=/path/to/config.yml"

# Run the JAR directly
java -jar node/build/libs/node-*.jar
```

### Command Line Options

```bash
spatialweb-node [options]

Options:
  -c, --config <file>    Configuration file path (default: config/node.yml)
  -h, --help            Show this help message
  -v, --version         Show version information
```

## Configuration

The node uses YAML configuration files for setup. The main configuration file is `config/node.yml`.

### Node Configuration

```yaml
nodeId: "did:spatialweb:node:example"
name: "Spatial Web Node"
version: "1.0.0"
transports:
  - "config/transports/http.yml"
  - "config/transports/mqtt.yml"
  - "config/transports/p2p.yml"
logging:
  level: "INFO"
  file: "logs/spatialweb-node.log"
  console: true
  maxFileSize: "10MB"
  maxFiles: 5
metrics:
  enabled: false
  port: 8080
  path: "/metrics"
security:
  tlsEnabled: false
  certificatePath: null
  keyPath: null
  allowedOrigins:
    - "*"
```

### Transport Configurations

Each transport type has its own configuration file:

#### HTTP Transport (`config/transports/http.yml`)
```yaml
- name: "http-local"
  type: "http"
  enabled: true
  config:
    baseUrl: "http://localhost:8080"
    timeoutMs: 30000
    enableWebSockets: true
```

#### MQTT Transport (`config/transports/mqtt.yml`)
```yaml
- name: "mqtt-local"
  type: "mqtt"
  enabled: true
  config:
    brokerUrl: "tcp://localhost:1883"
    topicPrefix: "spatialweb/local"
    cleanSession: true
    connectionTimeout: 30
    keepAliveInterval: 60
    qos: 1
    retainMessages: false
```

#### P2P Transport (`config/transports/p2p.yml`)
```yaml
- name: "p2p-local"
  type: "p2p"
  enabled: true
  config:
    port: 4001
    discoveryEnabled: true
    bootstrapPeers: []
    enableMetrics: false
```

## Docker Deployment

### Building the Image

```bash
# Build the application
./gradlew :node:build

# Build the Docker image
docker build -t spatialweb/node:latest .
```

### Running with Docker

```bash
# Run with default configuration
docker run -p 8080:8080 -p 4001:4001 -p 1883:1883 spatialweb/node:latest

# Run with custom configuration
docker run -v /path/to/config:/app/config spatialweb/node:latest --config=/app/config/node.yml
```

### Docker Compose

```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f spatialweb-node

# Stop all services
docker-compose down
```

## Kubernetes Deployment

### Deploy to Kubernetes

```bash
# Apply the deployment
kubectl apply -f k8s/deployment.yaml

# Check the deployment status
kubectl get pods -l app=spatialweb-node

# View logs
kubectl logs -l app=spatialweb-node
```

### Scaling

```bash
# Scale to 3 replicas
kubectl scale deployment spatialweb-node --replicas=3
```

## Development

### Project Structure

```
node/
├── src/main/kotlin/
│   └── com/geoknoesis/spatialweb/node/
│       ├── SpatialWebNodeApplication.kt    # Main application class
│       ├── NodeApplication.kt              # Entry point
│       ├── NodeConfig.kt                   # Configuration classes
│       ├── TransportConfig.kt
│       ├── NodeConfigLoader.kt             # YAML loaders
│       └── TransportConfigLoader.kt
├── src/main/resources/
│   ├── config/                            # Configuration files
│   │   ├── node.yml
│   │   └── transports/
│   │       ├── http.yml
│   │       ├── mqtt.yml
│   │       └── p2p.yml
│   └── logback.xml                        # Logging configuration
└── README.md
```

### Adding New Transport Types

1. Create a new transport binding provider
2. Add configuration support in `TransportConfig.kt`
3. Update the transport loading logic in `SpatialWebNodeApplication.kt`
4. Add sample configuration in `config/transports/`

### Adding New Operation Handlers

1. Implement the `OperationHandler` interface
2. Register the handler in `SpatialWebNodeApplication.registerOperationHandlers()`

## Monitoring

### Health Checks

The application exposes health endpoints:

- HTTP: `GET /health`
- Metrics: `GET /metrics` (when enabled)

### Logging

Logs are written to both console and file (configurable):

- Console: Real-time application logs
- File: `logs/spatialweb-node.log` with rotation

### Metrics

When enabled, the application exposes Prometheus metrics at `/metrics`.

## Troubleshooting

### Common Issues

1. **Port Already in Use**: Check if ports 8080, 4001, or 1883 are already occupied
2. **Configuration Not Found**: Ensure configuration files exist in the expected locations
3. **Transport Binding Failures**: Check transport-specific configuration and dependencies

### Debug Mode

Enable debug logging by setting the log level to DEBUG in the configuration:

```yaml
logging:
  level: "DEBUG"
```

### Getting Help

- Check the logs for detailed error messages
- Verify configuration file syntax
- Ensure all required dependencies are available
- Check network connectivity for external services

## License

This project is licensed under the MIT License - see the LICENSE file for details. 