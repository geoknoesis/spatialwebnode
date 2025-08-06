# Pluggable Activity Validation System

The Activity Validation System provides a flexible, pluggable architecture for validating activity schemas, inputs, outputs, and variables. It supports multiple validation strategies including SHACL-based validation in RDF stores, SQL triggers, and custom validation logic.

## Overview

The validation system consists of several key components:

- **ActivityValidator**: Pluggable interface for different validation strategies
- **ValidatorManager**: Central manager for multiple validators
- **CompositeValidator**: Chains multiple validators together
- **ShaclRdfValidator**: SHACL-based validation for RDF stores
- **ValidationContext**: Context information for validation operations
- **ValidationResult**: Rich validation results with errors and warnings

## Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│ ValidatorManager│    │ ActivityValidator│    │ ValidationResult│
│                 │    │                 │    │                 │
│ - SPI Discovery │◄──►│ - Schema        │◄──►│ - Errors        │
│ - Lifecycle     │    │ - Input/Output  │    │ - Warnings      │
│ - Routing       │    │ - Variables     │    │ - Metadata      │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         ▼                       ▼                       ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│CompositeValidator│    │ShaclRdfValidator│    │ValidationContext│
│                 │    │                 │    │                 │
│ - Chaining      │    │ - SHACL         │    │ - Phase         │
│ - Strategies    │    │ - RDF Store     │    │ - Options       │
│ - Error Handling│    │ - Constraints   │    │ - Data          │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

## Key Features

### 1. Pluggable Architecture
- **Multiple Validators**: Support for different validation strategies
- **SPI Discovery**: Automatic discovery of validators using Java SPI
- **Lifecycle Management**: Initialization and shutdown of validators
- **Error Handling**: Configurable error handling strategies

### 2. SHACL-Based Validation
- **RDF Integration**: Native support for RDF stores
- **Rich Constraints**: Data types, patterns, ranges, and custom validation
- **Semantic Validation**: W3C SHACL standard compliance
- **Flexible Rules**: Support for complex validation scenarios

### 3. Composite Validation
- **Multiple Strategies**: Chain different validators together
- **Configurable Behavior**: Stop on first failure, allow errors, etc.
- **Validation Strategies**: ALL, FIRST_SUCCESS, FIRST_FAILURE, PARALLEL, PRIORITY
- **Error Aggregation**: Collect errors from all validators

### 4. Rich Validation Results
- **Detailed Errors**: Error codes, messages, paths, and severity
- **Warnings**: Non-blocking validation issues
- **Metadata**: Additional validation information
- **Context**: Validation phase and options

## Quick Start

### 1. Basic Validation Setup

```kotlin
// Create validator manager
val validatorManager = ValidatorManager()

// Validate a schema
val schema = createActivitySchema()
val result = validatorManager.validateSchema(schema)

if (result.isValid) {
    println("Schema is valid")
} else {
    result.errors.forEach { error ->
        println("Error: ${error.message} at ${error.path}")
    }
}
```

### 2. SHACL RDF Validator

```kotlin
// Create SHACL validator with RDF store
val shaclConfig = ShaclValidatorConfig(
    enableRdfValidation = true,
    storeSchemasInRdf = true,
    rdfStoreConfig = RdfStoreConfig(
        url = "http://localhost:8080/rdf4j-server/repositories/activities"
    )
)

val shaclValidator = ShaclRdfValidator(persistence, shaclConfig)
validatorManager.registerValidator(shaclValidator)
```

### 3. Composite Validation

```kotlin
// Create composite validator configuration
val compositeConfig = CompositeValidatorConfig(
    stopOnFirstFailure = false,
    allowErrors = false,
    strategy = ValidationStrategy.ALL
)

val validatorConfig = ValidatorManagerConfig(
    enableCompositeValidation = true,
    useCompositeValidator = true,
    compositeConfig = compositeConfig
)

val validatorManager = ValidatorManager(validatorConfig)
```

### 4. Input Validation

```kotlin
// Validate input variables
val input = mapOf(
    "username" to "john_doe",
    "email" to "john@example.com",
    "age" to 25
)

val result = validatorManager.validateInput(schema, input, ValidationContext(
    phase = ValidationPhase.INPUT,
    validatedBy = "user123"
))

if (!result.isValid) {
    result.errors.forEach { error ->
        println("Input error: ${error.message}")
    }
}
```

## Components

### ActivityValidator Interface

The core interface for all validators:

```kotlin
interface ActivityValidator {
    val validatorId: String
    val name: String
    val version: String
    val description: String
    
    suspend fun validateSchema(schema: ActivitySchema): ValidationResult
    suspend fun validateInput(schema: ActivitySchema, input: Map<String, Any>, context: ValidationContext): ValidationResult
    suspend fun validateOutput(schema: ActivitySchema, output: Map<String, Any>, context: ValidationContext): ValidationResult
    suspend fun validateVariable(variable: VariableDefinition, value: Any?, context: ValidationContext): ValidationResult
    
    fun canValidate(schema: ActivitySchema): Boolean
    suspend fun initialize(): Boolean
    suspend fun shutdown()
    fun getMetadata(): Map<String, Any>
}
```

### ValidationContext

Provides context for validation operations:

```kotlin
data class ValidationContext(
    val activity: Activity? = null,
    val validatedBy: String? = null,
    val phase: ValidationPhase = ValidationPhase.INPUT,
    val data: Map<String, Any> = emptyMap(),
    val options: ValidationOptions = ValidationOptions()
)
```

### ValidationResult

Rich validation results with detailed information:

```kotlin
data class ValidationResult(
    val isValid: Boolean,
    val errors: List<ValidationError> = emptyList(),
    val warnings: List<ValidationWarning> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)
```

### ValidationError

