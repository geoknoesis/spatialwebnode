package com.geoknoesis.rdf

import java.time.Instant

// -----------------------------
// Core RDF Term Model
// -----------------------------

sealed interface RdfTerm

data class IRI(val value: String) : RdfTerm {
    override fun toString() = "<$value>"
}

data class Literal(
    val lexicalForm: String,
    val datatype: IRI = IRI(XSD.string.iri),
    val lang: String? = null
) : RdfTerm {
    override fun toString(): String =
        if (lang != null) "\"$lexicalForm\"@$lang"
        else "\"$lexicalForm\"^^${datatype.value}"
}

data class RdfTriple(
    val subject: RdfTerm,
    val predicate: IRI,
    val obj: RdfTerm
) : RdfTerm {
    override fun toString(): String = "$subject $predicate $obj ."
}

// Optional: Annotated triple convenience wrapper
data class AnnotatedTriple(
    val base: RdfTriple,
    val annotations: List<RdfTriple>
) {
    fun allTriples(): List<RdfTriple> = listOf(base) + annotations
}

// -----------------------------
// RDF Graph Abstraction
// -----------------------------

data class RDFGraph(
    val triples: List<RdfTriple>,
    val namedGraph: IRI? = null
)

// For validation results
data class ValidationReport(
    val conforms: Boolean,
    val messages: List<String> = emptyList()
)

// For query results
data class RdfResultSet(
    val vars: List<String>,
    val rows: List<Map<String, RdfTerm>>
)

// For diffing graphs
data class TripleDelta(
    val added: RDFGraph,
    val removed: RDFGraph
)

// -----------------------------
// RDF API Interface
// -----------------------------

interface RdfApi {

    // --- Named Graph Management ---
    fun createNamedGraph(graph: IRI): Boolean
    fun deleteNamedGraph(graph: IRI): Boolean
    fun listNamedGraphs(): List<IRI>

    // --- Triple CRUD ---
    fun insertTriple(graph: IRI, triple: RdfTriple): Boolean
    fun insertTriples(graph: IRI, triples: List<RdfTriple>): Boolean
    fun deleteTriple(graph: IRI, triple: RdfTriple): Boolean
    fun getTriples(
        graph: IRI,
        subject: RdfTerm? = null,
        predicate: IRI? = null,
        obj: RdfTerm? = null
    ): List<RdfTriple>

    // --- SPARQL Query & Update ---
    fun query(graph: IRI? = null, sparql: String): RdfResultSet
    fun update(graph: IRI? = null, sparql: String): Boolean

    // --- Transactions ---
    fun beginTransaction()
    fun commitTransaction()
    fun rollbackTransaction()

    // --- Validation ---
    fun validate(graph: IRI, shapesGraph: IRI): ValidationReport

    // --- Import / Export ---
    fun import(graph: IRI, rdfContent: String, format: RDFFormat): Boolean
    fun export(graph: IRI, format: RDFFormat): String

    // --- Diffing (optional) ---
    fun diff(before: RDFGraph, after: RDFGraph): TripleDelta
}

// -----------------------------
// RDF Formats
// -----------------------------

enum class RDFFormat {
    TURTLE,
    TRIG,
    JSONLD,
    NTRIPLES,
    NQUADS
} 