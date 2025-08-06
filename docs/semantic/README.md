# Semantic Layer

The Semantic Layer provides RDF processing, vocabulary management, and knowledge graph capabilities for the SpatialWeb Node.

## Overview

The semantic layer consists of several key components:

- **RDF Core**: Vendor-agnostic RDF API and vocabulary definitions
- **Adapters**: Backend implementations (Jena, RDF4J)
- **Kastor**: RDF to POJO mapping framework

## Components

### RDF Core
- Common RDF API interface
- Standard vocabularies (XSD, RDF, RDFS, OWL, SHACL)
- Enum-based vocabulary system

### Adapters
- **Jena Adapter**: Apache Jena backend
- **RDF4J Adapter**: Eclipse RDF4J backend

### Kastor Framework
Advanced RDF mapping framework with compile-time code generation.

[Learn more about Kastor →](kastor/README.md)