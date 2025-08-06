package com.geoknoesis.spatialweb.core.activity.validation

import com.geoknoesis.spatialweb.core.activity.model.Activity
import com.geoknoesis.spatialweb.core.activity.model.ActivitySchema
import com.geoknoesis.spatialweb.core.activity.model.ExecutionConstraints
import com.geoknoesis.spatialweb.core.activity.model.VariableDefinition
import org.slf4j.LoggerFactory
import java.util.ServiceLoader
import java.util.concurrent.ConcurrentHashMap

/**
 * Manages multiple activity validators and provides a unified validation interface.
 *
 * The ValidatorManager:
 * - Discovers validators using Java SPI
 * - Manages validator lifecycle
 * - Routes validation requests to appropriate validators
 * - Provides composite validation capabilities
 */
class ValidatorManager(
    private val config: ValidatorManagerConfig = ValidatorManagerConfig()
) {
    
    private val logger = LoggerFactory.getLogger(ValidatorManager::class.java)
    
    // Registry of available validators
    private val validators = ConcurrentHashMap<String, ActivityValidator>()
    
    // Composite validator for chaining multiple validators
    private var compositeValidator: CompositeValidator? = null
    
    // Event listeners for validation events
    private val eventListeners = mutableListOf<ValidationEventListener>()
    
    init {
        // Note: initialize() is called separately after construction
    }
    
    /**
     * Initializes the validator manager
     */
    suspend fun initialize() {
        logger.info("Initializing ValidatorManager...")
        
        // Load validators via SPI
        loadValidators()
        
        // Create composite validator if multiple validators are available
        if (validators.size > 1 && config.enableCompositeValidation) {
            createCompositeValidator()
        }
        
        logger.info("ValidatorManager initialized with ${validators.size} validators")
    }
    
    /**
     * Loads validators using Java Service Provider Interface
     */
    private fun loadValidators() {
        val loadedValidators = ServiceLoader.load(ActivityValidator::class.java)
        loadedValidators.forEach { validator ->
            registerValidator(validator)
        }
        logger.info("Loaded ${validators.size} validators via SPI")
    }
    
    /**
     * Registers a validator
     */
    fun registerValidator(validator: ActivityValidator) {
        validators[validator.validatorId] = validator
        logger.info("Registered validator: ${validator.name} (${validator.validatorId})")
    }
    
    /**
     * Unregisters a validator
     */
    fun unregisterValidator(validatorId: String) {
        validators.remove(validatorId)
        logger.info("Unregistered validator: $validatorId")
    }
    
    /**
     * Creates a composite validator
     */
    private fun createCompositeValidator() {
        val validatorList = validators.values.toList()
        compositeValidator = CompositeValidator(validatorList, config.compositeConfig)
        logger.info("Created composite validator with ${validatorList.size} validators")
    }
    
    /**
     * Validates an activity schema
     */
    suspend fun validateSchema(schema: ActivitySchema, context: ValidationContext? = null): ValidationResult {
        val validationContext = context ?: ValidationContext(phase = ValidationPhase.SCHEMA)
        
        logger.debug("Validating schema: ${schema.id}")
        
        return try {
            val result = if (compositeValidator != null && config.useCompositeValidator) {
                compositeValidator!!.validateSchema(schema)
            } else {
                // Find applicable validators
                val applicableValidators = validators.values.filter { it.canValidate(schema) }
                
                if (applicableValidators.isEmpty()) {
                    logger.warn("No applicable validators found for schema: ${schema.id}")
                    ValidationResult.success(metadata = mapOf("validatorsApplied" to 0))
                } else {
                    // Use the first applicable validator
                    val validator = applicableValidators.first()
                    validator.validateSchema(schema)
                }
            }
            
            // Notify listeners
            notifyValidationEvent(schema, validationContext, result)
            
            result
            
        } catch (e: Exception) {
            logger.error("Error validating schema: ${schema.id}", e)
            val errorResult = ValidationResult.failure(
                ValidationError(
                    code = "VALIDATION_ERROR",
                    message = "Error during schema validation: ${e.message}",
                    severity = ValidationSeverity.FATAL
                )
            )
            notifyValidationEvent(schema, validationContext, errorResult)
            errorResult
        }
    }
    
    /**
     * Validates input variables for an activity
     */
    suspend fun validateInput(
        schema: ActivitySchema,
        input: Map<String, Any>,
        context: ValidationContext? = null
    ): ValidationResult {
        val validationContext = context ?: ValidationContext(phase = ValidationPhase.INPUT)
        
        logger.debug("Validating input for schema: ${schema.id}")
        
        return try {
            val result = if (compositeValidator != null && config.useCompositeValidator) {
                compositeValidator!!.validateInput(schema, input, validationContext)
            } else {
                // Find applicable validators
                val applicableValidators = validators.values.filter { it.canValidate(schema) }
                
                if (applicableValidators.isEmpty()) {
                    logger.warn("No applicable validators found for schema: ${schema.id}")
                    ValidationResult.success(metadata = mapOf("validatorsApplied" to 0))
                } else {
                    // Use the first applicable validator
                    val validator = applicableValidators.first()
                    validator.validateInput(schema, input, validationContext)
                }
            }
            
            // Notify listeners
            notifyValidationEvent(schema, validationContext, result)
            
            result
            
        } catch (e: Exception) {
            logger.error("Error validating input for schema: ${schema.id}", e)
            val errorResult = ValidationResult.failure(
                ValidationError(
                    code = "VALIDATION_ERROR",
                    message = "Error during input validation: ${e.message}",
                    severity = ValidationSeverity.FATAL
                )
            )
            notifyValidationEvent(schema, validationContext, errorResult)
            errorResult
        }
    }
    
    /**
     * Validates output variables for an activity
     */
    suspend fun validateOutput(
        schema: ActivitySchema,
        output: Map<String, Any>,
        context: ValidationContext? = null
    ): ValidationResult {
        val validationContext = context ?: ValidationContext(phase = ValidationPhase.OUTPUT)
        
        logger.debug("Validating output for schema: ${schema.id}")
        
        return try {
            val result = if (compositeValidator != null && config.useCompositeValidator) {
                compositeValidator!!.validateOutput(schema, output, validationContext)
            } else {
                // Find applicable validators
                val applicableValidators = validators.values.filter { it.canValidate(schema) }
                
                if (applicableValidators.isEmpty()) {
                    logger.warn("No applicable validators found for schema: ${schema.id}")
                    ValidationResult.success(metadata = mapOf("validatorsApplied" to 0))
                } else {
                    // Use the first applicable validator
                    val validator = applicableValidators.first()
                    validator.validateOutput(schema, output, validationContext)
                }
            }
            
            // Notify listeners
            notifyValidationEvent(schema, validationContext, result)
            
            result
            
        } catch (e: Exception) {
            logger.error("Error validating output for schema: ${schema.id}", e)
            val errorResult = ValidationResult.failure(
                ValidationError(
                    code = "VALIDATION_ERROR",
                    message = "Error during output validation: ${e.message}",
                    severity = ValidationSeverity.FATAL
                )
            )
            notifyValidationEvent(schema, validationContext, errorResult)
            errorResult
        }
    }
    
    /**
     * Validates a specific variable
     */
    suspend fun validateVariable(
        variable: VariableDefinition,
        value: Any?,
        context: ValidationContext? = null
    ): ValidationResult {
        val validationContext = context ?: ValidationContext(phase = ValidationPhase.INPUT)
        
        logger.debug("Validating variable: ${variable.name}")
        
        return try {
            val result = if (compositeValidator != null && config.useCompositeValidator) {
                compositeValidator!!.validateVariable(variable, value, validationContext)
            } else {
                // Find applicable validators by creating a mock schema
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
                
                val applicableValidators = validators.values.filter { it.canValidate(mockSchema) }
                
                if (applicableValidators.isEmpty()) {
                    logger.warn("No applicable validators found for variable: ${variable.name}")
                    ValidationResult.success(metadata = mapOf("validatorsApplied" to 0))
                } else {
                    // Use the first applicable validator
                    val validator = applicableValidators.first()
                    validator.validateVariable(variable, value, validationContext)
                }
            }
            
            // Notify listeners
            notifyValidationEvent(null, validationContext, result)
            
            result
            
        } catch (e: Exception) {
            logger.error("Error validating variable: ${variable.name}", e)
            val errorResult = ValidationResult.failure(
                ValidationError(
                    code = "VALIDATION_ERROR",
                    message = "Error during variable validation: ${e.message}",
                    path = variable.name,
                    severity = ValidationSeverity.FATAL
                )
            )
            notifyValidationEvent(null, validationContext, errorResult)
            errorResult
        }
    }
    
    /**
     * Gets all registered validators
     */
    fun getAllValidators(): List<ActivityValidator> {
        return validators.values.toList()
    }
    
    /**
     * Gets a specific validator by ID
     */
    fun getValidator(validatorId: String): ActivityValidator? {
        return validators[validatorId]
    }
    
    /**
     * Gets validators that can handle a specific schema
     */
    fun getApplicableValidators(schema: ActivitySchema): List<ActivityValidator> {
        return validators.values.filter { it.canValidate(schema) }
    }
    
    /**
     * Adds a validation event listener
     */
    fun addEventListener(listener: ValidationEventListener) {
        eventListeners.add(listener)
    }
    
    /**
     * Removes a validation event listener
     */
    fun removeEventListener(listener: ValidationEventListener) {
        eventListeners.remove(listener)
    }
    
    /**
     * Notifies listeners of validation events
     */
    private fun notifyValidationEvent(
        schema: ActivitySchema?,
        context: ValidationContext,
        result: ValidationResult
    ) {
        eventListeners.forEach { listener ->
            try {
                listener.onValidationEvent(schema, context, result)
            } catch (e: Exception) {
                logger.error("Error in validation event listener", e)
            }
        }
    }
    
    /**
     * Shuts down the validator manager
     */
    suspend fun shutdown() {
        logger.info("Shutting down ValidatorManager...")
        
        // Shutdown all validators
        validators.values.forEach { validator ->
            try {
                validator.shutdown()
            } catch (e: Exception) {
                logger.error("Error shutting down validator ${validator.validatorId}", e)
            }
        }
        
        logger.info("ValidatorManager shutdown complete")
    }
}

/**
 * Configuration for validator manager
 */
data class ValidatorManagerConfig(
    /** Whether to enable composite validation */
    val enableCompositeValidation: Boolean = true,
    
    /** Whether to use composite validator by default */
    val useCompositeValidator: Boolean = true,
    
    /** Configuration for composite validator */
    val compositeConfig: CompositeValidatorConfig = CompositeValidatorConfig(),
    
    /** Validation options */
    val validationOptions: ValidationOptions = ValidationOptions(),
    
    /** Whether to enable validation event logging */
    val enableEventLogging: Boolean = true
)

/**
 * Interface for validation event listeners
 */
interface ValidationEventListener {
    fun onValidationEvent(
        schema: ActivitySchema?,
        context: ValidationContext,
        result: ValidationResult
    )
} 