# First Steps with hstpd

This guide will walk you through your first experience with hstpd, from installation to running your first HSTP operations.

## Quick Start

### Step 1: Install hstpd

Follow the [Installation Guide](installation.md) to install hstpd on your system.

### Step 2: Verify Installation

```bash
# Check if hstpd is properly installed
./gradlew :node:run --args="--version"

# Expected output:
# hstpd v1.0.0
# Developed by Geoknoesis LLC (https://www.geoknoesis.com)
# Main Developer: Stephane Fellah (stephanef@geoknoesis.com)
# Built with Kotlin 2.1.20
# JVM 17.0.2
```

### Step 3: Start hstpd

```bash
# Start with default configuration
./gradlew :node:run

# Or with custom configuration
./gradlew :node:run --args="--config config/custom.yml"
```

## Your First hstpd Session

### Understanding the Startup Process

When you start hstpd, you'll see output similar to this:

```
[INFO] Starting hstpd...
[INFO] Loading configuration from config/node.yml
[INFO] Initializing HSTP engine...
[INFO] Registering operation handlers...
[INFO] Starting transport managers...
[INFO] HTTP transport started on port 8080
[INFO] MQTT transport connected to tcp://localhost:1883
[INFO] P2P transport started on port 4001
[INFO] hstpd is running. Press Ctrl+C to stop.
```

### What's Happening

1. **Configuration Loading**: hstpd loads your configuration files
2. **HSTP Engine Initialization**: The core HSTP engine starts up
3. **Operation Registration**: Built-in operations (ping/pong) are registered
4. **Transport Startup**: HTTP, MQTT, and P2P transports are initialized
5. **Ready State**: hstpd is now ready to handle HSTP messages

## Testing Basic Functionality

### Test 1: Ping Operation

The ping operation is the simplest way to test hstpd functionality.

#### Using HTTP Transport

```bash
# Send a ping message via HTTP
curl -X POST http://localhost:8080/hstp \
  -H "Content-Type: application/json" \
  -d '{
    "header": {
      "id": "ping_1",
      "operation": "ping",
      "source": "did:example:alice",
      "destination": "did:example:bob",
      "timestamp": "'$(date -u +%Y-%m-%dT%H:%M:%SZ)'",
      "expectResponse": true
    },
    "payload": "Hello from Alice!"
  }'
```

#### Expected Response

```json
{
  "header": {
    "id": "pong_1",
    "operation": "pong",
    "source": "did:example:bob",
    "destination": "did:example:alice",
    "timestamp": "2024-01-15T10:30:00Z",
    "expectResponse": false
  },
  "payload": "Hello from Alice!"
}
```

### Test 2: MQTT Transport

#### Prerequisites

Install and start an MQTT broker (e.g., Mosquitto):

```bash
# Install Mosquitto (Ubuntu/Debian)
sudo apt install mosquitto mosquitto-clients

# Start Mosquitto
mosquitto -p 1883
```

#### Send MQTT Message

```bash
# Publish a ping message to MQTT topic
mosquitto_pub -h localhost -p 1883 -t "spatialweb/direct/did_example_bob" \
  -m '{
    "header": {
      "id": "ping_2",
      "operation": "ping",
      "source": "did:example:alice",
      "destination": "did:example:bob",
      "timestamp": "'$(date -u +%Y-%m-%dT%H:%M:%SZ)'",
      "expectResponse": true
    },
    "payload": "MQTT ping from Alice!"
  }'
```

### Test 3: P2P Transport

P2P transport allows direct peer-to-peer communication.

```bash
# Connect to P2P network
# (This will be implemented in future versions)
```

## Understanding HSTP Messages

### Message Structure

Every HSTP message has this structure:

```json
{
  "header": {
    "id": "unique_message_id",
    "operation": "operation_name",
    "source": "did:source:identifier",
    "destination": "did:destination:identifier",
    "timestamp": "2024-01-15T10:30:00Z",
    "expectResponse": true
  },
  "payload": "message_content"
}
```

### Message Fields

