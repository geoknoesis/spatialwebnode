# Kastor - RDF to POJO Mapper

Kastor is a comprehensive framework for mapping between RDF data and Plain Old Java Objects (POJOs) using compile-time code generation via KSP (Kotlin Symbol Processing).

## What is Kastor?

Kastor enables seamless bidirectional conversion between RDF triples and Kotlin/Java objects, making it easy to work with semantic data in strongly-typed applications.

```kotlin
// Define your data class
@RdfType("http://xmlns.com/foaf/0.1/Person")
data class Person(
    @RdfProperty("http://xmlns.com/foaf/0.1/name")
    val name: String,
    
    @RdfProperty("http://xmlns.com/foaf/0.1/email")
    val email: String?
)

// Use generated mapper
val person = Person("John Doe", "john@example.com")
val rdfGraph = person.toRdf() // Compile-time generated
```

## Key Features

- **🔧 Compile-time Code Generation**: Zero runtime reflection overhead
- **🎯 Type Safety**: Full Kotlin type safety maintained
- **🔌 Multiple Backends**: Support for Jena, RDF4J, and SPARQL endpoints
- **📋 Configuration-Driven**: YAML-based configuration
- **✅ SHACL Validation**: Built-in validation support
- **🏛️ Government Ready**: DCAT-US compliance out of the box

## Architecture

Kastor is organized into several specialized modules:

- **[Runtime](runtime.md)**: Core runtime functionality and annotations
- **[Processor](processor.md)**: KSP processor for code generation
- **[Backends](backends.md)**: RDF backend implementations
- **[Configuration](configuration.md)**: Configuration system and examples

## Quick Start

1. **Add Dependencies**
```kotlin
dependencies {
    implementation("semantic:kastor:runtime")
    ksp("semantic:kastor:processor")
    
    // Choose a backend
    implementation("semantic:kastor:backend-jena")
}
```

2. **Configure Kastor**
```yaml
# kastor.yaml
backend: "jena"
interfacePackages:
  - "com.example.model"
prefixMappings:
  foaf: "http://xmlns.com/foaf/0.1/"
```

3. **Define Data Classes**
```kotlin
@RdfType("foaf:Person")
data class Person(
    @RdfProperty("foaf:name") val name: String
)
```

4. **Use Generated Code**
```kotlin
val api = KastorRuntime.load()
val person = Person("John Doe")
// Generated code handles RDF conversion
```

## Use Cases

### Government Data Catalogs
Perfect for DCAT-US compliance and government data exchange.

### Knowledge Graphs
Build and query knowledge graphs with type-safe Kotlin objects.

### Semantic APIs
Create RDF-based APIs with automatic serialization.

### Data Integration
Convert between different RDF formats while maintaining type safety.

## Next Steps

- [Get Started with Runtime](runtime.md)
- [Learn about Configuration](configuration.md)
- [Explore Backend Options](backends.md)
- [See Examples](examples.md)