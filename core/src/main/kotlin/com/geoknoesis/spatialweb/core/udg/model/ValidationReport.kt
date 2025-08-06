package com.geoknoesis.spatialweb.core.udg.model

import com.geoknoesis.spatialweb.identity.did.Did
import java.time.Instant

/**
 * Represents a validation report for an entity
 *
 * Validation reports contain the results of validating an entity
 * against a shape graph or other validation rules.
 */
data class ValidationReport(
    /** Whether the validation was successful */
    val isValid: Boolean,
    
    /** The entity that was validated */
    val entityDid: Did,
    
    /** The shape graph used for validation */
    val shapeGraph: IRI,
    
    /** The timestamp when validation was performed */
    val timestamp: Instant = Instant.now(),
    
    /** Validation errors found */
    val errors: List<ValidationError> = emptyList(),
    
    /** Validation warnings found */
    val warnings: List<ValidationWarning> = emptyList(),
    
    /** Validation info messages */
    val info: List<ValidationInfo> = emptyList(),
    
    /** The validator that performed the validation */
    val validator: String? = null,
    
    /** Time taken for validation in milliseconds */
    val validationTimeMs: Long = 0,
    
    /** Additional metadata */
    val metadata: Map<String, Any> = emptyMap()
) {
    
    /**
     * Gets the total number of validation issues
     */
    val totalIssues: Int = errors.size + warnings.size + info.size
    
    /**
     * Gets the severity level of the most severe issue
     */
    val severity: ValidationSeverity
        get() = when {
            errors.isNotEmpty() -> ValidationSeverity.ERROR
            warnings.isNotEmpty() -> ValidationSeverity.WARNING
            info.isNotEmpty() -> ValidationSeverity.INFO
            else -> ValidationSeverity.INFO
        }
    
    /**
     * Gets all validation issues
     */
    val allIssues: List<ValidationIssue> = errors + warnings + info
    
    /**
     * Checks if there are any errors
     */
    fun hasErrors(): Boolean = errors.isNotEmpty()
    
    /**
     * Checks if there are any warnings
     */
    fun hasWarnings(): Boolean = warnings.isNotEmpty()
    
    /**
     * Gets errors for a specific property
     */
    fun getErrorsForProperty(property: Property): List<ValidationError> {
        return errors.filter { it.property == property }
    }
    
    /**
     * Gets warnings for a specific property
     */
    fun getWarningsForProperty(property: Property): List<ValidationWarning> {
        return warnings.filter { it.property == property }
    }
    
    /**
     * Creates a copy with additional metadata
     */
    fun withMetadata(key: String, value: Any): ValidationReport {
        return copy(metadata = metadata + (key to value))
    }
    
    companion object {
        /**
         * Creates a successful validation report
         */
        fun success(
            entityDid: Did,
            shapeGraph: IRI,
            validator: String? = null,
            validationTimeMs: Long = 0
        ): ValidationReport {
            return ValidationReport(
                isValid = true,
                entityDid = entityDid,
                shapeGraph = shapeGraph,
                validator = validator,
                validationTimeMs = validationTimeMs
            )
        }
        
        /**
         * Creates a failed validation report
         */
        fun failure(
            entityDid: Did,
            shapeGraph: IRI,
            errors: List<ValidationError>,
            warnings: List<ValidationWarning> = emptyList(),
            validator: String? = null,
            validationTimeMs: Long = 0
        ): ValidationReport {
            return ValidationReport(
                isValid = false,
                entityDid = entityDid,
                shapeGraph = shapeGraph,
                errors = errors,
                warnings = warnings,
                validator = validator,
                validationTimeMs = validationTimeMs
            )
        }
    }
}

/**
 * Base class for validation issues
 */
sealed class ValidationIssue {
    /** The property that has the issue */
    abstract val property: Property?
    
    /** The message describing the issue */
    abstract val message: String
    
    /** The severity of the issue */
    abstract val severity: ValidationSeverity
    
    /** Additional details about the issue */
    abstract val details: Map<String, Any>
}

/**
 * Represents a validation error
 */
data class ValidationError(
    override val property: Property?,
    override val message: String,
    override val details: Map<String, Any> = emptyMap(),
    /** The constraint that was violated */
    val constraint: String? = null,
    /** The expected value */
    val expectedValue: Any? = null,
    /** The actual value */
    val actualValue: Any? = null
) : ValidationIssue() {
    override val severity: ValidationSeverity = ValidationSeverity.ERROR
}

/**
 * Represents a validation warning
 */
data class ValidationWarning(
    override val property: Property?,
    override val message: String,
    override val details: Map<String, Any> = emptyMap(),
    /** The constraint that was violated */
    val constraint: String? = null,
    /** The expected value */
    val expectedValue: Any? = null,
    /** The actual value */
    val actualValue: Any? = null
) : ValidationIssue() {
    override val severity: ValidationSeverity = ValidationSeverity.WARNING
}

/**
 * Represents a validation info message
 */
data class ValidationInfo(
    override val property: Property?,
    override val message: String,
    override val details: Map<String, Any> = emptyMap()
) : ValidationIssue() {
    override val severity: ValidationSeverity = ValidationSeverity.INFO
}

/**
 * Severity levels for validation issues
 */
enum class ValidationSeverity {
    /** Informational message */
    INFO,
    
    /** Warning message */
    WARNING,
    
    /** Error message */
    ERROR,
    
    /** Fatal error */
    FATAL
} 