Detailed error information:

```kotlin
data class ValidationError(
    val code: String,
    val message: String,
    val path: String? = null,
    val severity: ValidationSeverity = ValidationSeverity.ERROR,
    val details: Map<String, Any> = emptyMap(),
    val timestamp: Long = System.currentTimeMillis()
)
```

## SHACL Validation

### SHACL Property Shapes

Rich constraint definitions using SHACL:

```kotlin
val emailConstraints = ShaclPropertyShape(
    path = "email",
    dataType = ShaclDataType.STRING,
    required = true,
    pattern = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
    message = "Must be a valid email address"
)

val ageConstraints = ShaclPropertyShape(
    path = "age",
    dataType = ShaclDataType.INTEGER,
    minInclusive = 18.0,
    maxInclusive = 65.0,
    message = "Age must be between 18 and 65"
)
```

### SHACL Data Types

Supported data types based on XSD:

```kotlin
enum class ShaclDataType {
    STRING,      // xsd:string
    INTEGER,     // xsd:integer
    DECIMAL,     // xsd:decimal
    DOUBLE,      // xsd:double
    BOOLEAN,     // xsd:boolean
    DATE,        // xsd:date
    DATETIME,    // xsd:dateTime
    TIME,        // xsd:time
    URI,         // xsd:anyURI
    LANG_STRING  // rdf:langString
}
```

### Common SHACL Constraints

```kotlin
// String constraints
ShaclPropertyShape(
    path = "name",
    dataType = ShaclDataType.STRING,
    required = true,
    minLength = 1,
    maxLength = 100,
    pattern = "^[a-zA-Z ]+$"
)

// Numeric constraints
ShaclPropertyShape(
    path = "score",
    dataType = ShaclDataType.DECIMAL,
    minInclusive = 0.0,
    maxInclusive = 100.0
)

// Collection constraints
ShaclPropertyShape(
    path = "tags",
    minCount = 1,
    maxCount = 10
)

// Allowed values
ShaclPropertyShape(
    path = "status",
    dataType = ShaclDataType.STRING,
    allowedValues = listOf("active", "inactive", "pending")
)
```

## Composite Validation

### Validation Strategies

```kotlin
enum class ValidationStrategy {
    ALL,           // Apply all validators
    FIRST_SUCCESS, // Apply validators until first success
    FIRST_FAILURE, // Apply validators until first failure
    PARALLEL,      // Apply validators in parallel
    PRIORITY       // Apply validators based on priority
}
```

### Composite Configuration

```kotlin
val compositeConfig = CompositeValidatorConfig(
    stopOnFirstFailure = false,    // Continue after first failure
    allowErrors = false,           // Don't allow any errors
    requireAllValidators = false,  // Don't require all validators
    strategy = ValidationStrategy.ALL
)
```

## Integration with ActivityManager

The validation system integrates seamlessly with the ActivityManager:

```kotlin
// ActivityManager automatically uses validation
val activityManager = ActivityManager(persistence, ActivityManagerConfig(
    validatorConfig = ValidatorManagerConfig(
        enableCompositeValidation = true,
        useCompositeValidator = true
    )
))

// Validation happens automatically during:
// - Schema registration
// - Activity start (input validation)
// - Activity completion (output validation)
```

## Event Handling

### Validation Event Listener

```kotlin
validatorManager.addEventListener(object : ValidationEventListener {
    override fun onValidationEvent(
        schema: ActivitySchema?,
        context: ValidationContext,
        result: ValidationResult
    ) {
        val schemaName = schema?.name ?: "Variable"
        val phase = context.phase.name.lowercase()
        
        if (!result.isValid) {
            logger.warn("Validation failed for $schemaName ($phase):")
            result.errors.forEach { error ->
                logger.warn("  - ${error.message} (${error.path})")
            }
        }
    }
})
```

## Configuration

### ValidatorManagerConfig

```kotlin
data class ValidatorManagerConfig(
    val enableCompositeValidation: Boolean = true,
    val useCompositeValidator: Boolean = true,
    val compositeConfig: CompositeValidatorConfig = CompositeValidatorConfig(),
    val validationOptions: ValidationOptions = ValidationOptions(),
    val enableEventLogging: Boolean = true
)
```

### ValidationOptions

```kotlin
data class ValidationOptions(
    val strict: Boolean = true,           // Strict validation
    val allowUnknown: Boolean = false,    // Allow unknown variables
    val recursive: Boolean = true,        // Recursive validation
    val maxDepth: Int = 10,              // Maximum validation depth
    val customRules: Map<String, Any> = emptyMap()
)
```

## Best Practices

### 1. Validator Implementation
- Implement proper error handling and logging
- Provide meaningful error messages and codes
- Support validation context and options
- Handle initialization and shutdown gracefully

### 2. SHACL Constraints
- Use appropriate data types for variables
- Define meaningful validation patterns
- Set reasonable min/max values
- Provide helpful error messages

### 3. Composite Validation
- Choose appropriate validation strategy
- Configure error handling behavior
- Monitor validation performance
- Handle validator dependencies

### 4. Integration
- Use validation in activity lifecycle
- Handle validation events appropriately
- Configure validation options per use case
- Monitor validation statistics

## Examples

See `ValidationExample.kt` for comprehensive examples demonstrating:

- Setting up different validators
- Configuring validation strategies
- Using validation in activity lifecycle
- Handling validation events
- Testing various validation scenarios

## Future Enhancements

- **SQL Trigger Validator**: Database trigger-based validation
- **External Service Validator**: External validation service integration
- **Parallel Validation**: Parallel execution of validators
- **Validation Caching**: Cache validation results for performance
- **Validation Rules Engine**: Rule-based validation system
- **Validation Analytics**: Advanced validation metrics and reporting 