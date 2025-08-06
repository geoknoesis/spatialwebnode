package com.geoknoesis.spatialweb.core.udg.model

/**
 * Represents an RDF term in the UDG
 *
 * An RDF term can be an IRI, a literal, or a blank node.
 * This sealed class provides type-safe representation of RDF terms.
 */
sealed class RdfTerm {
    
    /**
     * Represents an IRI term
     */
    data class IRITerm(val iri: IRI) : RdfTerm() {
        override fun toString(): String = iri.toString()
    }
    
    /**
     * Represents a literal term with optional datatype and language
     */
    data class LiteralTerm(
        val literalValue: String,
        val datatype: IRI? = null,
        val language: String? = null
    ) : RdfTerm() {
        
        init {
            require(literalValue.isNotBlank()) { "Literal value cannot be blank" }
        }
        
        override fun toString(): String {
            return when {
                language != null -> "\"$literalValue\"@$language"
                datatype != null -> "\"$literalValue\"^^<$datatype>"
                else -> "\"$literalValue\""
            }
        }
        
        companion object {
            /**
             * Creates a string literal
             */
            fun string(value: String): LiteralTerm = LiteralTerm(value)
            
            /**
             * Creates an integer literal
             */
            fun integer(value: Int): LiteralTerm = LiteralTerm(
                value.toString(),
                IRI("http://www.w3.org/2001/XMLSchema#integer")
            )
            
            /**
             * Creates a double literal
             */
            fun double(value: Double): LiteralTerm = LiteralTerm(
                value.toString(),
                IRI("http://www.w3.org/2001/XMLSchema#double")
            )
            
            /**
             * Creates a boolean literal
             */
            fun boolean(value: Boolean): LiteralTerm = LiteralTerm(
                value.toString(),
                IRI("http://www.w3.org/2001/XMLSchema#boolean")
            )
            
            /**
             * Creates a date literal
             */
            fun date(value: String): LiteralTerm = LiteralTerm(
                value,
                IRI("http://www.w3.org/2001/XMLSchema#date")
            )
            
            /**
             * Creates a dateTime literal
             */
            fun dateTime(value: String): LiteralTerm = LiteralTerm(
                value,
                IRI("http://www.w3.org/2001/XMLSchema#dateTime")
            )
            
            /**
             * Creates a language-tagged literal
             */
            fun languageString(value: String, language: String): LiteralTerm = LiteralTerm(
                value,
                language = language
            )
        }
    }
    
    /**
     * Represents a blank node
     */
    data class BlankNodeTerm(val id: String) : RdfTerm() {
        init {
            require(id.isNotBlank()) { "Blank node ID cannot be blank" }
        }
        
        override fun toString(): String = "_:$id"
        
        companion object {
            /**
             * Creates a blank node with a generated ID
             */
            fun create(): BlankNodeTerm = BlankNodeTerm("bn_${System.currentTimeMillis()}_${(0..9999).random()}")
        }
    }
    
    /**
     * Gets the string representation of the term value
     */
    fun getValue(): String = when (this) {
        is IRITerm -> iri.value
        is LiteralTerm -> literalValue
        is BlankNodeTerm -> id
    }
    
    /**
     * Checks if this term is an IRI
     */
    fun isIRI(): Boolean = this is IRITerm
    
    /**
     * Checks if this term is a literal
     */
    fun isLiteral(): Boolean = this is LiteralTerm
    
    /**
     * Checks if this term is a blank node
     */
    fun isBlankNode(): Boolean = this is BlankNodeTerm
    
    /**
     * Gets the IRI if this is an IRITerm, null otherwise
     */
    fun asIRI(): IRI? = if (this is IRITerm) iri else null
    
    /**
     * Gets the literal value if this is a LiteralTerm, null otherwise
     */
    fun asLiteral(): String? = if (this is LiteralTerm) literalValue else null
    
    /**
     * Gets the blank node ID if this is a BlankNodeTerm, null otherwise
     */
    fun asBlankNode(): String? = if (this is BlankNodeTerm) id else null
} 