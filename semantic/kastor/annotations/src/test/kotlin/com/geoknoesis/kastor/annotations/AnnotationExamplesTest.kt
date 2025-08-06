package com.geoknoesis.kastor.annotations

import com.geoknoesis.kastor.annotations.constraints.*
import jakarta.validation.constraints.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

/**
 * Examples demonstrating how to use Kastor annotations with Jakarta Bean Validation.
 */
class AnnotationExamplesTest {

    /**
     * Example of a FOAF Person with validation baked into the POJO.
     */
    @RdfClass("http://xmlns.com/foaf/0.1/Person")
    data class Person(
        
        @RdfProperty("http://xmlns.com/foaf/0.1/name")
        @NotBlank(message = "Name is required")
        @Size(min = 1, max = 100, message = "Name must be between 1 and 100 characters")
        val name: String,
        
        @RdfProperty("http://xmlns.com/foaf/0.1/email")
        @Email(message = "Must be a valid email address")
        val email: String?,
        
        @RdfProperty("http://xmlns.com/foaf/0.1/age")
        @Min(value = 0, message = "Age must be non-negative")
        @Max(value = 150, message = "Age must be realistic")
        val age: Int?,
        
        @RdfProperty("http://xmlns.com/foaf/0.1/gender")
        @OneOf(values = ["male", "female", "other"], message = "Gender must be male, female, or other")
        val gender: String?
    )

    /**
     * Example of a DCAT Dataset with government-specific validation.
     */
    @RdfShape("http://www.w3.org/ns/dcat#Dataset")
    data class Dataset(
        
        @RdfId(generate = true, pattern = "https://data.gov/dataset/{id}")
        val id: String,
        
        @RdfProperty("http://purl.org/dc/terms/title")
        @NotBlank(message = "Title is required for DCAT compliance")
        @Size(max = 255, message = "Title must not exceed 255 characters")
        val title: String,
        
        @RdfProperty("http://purl.org/dc/terms/description")
        @NotBlank(message = "Description is required for DCAT compliance")
        @Size(min = 10, max = 5000, message = "Description must be between 10 and 5000 characters")
        val description: String,
        
        @RdfProperty("http://www.w3.org/ns/dcat#keyword")
        @Size(min = 1, message = "At least one keyword is required")
        val keywords: List<@Pattern(regexp = "[a-zA-Z0-9\\s-]+") String>,
        
        @RdfProperty("http://purl.org/dc/terms/issued")
        @NotNull(message = "Issue date is required")
        @PastOrPresent(message = "Issue date cannot be in the future")
        val issued: java.time.LocalDate,
        
        @RdfProperty("http://purl.org/dc/terms/modified")
        @PastOrPresent(message = "Modification date cannot be in the future")
        @GreaterThan(other = "issued", message = "Modification date must be after issue date")
        val modified: java.time.LocalDate?
    )

    /**
     * Example of a multilingual resource using LangMap.
     */
    @RdfClass("http://www.w3.org/2004/02/skos/core#Concept")
    data class Concept(
        
        @RdfProperty("http://www.w3.org/2004/02/skos/core#prefLabel")
        @LangMap(iri = "http://www.w3.org/2004/02/skos/core#prefLabel", defaultLang = "en")
        @NotEmpty(message = "At least one preferred label is required")
        val prefLabel: Map<String, @NotBlank String>,
        
        @RdfProperty("http://www.w3.org/2004/02/skos/core#altLabel")
        @LangMap(iri = "http://www.w3.org/2004/02/skos/core#altLabel", defaultLang = "en")
        val altLabel: Map<String, String> = emptyMap(),
        
        @RdfProperty("http://www.w3.org/2004/02/skos/core#broader")
        @NodeKind(kind = NodeKind.Kind.IRI)
        val broader: List<String> = emptyList()
    )

    /**
     * Example of custom JSON-LD mapping with validation.
     */
    @RdfClass("https://schema.org/Product")
    data class Product(
        
        @RdfProperty("https://schema.org/name")
        @JsonKey("productName")
        @NotBlank(message = "Product name is required")
        val name: String,
        
        @RdfProperty("https://schema.org/price")
        @JsonKey("priceAmount")
        @DecimalMin(value = "0.0", message = "Price must be positive")
        @Digits(integer = 10, fraction = 2, message = "Price must have at most 2 decimal places")
        val price: java.math.BigDecimal,
        
        @RdfProperty("https://schema.org/priceCurrency")
        @JsonKey("currency")
        @Pattern(regexp = "[A-Z]{3}", message = "Currency must be a 3-letter ISO code")
        val currency: String = "USD",
        
        @RdfProperty("https://schema.org/category")
        @OneOf(values = ["electronics", "clothing", "books", "toys"], 
               message = "Category must be one of the predefined values")
        val category: String
    )

