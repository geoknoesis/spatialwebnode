# RDF Core Module

This module provides a vendor-agnostic RDF API and comprehensive vocabulary definitions using modern enum-based approach.

## 🎯 **Enum-Based Vocabulary System**

All RDF vocabularies are now implemented as Kotlin enums, providing:

- **Type Safety**: Compile-time validation
- **IDE Support**: Full auto-completion and refactoring
- **Performance**: JVM optimizations and memory efficiency
- **Extensibility**: Easy to add new features and metadata

## 📚 **Available Vocabularies**

### **XSD (XML Schema Datatypes)**
```kotlin
// 30 datatypes including:
XSD.string        // http://www.w3.org/2001/XMLSchema#string
XSD.boolean       // http://www.w3.org/2001/XMLSchema#boolean
XSD.integer       // http://www.w3.org/2001/XMLSchema#integer
XSD.date          // http://www.w3.org/2001/XMLSchema#date
XSD.dateTime      // http://www.w3.org/2001/XMLSchema#dateTime
// ... and 25 more
```

### **RDF (Resource Description Framework)**
```kotlin
// 19 core RDF properties and classes:
RDF.type          // http://www.w3.org/1999/02/22-rdf-syntax-ns#type
RDF.langString    // http://www.w3.org/1999/02/22-rdf-syntax-ns#langString
RDF.Property      // http://www.w3.org/1999/02/22-rdf-syntax-ns#Property
// ... and 16 more
```

### **RDFS (RDF Schema)**
```kotlin
// 15 RDFS properties and classes:
RDFS.label        // http://www.w3.org/2000/01/rdf-schema#label
RDFS.comment      // http://www.w3.org/2000/01/rdf-schema#comment
RDFS.Class        // http://www.w3.org/2000/01/rdf-schema#Class
// ... and 12 more
```

### **OWL (Web Ontology Language)**
```kotlin
// 42 OWL properties and classes:
OWL.Class         // http://www.w3.org/2002/07/owl#Class
OWL.ObjectProperty // http://www.w3.org/2002/07/owl#ObjectProperty
OWL.sameAs        // http://www.w3.org/2002/07/owl#sameAs
// ... and 39 more
```

### **SHACL (Shapes Constraint Language)**
```kotlin
// 47 SHACL properties and classes:
SHACL.NodeShape   // http://www.w3.org/ns/shacl#NodeShape
SHACL.PropertyShape // http://www.w3.org/ns/shacl#PropertyShape
SHACL.targetClass // http://www.w3.org/ns/shacl#targetClass
// ... and 44 more
```

### **SKOS (Simple Knowledge Organization System)**
```kotlin
// 33 SKOS properties and classes:
SKOS.Concept      // http://www.w3.org/2004/02/skos/core#Concept
SKOS.prefLabel    // http://www.w3.org/2004/02/skos/core#prefLabel
SKOS.broader      // http://www.w3.org/2004/02/skos/core#broader
// ... and 30 more
```

### **DC (Dublin Core Elements)**
```kotlin
// 15 DC properties:
DC.title          // http://purl.org/dc/elements/1.1/title
DC.creator        // http://purl.org/dc/elements/1.1/creator
DC.description    // http://purl.org/dc/elements/1.1/description
// ... and 12 more
```

### **DCT (Dublin Core Terms)**
```kotlin
// 55 DCT properties:
DCT.title         // http://purl.org/dc/terms/title
DCT.created       // http://purl.org/dc/terms/created
DCT.modified      // http://purl.org/dc/terms/modified
// ... and 52 more
```

## 🚀 **Usage Examples**

### **Basic Vocabulary Usage**
```kotlin
// Type-safe vocabulary access
val xsdType: XSD = XSD.string
val rdfType: RDF = RDF.type
val owlClass: OWL = OWL.Class

// Direct IRI conversion
val stringIRI: IRI = XSD.string.toIRI()
val typeIRI: IRI = RDF.type.toIRI()
```

### **Creating Triples**
```kotlin
val person = IRI("http://example.com/person/1")
val triples = listOf(
    RdfTriple(person, RDF.type.toIRI(), OWL.Class.toIRI()),
    RdfTriple(person, RDFS.label.toIRI(), "John Doe".toStringLiteral()),
    RdfTriple(person, DC.creator.toIRI(), "Jane Doe".toStringLiteral())
)
```

