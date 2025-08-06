# Kastor SPI Implementation Guide

This document describes the Service Provider Interface (SPI) implementation for Kastor backends.

## Overview

Kastor uses Java's ServiceLoader mechanism to dynamically discover and load RDF backend implementations at runtime. This allows for a clean plugin architecture where different backends can be added without modifying core code.

## SPI Architecture

### 1. Core SPI Interface

**Location**: `semantic/kastor/runtime/src/main/kotlin/com/geoknoesis/kastor/spi/RdfApiProvider.kt`

```kotlin
interface RdfApiProvider {
    fun id(): String                              // Backend identifier
    fun create(config: KastorConfig): RdfApi      // Factory method
    fun supports(config: KastorConfig): Boolean   // Configuration validation
    fun description(): String                     // Human-readable description
}
```

### 2. Service Loader Implementation

**Location**: `semantic/kastor/runtime/src/main/kotlin/com/geoknoesis/kastor/runtime/KastorRuntime.kt`

The `RdfApiLoader` object uses `java.util.ServiceLoader` to:
- Discover all available providers on the classpath
- Load providers based on configuration
- Provide error handling and fallback mechanisms

## Backend Implementations

### Jena Backend

**Provider**: `com.geoknoesis.kastor.backend.jena.JenaRdfApiProvider`
- **ID**: `"jena"`
- **Description**: Apache Jena RDF API Provider - In-memory and persistent RDF storage
- **Service File**: `semantic/kastor/backend-jena/src/main/resources/META-INF/services/com.geoknoesis.kastor.spi.RdfApiProvider`

### RDF4J Backend

**Provider**: `com.geoknoesis.kastor.backend.rdf4j.Rdf4jRdfApiProvider`
- **ID**: `"rdf4j"`
- **Description**: Eclipse RDF4J RDF API Provider - Repository-based RDF storage and SPARQL
- **Service File**: `semantic/kastor/backend-rdf4j/src/main/resources/META-INF/services/com.geoknoesis.kastor.spi.RdfApiProvider`

### SPARQL Backend

**Provider**: `com.geoknoesis.kastor.backend.sparql.SparqlRdfApiProvider`
- **ID**: `"sparql"`  
- **Description**: SPARQL Endpoint RDF API Provider - Remote HTTP-based SPARQL endpoints
- **Service File**: `semantic/kastor/backend-sparql/src/main/resources/META-INF/services/com.geoknoesis.kastor.spi.RdfApiProvider`

## Usage Examples

### Basic Usage

```kotlin
// Load with configuration
val config = KastorConfig(backend = "jena")
val api = KastorRuntime.load(config)

// Load with default backend
val defaultApi = KastorRuntime.loadDefault()
```

### Discovery and Introspection

```kotlin
// Check available backends
val backends = RdfApiLoader.getAvailableProviders()
println("Available backends: ${backends.joinToString(", ")}")

// Get provider details
val info = RdfApiLoader.getProviderInfo()
info.forEach { (id, desc) -> println("$id: $desc") }

// Check if specific backend is available
val hasJena = RdfApiLoader.isBackendAvailable("jena")
```

### Configuration-Based Loading

```yaml
# kastor.yaml
backend: "jena"
options:
  validation: true
```

```kotlin
val api = KastorRuntime.load("kastor.yaml")
```

## Error Handling

The SPI system provides comprehensive error handling:

1. **No Providers Found**: Clear error message listing what backends are needed
2. **Invalid Backend**: Lists available backends when requested backend is not found
3. **Configuration Errors**: Provider-specific validation with detailed error messages
4. **Loading Failures**: Wrapped exceptions with context about which provider failed

## Testing

### SPI Tests

**Location**: `semantic/kastor/runtime/src/test/kotlin/com/geoknoesis/kastor/runtime/SpiTest.kt`

Tests cover:
- Provider discovery
- Backend availability checking
- Error handling for invalid configurations
- Graceful handling of missing providers

### Example Applications

**Location**: `semantic/kastor/examples/src/main/kotlin/com/geoknoesis/kastor/examples/SpiProviderExample.kt`

Demonstrates:
- Dynamic provider discovery
- Configuration-based loading
- Error handling scenarios

## Creating New Backend Providers

### Step 1: Implement RdfApiProvider

```kotlin
class MyBackendProvider : RdfApiProvider {
    override fun id(): String = "mybackend"
    override fun description(): String = "My Custom Backend"
    override fun create(config: KastorConfig): RdfApi = MyRdfApi(config)
}
```

### Step 2: Create Service Registration File

Create: `src/main/resources/META-INF/services/com.geoknoesis.kastor.spi.RdfApiProvider`

Content:
```
com.example.MyBackendProvider
```

### Step 3: Add Dependencies

```kotlin
dependencies {
    implementation project(':semantic:kastor:runtime')
    // Add your backend-specific dependencies
}
```

## Current Status

✅ **Implemented**:
- SPI interface and service loader
- Provider discovery and loading
- Error handling and validation
- **Integration with existing adapters**:
  - **Jena Backend**: Uses existing `JenaAdapter` from `jena-adapter` module
  - **RDF4J Backend**: Uses existing `Rdf4jAdapter` with repository creation
  - **SPARQL Backend**: New `SparqlEndpointAdapter` for HTTP communication
- Configuration-based repository creation for RDF4J
- Comprehensive testing and examples
- Complete documentation

🚧 **Next Steps**:
- Complete Jena adapter implementation (currently stub)
- Implement SPARQL JSON result parsing
- Add authentication support for SPARQL endpoints
- Add configuration validation specific to each backend
- Implement connection pooling and optimization features

✅ **No Redundancy**: The SPI system leverages existing adapter implementations rather than duplicating functionality!