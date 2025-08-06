# Kastor Examples

Complete examples showing how to use Kastor for different scenarios.

## Basic Example

### 1. Configuration

```yaml
# kastor.yaml
backend: "jena"
interfacePackages:
  - "com.example.model"
prefixMappings:
  foaf: "http://xmlns.com/foaf/0.1/"
options:
  validation: true
```

### 2. Data Model

```kotlin
package com.example.model

import com.geoknoesis.kastor.runtime.*

@RdfType("foaf:Person")
data class Person(
    @RdfProperty("foaf:name")
    val name: String,
    
    @RdfProperty("foaf:email")
    val email: String?,
    
    @RdfProperty("foaf:knows")
    val friends: List<Person> = emptyList()
) : Resource {
    override val iri: IRI get() = IRI("http://example.org/person/${name.hashCode()}")
    override fun extras(): Map<IRI, List<Any>> = emptyMap()
}
```

### 3. Usage

```kotlin
fun main() {
    // Load Kastor runtime
    val api = KastorRuntime.load("kastor.yaml")
    
    // Create objects
    val alice = Person("Alice Smith", "alice@example.com")
    val bob = Person("Bob Jones", "bob@example.com", listOf(alice))
    
    // Store in RDF
    val graph = IRI("http://example.org/people")
    api.insertTriple(graph, alice.toRdfTriple()) // Generated method
    api.insertTriple(graph, bob.toRdfTriple())
    
    // Query RDF
    val sparql = """
        SELECT ?name ?email WHERE {
            ?person foaf:name ?name .
            ?person foaf:email ?email .
        }
    """
    val results = api.query(graph, sparql)
    
    // Convert back to objects
    results.rows.forEach { row ->
        val personIri = row["person"] as IRI
        val person = api.loadResource<Person>(personIri) // Generated method
        println("Found: ${person.name}")
    }
}
```

## DCAT-US Government Example

### 1. Configuration

```yaml
# dcat-us.yaml
backend: "rdf4j"
schemaPaths:
  - "schemas/dcat-us-3.0.shacl.ttl"
contextPaths:
  - "contexts/dcat.context.jsonld"

interfacePackages:
  - "gov.us.dcat.core"

prefixMappings:
  dcat: "http://www.w3.org/ns/dcat#"
  dcterms: "http://purl.org/dc/terms/"
  pod: "https://project-open-data.cio.gov/v1.1/schema#"

namespaceMappings:
  "http://www.w3.org/ns/dcat#": "gov.us.dcat.core"

options:
  repositoryType: "memory"
  validation: true
  generateImpl: true
```

### 2. Data Catalog Model

```kotlin
package gov.us.dcat.core

@RdfType("dcat:Catalog")
data class DataCatalog(
    @RdfProperty("dcterms:title")
    val title: String,
    
    @RdfProperty("dcterms:description")
    val description: String,
    
    @RdfProperty("dcat:dataset")
    val datasets: List<Dataset>,
    
    @RdfProperty("dcterms:publisher")
    val publisher: Organization,
    
    @RdfProperty("dcterms:issued")
    val issued: LocalDate,
    
    @RdfProperty("pod:conformsTo")
    val conformsTo: String = "https://project-open-data.cio.gov/v1.1/schema"
) : Resource

@RdfType("dcat:Dataset")
data class Dataset(
    @RdfProperty("dcterms:title")
    val title: String,
    
    @RdfProperty("dcterms:description")
    val description: String,
    
    @RdfProperty("dcat:keyword")
    val keywords: List<String>,
    
    @RdfProperty("dcterms:publisher")
    val publisher: Organization,
    
    @RdfProperty("dcat:distribution")
    val distributions: List<Distribution>
) : Resource

@RdfType("dcat:Distribution")
data class Distribution(
    @RdfProperty("dcterms:title")
    val title: String,
    
    @RdfProperty("dcat:downloadURL")
    val downloadUrl: String,
    
    @RdfProperty("dcat:mediaType")
    val mediaType: String,
    
    @RdfProperty("dcterms:format")
    val format: String
) : Resource
```

