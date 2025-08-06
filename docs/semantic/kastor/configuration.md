# Kastor Configuration

Comprehensive guide to configuring Kastor for different use cases.

## Configuration File Structure

Kastor uses YAML configuration files with the following structure:

```yaml
# Backend selection
backend: "jena"  # jena, rdf4j, sparql

# Schema and context files
schemaPaths:
  - "schemas/example.shacl.ttl"
contextPaths:
  - "contexts/example.jsonld"

# Code generation
interfacePackages:
  - "com.example.model"

# RDF namespaces
prefixMappings:
  ex: "http://example.org/"
  foaf: "http://xmlns.com/foaf/0.1/"

namespaceMappings:
  "http://example.org/": "com.example.core"

# Type mappings
datatypeMappings:
  "http://www.w3.org/2001/XMLSchema#date": "java.time.LocalDate"

# Backend-specific options
options:
  validation: true
  generateImpl: true

# JSON-LD settings
jsonld:
  generateFromPojo: true
  outputPath: "build/generated/context.jsonld"
```

## Backend-Specific Configuration

### Jena Backend

```yaml
backend: "jena"
options:
  validation: true
  # Jena-specific options will be added as needed
```

### RDF4J Backend

```yaml
backend: "rdf4j"
options:
  repositoryType: "memory"  # memory, native, http, sparql
  validation: true
  
  # For native repositories
  dataDirectory: "/path/to/data"
  
  # For HTTP repositories
  serverUrl: "http://localhost:8080/rdf4j-server"
  repositoryId: "my-repo"
```

### SPARQL Backend

```yaml
backend: "sparql"
options:
  queryEndpoint: "http://localhost:8080/sparql"
  updateEndpoint: "http://localhost:8080/update"
  
  # Authentication (optional)
  authentication:
    type: "basic"  # basic, bearer, oauth
    username: "user"
    password: "pass"
    
  # Connection settings
  timeout: 30000
  maxRetries: 3
```

## Real-World Examples

### DCAT-US Government Data Catalog

```yaml
backend: "rdf4j"
schemaPaths:
  - "src/main/resources/dcat-us/dcat-us-3.0.shacl.ttl"
contextPaths:
  - "src/main/resources/dcat-us/dcat.context.jsonld"

interfacePackages:
  - "gov.us.dcat.core"
  - "gov.us.dcat.terms"

prefixMappings:
  dcat: "http://www.w3.org/ns/dcat#"
  dcterms: "http://purl.org/dc/terms/"
  pod: "https://project-open-data.cio.gov/v1.1/schema#"

namespaceMappings:
  "http://www.w3.org/ns/dcat#": "gov.us.dcat.core"
  "http://purl.org/dc/terms/": "gov.us.dcat.terms"

datatypeMappings:
  "http://www.w3.org/2001/XMLSchema#date": "java.time.LocalDate"
  "http://www.w3.org/2001/XMLSchema#dateTime": "java.time.LocalDateTime"

options:
  repositoryType: "memory"
  validation: true
  generateImpl: true
  extrasProperty: true
```

### Enterprise Knowledge Graph

```yaml
backend: "sparql"
schemaPaths:
  - "schemas/enterprise-ontology.ttl"
  - "schemas/business-rules.shacl.ttl"

interfacePackages:
  - "com.company.knowledge.entities"
  - "com.company.knowledge.relations"

prefixMappings:
  company: "http://company.com/ontology/"
  foaf: "http://xmlns.com/foaf/0.1/"
  org: "http://www.w3.org/ns/org#"

options:
  queryEndpoint: "https://api.company.com/sparql"
  updateEndpoint: "https://api.company.com/update"
  authentication:
    type: "bearer"
    token: "${SPARQL_TOKEN}"
  timeout: 60000
  validation: false  # Server-side validation
```

### Development Environment

```yaml
backend: "jena"
schemaPaths:
  - "src/test/resources/test-schema.ttl"

interfacePackages:
  - "com.example.test.model"

prefixMappings:
  test: "http://test.example.org/"
  foaf: "http://xmlns.com/foaf/0.1/"

options:
  validation: true
  generateImpl: true
  debugMode: true

jsonld:
  generateFromPojo: true
  outputPath: "build/test-generated/context.jsonld"
```

## Configuration Loading

### From File

```kotlin
// Load from specific file
val api = KastorRuntime.load("my-config.yaml")

// Load from default location (kastor.yaml)
val api = KastorRuntime.load()

// Load with fallback to defaults
val config = KastorRuntime.loadConfig("kastor.yaml")
```

### From Configuration Object

```kotlin
val config = KastorConfig(
    backend = "rdf4j",
    interfacePackages = listOf("com.example.model"),
    options = mapOf(
        "repositoryType" to "memory",
        "validation" to true
    )
)
val api = KastorRuntime.load(config)
```

### Environment Variables

Use environment variables in configuration:

```yaml
backend: "sparql"
options:
  queryEndpoint: "${SPARQL_QUERY_URL}"
  updateEndpoint: "${SPARQL_UPDATE_URL}"
  authentication:
    type: "bearer"
    token: "${SPARQL_TOKEN}"
```

## Validation

### Schema Validation

Configure SHACL validation:

```yaml
schemaPaths:
  - "schemas/core.shacl.ttl"
  - "schemas/extensions.shacl.ttl"

options:
  validation: true
  strictMode: true  # Fail on any validation error
```

### Runtime Validation

Enable validation during object creation:

```kotlin
val person = resource.asType<Person>(validate = true)
```

## Advanced Configuration

### Custom Datatype Mappings

Map RDF datatypes to Java/Kotlin types:

```yaml
datatypeMappings:
  "http://www.w3.org/2001/XMLSchema#date": "java.time.LocalDate"
  "http://www.w3.org/2001/XMLSchema#dateTime": "java.time.LocalDateTime"
  "http://www.w3.org/2001/XMLSchema#decimal": "java.math.BigDecimal"
  "http://example.org/customType": "com.example.CustomType"
```

### Namespace Organization

Organize generated code by namespace:

```yaml
namespaceMappings:
  "http://xmlns.com/foaf/0.1/": "com.example.foaf"
  "https://schema.org/": "com.example.schema"
  "http://www.w3.org/ns/org#": "com.example.org"
```

### JSON-LD Context Generation

Generate JSON-LD contexts from your POJOs:

```yaml
jsonld:
  generateFromPojo: true
  outputPath: "build/generated/kastor/context.jsonld"
  mergeWithExisting: true
  baseUri: "http://example.org/"
```

## Best Practices

### Development
- Use Jena backend for development
- Enable validation for debugging
- Use in-memory repositories for tests

### Production
- Use RDF4J for better performance
- Configure appropriate repository type
- Set up proper error handling

### Integration
- Use SPARQL backend for existing systems
- Configure authentication properly
- Set appropriate timeouts

## Troubleshooting

### Common Issues

**Backend Not Found:**
```
No RDF API provider found for backend 'xyz'
Available providers: jena, rdf4j, sparql
```
*Solution: Ensure the backend module is included in dependencies*

**Missing Configuration:**
```
SPARQL backend requires 'queryEndpoint' configuration
```
*Solution: Add required configuration options*

**Validation Errors:**
```
Validation failed: Property foaf:name is required
```
*Solution: Check SHACL constraints and object properties*

## Next Steps

- [Learn about Runtime API](runtime.md)
- [Explore Backend Options](backends.md)
- [See Complete Examples](examples.md)