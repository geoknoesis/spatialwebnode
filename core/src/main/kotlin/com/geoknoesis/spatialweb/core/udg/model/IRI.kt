package com.geoknoesis.spatialweb.core.udg.model

/**
 * Internationalized Resource Identifier (IRI)
 *
 * An IRI is a generalization of URI that allows for Unicode characters.
 * In the context of the UDG, IRIs are used to identify types, properties,
 * and other resources in the RDF graph.
 */
data class IRI(
    /** The full IRI string */
    val value: String
) {
    
    init {
        require(value.isNotBlank()) { "IRI value cannot be blank" }
        require(isValidIRI(value)) { "Invalid IRI format: $value" }
    }
    
    /**
     * Gets the namespace part of the IRI
     */
    val namespace: String
        get() = value.substringBeforeLast("#").substringBeforeLast("/")
    
    /**
     * Gets the local name part of the IRI
     */
    val localName: String
        get() = value.substringAfterLast("#").substringAfterLast("/")
    
    /**
     * Gets the fragment part of the IRI (after #)
     */
    val fragment: String?
        get() = if (value.contains("#")) value.substringAfter("#") else null
    
    /**
     * Checks if this IRI is in the given namespace
     */
    fun isInNamespace(namespace: String): Boolean {
        return value.startsWith(namespace)
    }
    
    /**
     * Creates a new IRI by appending a local name to the namespace
     */
    fun append(localName: String): IRI {
        val separator = if (value.endsWith("/") || value.endsWith("#")) "" else "/"
        return IRI("$value$separator$localName")
    }
    
    override fun toString(): String = value
    
    companion object {
        /**
         * Creates an IRI from a string, returning null if invalid
         */
        fun fromString(value: String): IRI? {
            return try {
                IRI(value)
            } catch (e: IllegalArgumentException) {
                null
            }
        }
        
        /**
         * Creates an IRI from namespace and local name
         */
        fun fromNamespaceAndLocalName(namespace: String, localName: String): IRI {
            val separator = if (namespace.endsWith("/") || namespace.endsWith("#")) "" else "/"
            return IRI("$namespace$separator$localName")
        }
        
        /**
         * Validates IRI format
         */
        private fun isValidIRI(value: String): Boolean {
            // Basic IRI validation - can be enhanced for full RFC 3987 compliance
            return value.matches(Regex("^[a-zA-Z][a-zA-Z0-9+.-]*:.*"))
        }
        
        // Common IRIs
        val RDF_TYPE = IRI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")
        val RDFS_LABEL = IRI("http://www.w3.org/2000/01/rdf-schema#label")
        val RDFS_COMMENT = IRI("http://www.w3.org/2000/01/rdf-schema#comment")
        val OWL_CLASS = IRI("http://www.w3.org/2002/07/owl#Class")
        val OWL_OBJECT_PROPERTY = IRI("http://www.w3.org/2002/07/owl#ObjectProperty")
        val OWL_DATATYPE_PROPERTY = IRI("http://www.w3.org/2002/07/owl#DatatypeProperty")
    }
} 