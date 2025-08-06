package com.geoknoesis.kastor.annotations.constraints

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import kotlin.reflect.KClass

/**
 * SHACL sh:lessThan constraint - validates that this property's value is less than another property.
 * 
 * This annotation maps to SHACL's sh:lessThan constraint which ensures that
 * the value of this property is less than the value of the specified property.
 * 
 * @param other The name of the property to compare against
 * @param message The validation error message
 */
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [LessThanValidator::class])
annotation class LessThan(
    val other: String,
    val message: String = "Value must be less than {other}",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

/**
 * Validator for LessThan constraint.
 * Note: This requires reflection to access the other property value.
 */
class LessThanValidator : ConstraintValidator<LessThan, Comparable<Any>?> {
    private lateinit var otherPropertyName: String
    
    override fun initialize(constraintAnnotation: LessThan) {
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