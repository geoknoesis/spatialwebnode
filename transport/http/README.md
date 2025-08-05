# HTTP Transport Binding

This module provides HTTP/HTTPS and WebSocket transport binding for the Spatial Web Node HSTP (Hypermedia Spatial Transport Protocol) implementation with **configuration-driven instantiation** support.

## Features

- **Configuration-Driven**: Create multiple instances from YAML configuration
- **HTTP/HTTPS Support**: Standard HTTP requests for point-to-point communication
- **WebSocket Support**: Real-time bidirectional communication for channels and subscriptions
- **Automatic Protocol Selection**: Intelligently chooses between HTTP and WebSocket based on message type
- **HSTP Header Mapping**: Maps HSTP headers to HTTP headers for compatibility
- **SPI Registration**: Automatically discovered by TransportManager
- **Error Handling**: Comprehensive error handling and logging
- **Flexible Configuration**: Support for multiple named instances with different settings

## Configuration-Driven Usage

### YAML Configuration Format

Create a YAML file (e.g., `transports.yml`) with your HTTP transport configurations:

```yaml
httpTransports:
  # Local development transport
  - name: "local"
    baseUrl: "http://localhost:8080"
    timeoutMs: 30000
    enableWebSockets: true
    maxConnections: 10
    retryAttempts: 3
    retryDelayMs: 1000
    enableCompression: true
    enableLogging: true
    customHeaders:
      X-Client-Version: "1.0.0"
      X-Environment: "development"

  # Production API transport
  - name: "production-api"
    baseUrl: "https://api.example.com"
    timeoutMs: 60000
    enableWebSockets: true
    maxConnections: 50
    retryAttempts: 5
    retryDelayMs: 2000
    enableCompression: true
    enableLogging: false
    customHeaders:
      X-API-Key: "${API_KEY}"
      X-Client-Version: "1.0.0"
      X-Environment: "production"

  # Internal service transport
  - name: "internal-service"
    baseUrl: "https://internal.example.com"
    timeoutMs: 45000
    enableWebSockets: false
    maxConnections: 20
    retryAttempts: 3
    retryDelayMs: 1500
    enableCompression: true
    enableLogging: true
    customHeaders:
      X-Internal-Token: "${INTERNAL_TOKEN}"
      X-Service-Name: "spatial-web-node"
```

### Using with TransportManager

```kotlin
import com.geoknoesis.spatialweb.core.transport.TransportManager
import com.geoknoesis.spatialweb.core.hstp.engine.HstpEngine
import java.io.FileInputStream

val hstpEngine = HstpEngine()
val transportManager = TransportManager(hstpEngine)

// Load configurations from YAML file
val configStream = FileInputStream("transports.yml")
val configuredBindings = transportManager.createFromConfiguration(configStream)

// Configure the transport manager with the created bindings
transportManager.configure(configuredBindings)

// Start all configured bindings
transportManager.start()
```

### Direct Provider Usage

```kotlin
import com.geoknoesis.spatialweb.transport.http.HTTPTransportBindingProvider
import java.io.FileInputStream

val provider = HTTPTransportBindingProvider()

// Create instances from YAML configuration
val configStream = FileInputStream("transports.yml")
val instances = provider.createInstances(configStream)

// Access specific instances by name
val localInstance = provider.getInstance("local")
val productionInstance = provider.getInstance("production-api")

// Start instances
instances.forEach { it.start() }

// Clean up when done
provider.shutdown()
```

## Configuration Options

### HTTPTransportConfig Properties

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `name` | String | Required | Unique name for this transport instance |
| `baseUrl` | String | "http://localhost:8080" | Base URL for HTTP requests |
| `timeoutMs` | Long | 30000 | Request timeout in milliseconds |
| `enableWebSockets` | Boolean | true | Whether to enable WebSocket support |
| `maxConnections` | Int | 10 | Maximum number of concurrent connections |
| `retryAttempts` | Int | 3 | Number of retry attempts on failure |
| `retryDelayMs` | Long | 1000 | Delay between retry attempts |
| `enableCompression` | Boolean | true | Whether to enable HTTP compression |
| `enableLogging` | Boolean | true | Whether to enable detailed logging |
| `customHeaders` | Map<String, String> | empty | Custom HTTP headers to include |

## SPI Registration

The HTTP transport binding is registered as a Java Service Provider Interface (SPI) in the file:

```
META-INF/services/com.geoknoesis.spatialweb.core.transport.TransportBindingProvider
```

This allows the `TransportManager` to automatically discover and load the HTTP transport binding provider without explicit configuration.

### Provider Implementation

The `HTTPTransportBindingProvider` class implements the `TransportBindingProvider` interface and:

- **Discovers configurations** from YAML files via `createInstances(InputStream)`
- **Creates multiple instances** based on configuration
- **Manages instance lifecycle** with start/stop/shutdown
- **Provides instance access** by name via `getInstance(String)`
- **Supports protocol discovery** via `getSupportedProtocols()`

