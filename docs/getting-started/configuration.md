# Configuration Guide

This guide explains how to configure hstpd for different environments and use cases.

## Configuration Overview

hstpd uses YAML configuration files to define its behavior. The main configuration file is typically `config/node.yml`, and transport-specific configurations are stored in separate files.

## Main Configuration File

### Basic Configuration Structure

```yaml
# config/node.yml
nodeId: "did:example:node:1"
name: "My hstpd Node"
version: "1.0.0"
description: "A spatial web node for HSTP communication"

# Transport configurations
transports:
  - "config/transports/http.yml"
  - "config/transports/mqtt.yml"
  - "config/transports/p2p.yml"

# Logging configuration
logging:
  level: "INFO"
  file: "logs/hstpd.log"
  console: true
  maxFileSize: "10MB"
  maxHistory: 30

# Metrics and monitoring
metrics:
  enabled: true
  port: 9090
  endpoint: "/metrics"

# Security settings
security:
  tls:
    enabled: false
    certFile: "certs/server.crt"
    keyFile: "certs/server.key"
  authentication:
    enabled: false
    type: "basic"  # basic, oauth2, jwt
```

### Configuration Options

#### Node Identity
```yaml
nodeId: "did:example:node:1"        # Unique node identifier
name: "My hstpd Node"               # Human-readable name
version: "1.0.0"                    # Node version
description: "Node description"     # Optional description
```

#### Transport Configuration
```yaml
transports:
  - "config/transports/http.yml"    # HTTP transport config
  - "config/transports/mqtt.yml"    # MQTT transport config
  - "config/transports/p2p.yml"     # P2P transport config
```

#### Logging Configuration
```yaml
logging:
  level: "INFO"                     # Log level: DEBUG, INFO, WARN, ERROR
  file: "logs/hstpd.log"            # Log file path
  console: true                     # Output to console
  maxFileSize: "10MB"               # Max log file size
  maxHistory: 30                    # Number of backup files
  pattern: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

#### Metrics Configuration
```yaml
metrics:
  enabled: true                     # Enable metrics collection
  port: 9090                        # Metrics server port
  endpoint: "/metrics"              # Metrics endpoint
  jvm: true                         # Include JVM metrics
  system: true                      # Include system metrics
```

#### Security Configuration
```yaml
security:
  tls:
    enabled: false                  # Enable TLS/SSL
    certFile: "certs/server.crt"    # Certificate file
    keyFile: "certs/server.key"     # Private key file
    caFile: "certs/ca.crt"          # CA certificate file
  authentication:
    enabled: false                  # Enable authentication
    type: "basic"                   # Authentication type
    users:                          # User credentials
      - username: "admin"
        password: "password"
        roles: ["admin"]
```

## Transport Configurations

### HTTP Transport Configuration

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
    corsOrigins: ["*"]
    maxConnections: 1000
    compression: true
    ssl:
      enabled: false
      certFile: "certs/http.crt"
      keyFile: "certs/http.key"
```

### MQTT Transport Configuration

```yaml
# config/transports/mqtt.yml
- name: "mqtt-local"
  type: "mqtt"
  enabled: true
  config:
    brokerUrl: "tcp://localhost:1883"
    clientId: "hstpd-node-1"
    username: null
    password: null
    cleanSession: true
    connectionTimeout: 30
    keepAliveInterval: 60
    maxInflight: 1000
    automaticReconnect: true
    maxReconnectDelay: 10000
    topicPrefix: "spatialweb/local"
    qos: 1
    retainMessages: false
    enableTLS: false
    tlsVersion: "TLSv1.2"
    enableLogging: true
    enableMetrics: false
```

### P2P Transport Configuration

```yaml
# config/transports/p2p.yml
- name: "p2p-local"
  type: "p2p"
  enabled: true
  config:
    port: 4001
    host: "0.0.0.0"
    discoveryEnabled: true
    discoveryInterval: 30000
    maxPeers: 50
    connectionTimeout: 30000
    enableRelay: true
    enableNAT: true
    enableMetrics: false
    security:
      enabled: false
      keyFile: "keys/p2p.key"
      certFile: "keys/p2p.crt"
```

## Environment-Specific Configurations

### Development Configuration

```yaml
# config/development.yml
nodeId: "did:example:node:dev"
name: "hstpd Development Node"
version: "1.0.0"

transports:
  - "config/transports/http-dev.yml"
  - "config/transports/mqtt-dev.yml"

logging:
  level: "DEBUG"
  file: "logs/hstpd-dev.log"
  console: true

metrics:
  enabled: true
  port: 9091

security:
  tls:
    enabled: false
  authentication:
    enabled: false
```

### Production Configuration

```yaml
# config/production.yml
nodeId: "did:example:node:prod"
name: "hstpd Production Node"
version: "1.0.0"

transports:
  - "config/transports/http-prod.yml"
  - "config/transports/mqtt-prod.yml"
  - "config/transports/p2p-prod.yml"

logging:
  level: "INFO"
  file: "logs/hstpd-prod.log"
  console: false
  maxFileSize: "100MB"
  maxHistory: 100

metrics:
  enabled: true
  port: 9090

security:
  tls:
    enabled: true
    certFile: "/etc/hstpd/certs/server.crt"
    keyFile: "/etc/hstpd/certs/server.key"
  authentication:
    enabled: true
    type: "jwt"
    secret: "${JWT_SECRET}"
```

### Docker Configuration

