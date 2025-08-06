package com.geoknoesis.rdf4j

import com.geoknoesis.rdf.*
import org.eclipse.rdf4j.model.ValueFactory
import org.eclipse.rdf4j.model.impl.SimpleValueFactory
import org.eclipse.rdf4j.query.QueryLanguage
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.RepositoryConnection
import org.slf4j.LoggerFactory

/**
 * RDF4J adapter for the RDF API
 */
class Rdf4jAdapter(
    private val repository: Repository
) : RdfApi {
    
    private val logger = LoggerFactory.getLogger(Rdf4jAdapter::class.java)
    private val valueFactory: ValueFactory = SimpleValueFactory.getInstance()
    
    override fun createNamedGraph(graph: IRI): Boolean {
        return try {
            val connection = repository.connection
            connection.begin()
            try {
                // RDF4J doesn't have explicit named graph creation
                // We just ensure the graph exists by adding a dummy triple
                val graphIri = valueFactory.createIRI(graph.value)
                val dummySubject = valueFactory.createIRI("http://example.com/dummy")
                val dummyPredicate = valueFactory.createIRI("http://example.com/dummy")
                val dummyObject = valueFactory.createLiteral("dummy")
                
                connection.add(dummySubject, dummyPredicate, dummyObject, graphIri)
                connection.remove(dummySubject, dummyPredicate, dummyObject, graphIri)
                
                connection.commit()
                true
            } catch (e: Exception) {
                connection.rollback()
                logger.error("Failed to create named graph: ${graph.value}", e)
                false
            } finally {
                connection.close()
            }
        } catch (e: Exception) {
            logger.error("Error creating named graph: ${graph.value}", e)
            false
        }
    }
    
    override fun deleteNamedGraph(graph: IRI): Boolean {
        return try {
            val connection = repository.connection
            val graphIri = valueFactory.createIRI(graph.value)
            
            connection.begin()
            try {
                connection.clear(graphIri)
                connection.commit()
                true
            } catch (e: Exception) {
                connection.rollback()
                logger.error("Failed to delete named graph: ${graph.value}", e)
                false
            } finally {
                connection.close()
            }
        } catch (e: Exception) {
            logger.error("Error deleting named graph: ${graph.value}", e)
            false
        }
    }
    
    override fun listNamedGraphs(): List<IRI> {
        return try {
            val connection = repository.connection
            val contexts = connection.contextIDs.toList()
            connection.close()
            
            contexts.map { context ->
                IRI(context.stringValue())
            }
        } catch (e: Exception) {
            logger.error("Error listing named graphs", e)
            emptyList()
        }
    }
    
    override fun insertTriple(graph: IRI, triple: RdfTriple): Boolean {
        return try {
            val connection = repository.connection
            val graphIri = valueFactory.createIRI(graph.value)
            
            val subject = convertToRdf4jResource(triple.subject)
            val predicate = valueFactory.createIRI(triple.predicate.value)
            val obj = convertToRdf4jValue(triple.obj)
            
            connection.begin()
            try {
                val statement = valueFactory.createStatement(subject, predicate, obj, graphIri)
                connection.add(statement)
                connection.commit()
                true
            } catch (e: Exception) {
                connection.rollback()
                logger.error("Failed to insert triple: $triple", e)
                false
            } finally {
                connection.close()
            }
        } catch (e: Exception) {
            logger.error("Error inserting triple: $triple", e)
            false
        }
    }
    
    override fun insertTriples(graph: IRI, triples: List<RdfTriple>): Boolean {
        return try {
            val connection = repository.connection
            val graphIri = valueFactory.createIRI(graph.value)
            
            connection.begin()
            try {
                triples.forEach { triple ->
                    val subject = convertToRdf4jResource(triple.subject)
                    val predicate = valueFactory.createIRI(triple.predicate.value)
                    val obj = convertToRdf4jValue(triple.obj)
                    val statement = valueFactory.createStatement(subject, predicate, obj, graphIri)
                    connection.add(statement)
                }
                connection.commit()
                true
            } catch (e: Exception) {
                connection.rollback()
                logger.error("Failed to insert triples", e)
                false
            } finally {
                connection.close()
            }
        } catch (e: Exception) {
            logger.error("Error inserting triples", e)
            false
        }
    }
    
    override fun deleteTriple(graph: IRI, triple: RdfTriple): Boolean {
        return try {
            val connection = repository.connection
            val graphIri = valueFactory.createIRI(graph.value)
            
            val subject = convertToRdf4jResource(triple.subject)
            val predicate = valueFactory.createIRI(triple.predicate.value)
            val obj = convertToRdf4jValue(triple.obj)
            
            connection.begin()
            try {
                val statement = valueFactory.createStatement(subject, predicate, obj, graphIri)
                connection.remove(statement)
                connection.commit()
                true
            } catch (e: Exception) {
                connection.rollback()
                logger.error("Failed to delete triple: $triple", e)
                false
            } finally {
                connection.close()
            }
        } catch (e: Exception) {
            logger.error("Error deleting triple: $triple", e)
            false
        }
    }
    
    override fun getTriples(
        graph: IRI,
        subject: RdfTerm?,
        predicate: IRI?,
        obj: RdfTerm?
    ): List<RdfTriple> {
        return try {
            val connection = repository.connection
            val graphIri = valueFactory.createIRI(graph.value)
            
            val subjectTerm = subject?.let { convertToRdf4jResource(it) }
            val predicateTerm = predicate?.let { valueFactory.createIRI(it.value) }
            val objTerm = obj?.let { convertToRdf4jValue(it) }
            
            val statements = connection.getStatements(subjectTerm, predicateTerm, objTerm, false, graphIri)
            val triples = statements.map { statement ->
                RdfTriple(
                    subject = convertFromRdf4jTerm(statement.subject),
                    predicate = IRI(statement.predicate.stringValue()),
                    obj = convertFromRdf4jTerm(statement.`object`)
                )
            }.toList()
            
            statements.close()
            connection.close()
            triples
        } catch (e: Exception) {
            logger.error("Error getting triples", e)
            emptyList()
        }
    }
    
    override fun query(graph: IRI?, sparql: String): RdfResultSet {
        return try {
            val connection = repository.connection
            
            val tupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, sparql)
            if (graph != null) {
                tupleQuery.setBinding("g", valueFactory.createIRI(graph.value))
            }
            
            val result = tupleQuery.evaluate()
            val vars = result.bindingNames.toList()
            val rows = mutableListOf<Map<String, RdfTerm>>()
            
            while (result.hasNext()) {
                val bindingSet = result.next()
                val row = vars.associateWith { varName ->
                    val value = bindingSet.getValue(varName)
                    convertFromRdf4jTerm(value)
                }
                rows.add(row)
            }
            
            result.close()
            connection.close()
            RdfResultSet(vars, rows)
        } catch (e: Exception) {
            logger.error("Error executing SPARQL query", e)
            RdfResultSet(emptyList(), emptyList())
        }
    }
    
    override fun update(graph: IRI?, sparql: String): Boolean {
        return try {
            val connection = repository.connection
            
            val updateQuery = connection.prepareUpdate(QueryLanguage.SPARQL, sparql)
            if (graph != null) {
                updateQuery.setBinding("g", valueFactory.createIRI(graph.value))
            }
            
            connection.begin()
            try {
                updateQuery.execute()
                connection.commit()
                true
            } catch (e: Exception) {
                connection.rollback()
                logger.error("Failed to execute SPARQL update", e)
                false
            } finally {
                connection.close()
            }
        } catch (e: Exception) {
            logger.error("Error executing SPARQL update", e)
            false
        }
    }
    
    override fun beginTransaction() {
        // RDF4J transactions are handled per operation
        // This is a no-op for compatibility
    }
    
    override fun commitTransaction() {
        // RDF4J transactions are handled per operation
        // This is a no-op for compatibility
    }
    
    override fun rollbackTransaction() {
        // RDF4J transactions are handled per operation
        // This is a no-op for compatibility
    }
    
    override fun validate(graph: IRI, shapesGraph: IRI): ValidationReport {
        // TODO: Implement SHACL validation using RDF4J
        logger.warn("SHACL validation not yet implemented in RDF4J adapter")
        return ValidationReport(conforms = true, messages = listOf("Validation not implemented"))
    }
    
    override fun import(graph: IRI, rdfContent: String, format: RDFFormat): Boolean {
        // TODO: Implement RDF import using RDF4J
        logger.warn("RDF import not yet implemented in RDF4J adapter")
        return false
    }
    
    override fun export(graph: IRI, format: RDFFormat): String {
        // TODO: Implement RDF export using RDF4J
        logger.warn("RDF export not yet implemented in RDF4J adapter")
        return ""
    }
    
    override fun diff(before: RDFGraph, after: RDFGraph): TripleDelta {
        val beforeTriples = before.triples.toSet()
        val afterTriples = after.triples.toSet()
        
        val added = afterTriples - beforeTriples
        val removed = beforeTriples - afterTriples
        
        return TripleDelta(
            added = RDFGraph(added.toList(), after.namedGraph),
            removed = RDFGraph(removed.toList(), before.namedGraph)
        )
    }
    
    private fun convertToRdf4jResource(term: RdfTerm): org.eclipse.rdf4j.model.Resource {
        return when (term) {
            is IRI -> valueFactory.createIRI(term.value)
            else -> throw IllegalArgumentException("Subject must be an IRI, got: ${term::class.simpleName}")
        }
    }
    
    private fun convertToRdf4jValue(term: RdfTerm): org.eclipse.rdf4j.model.Value {
        return when (term) {
            is IRI -> valueFactory.createIRI(term.value)
            is Literal -> {
                if (term.lang != null) {
                    valueFactory.createLiteral(term.lexicalForm, term.lang)
                } else {
                    valueFactory.createLiteral(term.lexicalForm, valueFactory.createIRI(term.datatype.value))
                }
            }
            else -> throw IllegalArgumentException("Unsupported RDF term type: ${term::class.simpleName}")
        }
    }
    
    private fun convertFromRdf4jTerm(value: org.eclipse.rdf4j.model.Value): RdfTerm {
        return when (value) {
            is org.eclipse.rdf4j.model.IRI -> IRI(value.stringValue())
            is org.eclipse.rdf4j.model.Literal -> {
                if (value.language.isPresent) {
                    Literal(value.label, IRI(value.datatype.stringValue()), value.language.get())
                } else {
                    Literal(value.label, IRI(value.datatype.stringValue()))
                }
            }
            else -> throw IllegalArgumentException("Unsupported RDF4J value type: ${value::class.simpleName}")
        }
    }
} 