# Kastor Runtime

The Kastor Runtime provides the core functionality for RDF to POJO mapping at runtime.

## Core Components

### Resource Interface

The foundation of all mapped objects:

```kotlin
interface Resource {
    val iri: IRI
    fun extras(): Map<IRI, List<Any>>
}
```

### Type Casting

Safe casting between resource types:

```kotlin
val person: Person = resource.asType<Person>(validate = true)
```

### Configuration System

YAML-based configuration with full backend support:

```kotlin
val config = KastorRuntime.loadConfig("kastor.yaml")
val api = KastorRuntime.load(config)
```

## Runtime API

### KastorRuntime

Main entry point for loading RDF APIs:

```kotlin
// Load from configuration file
val api = KastorRuntime.load("kastor.yaml")

// Load from configuration object
val config = KastorConfig(backend = "jena")
val api = KastorRuntime.load(config)

// Load default backend
val api = KastorRuntime.loadDefault()
```

### Resource Extensions

Utility functions for working with resources:

```kotlin
// Cast to specific type with validation
val person = resource.asType<Person>(validate = true)

// Access unmapped properties
val extras = resource.extras()
```

## Error Handling

### ValidationException

Thrown when SHACL validation fails:

```kotlin
try {
    val person = resource.asType<Person>(validate = true)
} catch (e: ValidationException) {
    println("Validation errors: ${e.message}")
}
```

## Dependencies

The runtime module depends on:

- `semantic:rdf-core` - Core RDF functionality
- Jackson YAML - Configuration loading
- Kotlin reflection - Advanced mapping scenarios

## Next Steps

- [Learn about the Processor](processor.md)
- [Configure Backends](backends.md)
- [See Configuration Examples](configuration.md)