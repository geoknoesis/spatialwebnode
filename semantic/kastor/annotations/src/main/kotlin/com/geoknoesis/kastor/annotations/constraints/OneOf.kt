package com.geoknoesis.kastor.annotations.constraints

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import kotlin.reflect.KClass

/**
 * SHACL sh:in constraint - validates that the value is one of the allowed values.
 * 
 * This annotation maps to SHACL's sh:in constraint which restricts values
 * to a specific enumeration.
 * 
 * @param values The allowed values
 * @param message The validation error message
 */
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [OneOfValidator::class])
annotation class OneOf(
    val values: Array<String>,
    val message: String = "Value must be one of: {values}",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

/**
 * Validator for OneOf constraint.
 */
class OneOfValidator : ConstraintValidator<OneOf, String?> {
    private lateinit var allowedValues: Set<String>
    
    override fun initialize(constraintAnnotation: OneOf) {
        allowedValues = constraintAnnotation.values.toSet()
    }
    
    override fun isValid(value: String?, context: ConstraintValidatorContext): Boolean {
        return value == null || value in allowedValues
    }
}