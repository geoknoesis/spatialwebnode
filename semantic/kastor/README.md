# Kastor - RDF to POJO Mapper

Kastor is a comprehensive framework for mapping between RDF data and Plain Old Java Objects (POJOs) using compile-time code generation via KSP (Kotlin Symbol Processing).

## Architecture

The Kastor framework is organized into several submodules:

### Core Modules

- **[runtime](runtime/)** - Core runtime functionality, annotations, and base classes
- **[processor](processor/)** - KSP processor for compile-time code generation

### Backend Implementations

- **[backend-jena](backend-jena/)** - Apache Jena backend implementation
- **[backend-rdf4j](backend-rdf4j/)** - Eclipse RDF4J backend implementation  
- **[backend-sparql](backend-sparql/)** - SPARQL endpoint backend implementation

### Examples and Documentation

- **[examples](examples/)** - Usage examples and sample code

## Quick Start

1. Add the runtime dependency to your project
2. Choose a backend implementation (jena, rdf4j, or sparql)
3. Apply KSP processor for code generation
4. Annotate your data classes with Kastor annotations
5. Use the generated mappers to convert between RDF and POJOs

## Features

- **Compile-time Safety**: All mapping code generated and validated at compile time
- **Zero Runtime Reflection**: Optimal performance with no reflection overhead
- **Multiple Backends**: Support for Jena, RDF4J, and SPARQL endpoints
- **Type Safety**: Full Kotlin type safety maintained throughout
- **Flexible Mapping**: Support for complex object hierarchies and custom mappings

## Dependencies

Each submodule has its own specific dependencies. See individual module READMEs for details.