# Kastor Annotations

Comprehensive annotation system for RDF to POJO mapping with baked-in validation using Jakarta Bean Validation and custom SHACL constraints.

## Overview

The Kastor Annotations module provides a rich set of annotations that combine RDF mapping with validation, enabling you to:

- Map Kotlin classes to RDF classes and SHACL shapes
- Map properties to RDF predicates
- Embed validation constraints directly in your POJOs
- Support multilingual content with language maps
- Customize JSON-LD serialization

## Core Annotations

### RDF Mapping

#### `@RdfClass`
Maps a Kotlin class to an RDF class:
```kotlin
@RdfClass("http://xmlns.com/foaf/0.1/Person")
data class Person(...)
```

#### `@RdfShape`
Maps a class to a SHACL NodeShape with validation:
```kotlin
@RdfShape("http://example.org/shapes/PersonShape")
data class Person(...)
```

#### `@RdfProperty`
Maps a property to an RDF predicate:
```kotlin
@RdfProperty("http://xmlns.com/foaf/0.1/name")
val name: String
```

#### `@RdfId`
Marks the RDF subject IRI:
```kotlin
@RdfId(generate = true, pattern = "http://example.org/person/{id}")
val id: String
```

### Specialized Mapping

#### `@LangMap`
For multilingual properties with SHACL `sh:uniqueLang`:
```kotlin
@LangMap(iri = "http://www.w3.org/2004/02/skos/core#prefLabel", defaultLang = "en")
val labels: Map<String, String>
```

#### `@JsonKey`
Custom JSON-LD property names:
```kotlin
@JsonKey("productName")
@RdfProperty("https://schema.org/name")
val name: String
```

#### `@PrefixMapping`
Define namespace prefix mappings for cleaner RDF and JSON-LD:
```kotlin
@PrefixMapping(prefix = "foaf", namespace = "http://xmlns.com/foaf/0.1/")
@PrefixMapping(prefix = "dct", namespace = "http://purl.org/dc/terms/")
@RdfClass("foaf:Person")
data class Person(
    @RdfProperty("foaf:name") val name: String,
    @RdfProperty("dct:created") val created: String
)
```

Multiple prefix mappings can also be defined using the container annotation:
```kotlin
@PrefixMappings([
    PrefixMapping(prefix = "dcat", namespace = "http://www.w3.org/ns/dcat#"),
    PrefixMapping(prefix = "dct", namespace = "http://purl.org/dc/terms/")
])
@RdfShape("dcat:Dataset")
data class Dataset(...)
```

## Validation Integration

### Jakarta Bean Validation (Direct Mappings)

| SHACL Constraint | Jakarta Bean Validation |
|------------------|-------------------------|
| `sh:minCount` | `@NotNull` or `@Size(min = X)` |
| `sh:maxCount` | `@Size(max = X)` |
| `sh:pattern` | `@Pattern(regexp = "...")` |
| `sh:minLength` | `@Size(min = X)` |
| `sh:maxLength` | `@Size(max = X)` |
| `sh:minInclusive` | `@Min(value = X)` |
| `sh:maxInclusive` | `@Max(value = X)` |

### Custom SHACL Constraints

#### `@OneOf`
For SHACL `sh:in` constraints:
```kotlin
@OneOf(values = ["male", "female", "other"])
val gender: String?
```

#### `@LessThan` / `@GreaterThan`
For SHACL comparative constraints:
```kotlin
@GreaterThan(other = "startDate")
val endDate: LocalDate
```

#### `@NodeKind`
For SHACL `sh:nodeKind` constraints:
```kotlin
@NodeKind(kind = NodeKind.Kind.IRI)
val references: List<String>
```

## Complete Examples

### FOAF Person with Validation