    @Test
    fun `annotations should be properly configured`() {
        // Test that annotations are present and have correct values
        val personAnnotation = Person::class.annotations.find { it is RdfClass } as? RdfClass
        assertEquals("http://xmlns.com/foaf/0.1/Person", personAnnotation?.iri)
        
        val datasetAnnotation = Dataset::class.annotations.find { it is RdfShape } as? RdfShape
        assertEquals("http://www.w3.org/ns/dcat#Dataset", datasetAnnotation?.iri)
        
        // Test property annotations
        val nameProperty = Person::class.members.find { it.name == "name" }
        assertNotNull(nameProperty)
        
        // This would be expanded in actual tests to verify all annotation properties
    }

    /**
     * Example demonstrating prefix mappings with FOAF Person.
     */
    @PrefixMapping(prefix = "foaf", namespace = "http://xmlns.com/foaf/0.1/")
    @PrefixMapping(prefix = "dct", namespace = "http://purl.org/dc/terms/")
    @PrefixMapping(prefix = "schema", namespace = "https://schema.org/")
    @RdfClass("foaf:Person")
    data class PersonWithPrefixes(
        @RdfProperty("foaf:name")
        @NotBlank
        val name: String,
        
        @RdfProperty("foaf:email")
        @Email
        val email: String?,
        
        @RdfProperty("dct:created")
        val created: String?,
        
        @RdfProperty("schema:birthDate")
        val birthDate: String?
    )

    /**
     * Example demonstrating prefix mappings with DCAT Dataset using container annotation.
     */
    @PrefixMappings([
        PrefixMapping(prefix = "dcat", namespace = "http://www.w3.org/ns/dcat#"),
        PrefixMapping(prefix = "dct", namespace = "http://purl.org/dc/terms/"),
        PrefixMapping(prefix = "foaf", namespace = "http://xmlns.com/foaf/0.1/")
    ])
    @RdfShape("dcat:Dataset")
    data class DatasetWithPrefixes(
        @RdfId(generate = true, pattern = "https://data.gov/dataset/{id}")
        val id: String,
        
        @RdfProperty("dct:title")
        @NotBlank
        val title: String,
        
        @RdfProperty("dct:description")
        val description: String?,
        
        @RdfProperty("dcat:distribution")
        val distributions: List<String>?
    )

    @Test
    fun testPrefixMappingAnnotations() {
        // Test that prefix mapping annotations are present
        val personAnnotations = PersonWithPrefixes::class.annotations
        val prefixMappings = personAnnotations.filterIsInstance<PrefixMapping>()
        
        assertEquals(3, prefixMappings.size)
        
        val foafMapping = prefixMappings.find { it.prefix == "foaf" }
        assertNotNull(foafMapping)
        assertEquals("http://xmlns.com/foaf/0.1/", foafMapping!!.namespace)
        
        val dctMapping = prefixMappings.find { it.prefix == "dct" }
        assertNotNull(dctMapping)
        assertEquals("http://purl.org/dc/terms/", dctMapping!!.namespace)
        
        val schemaMapping = prefixMappings.find { it.prefix == "schema" }
        assertNotNull(schemaMapping)
        assertEquals("https://schema.org/", schemaMapping!!.namespace)
    }

    @Test
    fun testPrefixMappingsContainerAnnotation() {
        // Test that the container annotation works properly
        val datasetAnnotations = DatasetWithPrefixes::class.annotations
        val prefixMappingsContainer = datasetAnnotations.filterIsInstance<PrefixMappings>()
        
        assertEquals(1, prefixMappingsContainer.size)
        
        val mappings = prefixMappingsContainer.first().value
        assertEquals(3, mappings.size)
        
        val dcatMapping = mappings.find { it.prefix == "dcat" }
        assertNotNull(dcatMapping)
        assertEquals("http://www.w3.org/ns/dcat#", dcatMapping!!.namespace)
    }
    
    private fun assertNotNull(value: Any?) {
        if (value == null) throw AssertionError("Expected non-null value")
    }
}