### 3. Government Data Processing

```kotlin
fun processGovernmentData() {
    val api = KastorRuntime.load("dcat-us.yaml")
    
    // Create government data catalog
    val catalog = DataCatalog(
        title = "City of Example Open Data",
        description = "Open data from the City of Example",
        datasets = listOf(
            Dataset(
                title = "Budget Data 2024",
                description = "Annual budget allocation data",
                keywords = listOf("budget", "finance", "government"),
                publisher = Organization("City of Example"),
                distributions = listOf(
                    Distribution(
                        title = "Budget CSV",
                        downloadUrl = "https://data.example.gov/budget.csv",
                        mediaType = "text/csv",
                        format = "CSV"
                    )
                )
            )
        ),
        publisher = Organization("City of Example"),
        issued = LocalDate.now()
    )
    
    // Validate against DCAT-US schema
    val report = api.validate(catalog.iri, IRI("http://example.gov/shapes"))
    if (!report.conforms) {
        println("Validation errors: ${report.messages}")
        return
    }
    
    // Export as JSON-LD
    val jsonld = catalog.toJsonLd() // Generated method
    println("DCAT-US JSON-LD: $jsonld")
    
    // Store in repository
    val graph = IRI("http://data.example.gov/catalog")
    api.insertResource(graph, catalog)
}
```

## Enterprise Knowledge Graph

### 1. Configuration

```yaml
# enterprise.yaml
backend: "sparql"
schemaPaths:
  - "schemas/organization.owl"
  - "schemas/business-rules.shacl.ttl"

interfacePackages:
  - "com.company.knowledge"

prefixMappings:
  company: "http://company.com/ontology/"
  org: "http://www.w3.org/ns/org#"
  foaf: "http://xmlns.com/foaf/0.1/"

options:
  queryEndpoint: "https://knowledge.company.com/sparql"
  updateEndpoint: "https://knowledge.company.com/update"
  authentication:
    type: "bearer"
    token: "${KNOWLEDGE_GRAPH_TOKEN}"
```

### 2. Organization Model

```kotlin
package com.company.knowledge

@RdfType("org:Organization")
data class Organization(
    @RdfProperty("org:identifier")
    val id: String,
    
    @RdfProperty("foaf:name")
    val name: String,
    
    @RdfProperty("org:hasSubOrganization")
    val subOrganizations: List<Organization> = emptyList(),
    
    @RdfProperty("org:hasMember")
    val members: List<Person> = emptyList()
) : Resource

@RdfType("foaf:Person")
data class Employee(
    @RdfProperty("foaf:name")
    val name: String,
    
    @RdfProperty("foaf:email")
    val email: String,
    
    @RdfProperty("org:holds")
    val position: Position,
    
    @RdfProperty("company:reportsto")
    val manager: Employee?
) : Resource

@RdfType("org:Role")
data class Position(
    @RdfProperty("org:roleProperty")
    val title: String,
    
    @RdfProperty("company:department")
    val department: String,
    
    @RdfProperty("company:level")
    val level: Int
) : Resource
```

### 3. Knowledge Graph Operations

```kotlin
fun manageOrganizationalKnowledge() {
    val api = KastorRuntime.load("enterprise.yaml")
    
    // Query organizational structure
    val sparql = """
        PREFIX org: <http://www.w3.org/ns/org#>
        PREFIX foaf: <http://xmlns.com/foaf/0.1/>
        
        SELECT ?person ?name ?title WHERE {
            ?person a foaf:Person ;
                    foaf:name ?name ;
                    org:holds ?position .
            ?position org:roleProperty ?title .
        }
    """
    
    val results = api.query(null, sparql)
    
    // Convert SPARQL results to typed objects
    val employees = results.rows.map { row ->
        val personIri = row["person"] as IRI
        api.loadResource<Employee>(personIri)
    }
    
    // Find reporting relationships
    employees.forEach { employee ->
        employee.manager?.let { manager ->
            println("${employee.name} reports to ${manager.name}")
        }
    }
    
    // Create new organizational structure
    val engineering = Organization(
        id = "ENG",
        name = "Engineering Department",
        members = employees.filter { it.position.department == "Engineering" }
    )
    
    // Update knowledge graph
    api.insertResource(IRI("http://company.com/org"), engineering)
}
```

