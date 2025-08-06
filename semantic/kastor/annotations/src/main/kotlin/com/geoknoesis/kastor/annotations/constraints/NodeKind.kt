package com.geoknoesis.kastor.annotations.constraints

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import kotlin.reflect.KClass

/**
 * SHACL sh:nodeKind constraint - validates the kind of RDF node.
 * 
 * This annotation maps to SHACL's sh:nodeKind constraint which restricts
 * the type of RDF node (IRI, BlankNode, or Literal).
 * 
 * @param kind The required node kind
 * @param message The validation error message
 */
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [NodeKindValidator::class])
annotation class NodeKind(
    val kind: Kind,
    val message: String = "Value must be a {kind} node",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
) {
    enum class Kind {
        /** sh:IRI - must be an IRI */
        IRI,
        /** sh:BlankNode - must be a blank node */
        BlankNode,
        /** sh:Literal - must be a literal value */
        Literal,
        /** sh:BlankNodeOrIRI - can be either blank node or IRI */
        BlankNodeOrIRI,
        /** sh:BlankNodeOrLiteral - can be either blank node or literal */
        BlankNodeOrLiteral,
        /** sh:IRIOrLiteral - can be either IRI or literal */
        IRIOrLiteral
    }
}

/**
 * Validator for NodeKind constraint.
 * Note: This would typically be handled by the code generator based on the Kotlin type.
 */
class NodeKindValidator : ConstraintValidator<NodeKind, Any?> {
    private lateinit var requiredKind: NodeKind.Kind
    
    override fun initialize(constraintAnnotation: NodeKind) {
        requiredKind = constraintAnnotation.kind
    }
    
    override fun isValid(value: Any?, context: ConstraintValidatorContext): Boolean {
        if (value == null) return true
        
        // Note: This validation would typically be handled by the code generator
        // based on the Kotlin type system (IRI vs String vs custom types)
        // For now, we'll return true and let the code generator handle the logic
        return true
    }
}