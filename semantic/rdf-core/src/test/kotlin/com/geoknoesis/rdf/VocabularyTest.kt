package com.geoknoesis.rdf

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class VocabularyTest {
    
    @Test
    fun `test XSD vocabulary`() {
        // Test basic functionality
        assertEquals("http://www.w3.org/2001/XMLSchema#string", XSD.string.iri)
        assertEquals("string", XSD.string.localName)
        assertEquals(1, XSD.string.code)
        assertEquals("XSD.string", XSD.string.toString())
        
        // Test lookup functionality
        assertEquals(XSD.string, XSD.fromLocalName("string"))
        assertEquals(XSD.boolean, XSD.fromLocalName("boolean"))
        assertEquals(XSD.int, XSD.fromLocalName("int"))
        
        // Test IRI lookup
        assertEquals(XSD.string, XSD.fromIRI("http://www.w3.org/2001/XMLSchema#string"))
        assertEquals(XSD.boolean, XSD.fromIRI("http://www.w3.org/2001/XMLSchema#boolean"))
        
        // Test all values exist
        assertTrue(XSD.values().isNotEmpty())
        assertEquals(3, XSD.values().size)
        
        // Test namespace constant
        assertEquals("http://www.w3.org/2001/XMLSchema#", XSD.NAMESPACE)
    }
    
    @Test
    fun `test RDF vocabulary`() {
        // Test basic functionality
        assertEquals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type", RDF.type.iri)
        assertEquals("type", RDF.type.localName)
        assertEquals("RDF.type", RDF.type.toString())
        
        // Test lookup functionality
        assertEquals(RDF.type, RDF.fromLocalName("type"))
        assertEquals(RDF.subject, RDF.fromLocalName("subject"))
        assertEquals(RDF.predicate, RDF.fromLocalName("predicate"))
        assertEquals(RDF.object_, RDF.fromLocalName("object"))
        
        // Test IRI lookup
        assertEquals(RDF.type, RDF.fromIRI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"))
        
        // Test all values exist
        assertTrue(RDF.values().isNotEmpty())
        assertEquals(4, RDF.values().size)
        
        // Test namespace constant
        assertEquals("http://www.w3.org/1999/02/22-rdf-syntax-ns#", RDF.NAMESPACE)
    }
    
    @Test
    fun `test Vocabulary interface`() {
        // Test that enums implement the Vocabulary interface
        assertTrue(XSD.string is Vocabulary)
        assertTrue(RDF.type is Vocabulary)
        
        // Test localName property
        assertEquals("string", XSD.string.localName)
        assertEquals("type", RDF.type.localName)
    }
    
    @Test
    fun `test IRI creation from vocabulary`() {
        // Test creating IRI objects from vocabulary enums
        val stringIRI = IRI(XSD.string.iri)
        val typeIRI = IRI(RDF.type.iri)
        
        assertEquals("http://www.w3.org/2001/XMLSchema#string", stringIRI.value)
        assertEquals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type", typeIRI.value)
    }
    
    @Test
    fun `test literal creation with XSD types`() {
        // Test creating literals with XSD datatypes
        val stringLiteral = Literal("Hello World", IRI(XSD.string.iri))
        val intLiteral = Literal("42", IRI(XSD.int.iri))
        val booleanLiteral = Literal("true", IRI(XSD.boolean.iri))
        
        assertEquals("Hello World", stringLiteral.lexicalForm)
        assertEquals("42", intLiteral.lexicalForm)
        assertEquals("true", booleanLiteral.lexicalForm)
        
        assertEquals(XSD.string.iri, stringLiteral.datatype.value)
        assertEquals(XSD.int.iri, intLiteral.datatype.value)
        assertEquals(XSD.boolean.iri, booleanLiteral.datatype.value)
    }
    
    @Test
    fun `test lookup with invalid values`() {
        // Test that lookup returns null for invalid values
        assertNull(XSD.fromLocalName("invalid"))
        assertNull(XSD.fromIRI("http://invalid.iri"))
        
        assertNull(RDF.fromLocalName("invalid"))
        assertNull(RDF.fromIRI("http://invalid.iri"))
    }
} 