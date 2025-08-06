package com.geoknoesis.spatialweb.core.activity.validation

import com.geoknoesis.spatialweb.core.activity.model.*

import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

/**
 * Comprehensive example demonstrating the pluggable validation system.
 *
 * This example shows:
 * - Setting up different validators (SHACL RDF, Composite)
 * - Configuring validation strategies
 * - Using validation in activity lifecycle
 * - Handling validation events
 */
object ValidationExample {
    
    private val logger = LoggerFactory.getLogger(ValidationExample::class.java)
    
    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        logger.info("Starting Validation Example")
        

        
        // Create validator manager with configuration
        val validatorConfig = ValidatorManagerConfig(
            enableCompositeValidation = true,
            useCompositeValidator = true,
            compositeConfig = CompositeValidatorConfig(
                stopOnFirstFailure = false,
                allowErrors = false,
                strategy = ValidationStrategy.ALL
            ),
            validationOptions = ValidationOptions(
                strict = true,
                allowUnknown = false,
                recursive = true
            )
        )
        
        val validatorManager = ValidatorManager(validatorConfig)
        
        // Add validation event listener
        validatorManager.addEventListener(createValidationEventListener())
        
        // Create activity schemas with SHACL constraints
        val schemas = createExampleSchemas()
        
        // Test schema validation
        logger.info("Testing schema validation...")
        schemas.forEach { schema ->
            val validation = validatorManager.validateSchema(schema)
            logger.info("Schema ${schema.name}: ${if (validation.isValid) "✅ Valid" else "❌ Invalid"}")
            if (!validation.isValid) {
                validation.errors.forEach { error ->
                    logger.error("  - ${error.message} (${error.path})")
                }
            }
        }
        
        // Test input validation
        logger.info("Testing input validation...")
        val testInputs = createTestInputs()
        
        testInputs.forEach { (schemaId, input, description) ->
            val schema = schemas.find { it.id == schemaId }
            if (schema != null) {
                val validation = validatorManager.validateInput(schema, input)
                logger.info("Input validation for $description: ${if (validation.isValid) "✅ Valid" else "❌ Invalid"}")
                if (!validation.isValid) {
                    validation.errors.forEach { error ->
                        logger.error("  - ${error.message} (${error.path})")
                    }
                }
            }
        }
        
        // Test variable validation
        logger.info("Testing variable validation...")
        val testVariables = createTestVariables()
        
        testVariables.forEach { (variable, value, description) ->
            val validation = validatorManager.validateVariable(variable, value)
            logger.info("Variable validation for $description: ${if (validation.isValid) "✅ Valid" else "❌ Invalid"}")
            if (!validation.isValid) {
                validation.errors.forEach { error ->
                    logger.error("  - ${error.message} (${error.path})")
                }
            }
        }
        
        // Show validator information
        logger.info("Available validators:")
        validatorManager.getAllValidators().forEach { validator ->
            logger.info("  - ${validator.name} (${validator.validatorId}) - ${validator.description}")
            logger.info("    Metadata: ${validator.getMetadata()}")
        }
        
