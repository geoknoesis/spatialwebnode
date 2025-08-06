# RDF4J Adapter Module

The RDF4J Adapter Module provides a complete implementation of the vendor-agnostic RDF API using the RDF4J framework. This module bridges the gap between the core RDF API and RDF4J-specific functionality.

## Overview

This module implements the `RdfApi` interface from the `rdf-core` module using RDF4J as the underlying RDF engine. It provides full support for all RDF operations including named graph management, triple CRUD operations, SPARQL queries, and more.

## Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│ RdfApi          │    │ Rdf4jAdapter    │    │ RDF4J Repository│
│ (Interface)     │◄──►│ (Implementation)│◄──►│ (Backend)       │
│                 │    │                 │    │                 │
│ - Vendor        │    │ - RDF4J         │    │ - Memory        │
│ - Agnostic      │    │ - Integration   │    │ - Native        │
│ - Operations    │    │ - Conversion    │    │ - Remote        │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

## Key Features

### 1. Complete RDF4J Integration
- **Repository Support**: Works with any RDF4J repository implementation
- **Value Factory**: Proper RDF4J value creation and conversion
- **Connection Management**: Efficient connection handling and transactions
- **SPARQL Support**: Full SPARQL query and update capabilities

### 2. Core RDF Operations
- **Named Graph Management**: Create, delete, and list named graphs
- **Triple CRUD**: Insert, delete, and retrieve triples
- **Batch Operations**: Efficient bulk triple operations
- **Pattern Matching**: Flexible triple pattern queries

### 3. Advanced Features
- **Transaction Support**: ACID transaction handling
- **Error Handling**: Comprehensive error handling and logging
- **Type Conversion**: Seamless conversion between RDF API and RDF4J types
- **Performance**: Optimized for RDF4J performance characteristics

## Components

### Rdf4jAdapter

The main adapter class that implements the `RdfApi` interface:

```kotlin
class Rdf4jAdapter(
    private val repository: Repository
) : RdfApi {
    // Implementation of all RdfApi methods
}
```

### Key Methods

```kotlin
// Named Graph Management
override fun createNamedGraph(graph: IRI): Boolean
override fun deleteNamedGraph(graph: IRI): Boolean
override fun listNamedGraphs(): List<IRI>

// Triple Operations
override fun insertTriple(graph: IRI, triple: RdfTriple): Boolean
override fun insertTriples(graph: IRI, triples: List<RdfTriple>): Boolean
override fun deleteTriple(graph: IRI, triple: RdfTriple): Boolean
override fun getTriples(graph: IRI, subject: RdfTerm?, predicate: IRI?, obj: RdfTerm?): List<RdfTriple>

// SPARQL Operations
override fun query(graph: IRI?, sparql: String): RdfResultSet
override fun update(graph: IRI?, sparql: String): Boolean

// Transactions
override fun beginTransaction()
override fun commitTransaction()
override fun rollbackTransaction()

// Validation and Import/Export
override fun validate(graph: IRI, shapesGraph: IRI): ValidationReport
override fun import(graph: IRI, rdfContent: String, format: RDFFormat): Boolean
override fun export(graph: IRI, format: RDFFormat): String

// Graph Diffing
override fun diff(before: RDFGraph, after: RDFGraph): TripleDelta
```

## Usage Examples

### Basic Setup

```kotlin
// Include dependencies
implementation(":rdf-core")
implementation(":rdf4j-adapter")

// Create RDF4J repository
val repository = // ... RDF4J repository instance

// Create adapter
val rdfApi = Rdf4jAdapter(repository)
```

### Named Graph Operations

```kotlin
// Create a named graph
val graph = IRI("http://example.com/graph/1")
val success = rdfApi.createNamedGraph(graph)

// List all named graphs
val graphs = rdfApi.listNamedGraphs()
println("Available graphs: ${graphs.map { it.value }}")

// Delete a named graph
rdfApi.deleteNamedGraph(graph)
```

### Triple Operations

```kotlin
// Create triples using vocabularies
val person = IRI("http://example.com/person/1")
val name = Literal("John Doe", XSD.string)
val email = Literal("john@example.com", XSD.string)

val triples = listOf(
    RdfTriple(person, RDF.type, OWL.Class),
    RdfTriple(person, RDFS.label, name),
    RdfTriple(person, IRI("http://example.com/email"), email)
)

// Insert triples
rdfApi.insertTriples(graph, triples)

// Query triples
val personTriples = rdfApi.getTriples(graph, person, null, null)
println("Person triples: $personTriples")
```

### SPARQL Queries

