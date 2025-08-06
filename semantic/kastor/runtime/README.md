# Kastor

> **Casting RDF Shapes into Kotlin POJOs**

Kastor is a Kotlin Symbol Processing (KSP) framework that maps RDF data into idiomatic Kotlin POJOs using **SHACL shapes**, **OWL ontologies**, and **JSON-LD contexts**.  
It provides a **type-safe, backend-agnostic API** for working with RDF while hiding the complexity of libraries like Jena and RDF4J.

---

## ✨ Features

- ⚡ **Schema-first**: Generate Kotlin interfaces from SHACL or OWL schemas.
- 🛠 **Code-first**: Use annotated Kotlin interfaces (`@RdfShape`, `@RdfProperty`, `@LangMap`).
- 🌐 **JSON-LD context integration**:
  - Generate contexts from POJOs.
  - Consume contexts to produce developer-friendly property names.
- 📜 **SHACL validation**: Runtime constraint checking with integrated reports.
- 🗂 **Language maps**: First-class support for multilingual properties (`Map<String, String>`).
- 🔌 **Backend-agnostic**: Works with Jena, RDF4J, or SPARQL endpoints via plugin adapters.
- 📦 **Pluggable architecture**: Easily extend to new RDF engines.
- 🧩 **Extras**: Access unmapped triples via `extras()`.

---

## 🚀 Quick Start

### 1. Add Dependencies

```kotlin
plugins {
    kotlin("jvm") version "1.9.22"
    id("com.google.devtools.ksp") version "1.9.22-1.0.17"
}

dependencies {
    implementation("com.kastor:kastor-runtime:1.0.0")
    ksp("com.kastor:kastor-processor:1.0.0")

    // choose backend plugin
    implementation("com.kastor:kastor-backend-jena:1.0.0")
}
````

---

### 2. Configure Kastor

`kastor.yaml`

```yaml
schemaPaths:
  - src/main/resources/dcat-us/dcat-us-3.0.shacl.ttl

contextPaths:
  - src/main/resources/dcat-us/dcat.context.jsonld

prefixMappings:
  dcat: "http://www.w3.org/ns/dcat#"
  dcterms: "http://purl.org/dc/terms/"

namespaceMappings:
  dcat: "gov.us.dcat.core"
  dcterms: "gov.us.dcat.terms"

datatypeMappings:
  "http://www.w3.org/2001/XMLSchema#date": "java.time.LocalDate"

options:
  validation: true
  generateImpl: true

backend: "jena"
```

---

### 3. Generate & Use POJOs

```kotlin
@RdfShape("http://www.w3.org/ns/dcat#Dataset")
interface Dataset {
    @LangMap("http://purl.org/dc/terms/title")
    val titles: Map<String, String>

    @RdfProperty("http://purl.org/dc/terms/description")
    val description: String?
}
```

Access RDF data:

```kotlin
fun main() {
    val config = KastorConfig.load("kastor.yaml")
    val api = RdfApiLoader.fromConfig(config)

    val dataset: Dataset = Kastor.fromIri(
        IRI("http://example.org/ds1"),
        api,
        Dataset::class.java,
        validate = true
    )

    println(dataset.titles["en"])
}
```

---

## 📜 Annotations

* `@RdfShape(iri)` — Declares an interface as a mapped RDF shape.
* `@RdfProperty(iri)` — Maps a property to an RDF predicate.
* `@LangMap(iri, defaultLang)` — Maps a property to a language-tagged literal map.

---

## 🧩 Backends

Kastor uses a **plugin system** so backends can be swapped without changing code.

Available backends:

* `kastor-backend-jena`
* `kastor-backend-rdf4j`
* `kastor-backend-sparql`

Select in `kastor.yaml`:

```yaml
backend: "rdf4j"
```

---

## 📂 Project Structure

```
kastor/
 ├── runtime/        # Core runtime (Resource, RdfApi abstractions)
 ├── processor/      # KSP processor for code generation
 ├── backend-jena/   # Jena implementation of RdfApi
 ├── backend-rdf4j/  # RDF4J implementation of RdfApi
 ├── backend-sparql/ # SPARQL-only implementation
 └── examples/       # Sample projects (DCAT-US, FOAF)
```

---

## 🧪 Validation Reports

```kotlin
val report = api.validate(
    graph = IRI("http://example.org/graph"),
    shapesGraph = IRI("http://example.org/shapes")
)

if (!report.conforms) {
    println("Validation errors: ${report.messages}")
}
```

---

## 📜 License

Kastor is open source under the **Apache 2.0 License**.
Commercial support, enterprise connectors, and hosted KastorHub are available — see [CONTRIBUTING.md](CONTRIBUTING.md).

---

## 🤝 Contributing

We welcome contributions!

* Fork the repo
* Create a feature branch
* Submit a PR

Check out [CONTRIBUTING.md](CONTRIBUTING.md) for coding guidelines.

---

## 💡 Roadmap

* [ ] JSON-LD context round-trip (POJO ⇄ context)
* [ ] SHACL-Rules support
* [ ] Visual schema explorer (KastorHub)
* [ ] Kotlin Multiplatform (JVM, JS, Native)
* [ ] Enterprise connectors (Stardog, AllegroGraph)

---

> **Kastor** — *Casting RDF Shapes into Kotlin POJOs*



