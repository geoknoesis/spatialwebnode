package com.geoknoesis.spatialweb.core.activity.validation

import com.geoknoesis.spatialweb.core.activity.model.*

import org.slf4j.LoggerFactory

/**
 * SHACL-based validator for RDF stores.
 *
 * This validator uses SHACL (Shapes Constraint Language) to validate
 * activity variables against constraints defined in RDF format.
 */
class ShaclRdfValidator(
    private val config: ShaclValidatorConfig = ShaclValidatorConfig()
) : ActivityValidator {
    
    private val logger = LoggerFactory.getLogger(ShaclRdfValidator::class.java)
    
    override val validatorId: String = "shacl-rdf-validator"
    override val name: String = "SHACL RDF Validator"
    override val version: String = "1.0.0"
    override val description: String = "Validates activities using SHACL constraints in RDF stores"
    
    override suspend fun validateSchema(schema: ActivitySchema): ValidationResult {
        val errors = mutableListOf<ValidationError>()
        
        try {
            // Validate schema structure
            if (schema.id.isBlank()) {
                errors.add(ValidationError(
                    code = "INVALID_SCHEMA_ID",
                    message = "Schema ID cannot be blank",
                    path = "id"
                ))
            }
            
            if (schema.name.isBlank()) {
                errors.add(ValidationError(
                    code = "INVALID_SCHEMA_NAME",
                    message = "Schema name cannot be blank",
                    path = "name"
                ))
            }
            
            // Validate input variables
            schema.inputSchema.forEach { (name, variable) ->
                val variableValidation = validateVariableDefinition(variable, "input.$name")
                if (!variableValidation.isValid) {
                    errors.addAll(variableValidation.errors)
                }
            }
            
            // Validate output variables
            schema.outputSchema.forEach { (name, variable) ->
                val variableValidation = validateVariableDefinition(variable, "output.$name")
                if (!variableValidation.isValid) {
                    errors.addAll(variableValidation.errors)
                }
            }
            
            // Store schema in RDF store for SHACL validation
            if (config.storeSchemasInRdf) {
                storeSchemaInRdf(schema)
            }
            
        } catch (e: Exception) {
            logger.error("Error validating schema: ${schema.id}", e)
            errors.add(ValidationError(
                code = "VALIDATION_ERROR",
                message = "Error during schema validation: ${e.message}",
                severity = ValidationSeverity.FATAL
            ))
        }
        
        return ValidationResult(errors.isEmpty(), errors)
    }
    
    override suspend fun validateInput(
        schema: ActivitySchema,
        input: Map<String, Any>,
        context: ValidationContext
    ): ValidationResult {
        val errors = mutableListOf<ValidationError>()
        val warnings = mutableListOf<ValidationWarning>()
        
        try {
            // Validate each input variable
            schema.inputSchema.forEach { (name, variable) ->
                val value = input[name]
                val variableValidation = validateVariable(variable, value, context.copy(
                    data = context.data + mapOf("variablePath" to "input.$name")
                ))
                
                if (!variableValidation.isValid) {
                    errors.addAll(variableValidation.errors)
                }
                warnings.addAll(variableValidation.warnings)
            }
            
            // Check for unknown variables
            if (!context.options.allowUnknown) {
                val unknownVariables = input.keys - schema.inputSchema.keys
                if (unknownVariables.isNotEmpty()) {
                    errors.add(ValidationError(
                        code = "UNKNOWN_VARIABLES",
                        message = "Unknown input variables: ${unknownVariables.joinToString(", ")}",
                        path = "input"
                    ))
                }
            }
            
            // Perform RDF-based SHACL validation if enabled
            if (config.enableRdfValidation) {
                val rdfValidation = validateInRdfStore(schema, input, context)
                if (!rdfValidation.isValid) {
                    errors.addAll(rdfValidation.errors)
                }
                warnings.addAll(rdfValidation.warnings)
            }
            
        } catch (e: Exception) {
            logger.error("Error validating input for schema: ${schema.id}", e)
            errors.add(ValidationError(
                code = "VALIDATION_ERROR",
                message = "Error during input validation: ${e.message}",
                severity = ValidationSeverity.FATAL
            ))
        }
        
        return ValidationResult(errors.isEmpty(), errors, warnings)
    }
    
    override suspend fun validateOutput(
        schema: ActivitySchema,
        output: Map<String, Any>,
        context: ValidationContext
    ): ValidationResult {
        val errors = mutableListOf<ValidationError>()
        val warnings = mutableListOf<ValidationWarning>()
        
        try {
            // Validate each output variable
            schema.outputSchema.forEach { (name, variable) ->
                val value = output[name]
                val variableValidation = validateVariable(variable, value, context.copy(
                    data = context.data + mapOf("variablePath" to "output.$name")
                ))
                
                if (!variableValidation.isValid) {
                    errors.addAll(variableValidation.errors)
                }
                warnings.addAll(variableValidation.warnings)
            }
            
            // Perform RDF-based SHACL validation if enabled
            if (config.enableRdfValidation) {
                val rdfValidation = validateInRdfStore(schema, output, context)
                if (!rdfValidation.isValid) {
                    errors.addAll(rdfValidation.errors)
                }
                warnings.addAll(rdfValidation.warnings)
            }
            
        } catch (e: Exception) {
            logger.error("Error validating output for schema: ${schema.id}", e)
            errors.add(ValidationError(
                code = "VALIDATION_ERROR",
                message = "Error during output validation: ${e.message}",
                severity = ValidationSeverity.FATAL
            ))
        }
        
        return ValidationResult(errors.isEmpty(), errors, warnings)
    }
    
    override suspend fun validateVariable(
        variable: VariableDefinition,
        value: Any?,
        context: ValidationContext
    ): ValidationResult {
        val errors = mutableListOf<ValidationError>()
        val warnings = mutableListOf<ValidationWarning>()
        
        try {
            // Validate variable definition
            val definitionValidation = validateVariableDefinition(variable, context.data["variablePath"] as? String ?: variable.name)
            if (!definitionValidation.isValid) {
                errors.addAll(definitionValidation.errors)
            }
            
            // Validate value against SHACL constraints
            variable.constraints?.let { shacl ->
                val shaclValidation = validateShaclConstraints(shacl, value, context)
                if (!shaclValidation.isValid) {
                    errors.addAll(shaclValidation.errors)
                }
                warnings.addAll(shaclValidation.warnings)
            }
            
            // Perform RDF-based validation if enabled
            if (config.enableRdfValidation && variable.constraints != null) {
                val rdfValidation = validateVariableInRdf(variable, value, context)
                if (!rdfValidation.isValid) {
                    errors.addAll(rdfValidation.errors)
                }
                warnings.addAll(rdfValidation.warnings)
            }
            
        } catch (e: Exception) {
            logger.error("Error validating variable: ${variable.name}", e)
            errors.add(ValidationError(
                code = "VALIDATION_ERROR",
                message = "Error during variable validation: ${e.message}",
                path = variable.name,
                severity = ValidationSeverity.FATAL
            ))
        }
        
        return ValidationResult(errors.isEmpty(), errors, warnings)
    }
    
    override fun canValidate(schema: ActivitySchema): Boolean {
        // This validator can handle any schema that has SHACL constraints
        return schema.inputSchema.values.any { it.constraints != null } ||
               schema.outputSchema.values.any { it.constraints != null }
    }
    
    override suspend fun initialize(): Boolean {
        return try {
            logger.info("Initializing SHACL RDF Validator")
            
            // Initialize RDF store connection
            if (config.enableRdfValidation) {
                initializeRdfStore()
            }
            
            // Load SHACL shapes if configured
            if (config.loadShaclShapes) {
                loadShaclShapes()
            }
            
            logger.info("SHACL RDF Validator initialized successfully")
            true
        } catch (e: Exception) {
            logger.error("Failed to initialize SHACL RDF Validator", e)
            false
        }
    }
    
    override suspend fun shutdown() {
        logger.info("Shutting down SHACL RDF Validator")
        // Cleanup RDF store connections if needed
    }
    
    override fun getMetadata(): Map<String, Any> {
        return mapOf(
            "validatorId" to validatorId,
            "name" to name,
            "version" to version,
            "description" to description,
            "config" to config,
            "rdfEnabled" to config.enableRdfValidation,
            "shapesLoaded" to config.loadShaclShapes
        )
    }
    
    /**
     * Validates a variable definition
     */
    private fun validateVariableDefinition(variable: VariableDefinition, path: String): ValidationResult {
        val errors = mutableListOf<ValidationError>()
        
        if (variable.id.isBlank()) {
            errors.add(ValidationError(
                code = "INVALID_VARIABLE_ID",
                message = "Variable ID cannot be blank",
                path = path
            ))
        }
        
        if (variable.name.isBlank()) {
            errors.add(ValidationError(
                code = "INVALID_VARIABLE_NAME",
                message = "Variable name cannot be blank",
                path = path
            ))
        }
        
        return ValidationResult(errors.isEmpty(), errors)
    }
    
    /**
     * Validates value against SHACL constraints
     */
    private fun validateShaclConstraints(
        shacl: ShaclPropertyShape,
        value: Any?,
        context: ValidationContext
    ): ValidationResult {
        val errors = mutableListOf<ValidationError>()
        val warnings = mutableListOf<ValidationWarning>()
        
        // Use the existing SHACL validation logic
        val validation = shacl.validate(value)
        if (!validation.isValid) {
            validation.errors.forEach { error ->
                errors.add(ValidationError(
                    code = "SHACL_CONSTRAINT_VIOLATION",
                    message = error,
                    path = shacl.path,
                    details = mapOf(
                        "constraint" to shacl.path,
                        "value" to (value?.toString() ?: "null")
                    )
                ))
            }
        }
        
        return ValidationResult(errors.isEmpty(), errors, warnings)
    }
    
    /**
     * Stores schema in RDF store for SHACL validation
     */
    private suspend fun storeSchemaInRdf(schema: ActivitySchema) {
        // Implementation would convert schema to RDF and store it
        logger.debug("Storing schema ${schema.id} in RDF store")
    }
    
    /**
     * Validates in RDF store
     */
    private suspend fun validateInRdfStore(
        schema: ActivitySchema,
        data: Map<String, Any>,
        context: ValidationContext
    ): ValidationResult {
        // Implementation would perform RDF-based SHACL validation
        logger.debug("Performing RDF-based validation for schema ${schema.id}")
        return ValidationResult.success()
    }
    
    /**
     * Validates variable in RDF store
     */
    private suspend fun validateVariableInRdf(
        variable: VariableDefinition,
        value: Any?,
        context: ValidationContext
    ): ValidationResult {
        // Implementation would perform RDF-based variable validation
        logger.debug("Performing RDF-based validation for variable ${variable.name}")
        return ValidationResult.success()
    }
    
    /**
     * Initializes RDF store connection
     */
    private suspend fun initializeRdfStore() {
        // Implementation would initialize connection to RDF store
        logger.debug("Initializing RDF store connection")
    }
    
    /**
     * Loads SHACL shapes
     */
    private suspend fun loadShaclShapes() {
        // Implementation would load SHACL shapes from RDF store
        logger.debug("Loading SHACL shapes")
    }
}