- **id**: Unique identifier for the message
- **operation**: Type of operation (ping, pong, custom, etc.)
- **source**: DID of the message sender
- **destination**: DID of the message recipient
- **timestamp**: ISO 8601 timestamp
- **expectResponse**: Whether a response is expected
- **payload**: The actual message content

### DIDs (Decentralized Identifiers)

DIDs are used to identify entities in the spatial web:

```
did:example:alice
did:example:bob
did:hstp:node:1
did:geoknoesis:node:production
```

## Working with Different Transports

### HTTP Transport

HTTP transport is ideal for web applications and REST APIs.

#### Configuration

```yaml
# config/transports/http.yml
- name: "http-local"
  type: "http"
  enabled: true
  config:
    baseUrl: "http://localhost:8080"
    port: 8080
    host: "0.0.0.0"
    timeoutMs: 30000
    enableWebSockets: true
    enableCORS: true
```

#### Usage Examples

```bash
# Send message
curl -X POST http://localhost:8080/hstp \
  -H "Content-Type: application/json" \
  -d '{"header": {...}, "payload": "..."}'

# Get node status
curl http://localhost:8080/status

# Get metrics
curl http://localhost:8080/metrics
```

### MQTT Transport

MQTT transport is perfect for IoT devices and real-time messaging.

#### Configuration

```yaml
# config/transports/mqtt.yml
- name: "mqtt-local"
  type: "mqtt"
  enabled: true
  config:
    brokerUrl: "tcp://localhost:1883"
    clientId: "hstpd-node-1"
    topicPrefix: "spatialweb"
    qos: 1
```

#### Usage Examples

```bash
# Subscribe to messages
mosquitto_sub -h localhost -p 1883 -t "spatialweb/direct/#"

# Publish message
mosquitto_pub -h localhost -p 1883 -t "spatialweb/direct/did_example_bob" \
  -m '{"header": {...}, "payload": "..."}'
```

### P2P Transport

P2P transport enables direct peer-to-peer communication without central servers.

#### Configuration

```yaml
# config/transports/p2p.yml
- name: "p2p-local"
  type: "p2p"
  enabled: true
  config:
    port: 4001
    host: "0.0.0.0"
    discoveryEnabled: true
    maxPeers: 50
```

## Creating Your First Custom Operation

### Step 1: Create Operation Handler

```kotlin
// src/main/kotlin/com/example/CustomOperationHandler.kt
package com.example

import com.geoknoesis.spatialweb.core.hstp.operation.OperationHandler
import com.geoknoesis.spatialweb.core.hstp.model.HSTPMessage
import com.geoknoesis.spatialweb.core.hstp.engine.MessageContext

class CustomOperationHandler : OperationHandler {
    override val operationName: String = "custom"
    
    override suspend fun handle(context: MessageContext): HSTPMessage {
        val request = context.message
        val payload = request.payload.first().toString(Charsets.UTF_8)
        
        // Process the custom operation
        val response = "Processed: $payload"
        
        // Create response message
        return HSTPMessage(
            header = request.header.copy(
                id = "response_${request.header.id}",
                operation = "custom_response",
                source = request.header.destination,
                destination = request.header.source,
                expectResponse = false
            ),
            payload = flowOf(response.toByteArray())
        )
    }
}
```

### Step 2: Register the Handler

```yaml
# config/operations.yml
operations:
  - name: "custom"
    handler: "com.example.CustomOperationHandler"
    enabled: true
```

### Step 3: Test the Custom Operation

```bash
# Send custom operation
curl -X POST http://localhost:8080/hstp \
  -H "Content-Type: application/json" \
  -d '{
    "header": {
      "id": "custom_1",
      "operation": "custom",
      "source": "did:example:alice",
      "destination": "did:example:bob",
      "timestamp": "'$(date -u +%Y-%m-%dT%H:%M:%SZ)'",
      "expectResponse": true
    },
    "payload": "Hello Custom Operation!"
  }'
```

## Monitoring and Debugging

### Viewing Logs

```bash
# View real-time logs
tail -f logs/hstpd.log

# View specific log levels
grep "ERROR" logs/hstpd.log
grep "WARN" logs/hstpd.log
```

### Checking Metrics