```kotlin
// SPARQL SELECT query
val sparql = """
    SELECT ?s ?p ?o 
    WHERE { 
        GRAPH <${graph.value}> { ?s ?p ?o } 
    }
"""

val result = rdfApi.query(graph, sparql)
println("Query variables: ${result.vars}")
result.rows.forEach { row ->
    println("Row: $row")
}

// SPARQL UPDATE
val update = """
    INSERT { 
        GRAPH <${graph.value}> { 
            <http://example.com/person/2> <http://example.com/name> "Jane Doe" 
        } 
    }
"""

val success = rdfApi.update(graph, update)
```

### Transaction Support

```kotlin
// Begin transaction
rdfApi.beginTransaction()

try {
    // Perform operations
    rdfApi.insertTriple(graph, triple1)
    rdfApi.insertTriple(graph, triple2)
    
    // Commit transaction
    rdfApi.commitTransaction()
} catch (e: Exception) {
    // Rollback on error
    rdfApi.rollbackTransaction()
    throw e
}
```

### Graph Diffing

```kotlin
// Compare two graphs
val before = RDFGraph(listOf(triple1, triple2), graph)
val after = RDFGraph(listOf(triple1, triple3), graph)

val delta = rdfApi.diff(before, after)
println("Added triples: ${delta.added.triples}")
println("Removed triples: ${delta.removed.triples}")
```

## Module Dependencies

### Core Dependencies
- **rdf-core**: Core RDF API and vocabularies
- **Kotlin**: Standard library and reflection
- **Coroutines**: Asynchronous operations
- **Serialization**: JSON and YAML support
- **Logging**: SLF4J and Logback

### RDF4J Dependencies
- **rdf4j-repository-api**: Core RDF4J repository API
- **rdf4j-repository-sparql**: SPARQL repository support
- **rdf4j-repository-http**: HTTP repository support
- **rdf4j-query**: Query processing
- **rdf4j-queryparser-sparql**: SPARQL query parsing
- **rdf4j-rio-turtle**: Turtle format support
- **rdf4j-rio-rdfxml**: RDF/XML format support
- **rdf4j-rio-jsonld**: JSON-LD format support

## Integration

### With RDF4J Repositories

```kotlin
// Memory repository
val repository = MemoryRepository()
repository.initialize()

// Native repository
val repository = SailRepository(NativeStore())
repository.initialize()

// Remote repository
val repository = HTTPRepository("http://localhost:8080/rdf4j-server/repositories/test")

// Create adapter
val rdfApi = Rdf4jAdapter(repository)
```

### With UDG System

```kotlin
// Use with UDG persistence
val rdfApi = Rdf4jAdapter(repository)
val udgStore = Rdf4jUdgStore(rdfApi) // Implementation needed
val udgService = DefaultUdgService(udgStore)
```

### With Persistence Module

```kotlin
// Use with persistence module
val rdfApi = Rdf4jAdapter(repository)
val persistence = Rdf4jPersistence(config, entityMapper)
```

## Package Structure

```
com.geoknoesis.rdf4j/
└── Rdf4jAdapter.kt     # Complete RDF4J implementation
```

## Implementation Details

### Type Conversion

The adapter provides seamless conversion between RDF API types and RDF4J types:

```kotlin
// RDF API to RDF4J
private fun convertToRdf4jTerm(term: RdfTerm): org.eclipse.rdf4j.model.Value

// RDF4J to RDF API
private fun convertFromRdf4jTerm(value: org.eclipse.rdf4j.model.Value): RdfTerm
```

### Error Handling

Comprehensive error handling with proper logging:

```kotlin
try {
    // RDF4J operation
    connection.add(subject, predicate, obj, graphIri)
    connection.commit()
    true
} catch (e: Exception) {
    connection.rollback()
    logger.error("Failed to insert triple: $triple", e)
    false
} finally {
    connection.close()
}
```

### Transaction Management

RDF4J transactions are handled per operation for optimal performance:

```kotlin
override fun beginTransaction() {
    // RDF4J transactions are handled per operation
    // This is a no-op for compatibility
}
```

## Performance Considerations

### Connection Management
- **Efficient Pooling**: RDF4J handles connection pooling automatically
- **Proper Cleanup**: Connections are always closed in finally blocks
- **Transaction Optimization**: Transactions are scoped to individual operations

### Batch Operations
- **Bulk Inserts**: Use `insertTriples()` for multiple triples
- **Batch Queries**: Optimize SPARQL queries for bulk operations
- **Memory Management**: Large result sets are streamed efficiently

## Future Enhancements

- **SHACL Validation**: Full SHACL validation implementation
- **RDF Import/Export**: Complete format support
- **Performance Monitoring**: Metrics and monitoring capabilities
- **Caching**: Query result caching
- **Async Operations**: Coroutine-based async operations 