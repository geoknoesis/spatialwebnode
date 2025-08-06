package com.geoknoesis.spatialweb.core.activity.validation

import com.geoknoesis.spatialweb.core.activity.model.Activity
import com.geoknoesis.spatialweb.core.activity.model.ActivitySchema
import com.geoknoesis.spatialweb.core.activity.model.VariableDefinition

/**
 * Pluggable interface for activity validation.
 *
 * This interface allows different validation strategies to be implemented:
 * - SHACL validation in RDF stores
 * - SQL triggers in relational databases
 * - Custom validation logic
 * - External validation services
 */
interface ActivityValidator {
    
    /**
     * Unique identifier for this validator
     */
    val validatorId: String
    
    /**
     * Human-readable name of this validator
     */
    val name: String
    
    /**
     * Version of this validator
     */
    val version: String
    
    /**
     * Description of what this validator does
     */
    val description: String
    
    /**
     * Validates an activity schema
     */
    suspend fun validateSchema(schema: ActivitySchema): ValidationResult
    
    /**
     * Validates input variables for an activity
     */
    suspend fun validateInput(
        schema: ActivitySchema,
        input: Map<String, Any>,
        context: ValidationContext
    ): ValidationResult
    
    /**
     * Validates output variables for an activity
     */
    suspend fun validateOutput(
        schema: ActivitySchema,
        output: Map<String, Any>,
        context: ValidationContext
    ): ValidationResult
    
    /**
     * Validates a specific variable
     */
    suspend fun validateVariable(
        variable: VariableDefinition,
        value: Any?,
        context: ValidationContext
    ): ValidationResult
    
    /**
     * Checks if this validator can handle the given schema
     */
    fun canValidate(schema: ActivitySchema): Boolean
    
    /**
     * Initializes the validator
     */
    suspend fun initialize(): Boolean
    
    /**
     * Shuts down the validator
     */
    suspend fun shutdown()
    
    /**
     * Gets validator metadata
     */
    fun getMetadata(): Map<String, Any>
}

/**
 * Context information for validation operations
 */
data class ValidationContext(
    /** Activity being validated */
    val activity: Activity? = null,
    
    /** User or system performing the validation */
    val validatedBy: String? = null,
    
    /** Validation phase */
    val phase: ValidationPhase = ValidationPhase.INPUT,
    
    /** Additional context data */
    val data: Map<String, Any> = emptyMap(),
    
    /** Validation options */
    val options: ValidationOptions = ValidationOptions()
)

/**
 * Validation phases
 */
enum class ValidationPhase {
    SCHEMA,     // Schema validation
    INPUT,      // Input validation
    OUTPUT,     // Output validation
    RUNTIME     // Runtime validation
}

/**
 * Validation options
 */
data class ValidationOptions(
    /** Whether to perform strict validation */
    val strict: Boolean = true,
    
    /** Whether to allow unknown variables */
    val allowUnknown: Boolean = false,
    
    /** Whether to validate recursively */
    val recursive: Boolean = true,
    
    /** Maximum validation depth */
    val maxDepth: Int = 10,
    
    /** Custom validation rules */
    val customRules: Map<String, Any> = emptyMap()
)

/**
 * Result of validation operations
 */
data class ValidationResult(
    /** Whether the validation passed */
    val isValid: Boolean,
    
    /** Validation errors */
    val errors: List<ValidationError> = emptyList(),
    
    /** Validation warnings */
    val warnings: List<ValidationWarning> = emptyList(),
    
    /** Validation metadata */
    val metadata: Map<String, Any> = emptyMap()
) {
    companion object {
        fun success(metadata: Map<String, Any> = emptyMap()) = ValidationResult(true, metadata = metadata)
        fun failure(vararg errors: ValidationError) = ValidationResult(false, errors = errors.toList())
        fun failure(errors: List<ValidationError>) = ValidationResult(false, errors = errors)
    }
}

/**
 * Represents a validation error
 */
data class ValidationError(
    /** Error code */
    val code: String,
    
    /** Error message */
    val message: String,
    
    /** Variable path where the error occurred */
    val path: String? = null,
    
    /** Error severity */
    val severity: ValidationSeverity = ValidationSeverity.ERROR,
    
    /** Additional error details */
    val details: Map<String, Any> = emptyMap(),
    
    /** When the error occurred */
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Represents a validation warning
 */
data class ValidationWarning(
    /** Warning code */
    val code: String,
    
    /** Warning message */
    val message: String,
    
    /** Variable path where the warning occurred */
    val path: String? = null,
    
    /** Additional warning details */
    val details: Map<String, Any> = emptyMap(),
    
    /** When the warning occurred */
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Validation severity levels
 */
enum class ValidationSeverity {
    INFO,       // Informational
    WARNING,    // Warning
    ERROR,      // Error
    FATAL       // Fatal error
}

/**
 * Configuration for activity validators
 */
data class ValidatorConfig(
    /** Validator type */
    val type: ValidatorType,
    
    /** Validator-specific configuration */
    val config: Map<String, Any> = emptyMap(),
    
    /** Whether this validator is enabled */
    val enabled: Boolean = true,
    
    /** Validator priority (higher = more important) */
    val priority: Int = 0,
    
    /** Validator dependencies */
    val dependencies: List<String> = emptyList()
)

/**
 * Supported validator types
 */
enum class ValidatorType {
    /** SHACL validation in RDF stores */
    SHACL_RDF,
    
    /** SQL trigger validation in relational databases */
    SQL_TRIGGER,
    
    /** Custom validation logic */
    CUSTOM,
    
    /** External validation service */
    EXTERNAL,
    
    /** Composite validation (multiple validators) */
    COMPOSITE
} 