## Performance Testing

### 1. Bulk Data Operations

```kotlin
fun performanceBenchmark() {
    val api = KastorRuntime.load(KastorConfig(backend = "rdf4j"))
    
    // Generate test data
    val testData = (1..10000).map { i ->
        Person(
            name = "Person $i",
            email = "person$i@example.com"
        )
    }
    
    // Measure insertion performance
    val startTime = System.currentTimeMillis()
    
    val graph = IRI("http://example.org/test")
    api.beginTransaction()
    try {
        testData.forEach { person ->
            api.insertResource(graph, person)
        }
        api.commitTransaction()
    } catch (e: Exception) {
        api.rollbackTransaction()
        throw e
    }
    
    val endTime = System.currentTimeMillis()
    println("Inserted ${testData.size} records in ${endTime - startTime}ms")
    
    // Measure query performance
    val queryStart = System.currentTimeMillis()
    val results = api.query(graph, "SELECT ?s ?p ?o WHERE { ?s ?p ?o }")
    val queryEnd = System.currentTimeMillis()
    
    println("Queried ${results.rows.size} triples in ${queryEnd - queryStart}ms")
}
```

## Error Handling

### 1. Validation Errors

```kotlin
fun handleValidationErrors() {
    val api = KastorRuntime.load("kastor.yaml")
    
    try {
        // This person is missing required properties
        val invalidPerson = Person("", null) // Empty name
        
        // Validation will fail
        val validatedPerson = invalidPerson.asType<Person>(validate = true)
        
    } catch (e: ValidationException) {
        println("Validation failed:")
        e.message?.let { println(it) }
        
        // Handle validation errors gracefully
        // Maybe show user-friendly error messages
        // Or attempt to fix the data
    }
}
```

### 2. Backend Errors

```kotlin
fun handleBackendErrors() {
    try {
        val api = KastorRuntime.load("invalid-config.yaml")
    } catch (e: IllegalStateException) {
        when {
            e.message?.contains("No RDF API provider found") == true -> {
                println("Backend not available. Please check dependencies.")
                println("Available backends: ${RdfApiLoader.getAvailableProviders()}")
            }
            e.message?.contains("Failed to initialize") == true -> {
                println("Backend initialization failed. Check configuration.")
            }
        }
    }
}
```

## Integration Examples

### 1. Spring Boot Integration

```kotlin
@Configuration
class KastorConfiguration {
    
    @Bean
    fun kastorApi(): RdfApi {
        return KastorRuntime.load("kastor.yaml")
    }
}

@Service
class PersonService(private val kastorApi: RdfApi) {
    
    fun savePerson(person: Person) {
        val graph = IRI("http://example.org/people")
        kastorApi.insertResource(graph, person)
    }
    
    fun findPersonByName(name: String): Person? {
        val sparql = """
            SELECT ?person WHERE {
                ?person foaf:name "$name" .
            }
        """
        val results = kastorApi.query(null, sparql)
        return results.rows.firstOrNull()?.let { row ->
            val personIri = row["person"] as IRI
            kastorApi.loadResource<Person>(personIri)
        }
    }
}
```

### 2. Testing

```kotlin
class KastorTest {
    
    @Test
    fun testPersonMapping() {
        val api = KastorRuntime.load(KastorConfig(backend = "jena"))
        
        val person = Person("John Doe", "john@example.com")
        val graph = IRI("http://test.example.org/")
        
        // Insert
        api.insertResource(graph, person)
        
        // Query back
        val found = api.loadResource<Person>(person.iri)
        
        assertEquals(person.name, found.name)
        assertEquals(person.email, found.email)
    }
}
```

## Next Steps

- [Learn about Runtime Internals](runtime.md)
- [Configure Different Backends](backends.md)
- [Explore Advanced Configuration](configuration.md)