        // Shutdown
        validatorManager.shutdown()
        logger.info("Validation Example completed")
    }
    
    /**
     * Creates example activity schemas with SHACL constraints
     */
    private fun createExampleSchemas(): List<ActivitySchema> {
        return listOf(
            // User registration schema
            ActivitySchema(
                id = "user-registration",
                name = "User Registration",
                description = "Registers a new user in the system",
                version = "1.0.0",
                category = "user-management",
                executorPluginId = "simple-executor",
                inputSchema = mapOf(
                    "username" to VariableDefinition(
                        id = "username-var",
                        name = "username",
                        description = "User's unique username",
                        constraints = ShaclPropertyShape(
                            path = "username",
                            dataType = ShaclDataType.STRING,
                            required = true,
                            minLength = 3,
                            maxLength = 20,
                            pattern = "^[a-zA-Z0-9_]+$",
                            message = "Username must be 3-20 characters, alphanumeric and underscore only"
                        )
                    ),
                    "email" to VariableDefinition(
                        id = "email-var",
                        name = "email",
                        description = "User's email address",
                        constraints = ShaclPropertyShape(
                            path = "email",
                            dataType = ShaclDataType.STRING,
                            required = true,
                            pattern = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
                            message = "Must be a valid email address"
                        )
                    ),
                    "age" to VariableDefinition(
                        id = "age-var",
                        name = "age",
                        description = "User's age",
                        constraints = ShaclPropertyShape(
                            path = "age",
                            dataType = ShaclDataType.INTEGER,
                            minInclusive = 13.0,
                            maxInclusive = 120.0,
                            message = "Age must be between 13 and 120"
                        )
                    ),
                    "preferences" to VariableDefinition(
                        id = "preferences-var",
                        name = "preferences",
                        description = "User preferences",
                        constraints = ShaclPropertyShape(
                            path = "preferences",
                            minCount = 1,
                            maxCount = 10
                        )
                    )
                ),
                outputSchema = mapOf(
                    "userId" to VariableDefinition(
                        id = "user-id-var",
                        name = "userId",
                        description = "Generated user ID",
                        constraints = ShaclPropertyShape(
                            path = "userId",
                            dataType = ShaclDataType.STRING,
                            required = true,
                            minLength = 1
                        )
                    ),
                    "status" to VariableDefinition(
                        id = "status-var",
                        name = "status",
                        description = "Registration status",
                        constraints = ShaclPropertyShape(
                            path = "status",
                            dataType = ShaclDataType.STRING,
                            allowedValues = listOf("active", "pending", "suspended")
                        )
                    )
                ),
                constraints = ExecutionConstraints(
                    maxExecutionTime = 60,
                    allowParallel = true
                ),
                metadata = mapOf(
                    "tags" to "user,registration,validation"
                )
            ),
            
            // Data processing schema
            ActivitySchema(
                id = "data-processing",
                name = "Data Processing",
                description = "Processes data with validation",
                version = "1.0.0",
                category = "data-processing",
                executorPluginId = "simple-executor",
                inputSchema = mapOf(
                    "data" to VariableDefinition(
                        id = "data-var",
                        name = "data",
                        description = "Data to process",
                        constraints = ShaclPropertyShape(
                            path = "data",
                            required = true,
                            minCount = 1,
                            maxCount = 10000
                        )
                    ),
                    "operation" to VariableDefinition(
                        id = "operation-var",
                        name = "operation",
                        description = "Processing operation",
                        constraints = ShaclPropertyShape(
                            path = "operation",
                            dataType = ShaclDataType.STRING,
                            required = true,
                            allowedValues = listOf("transform", "filter", "aggregate", "sort", "validate")
                        )
                    ),
                    "batchSize" to VariableDefinition(
                        id = "batch-size-var",
                        name = "batchSize",
                        description = "Batch size for processing",
                        constraints = ShaclPropertyShape(
                            path = "batchSize",
                            dataType = ShaclDataType.INTEGER,
                            minInclusive = 1.0,
                            maxInclusive = 1000.0
                        )
                    )
                ),
                outputSchema = mapOf(
                    "processedCount" to VariableDefinition(
                        id = "processed-count-var",
                        name = "processedCount",
                        description = "Number of items processed",
                        constraints = ShaclPropertyShape(
                            path = "processedCount",
                            dataType = ShaclDataType.INTEGER,
                            minInclusive = 0.0
                        )
                    ),
                    "results" to VariableDefinition(
                        id = "results-var",
                        name = "results",
                        description = "Processing results",
                        constraints = ShaclPropertyShape(
                            path = "results"
                        )
                    )
                ),
                constraints = ExecutionConstraints(
                    maxExecutionTime = 300,
                    allowParallel = true
                ),
                metadata = mapOf(
                    "tags" to "data,processing,batch"
                )
            )
        )
    }
    
    /**
     * Creates test inputs for validation
     */
    private fun createTestInputs(): List<Triple<String, Map<String, Any>, String>> {
        return listOf(
            // Valid user registration
            Triple(
                "user-registration",
                mapOf(
                    "username" to "john_doe",
                    "email" to "john@example.com",
                    "age" to 25,
                    "preferences" to listOf("newsletter", "notifications")
                ),
                "Valid user registration"
            ),
            
            // Invalid user registration - invalid username
            Triple(
                "user-registration",
                mapOf(
                    "username" to "jo", // Too short
                    "email" to "john@example.com",
                    "age" to 25
                ),
                "Invalid username (too short)"
            ),
            
            // Invalid user registration - invalid email
            Triple(
                "user-registration",
                mapOf(
                    "username" to "john_doe",
                    "email" to "invalid-email",
                    "age" to 25
                ),
                "Invalid email format"
            ),
            
            // Invalid user registration - invalid age
            Triple(
                "user-registration",
                mapOf(
                    "username" to "john_doe",
                    "email" to "john@example.com",
                    "age" to 5 // Too young
                ),
                "Invalid age (too young)"
            ),
            
            // Valid data processing
            Triple(
                "data-processing",
                mapOf(
                    "data" to listOf("item1", "item2", "item3"),
                    "operation" to "transform",
                    "batchSize" to 10
                ),
                "Valid data processing"
            ),
            
            // Invalid data processing - invalid operation
            Triple(
                "data-processing",
                mapOf(
                    "data" to listOf("item1", "item2"),
                    "operation" to "invalid-operation",
                    "batchSize" to 10
                ),
                "Invalid operation"
            )
        )
    }
    
    /**
     * Creates test variables for validation
     */
    private fun createTestVariables(): List<Triple<VariableDefinition, Any?, String>> {
        return listOf(
            // Valid email
            Triple(
                VariableDefinition(
                    id = "test-email",
                    name = "email",
                    description = "Test email",
                    constraints = ShaclPropertyShape(
                        path = "email",
                        dataType = ShaclDataType.STRING,
                        required = true,
                        pattern = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
                    )
                ),
                "test@example.com",
                "Valid email"
            ),
            
            // Invalid email
            Triple(
                VariableDefinition(
                    id = "test-email",
                    name = "email",
                    description = "Test email",
                    constraints = ShaclPropertyShape(
                        path = "email",
                        dataType = ShaclDataType.STRING,
                        required = true,
                        pattern = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
                    )
                ),
                "invalid-email",
                "Invalid email format"
            ),
            
            // Valid age
            Triple(
                VariableDefinition(
                    id = "test-age",
                    name = "age",
                    description = "Test age",
                    constraints = ShaclPropertyShape(
                        path = "age",
                        dataType = ShaclDataType.INTEGER,
                        minInclusive = 18.0,
                        maxInclusive = 65.0
                    )
                ),
                25,
                "Valid age"
            ),
            
            // Invalid age
            Triple(
                VariableDefinition(
                    id = "test-age",
                    name = "age",
                    description = "Test age",
                    constraints = ShaclPropertyShape(
                        path = "age",
                        dataType = ShaclDataType.INTEGER,
                        minInclusive = 18.0,
                        maxInclusive = 65.0
                    )
                ),
                15,
                "Invalid age (too young)"
            )
        )
    }
    
    /**
     * Creates a validation event listener
     */
    private fun createValidationEventListener(): ValidationEventListener {
        return object : ValidationEventListener {
            override fun onValidationEvent(
                schema: ActivitySchema?,
                context: ValidationContext,
                result: ValidationResult
            ) {
                val schemaName = schema?.name ?: "Variable"
                val phase = context.phase.name.lowercase()
                
                logger.debug("Validation event: $schemaName ($phase) - ${if (result.isValid) "Valid" else "Invalid"}")
                
                if (!result.isValid) {
                    logger.warn("Validation failed for $schemaName ($phase):")
                    result.errors.forEach { error ->
                        logger.warn("  - ${error.message} (${error.path})")
                    }
                }
                
                if (result.warnings.isNotEmpty()) {
                    logger.info("Validation warnings for $schemaName ($phase):")
                    result.warnings.forEach { warning ->
                        logger.info("  - ${warning.message} (${warning.path})")
                    }
                }
            }
        }
    }
} 