## Programmatic Usage

### Factory Methods

```kotlin
import com.geoknoesis.spatialweb.transport.http.HTTPTransportFactory

// Create individual instances
val local = HTTPTransportFactory.createLocal()
val production = HTTPTransportFactory.createProduction("https://api.example.com")
val custom = HTTPTransportFactory.createCustom(
    baseUrl = "https://custom.example.com",
    timeoutMs = 45000,
    enableWebSockets = false
)
```

### Configuration Factory

```kotlin
import com.geoknoesis.spatialweb.transport.http.HTTPTransportConfigFactory

// Create from configuration object
val config = HTTPTransportConfig(
    name = "custom",
    baseUrl = "https://custom.example.com",
    timeoutMs = 45000,
    enableWebSockets = false
)
val binding = HTTPTransportConfigFactory.createFromConfig(config)

// Create from YAML string
val yamlString = """
    httpTransports:
      - name: "yaml-test"
        baseUrl: "https://yaml.example.com"
        timeoutMs: 60000
        enableWebSockets: true
""".trimIndent()

val bindings = HTTPTransportConfigFactory.createFromYamlString(yamlString)
```

## URL Patterns

The transport binding automatically constructs URLs based on message type:

- **Direct Messages**: `{baseUrl}/hstp/direct` (HTTP) or `{baseUrl}/hstp/ws/direct` (WebSocket)
- **Channel Messages**: `{baseUrl}/hstp/channel/{channelId}` (HTTP) or `{baseUrl}/hstp/ws/channel/{channelId}` (WebSocket)
- **General Messages**: `{baseUrl}/hstp` (HTTP) or `{baseUrl}/hstp/ws` (WebSocket)

## Protocol Selection

The transport binding automatically selects the appropriate protocol:

**HTTP is used for:**
- Simple request-response messages
- One-time operations
- When WebSockets are disabled

**WebSocket is used for:**
- Messages expecting responses (`expectResponse = true`)
- Channel-based communication
- Subscribe/publish operations
- Real-time bidirectional communication

## HSTP Header Mapping

HSTP headers are mapped to HTTP headers as follows:

- `X-HSTP-Operation`: The HSTP operation
- `X-HSTP-Source`: Source DID
- `X-HSTP-Destination`: Destination DID (if present)
- `X-HSTP-Channel`: Channel DID (if present)
- `X-HSTP-InReplyTo`: Reply-to message ID (if present)
- `X-HSTP-ExpectResponse`: Whether response is expected
- `X-HSTP-Timestamp`: Message timestamp

## Error Handling

The transport binding includes comprehensive error handling:

- **Connection Errors**: Logged and can be retried
- **WebSocket Failures**: Automatic session cleanup and reconnection
- **Message Handler Errors**: Isolated to prevent affecting other handlers
- **Invalid State**: Throws `IllegalStateException` for operations on stopped transport
- **Configuration Errors**: Graceful fallback to default configuration

## Dependencies

- **Ktor**: HTTP client and WebSocket support
- **Kotlin Coroutines**: Asynchronous programming
- **SnakeYAML**: YAML configuration parsing
- **SLF4J/Logback**: Logging
- **JUnit 5**: Testing

## Testing

Run the tests with:

```bash
./gradlew :transport:http:test
```

The test suite includes:
- Configuration factory tests
- Provider SPI discovery tests
- YAML parsing tests
- Instance management tests
- Error handling tests

## Security Considerations

- Use HTTPS in production environments
- Implement proper authentication and authorization
- Validate all incoming messages
- Consider rate limiting for WebSocket connections
- Use secure WebSocket connections (WSS) in production
- Validate YAML configuration to prevent injection attacks

## Performance Considerations

- WebSocket connections are pooled and reused
- HTTP connections use connection pooling via Ktor
- Large payloads are streamed efficiently
- Timeouts prevent hanging connections
- Coroutine-based for non-blocking I/O
- Multiple instances can be configured for load balancing

## Example Integration

Here's a complete example of how to integrate the HTTP transport binding with a spatial web node:

```kotlin
import com.geoknoesis.spatialweb.core.transport.TransportManager
import com.geoknoesis.spatialweb.core.hstp.engine.HstpEngine
import java.io.FileInputStream

fun main() {
    // Create HSTP engine
    val hstpEngine = HstpEngine()
    
    // Create transport manager
    val transportManager = TransportManager(hstpEngine)
    
    // Load HTTP transport configurations
    val configStream = FileInputStream("config/transports.yml")
    val configuredBindings = transportManager.createFromConfiguration(configStream)
    
    // Configure transport manager
    transportManager.configure(configuredBindings)
    
    // Start all transports
    transportManager.start()
    
    // Your spatial web node is now running with configured HTTP transports
    println("Spatial Web Node started with ${configuredBindings.size} HTTP transport bindings")
}
``` 