# UDG Model Package

This package contains the core model classes for the Universal Domain Graph (UDG) system.

## Overview

The UDG model package provides the fundamental data structures and types used throughout the UDG system:

- **Entity**: Represents entities in the UDG
- **IRI**: Internationalized Resource Identifiers
- **Property**: Properties that can be associated with entities
- **RdfTerm**: RDF terms (IRI, Literal, Blank Node)
- **HolonicLinkInstance**: Relationships between wholes and parts
- **Hyperspace**: Spatial and temporal context for domains
- **EntityChange**: Change tracking for entities
- **ValidationReport**: Validation results

## Key Classes

### Entity
Represents an entity in the UDG with properties, metadata, and lifecycle information.

### IRI
Internationalized Resource Identifier for identifying types, properties, and resources.

### Property
Defines relationships or attributes that can be associated with entities.

### RdfTerm
Sealed class representing RDF terms (IRI, Literal, Blank Node).

### HolonicLinkInstance
Represents holonic relationships between entities (whole-part relationships).

### Hyperspace
Defines spatial and temporal context for domains with coordinate systems and dimensions.

### EntityChange
Tracks changes to entities for audit trails and history reconstruction.

### ValidationReport
Contains validation results when entities are validated against shape graphs.

## Usage

These model classes are used throughout the UDG system to provide type-safe, consistent representation of domain concepts and relationships. 