```yaml
# config/docker.yml
nodeId: "did:example:node:docker"
name: "hstpd Docker Node"
version: "1.0.0"

transports:
  - "config/transports/http-docker.yml"
  - "config/transports/mqtt-docker.yml"

logging:
  level: "INFO"
  file: "/var/log/hstpd/hstpd.log"
  console: true

metrics:
  enabled: true
  port: 9090

security:
  tls:
    enabled: false
  authentication:
    enabled: false
```

## Configuration Management

### Using Environment Variables

You can use environment variables in your configuration:

```yaml
# config/node.yml
nodeId: "${NODE_ID:-did:example:node:1}"
name: "${NODE_NAME:-hstpd Node}"
version: "${NODE_VERSION:-1.0.0}"

logging:
  level: "${LOG_LEVEL:-INFO}"
  file: "${LOG_FILE:-logs/hstpd.log}"

security:
  authentication:
    secret: "${JWT_SECRET}"
```

### Configuration Validation

hstpd validates configuration files on startup:

```bash
# Validate configuration
./gradlew :node:run --args="--config config/node.yml --validate"

# Expected output for valid config
Configuration is valid
```

### Configuration Reloading

Some configuration changes can be applied without restart:

```yaml
# Enable configuration reloading
config:
  reloadEnabled: true
  reloadInterval: 30000  # 30 seconds
  watchFiles: true
```

## Advanced Configuration

### Custom Operation Handlers

```yaml
# config/operations.yml
operations:
  - name: "ping"
    handler: "com.geoknoesis.spatialweb.core.hstp.operation.PingOperationHandler"
    enabled: true
  - name: "pong"
    handler: "com.geoknoesis.spatialweb.core.hstp.operation.PongOperationHandler"
    enabled: true
  - name: "custom"
    handler: "com.example.CustomOperationHandler"
    enabled: true
    config:
      customParam: "value"
```

### Interceptor Configuration

```yaml
# config/interceptors.yml
interceptors:
  - name: "logging"
    handler: "com.geoknoesis.spatialweb.core.hstp.interceptor.LoggingInterceptor"
    enabled: true
    order: 1
  - name: "did-resolution"
    handler: "com.geoknoesis.spatialweb.core.hstp.interceptor.DidResolutionInterceptor"
    enabled: true
    order: 2
    config:
      cacheEnabled: true
      cacheSize: 1000
```

### Performance Tuning

```yaml
# config/performance.yml
performance:
  threads:
    corePoolSize: 4
    maxPoolSize: 16
    queueCapacity: 1000
  memory:
    maxHeapSize: "2g"
    initialHeapSize: "512m"
  network:
    bufferSize: 8192
    backlog: 1000
    keepAlive: true
```

## Configuration Examples

### Minimal Configuration

```yaml
# config/minimal.yml
nodeId: "did:example:node:minimal"
name: "Minimal Node"
transports:
  - "config/transports/http-minimal.yml"
logging:
  level: "INFO"
  console: true
```

### High-Performance Configuration

```yaml
# config/high-performance.yml
nodeId: "did:example:node:perf"
name: "High-Performance Node"
transports:
  - "config/transports/http-perf.yml"
  - "config/transports/mqtt-perf.yml"

logging:
  level: "WARN"
  file: "logs/hstpd-perf.log"
  console: false

performance:
  threads:
    corePoolSize: 8
    maxPoolSize: 32
  memory:
    maxHeapSize: "4g"
  network:
    bufferSize: 16384
```

### Secure Configuration

```yaml
# config/secure.yml
nodeId: "did:example:node:secure"
name: "Secure Node"
transports:
  - "config/transports/http-secure.yml"

security:
  tls:
    enabled: true
    certFile: "/etc/ssl/certs/hstpd.crt"
    keyFile: "/etc/ssl/private/hstpd.key"
  authentication:
    enabled: true
    type: "jwt"
    secret: "${JWT_SECRET}"
    tokenExpiration: 3600

logging:
  level: "INFO"
  file: "/var/log/hstpd/secure.log"
  console: false
```

## Troubleshooting Configuration

### Common Configuration Issues

#### Invalid YAML Syntax
```bash
# Error: Invalid YAML syntax
# Solution: Validate YAML syntax
yamllint config/node.yml
```

#### Missing Configuration Files
```bash
# Error: Transport config file not found
# Solution: Check file paths and permissions
ls -la config/transports/
```

#### Invalid Configuration Values
```bash
# Error: Invalid port number
# Solution: Use valid port numbers (1-65535)
port: 8080  # Valid
port: 99999 # Invalid
```

### Configuration Validation

```bash
# Validate configuration before starting
./gradlew :node:run --args="--config config/node.yml --validate"

# Check configuration syntax
./gradlew :node:run --args="--config config/node.yml --check-syntax"
```

### Configuration Debugging

```bash
# Run with debug logging
./gradlew :node:run --args="--config config/node.yml --debug"

# Show effective configuration
./gradlew :node:run --args="--config config/node.yml --show-config"
```

## Best Practices

1. **Use Environment Variables**: For sensitive data and environment-specific values
2. **Validate Configurations**: Always validate before deployment
3. **Use Separate Configs**: Different configs for different environments
4. **Document Changes**: Keep track of configuration changes
5. **Backup Configurations**: Regularly backup your configuration files
6. **Test Configurations**: Test configurations in development before production
7. **Use Version Control**: Keep configurations in version control
8. **Monitor Configuration**: Monitor configuration changes and their effects

## Next Steps

After configuring hstpd:

1. **Test Configuration**: Validate and test your configuration
2. **Start hstpd**: Run with your configuration
3. **Monitor Performance**: Use metrics to monitor performance
4. **Scale Up**: Add more nodes and configure clustering
5. **Secure Deployment**: Implement security best practices 