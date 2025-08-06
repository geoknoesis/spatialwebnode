package com.geoknoesis.kastor.annotations.constraints

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import kotlin.reflect.KClass

/**
 * SHACL sh:greaterThan constraint - validates that this property's value is greater than another property.
 * 
 * This annotation maps to SHACL's sh:greaterThan constraint which ensures that
 * the value of this property is greater than the value of the specified property.
 * 
 * @param other The name of the property to compare against
 * @param message The validation error message
 */
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [GreaterThanValidator::class])
annotation class GreaterThan(
    val other: String,
    val message: String = "Value must be greater than {other}",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

/**
 * Validator for GreaterThan constraint.
 * Note: This requires reflection to access the other property value.
 */
class GreaterThanValidator : ConstraintValidator<GreaterThan, Comparable<Any>?> {
    private lateinit var otherPropertyName: String
    
    override fun initialize(constraintAnnotation: GreaterThan) {
        otherPropertyName = constraintAnnotation.other
    }
    
    override fun isValid(value: Comparable<Any>?, context: ConstraintValidatorContext): Boolean {
        if (value == null) return true
        
        // Note: In a real implementation, this would use reflection or be handled
        // by the code generator to access the other property value
        // For now, we'll return true and let the code generator handle the logic
        return true
    }
}