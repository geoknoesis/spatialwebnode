# Kastor Backends

Kastor supports multiple RDF backends through a plugin architecture using the Service Provider Interface (SPI) pattern.

## Available Backends

### Jena Backend

Apache Jena backend for in-memory and persistent RDF storage.

**Configuration:**
```yaml
backend: "jena"
options:
  validation: true
```

**Features:**
- In-memory RDF storage
- File-based persistence
- Full SPARQL support
- Transaction support

### RDF4J Backend

Eclipse RDF4J backend for repository-based RDF storage.

**Configuration:**
```yaml
backend: "rdf4j"
options:
  repositoryType: "memory"  # memory, native, http
  validation: true
```

**Repository Types:**
- `memory` - In-memory repository
- `native` - Native file-based repository
- `http` - Remote HTTP repository

**Features:**
- Multiple repository implementations
- Excellent SPARQL performance
- Transaction support
- Remote repository support

### SPARQL Backend

HTTP-based SPARQL endpoint communication.

**Configuration:**
```yaml
backend: "sparql"
options:
  queryEndpoint: "http://localhost:8080/sparql"
  updateEndpoint: "http://localhost:8080/update"
  authentication:
    type: "basic"
    username: "user"
    password: "pass"
```

**Features:**
- Remote SPARQL endpoints
- HTTP authentication
- Configurable timeouts
- Query and update operations

## Backend Selection

### Automatic Discovery

Kastor automatically discovers available backends:

```kotlin
val availableBackends = RdfApiLoader.getAvailableProviders()
println("Available backends: ${availableBackends.joinToString(", ")}")
```

### Configuration-Based Loading

Select backend via configuration:

```kotlin
val config = KastorConfig(backend = "rdf4j")
val api = RdfApiLoader.fromConfig(config)
```

### Runtime Introspection

Check backend availability:

```kotlin
val hasJena = RdfApiLoader.isBackendAvailable("jena")
val backendInfo = RdfApiLoader.getProviderInfo()
```

## Configuration Examples

### DCAT-US with RDF4J

Government data catalog configuration:

```yaml
backend: "rdf4j"
schemaPaths:
  - "schemas/dcat-us-3.0.shacl.ttl"
contextPaths:
  - "contexts/dcat.context.jsonld"
interfacePackages:
  - "gov.us.dcat.core"
prefixMappings:
  dcat: "http://www.w3.org/ns/dcat#"
  dcterms: "http://purl.org/dc/terms/"
options:
  repositoryType: "memory"
  validation: true
```

### Production SPARQL Endpoint

Enterprise configuration with authentication:

```yaml
backend: "sparql"
options:
  queryEndpoint: "https://api.company.com/sparql"
  updateEndpoint: "https://api.company.com/update"
  authentication:
    type: "bearer"
    token: "${SPARQL_TOKEN}"
  timeout: 30000
  validation: false  # Server-side validation
```

## Error Handling

### Missing Backend

Clear error messages when backend is not available:

```kotlin
try {
    val api = RdfApiLoader.fromConfig(config)
} catch (e: IllegalStateException) {
    // "No RDF API provider found for backend 'xyz'"
    // "Available providers: jena, rdf4j, sparql"
}
```

### Configuration Errors

Backend-specific validation:

```kotlin
try {
    val api = RdfApiLoader.fromConfig(sparqlConfig)
} catch (e: IllegalArgumentException) {
    // "SPARQL backend requires 'queryEndpoint' configuration"
}
```

## Performance Considerations

### Backend Comparison

| Backend | Use Case | Performance | Features |
|---------|----------|-------------|----------|
| Jena | Development, small datasets | Good | Full-featured |
| RDF4J | Production, large datasets | Excellent | High performance |
| SPARQL | Distributed systems | Network dependent | Remote access |

### Recommendations

- **Development**: Use Jena backend for simplicity
- **Production**: Use RDF4J for performance
- **Integration**: Use SPARQL for existing systems

## Custom Backends

Create custom backends by implementing the SPI:

```kotlin
class CustomRdfApiProvider : RdfApiProvider {
    override fun id(): String = "custom"
    override fun create(config: KastorConfig): RdfApi = CustomRdfApi(config)
}
```

Register via META-INF:
```
META-INF/services/com.geoknoesis.kastor.spi.RdfApiProvider
```

## Next Steps

- [Configure Your Backend](configuration.md)
- [Learn about Code Generation](processor.md)
- [See Complete Examples](examples.md)