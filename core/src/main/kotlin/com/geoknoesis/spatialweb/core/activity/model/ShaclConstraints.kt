package com.geoknoesis.spatialweb.core.activity.model

import kotlinx.serialization.Serializable

/**
 * SHACL (Shapes Constraint Language) Property Shape for variable validation.
 *
 * SHACL provides a rich set of constraints for validating RDF data.
 * This implementation provides a subset of common SHACL constraints
 * that can be used to validate activity variables.
 */
@Serializable
data class ShaclPropertyShape(
    /** The property path (e.g., "name", "age", "email") */
    val path: String,
    
    /** The expected data type */
    val dataType: ShaclDataType? = null,
    
    /** Whether the property is required (sh:minCount >= 1) */
    val required: Boolean = false,
    
    /** Minimum count of values */
    val minCount: Int? = null,
    
    /** Maximum count of values */
    val maxCount: Int? = null,
    
    /** Minimum length for string values */
    val minLength: Int? = null,
    
    /** Maximum length for string values */
    val maxLength: Int? = null,
    
    /** Regular expression pattern for string values */
    val pattern: String? = null,
    
    /** Minimum value for numeric values */
    val minInclusive: Double? = null,
    
    /** Maximum value for numeric values */
    val maxInclusive: Double? = null,
    
    /** Minimum value (exclusive) for numeric values */
    val minExclusive: Double? = null,
    
    /** Maximum value (exclusive) for numeric values */
    val maxExclusive: Double? = null,
    
    /** Allowed values (sh:in) */
    val allowedValues: List<Any>? = null,
    
    /** Disallowed values (sh:not) */
    val disallowedValues: List<Any>? = null,
    
    /** Custom validation message */
    val message: String? = null,
    
    /** Severity level of the constraint */
    val severity: ShaclSeverity = ShaclSeverity.VIOLATION,
    
    /** Additional SHACL properties */
    val additionalProperties: Map<String, Any> = emptyMap()
) {
    /**
     * Validates a value against this SHACL property shape
     */
    fun validate(value: Any?): ValidationResult {
        val errors = mutableListOf<String>()
        
        // Data type validation
        dataType?.let { type ->
            if (value != null && !type.isValid(value)) {
                errors.add("Value must be of type ${type.name}")
            }
        }
        
        // Required validation
        if (required && value == null) {
            errors.add("Property '$path' is required")
        }
        
        // Count validation (for arrays/collections)
        if (value is Collection<*>) {
            minCount?.let { min ->
                if (value.size < min) {
                    errors.add("Property '$path' must have at least $min values")
                }
            }
            maxCount?.let { max ->
                if (value.size > max) {
                    errors.add("Property '$path' must have at most $max values")
                }
            }
        }
        
        // String validation
        if (value is String) {
            minLength?.let { min ->
                if (value.length < min) {
                    errors.add("Property '$path' must be at least $min characters long")
                }
            }
            maxLength?.let { max ->
                if (value.length > max) {
                    errors.add("Property '$path' must be at most $max characters long")
                }
            }
            pattern?.let { regex ->
                if (!value.matches(regex.toRegex())) {
                    errors.add("Property '$path' must match pattern: $regex")
                }
            }
        }
        
        // Numeric validation
        if (value is Number) {
            val numValue = value.toDouble()
            
            minInclusive?.let { min ->
                if (numValue < min) {
                    errors.add("Property '$path' must be at least $min")
                }
            }
            maxInclusive?.let { max ->
                if (numValue > max) {
                    errors.add("Property '$path' must be at most $max")
                }
            }
            minExclusive?.let { min ->
                if (numValue <= min) {
                    errors.add("Property '$path' must be greater than $min")
                }
            }
            maxExclusive?.let { max ->
                if (numValue >= max) {
                    errors.add("Property '$path' must be less than $max")
                }
            }
        }
        
        // Allowed values validation
        allowedValues?.let { allowed ->
            if (!allowed.contains(value)) {
                errors.add("Property '$path' must be one of: ${allowed.joinToString(", ")}")
            }
        }
        
        // Disallowed values validation
        disallowedValues?.let { disallowed ->
            if (disallowed.contains(value)) {
                errors.add("Property '$path' cannot be: ${disallowed.joinToString(", ")}")
            }
        }
        
        return ValidationResult(errors.isEmpty(), errors)
    }
    
    /**
     * Creates a SHACL property shape from a map of properties
     */
    companion object {
        fun fromMap(properties: Map<String, Any>): ShaclPropertyShape {
            return ShaclPropertyShape(
                path = properties["path"] as? String ?: "",
                dataType = (properties["dataType"] as? String)?.let { ShaclDataType.valueOf(it) },
                required = properties["required"] as? Boolean ?: false,
                minCount = properties["minCount"] as? Int,
                maxCount = properties["maxCount"] as? Int,
                minLength = properties["minLength"] as? Int,
                maxLength = properties["maxLength"] as? Int,
                pattern = properties["pattern"] as? String,
                minInclusive = properties["minInclusive"] as? Double,
                maxInclusive = properties["maxInclusive"] as? Double,
                minExclusive = properties["minExclusive"] as? Double,
                maxExclusive = properties["maxExclusive"] as? Double,
                allowedValues = properties["allowedValues"] as? List<Any>,
                disallowedValues = properties["disallowedValues"] as? List<Any>,
                message = properties["message"] as? String,
                severity = (properties["severity"] as? String)?.let { ShaclSeverity.valueOf(it) } ?: ShaclSeverity.VIOLATION
            )
        }
    }
}