### **Literal Creation with XSD Types**
```kotlin
// Convenience extensions for creating typed literals
val stringLiteral = "Hello".toStringLiteral()
val intLiteral = 42.toIntegerLiteral()
val boolLiteral = true.toBooleanLiteral()
val dateLiteral = "2024-01-01".toDateLiteral()
val dateTimeLiteral = "2024-01-01T12:00:00".toDateTimeLiteral()
```

### **Lookup Functionality**
```kotlin
// Find vocabulary by local name
val foundXSD = XSD.fromLocalName("string")
val foundRDF = RDF.fromLocalName("type")

// Find vocabulary by IRI
val foundByIRI = XSD.fromIRI("http://www.w3.org/2001/XMLSchema#string")
```

### **SPARQL Query Building**
```kotlin
val sparql = """
    PREFIX rdf: <${RDF.NAMESPACE}>
    PREFIX xsd: <${XSD.NAMESPACE}>
    PREFIX dc: <${DC.NAMESPACE}>
    
    SELECT ?s ?p ?o 
    WHERE { 
        ?s rdf:type owl:Class .
        ?s dc:creator ?creator .
        FILTER(datatype(?o) = xsd:string)
    }
"""
```

### **SHACL Validation**
```kotlin
// SHACL shape definition
val personShape = IRI("http://example.com/shapes/PersonShape")
val shapeTriples = listOf(
    RdfTriple(personShape, RDF.type.toIRI(), SHACL.NodeShape.toIRI()),
    RdfTriple(personShape, SHACL.targetClass.toIRI(), personClass),
    RdfTriple(personShape, SHACL.property.toIRI(), namePropertyShape)
)
```

## 🔧 **Features**

### **Type Safety**
- Compile-time validation of vocabulary usage
- No runtime null pointer exceptions
- Guaranteed vocabulary existence

### **IDE Support**
- Full auto-completion for all vocabulary values
- Safe refactoring operations
- Usage tracking and navigation

### **Performance**
- JVM-optimized enum constants
- Memory-efficient single instances
- No reflection overhead

### **Extensibility**
- Easy to add new vocabularies
- Support for additional metadata
- Lookup functionality built-in

### **Namespace Constants**
```kotlin
XSD.NAMESPACE     // http://www.w3.org/2001/XMLSchema#
RDF.NAMESPACE     // http://www.w3.org/1999/02/22-rdf-syntax-ns#
RDFS.NAMESPACE    // http://www.w3.org/2000/01/rdf-schema#
OWL.NAMESPACE     // http://www.w3.org/2002/07/owl#
SHACL.NAMESPACE   // http://www.w3.org/ns/shacl#
SKOS.NAMESPACE    // http://www.w3.org/2004/02/skos/core#
DC.NAMESPACE      // http://purl.org/dc/elements/1.1/
DCT.NAMESPACE     // http://purl.org/dc/terms/
```

## 📦 **Dependencies**

This module has no dependencies on other modules in the project, making it completely self-contained and reusable.

## 🧪 **Testing**

Run the comprehensive test suite:

```bash
./gradlew :rdf-core:test
```

The tests verify:
- All vocabulary values are correctly defined
- Lookup functionality works properly
- Extension functions operate correctly
- Namespace constants are accurate
- Interface implementation is complete

## 🔄 **Migration from Object-Based Approach**

The enum-based approach provides significant improvements over the previous object-based approach:

| Feature | Object Approach | Enum Approach |
|---------|----------------|---------------|
| **Type Safety** | ❌ Runtime risk | ✅ Compile-time safety |
| **IDE Support** | ⚠️ Limited | ✅ Full support |
| **Performance** | ⚠️ Reflection | ✅ Optimized |
| **Memory** | ⚠️ Multiple instances | ✅ Single instances |
| **Lookup** | ❌ Manual search | ✅ Built-in methods |
| **Extensibility** | ⚠️ Hard to extend | ✅ Easy to extend |

## 📄 **Files**

- `Vocabulary.kt` - Core vocabulary interface and all enum definitions
- `VocabularyExample.kt` - Comprehensive usage examples
- `VocabularyTest.kt` - Complete test suite
- `RdfApi.kt` - Vendor-agnostic RDF API interface 