```kotlin
@RdfClass("http://xmlns.com/foaf/0.1/Person")
data class Person(
    @RdfProperty("http://xmlns.com/foaf/0.1/name")
    @NotBlank(message = "Name is required")
    @Size(min = 1, max = 100)
    val name: String,
    
    @RdfProperty("http://xmlns.com/foaf/0.1/email")
    @Email(message = "Must be a valid email")
    val email: String?,
    
    @RdfProperty("http://xmlns.com/foaf/0.1/age")
    @Min(0) @Max(150)
    val age: Int?,
    
    @RdfProperty("http://xmlns.com/foaf/0.1/gender")
    @OneOf(values = ["male", "female", "other"])
    val gender: String?
)
```

### DCAT-US Government Dataset

```kotlin
@RdfShape("http://www.w3.org/ns/dcat#Dataset")
data class Dataset(
    @RdfId(generate = true, pattern = "https://data.gov/dataset/{id}")
    val id: String,
    
    @RdfProperty("http://purl.org/dc/terms/title")
    @NotBlank(message = "Title required for DCAT compliance")
    @Size(max = 255)
    val title: String,
    
    @RdfProperty("http://purl.org/dc/terms/description")
    @Size(min = 10, max = 5000)
    val description: String,
    
    @RdfProperty("http://www.w3.org/ns/dcat#keyword")
    @Size(min = 1, message = "At least one keyword required")
    val keywords: List<String>,
    
    @RdfProperty("http://purl.org/dc/terms/issued")
    @NotNull @PastOrPresent
    val issued: LocalDate,
    
    @RdfProperty("http://purl.org/dc/terms/modified")
    @PastOrPresent
    @GreaterThan(other = "issued")
    val modified: LocalDate?
)
```

### Multilingual SKOS Concept

```kotlin
@RdfClass("http://www.w3.org/2004/02/skos/core#Concept")
data class Concept(
    @LangMap(iri = "http://www.w3.org/2004/02/skos/core#prefLabel", defaultLang = "en")
    @NotEmpty(message = "At least one label required")
    val prefLabel: Map<String, String>,
    
    @LangMap(iri = "http://www.w3.org/2004/02/skos/core#altLabel")
    val altLabel: Map<String, String> = emptyMap(),
    
    @RdfProperty("http://www.w3.org/2004/02/skos/core#broader")
    @NodeKind(kind = NodeKind.Kind.IRI)
    val broader: List<String> = emptyList()
)
```

## Validation Benefits

### Compile-Time Safety
- Type-safe mapping between RDF and Kotlin objects
- Validation constraints enforced during code generation

### Runtime Validation
- Jakarta Bean Validation integration
- SHACL-compliant validation rules
- Detailed error messages

### Government Compliance
- DCAT-US validation out of the box
- Schema.org compatibility
- Enterprise-ready constraint handling

## Dependencies

- **Jakarta Bean Validation API**: Standard validation annotations
- **Kotlin Standard Library**: Core language support
- **Hibernate Validator** (test only): Reference implementation for testing

## Usage with KSP

The annotations are processed at compile time by the Kastor KSP processor:

```kotlin
dependencies {
    implementation("semantic:kastor:annotations")
    implementation("semantic:kastor:runtime")
    ksp("semantic:kastor:processor")
}
```

## Integration with Backends

All Kastor backends (Jena, RDF4J, SPARQL) support validation:

```kotlin
// Enable validation during mapping
val person = resource.asType<Person>(validate = true)

// Validation errors are thrown as ValidationException
try {
    val invalidPerson = resource.asType<Person>(validate = true)
} catch (e: ValidationException) {
    println("Validation failed: ${e.message}")
}
```

## Best Practices

1. **Use Jakarta Bean Validation** for standard constraints (size, pattern, etc.)
2. **Use Custom Annotations** for SHACL-specific constraints (oneOf, lessThan, etc.)
3. **Combine Annotations** for comprehensive validation
4. **Provide Clear Messages** for better user experience
5. **Test Validation Logic** with edge cases

## Next Steps

- [Learn about the KSP Processor](../processor/README.md)
- [Configure Backends](../backends.md)
- [See Complete Examples](../examples.md)