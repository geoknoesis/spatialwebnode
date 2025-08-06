package com.geoknoesis.spatialweb.core.activity.validation

import com.geoknoesis.spatialweb.core.activity.model.Activity
import com.geoknoesis.spatialweb.core.activity.model.ActivitySchema
import com.geoknoesis.spatialweb.core.activity.model.ExecutionConstraints
import com.geoknoesis.spatialweb.core.activity.model.VariableDefinition
import org.slf4j.LoggerFactory

/**
 * Composite validator that chains multiple validators together.
 *
 * This validator allows multiple validation strategies to be applied
 * in sequence, with configurable behavior for handling failures.
 */
class CompositeValidator(
    private val validators: List<ActivityValidator>,
    private val config: CompositeValidatorConfig = CompositeValidatorConfig()
) : ActivityValidator {
    
    private val logger = LoggerFactory.getLogger(CompositeValidator::class.java)
    
    override val validatorId: String = "composite-validator"
    override val name: String = "Composite Validator"
    override val version: String = "1.0.0"
    override val description: String = "Chains multiple validators together"
    
    override suspend fun validateSchema(schema: ActivitySchema): ValidationResult {
        val allErrors = mutableListOf<ValidationError>()
        val allWarnings = mutableListOf<ValidationWarning>()
        val metadata = mutableMapOf<String, Any>()
        
        val applicableValidators = validators.filter { it.canValidate(schema) }
        
        if (applicableValidators.isEmpty()) {
            logger.warn("No applicable validators found for schema: ${schema.id}")
            return ValidationResult.success(metadata = mapOf("validatorsApplied" to 0))
        }
        
        logger.debug("Applying ${applicableValidators.size} validators to schema: ${schema.id}")
        
        for (validator in applicableValidators) {
            try {
                val result = validator.validateSchema(schema)
                
                allErrors.addAll(result.errors)
                allWarnings.addAll(result.warnings)
                metadata["${validator.validatorId}_result"] = result.metadata
                
                // Check if we should stop on first failure
                if (config.stopOnFirstFailure && !result.isValid) {
                    logger.debug("Stopping validation on first failure from validator: ${validator.validatorId}")
                    break
                }
                
            } catch (e: Exception) {
                logger.error("Error in validator ${validator.validatorId} for schema ${schema.id}", e)
                allErrors.add(ValidationError(
                    code = "VALIDATOR_ERROR",
                    message = "Error in validator ${validator.validatorId}: ${e.message}",
                    severity = ValidationSeverity.FATAL,
                    details = mapOf("validator" to validator.validatorId)
                ))
                
                if (config.stopOnFirstFailure) {
                    break
                }
            }
        }
        
        metadata["validatorsApplied"] = applicableValidators.size
        metadata["validatorsTotal"] = validators.size
        
        return ValidationResult(
            isValid = allErrors.isEmpty() || config.allowErrors,
            errors = allErrors,
            warnings = allWarnings,
            metadata = metadata
        )
    }
    
    override suspend fun validateInput(
        schema: ActivitySchema,
        input: Map<String, Any>,
        context: ValidationContext
    ): ValidationResult {
        val allErrors = mutableListOf<ValidationError>()
        val allWarnings = mutableListOf<ValidationWarning>()
        val metadata = mutableMapOf<String, Any>()
        
        val applicableValidators = validators.filter { it.canValidate(schema) }
        
        if (applicableValidators.isEmpty()) {
            logger.warn("No applicable validators found for schema: ${schema.id}")
            return ValidationResult.success(metadata = mapOf("validatorsApplied" to 0))
        }
        
        logger.debug("Applying ${applicableValidators.size} validators to input for schema: ${schema.id}")
        
        for (validator in applicableValidators) {
            try {
                val result = validator.validateInput(schema, input, context)
                
                allErrors.addAll(result.errors)
                allWarnings.addAll(result.warnings)
                metadata["${validator.validatorId}_result"] = result.metadata
                
                // Check if we should stop on first failure
                if (config.stopOnFirstFailure && !result.isValid) {
                    logger.debug("Stopping validation on first failure from validator: ${validator.validatorId}")
                    break
                }
                
            } catch (e: Exception) {
                logger.error("Error in validator ${validator.validatorId} for input validation", e)
                allErrors.add(ValidationError(
                    code = "VALIDATOR_ERROR",
                    message = "Error in validator ${validator.validatorId}: ${e.message}",
                    severity = ValidationSeverity.FATAL,
                    details = mapOf("validator" to validator.validatorId)
                ))
                
                if (config.stopOnFirstFailure) {
                    break
                }
            }
        }
        
        metadata["validatorsApplied"] = applicableValidators.size
        metadata["validatorsTotal"] = validators.size
        
        return ValidationResult(
            isValid = allErrors.isEmpty() || config.allowErrors,
            errors = allErrors,
            warnings = allWarnings,
            metadata = metadata
        )
    }
    
    override suspend fun validateOutput(
        schema: ActivitySchema,
        output: Map<String, Any>,
        context: ValidationContext
    ): ValidationResult {
        val allErrors = mutableListOf<ValidationError>()
        val allWarnings = mutableListOf<ValidationWarning>()
        val metadata = mutableMapOf<String, Any>()
        
        val applicableValidators = validators.filter { it.canValidate(schema) }
        
        if (applicableValidators.isEmpty()) {
            logger.warn("No applicable validators found for schema: ${schema.id}")
            return ValidationResult.success(metadata = mapOf("validatorsApplied" to 0))
        }
        
        logger.debug("Applying ${applicableValidators.size} validators to output for schema: ${schema.id}")
        
        for (validator in applicableValidators) {
            try {
                val result = validator.validateOutput(schema, output, context)
                
                allErrors.addAll(result.errors)
                allWarnings.addAll(result.warnings)
                metadata["${validator.validatorId}_result"] = result.metadata
                
                // Check if we should stop on first failure
                if (config.stopOnFirstFailure && !result.isValid) {
                    logger.debug("Stopping validation on first failure from validator: ${validator.validatorId}")
                    break
                }
                
            } catch (e: Exception) {
                logger.error("Error in validator ${validator.validatorId} for output validation", e)
                allErrors.add(ValidationError(
                    code = "VALIDATOR_ERROR",
                    message = "Error in validator ${validator.validatorId}: ${e.message}",
                    severity = ValidationSeverity.FATAL,
                    details = mapOf("validator" to validator.validatorId)
                ))
                
                if (config.stopOnFirstFailure) {
                    break
                }
            }
        }
        
        metadata["validatorsApplied"] = applicableValidators.size
        metadata["validatorsTotal"] = validators.size
        
        return ValidationResult(
            isValid = allErrors.isEmpty() || config.allowErrors,
            errors = allErrors,
            warnings = allWarnings,
            metadata = metadata
        )
    }
    
    override suspend fun validateVariable(
        variable: VariableDefinition,
        value: Any?,
        context: ValidationContext
    ): ValidationResult {
        val allErrors = mutableListOf<ValidationError>()
        val allWarnings = mutableListOf<ValidationWarning>()
        val metadata = mutableMapOf<String, Any>()
        
        // For variable validation, we need to create a mock schema to check if validators can handle it
        val mockSchema = ActivitySchema(
            id = "mock-schema",
            name = "Mock Schema",
            description = "Mock schema for variable validation",
            version = "1.0.0",
            category = "validation",
            executorPluginId = "mock-executor",
            inputSchema = mapOf(variable.name to variable),
            outputSchema = emptyMap(),
            constraints = ExecutionConstraints(),
            metadata = emptyMap()
        )
        
        val applicableValidators = validators.filter { it.canValidate(mockSchema) }
        
        if (applicableValidators.isEmpty()) {
            logger.warn("No applicable validators found for variable: ${variable.name}")
            return ValidationResult.success(metadata = mapOf("validatorsApplied" to 0))
        }
        
        logger.debug("Applying ${applicableValidators.size} validators to variable: ${variable.name}")
        
        for (validator in applicableValidators) {
            try {
                val result = validator.validateVariable(variable, value, context)
                
                allErrors.addAll(result.errors)
                allWarnings.addAll(result.warnings)
                metadata["${validator.validatorId}_result"] = result.metadata
                
                // Check if we should stop on first failure
                if (config.stopOnFirstFailure && !result.isValid) {
                    logger.debug("Stopping validation on first failure from validator: ${validator.validatorId}")
                    break
                }
                
            } catch (e: Exception) {
                logger.error("Error in validator ${validator.validatorId} for variable validation", e)
                allErrors.add(ValidationError(
                    code = "VALIDATOR_ERROR",
                    message = "Error in validator ${validator.validatorId}: ${e.message}",
                    severity = ValidationSeverity.FATAL,
                    details = mapOf("validator" to validator.validatorId)
                ))
                
                if (config.stopOnFirstFailure) {
                    break
                }
            }
        }
        
        metadata["validatorsApplied"] = applicableValidators.size
        metadata["validatorsTotal"] = validators.size
        
        return ValidationResult(
            isValid = allErrors.isEmpty() || config.allowErrors,
            errors = allErrors,
            warnings = allWarnings,
            metadata = metadata
        )
    }
    
    override fun canValidate(schema: ActivitySchema): Boolean {
        return validators.any { it.canValidate(schema) }
    }
    
    override suspend fun initialize(): Boolean {
        logger.info("Initializing Composite Validator with ${validators.size} validators")
        
        val initializationResults = mutableListOf<Boolean>()
        
        for (validator in validators) {
            try {
                val result = validator.initialize()
                initializationResults.add(result)
                
                if (result) {
                    logger.debug("Validator ${validator.validatorId} initialized successfully")
                } else {
                    logger.warn("Validator ${validator.validatorId} failed to initialize")
                }
                
                // Check if we should stop on first failure
                if (config.stopOnFirstFailure && !result) {
                    logger.error("Stopping initialization on first failure from validator: ${validator.validatorId}")
                    break
                }
                
            } catch (e: Exception) {
                logger.error("Error initializing validator ${validator.validatorId}", e)
                initializationResults.add(false)
                
                if (config.stopOnFirstFailure) {
                    break
                }
            }
        }
        
        val successCount = initializationResults.count { it }
        val totalCount = initializationResults.size
        
        logger.info("Composite Validator initialization complete: $successCount/$totalCount validators initialized")
        
        return if (config.requireAllValidators) {
            successCount == totalCount
        } else {
            successCount > 0
        }
    }
    
    override suspend fun shutdown() {
        logger.info("Shutting down Composite Validator")
        
        for (validator in validators) {
            try {
                validator.shutdown()
                logger.debug("Validator ${validator.validatorId} shut down successfully")
            } catch (e: Exception) {
                logger.error("Error shutting down validator ${validator.validatorId}", e)
            }
        }
    }
    
    override fun getMetadata(): Map<String, Any> {
        return mapOf(
            "validatorId" to validatorId,
            "name" to name,
            "version" to version,
            "description" to description,
            "config" to config,
            "validators" to validators.map { it.getMetadata() },
            "validatorCount" to validators.size
        )
    }
}

/**
 * Configuration for composite validator
 */
data class CompositeValidatorConfig(
    /** Whether to stop validation on first failure */
    val stopOnFirstFailure: Boolean = false,
    
    /** Whether to allow errors and still consider validation successful */
    val allowErrors: Boolean = false,
    
    /** Whether to require all validators to initialize successfully */
    val requireAllValidators: Boolean = false,
    
    /** Validation strategy */
    val strategy: ValidationStrategy = ValidationStrategy.ALL,
    
    /** Custom validation rules */
    val customRules: Map<String, Any> = emptyMap()
)

/**
 * Validation strategies for composite validator
 */
enum class ValidationStrategy {
    /** Apply all validators */
    ALL,
    
    /** Apply validators until first success */
    FIRST_SUCCESS,
    
    /** Apply validators until first failure */
    FIRST_FAILURE,
    
    /** Apply validators in parallel */
    PARALLEL,
    
    /** Apply validators based on priority */
    PRIORITY
} 