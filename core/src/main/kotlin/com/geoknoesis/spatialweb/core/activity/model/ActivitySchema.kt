package com.geoknoesis.spatialweb.core.activity.model

import kotlinx.serialization.Serializable
import java.time.Instant
import java.util.UUID

/**
 * Defines the schema for an activity, including its structure, metadata, and execution requirements.
 *
 * An ActivitySchema serves as a template that defines:
 * - The activity's unique identifier and version
 * - Input and output data structures
 * - Execution requirements and constraints
 * - Associated executor plugin
 * - Validation rules and metadata
 */
@Serializable
data class ActivitySchema(
    /** Unique identifier for this activity schema */
    val id: String,
    
    /** Human-readable name of the activity */
    val name: String,
    
    /** Detailed description of what this activity does */
    val description: String,
    
    /** Version of this schema */
    val version: String,
    
    /** Category or type of activity */
    val category: String,
    
    /** Plugin identifier that will execute this activity */
    val executorPluginId: String,
    
    /** Input variables definition */
    val inputSchema: Map<String, VariableDefinition>,
    
    /** Output variables definition */
    val outputSchema: Map<String, VariableDefinition>,
    
    /** Execution constraints and requirements */
    val constraints: ExecutionConstraints,
    
    /** Metadata and tags for categorization */
    val metadata: Map<String, String>,
    
    /** When this schema was created */
    val createdAt: Instant = Instant.now(),
    
    /** When this schema was last updated */
    val updatedAt: Instant = Instant.now(),
    
    /** Whether this schema is active and can be used */
    val isActive: Boolean = true
) {
    /**
     * Validates that the provided input variables match the schema definition
     */
    fun validateInput(input: Map<String, Any>): ValidationResult {
        val errors = mutableListOf<String>()
        
        // Check required variables
        inputSchema.forEach { (name, definition) ->
            if (definition.constraints?.required == true && !input.containsKey(name)) {
                errors.add("Required variable '$name' is missing")
            }
        }
        
        // Check variable constraints
        input.forEach { (name, value) ->
            val definition = inputSchema[name]
            if (definition != null) {
                val validation = definition.validate(value)
                if (!validation.isValid) {
                    errors.add("Variable '$name': ${validation.errors.joinToString(", ")}")
                }
            } else {
                errors.add("Unknown variable '$name'")
            }
        }
        
        return ValidationResult(errors.isEmpty(), errors)
    }
    
    /**
     * Creates a new activity instance based on this schema
     */
    fun createActivity(
        input: Map<String, Any>,
        createdBy: String,
        correlationId: String? = null
    ): Activity {
        val validation = validateInput(input)
        if (!validation.isValid) {
            throw IllegalArgumentException("Invalid input: ${validation.errors.joinToString(", ")}")
        }
        
        return Activity(
            id = UUID.randomUUID().toString(),
            schemaId = id,
            schemaVersion = version,
            input = input,
            status = ActivityStatus.CREATED,
            createdBy = createdBy,
            correlationId = correlationId,
            createdAt = Instant.now()
        )
    }
}

/**
 * Defines a variable in an activity schema
 */
@Serializable
data class VariableDefinition(
    /** Variable ID */
    val id: String,
    
    /** Variable name */
    val name: String,
    
    /** Description of this variable */
    val description: String? = null,
    
    /** SHACL property shape constraints */
    val constraints: ShaclPropertyShape? = null
) {
    fun validate(value: Any): ValidationResult {
        val errors = mutableListOf<String>()
        
        // Apply SHACL constraints if defined
        constraints?.let { shacl ->
            val validation = shacl.validate(value)
            if (!validation.isValid) {
                errors.addAll(validation.errors)
            }
        }
        
        return ValidationResult(errors.isEmpty(), errors)
    }
}

/**
 * Execution constraints for an activity
 */
@Serializable
data class ExecutionConstraints(
    /** Maximum execution time in seconds */
    val maxExecutionTime: Long? = null,
    
    /** Maximum memory usage in MB */
    val maxMemoryUsage: Long? = null,
    
    /** Whether this activity can be executed in parallel */
    val allowParallel: Boolean = true,
    
    /** Required permissions or capabilities */
    val requiredPermissions: List<String> = emptyList(),
    
    /** Retry configuration */
    val retryConfig: RetryConfig? = null,
    
    /** Timeout configuration */
    val timeoutConfig: TimeoutConfig? = null
)

/**
 * Retry configuration for activity execution
 */
@Serializable
data class RetryConfig(
    val maxRetries: Int = 3,
    val retryDelay: Long = 1000, // milliseconds
    val backoffMultiplier: Double = 2.0,
    val maxRetryDelay: Long = 30000 // milliseconds
)

/**
 * Timeout configuration for activity execution
 */
@Serializable
data class TimeoutConfig(
    val connectionTimeout: Long = 30000, // milliseconds
    val readTimeout: Long = 60000, // milliseconds
    val executionTimeout: Long = 300000 // milliseconds
)

/**
 * Result of validation operations
 */
data class ValidationResult(
    val isValid: Boolean,
    val errors: List<String>
) 