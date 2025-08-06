package com.geoknoesis.kastor.annotations

/**
 * Documentation and mapping guide for SHACL constraints to Jakarta Bean Validation annotations.
 * 
 * This object provides information about how SHACL constraints map to Jakarta Bean Validation
 * and custom Kastor validation annotations.
 */
object ValidationMapping {
    
    /**
     * Direct mappings from SHACL constraints to Jakarta Bean Validation annotations.
     */
    val directMappings = mapOf(
        "sh:minCount" to "@NotNull or @Size(min = X)",
        "sh:maxCount" to "@Size(max = X)",
        "sh:pattern" to "@Pattern(regexp = \"...\")",
        "sh:minLength" to "@Size(min = X)",
        "sh:maxLength" to "@Size(max = X)",
        "sh:minInclusive" to "@Min(value = X)",
        "sh:maxInclusive" to "@Max(value = X)",
        "sh:minExclusive" to "@DecimalMin(value = \"X\", inclusive = false)",
        "sh:maxExclusive" to "@DecimalMax(value = \"X\", inclusive = false)"
    )
    
    /**
     * Custom Kastor annotations for SHACL constraints that don't map directly to Jakarta Bean Validation.
     */
    val customMappings = mapOf(
        "sh:in" to "@OneOf(values = [...])",
        "sh:lessThan" to "@LessThan(other = \"propertyName\")",
        "sh:greaterThan" to "@GreaterThan(other = \"propertyName\")",
        "sh:nodeKind" to "@NodeKind(kind = Kind.IRI|BlankNode|Literal)",
        "sh:uniqueLang" to "@LangMap(iri = \"...\", defaultLang = \"en\")"
    )
    
    /**
     * Type mappings for SHACL datatypes to Kotlin/Java types with validation.
     */
    val typeMappings = mapOf(
        "xsd:string" to "String with @Size validation",
        "xsd:int" to "Int with @Min/@Max validation",
        "xsd:decimal" to "BigDecimal with @DecimalMin/@DecimalMax validation",
        "xsd:boolean" to "Boolean (no additional validation needed)",
        "xsd:date" to "LocalDate with @Past/@Future validation",
        "xsd:dateTime" to "LocalDateTime with @Past/@Future validation",
        "xsd:anyURI" to "IRI with @NodeKind(Kind.IRI) validation"
    )
}