/**
 * SHACL data types
 */
@Serializable
enum class ShaclDataType(val uri: String) {
    STRING("http://www.w3.org/2001/XMLSchema#string"),
    INTEGER("http://www.w3.org/2001/XMLSchema#integer"),
    DECIMAL("http://www.w3.org/2001/XMLSchema#decimal"),
    DOUBLE("http://www.w3.org/2001/XMLSchema#double"),
    BOOLEAN("http://www.w3.org/2001/XMLSchema#boolean"),
    DATE("http://www.w3.org/2001/XMLSchema#date"),
    DATETIME("http://www.w3.org/2001/XMLSchema#dateTime"),
    TIME("http://www.w3.org/2001/XMLSchema#time"),
    URI("http://www.w3.org/2001/XMLSchema#anyURI"),
    LANG_STRING("http://www.w3.org/1999/02/22-rdf-syntax-ns#langString");
    
    fun isValid(value: Any): Boolean {
        return when (this) {
            STRING -> value is String
            INTEGER -> value is Int || value is Long
            DECIMAL -> value is Number
            DOUBLE -> value is Double || value is Float
            BOOLEAN -> value is Boolean
            DATE -> value is String && value.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))
            DATETIME -> value is String && value.matches(Regex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}"))
            TIME -> value is String && value.matches(Regex("\\d{2}:\\d{2}:\\d{2}"))
            URI -> value is String && value.matches(Regex("^[a-zA-Z][a-zA-Z0-9+.-]*:.*"))
            LANG_STRING -> value is String
        }
    }
}

/**
 * SHACL severity levels
 */
@Serializable
enum class ShaclSeverity {
    VIOLATION,   // sh:Violation
    WARNING,     // sh:Warning
    INFO         // sh:Info
}

/**
 * SHACL Node Shape for complex object validation
 */
@Serializable
data class ShaclNodeShape(
    /** The shape identifier */
    val id: String,
    
    /** The shape type */
    val type: String = "http://www.w3.org/ns/shacl#NodeShape",
    
    /** Target class for this shape */
    val targetClass: String? = null,
    
    /** Property shapes */
    val properties: List<ShaclPropertyShape> = emptyList(),
    
    /** Whether the shape is closed (no additional properties allowed) */
    val closed: Boolean = false,
    
    /** Custom validation message */
    val message: String? = null,
    
    /** Severity level */
    val severity: ShaclSeverity = ShaclSeverity.VIOLATION
) {
    /**
     * Validates an object against this node shape
     */
    fun validate(obj: Map<String, Any>): ValidationResult {
        val errors = mutableListOf<String>()
        
        // Validate each property
        properties.forEach { propertyShape ->
            val value = obj[propertyShape.path]
            val validation = propertyShape.validate(value ?: null)
            if (!validation.isValid) {
                errors.addAll(validation.errors)
            }
        }
        
        // Check for closed shape
        if (closed) {
            val allowedPaths = properties.map { it.path }.toSet()
            val extraPaths = obj.keys - allowedPaths
            if (extraPaths.isNotEmpty()) {
                errors.add("Additional properties not allowed: ${extraPaths.joinToString(", ")}")
            }
        }
        
        return ValidationResult(errors.isEmpty(), errors)
    }
} 