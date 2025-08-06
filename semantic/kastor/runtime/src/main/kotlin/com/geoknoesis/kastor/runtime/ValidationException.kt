package com.geoknoesis.kastor.runtime

/**
 * Validation Exception
 * 
 * Thrown when RDF validation fails during resource operations.
 * Contains a list of validation error messages for debugging.
 */
class ValidationException(messages: List<String>) :
    RuntimeException("Validation failed: ${messages.joinToString("; ")}")