/**
 * Configuration for SHACL RDF validator
 */
data class ShaclValidatorConfig(
    /** Whether to enable RDF-based validation */
    val enableRdfValidation: Boolean = true,
    
    /** Whether to store schemas in RDF store */
    val storeSchemasInRdf: Boolean = true,
    
    /** Whether to load SHACL shapes */
    val loadShaclShapes: Boolean = true,
    
    /** RDF store connection configuration */
    val rdfStoreConfig: RdfStoreConfig = RdfStoreConfig(),
    
    /** SHACL validation options */
    val shaclOptions: ShaclOptions = ShaclOptions()
)

/**
 * RDF store configuration
 */
data class RdfStoreConfig(
    /** RDF store URL */
    val url: String = "http://localhost:8080/rdf4j-server/repositories/activities",
    
    /** Username for authentication */
    val username: String? = null,
    
    /** Password for authentication */
    val password: String? = null,
    
    /** Connection timeout in milliseconds */
    val connectionTimeout: Long = 30000,
    
    /** Read timeout in milliseconds */
    val readTimeout: Long = 60000
)

/**
 * SHACL validation options
 */
data class ShaclOptions(
    /** Whether to validate against SHACL shapes */
    val validateShapes: Boolean = true,
    
    /** Whether to allow unknown properties */
    val allowUnknownProperties: Boolean = false,
    
    /** Maximum validation depth */
    val maxDepth: Int = 10,
    
    /** Whether to include validation details */
    val includeDetails: Boolean = true
) 