```bash
# Get metrics (if enabled)
curl http://localhost:9090/metrics

# Expected metrics
# hstpd_messages_total{operation="ping"} 10
# hstpd_messages_total{operation="pong"} 10
# hstpd_transport_connections{transport="http"} 5
```

### Health Check

```bash
# Check node health
curl http://localhost:8080/health

# Expected response
{
  "status": "healthy",
  "timestamp": "2024-01-15T10:30:00Z",
  "version": "1.0.0",
  "uptime": "PT1H30M"
}
```

## Common First Steps Scenarios

### Scenario 1: Web Application Integration

```javascript
// JavaScript client example
async function sendHSTPMessage(operation, payload) {
    const message = {
        header: {
            id: generateId(),
            operation: operation,
            source: "did:example:webapp",
            destination: "did:example:node",
            timestamp: new Date().toISOString(),
            expectResponse: true
        },
        payload: payload
    };
    
    const response = await fetch('http://localhost:8080/hstp', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(message)
    });
    
    return await response.json();
}

// Usage
sendHSTPMessage('ping', 'Hello from web app!')
    .then(response => console.log('Response:', response));
```

### Scenario 2: IoT Device Integration

```python
# Python MQTT client example
import paho.mqtt.client as mqtt
import json
import uuid
from datetime import datetime

def send_hstp_message(operation, payload):
    message = {
        "header": {
            "id": str(uuid.uuid4()),
            "operation": operation,
            "source": "did:example:iot-device",
            "destination": "did:example:node",
            "timestamp": datetime.utcnow().isoformat() + "Z",
            "expectResponse": True
        },
        "payload": payload
    }
    
    client.publish("spatialweb/direct/did_example_node", json.dumps(message))

# Connect to MQTT broker
client = mqtt.Client()
client.connect("localhost", 1883, 60)

# Send message
send_hstp_message("ping", "Hello from IoT device!")
```

### Scenario 3: Multiple Node Communication

```bash
# Start first node
./gradlew :node:run --args="--config config/node1.yml"

# Start second node (in another terminal)
./gradlew :node:run --args="--config config/node2.yml"

# Send message between nodes
curl -X POST http://localhost:8080/hstp \
  -H "Content-Type: application/json" \
  -d '{
    "header": {
      "id": "inter_node_1",
      "operation": "ping",
      "source": "did:example:node1",
      "destination": "did:example:node2",
      "timestamp": "'$(date -u +%Y-%m-%dT%H:%M:%SZ)'",
      "expectResponse": true
    },
    "payload": "Hello from Node 1!"
  }'
```

## Troubleshooting First Steps

### Common Issues

#### Node Won't Start
```bash
# Check Java version
java -version

# Check configuration
./gradlew :node:run --args="--config config/node.yml --validate"

# Check logs
cat logs/hstpd.log
```

#### Transport Connection Issues
```bash
# Check if ports are available
netstat -tulpn | grep :8080
netstat -tulpn | grep :1883
netstat -tulpn | grep :4001

# Check firewall settings
sudo ufw status
```

#### Message Not Received
```bash
# Check if operation handler is registered
grep "Registering operation" logs/hstpd.log

# Check transport status
curl http://localhost:8080/status
```

### Debug Mode

```bash
# Run in debug mode
./gradlew :node:run --args="--config config/node.yml --debug"

# This will show detailed logging information
```

## Next Steps

After completing your first steps:

1. **Explore Advanced Features**: Learn about [custom operations](../api/operations.md)
2. **Set Up Production**: Follow the [deployment guide](../deployment/README.md)
3. **Join the Community**: Participate in [GitHub discussions](https://github.com/geoknoesis/spatialwebnode/discussions)
4. **Contribute**: Check out the [contributing guide](../developer-guide/contributing.md)

## Getting Help

- **Documentation**: [https://geoknoesis.github.io/spatialwebnode/](https://geoknoesis.github.io/spatialwebnode/)
- **Issues**: [GitHub Issues](https://github.com/geoknoesis/spatialwebnode/issues)
- **Email**: stephanef@geoknoesis.com
- **Discussions**: [GitHub Discussions](https://github.com/geoknoesis/spatialwebnode/discussions) 