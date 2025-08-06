package com.geoknoesis.spatialweb.core.activity.executor

import com.geoknoesis.spatialweb.core.activity.model.Activity
import com.geoknoesis.spatialweb.core.activity.model.ActivitySchema
import com.geoknoesis.spatialweb.core.activity.model.ActivityStatus
import com.geoknoesis.spatialweb.core.activity.model.ShaclDataType
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.slf4j.LoggerFactory

/**
 * Simple example activity executor that demonstrates basic activity execution.
 *
 * This executor can handle activities with simple input/output variables and
 * simulates work by adding delays and progress updates.
 */
class SimpleActivityExecutor : ActivityExecutor {
    
    private val logger = LoggerFactory.getLogger(SimpleActivityExecutor::class.java)
    
    override val pluginId: String = "simple-executor"
    override val name: String = "Simple Activity Executor"
    override val version: String = "1.0.0"
    override val description: String = "A simple activity executor for demonstration purposes"
    
    override fun canExecute(schema: ActivitySchema): Boolean {
        // This executor can handle any schema that has the simple-executor plugin ID
        return schema.executorPluginId == pluginId
    }
    
    override fun validateInput(schema: ActivitySchema, input: Map<String, Any>): ValidationResult {
        val errors = mutableListOf<String>()
        
        // Check required variables
        schema.inputSchema.forEach { (name, definition) ->
            if (definition.constraints?.required == true && !input.containsKey(name)) {
                errors.add("Required variable '$name' is missing")
            }
        }
        
        // Check variable constraints
        input.forEach { (name, value) ->
            val definition = schema.inputSchema[name]
            if (definition != null) {
                val validation = definition.validate(value)
                if (!validation.isValid) {
                    errors.add("Variable '$name': ${validation.errors.joinToString(", ")}")
                }
            } else {
                errors.add("Unknown variable '$name'")
            }
        }
        
        return if (errors.isEmpty()) {
            ValidationResult.success()
        } else {
            ValidationResult.failure(errors)
        }
    }
    
    override suspend fun execute(
        activity: Activity,
        schema: ActivitySchema,
        context: ExecutionContext
    ): Flow<ExecutionEvent> = flow {
        logger.info("Starting simple activity execution: ${activity.id}")
        
        // Emit started event
        emit(ExecutionEvent.Started(activity.id))
        
        try {
            // Simulate work with progress updates
            val totalSteps = 10
            for (step in 1..totalSteps) {
                delay(100) // Simulate work
                
                val progress = step.toDouble() / totalSteps
                emit(ExecutionEvent.Progress(
                    activityId = activity.id,
                    progress = progress,
                    message = "Processing step $step of $totalSteps"
                ))
            }
            
            // Generate output based on input
            val output = generateOutput(activity.input, schema)
            
            // Emit output event
            emit(ExecutionEvent.Output(activity.id, output))
            
            // Emit completed event
            emit(ExecutionEvent.Completed(
                activityId = activity.id,
                output = output,
                duration = System.currentTimeMillis() - context.executionId.hashCode()
            ))
            
            logger.info("Completed simple activity execution: ${activity.id}")
            
        } catch (e: Exception) {
            logger.error("Simple activity execution failed: ${activity.id}", e)
            emit(ExecutionEvent.Failed(
                activityId = activity.id,
                error = e.message ?: "Unknown error",
                details = e.toString()
            ))
        }
    }
    
    override suspend fun cancel(activityId: String, context: ExecutionContext): Boolean {
        logger.info("Cancelling simple activity: $activityId")
        // In a real implementation, you would implement cancellation logic
        return true
    }
    
    override suspend fun pause(activityId: String, context: ExecutionContext): Boolean {
        logger.info("Pausing simple activity: $activityId")
        // In a real implementation, you would implement pause logic
        return true
    }
    
    override suspend fun resume(activityId: String, context: ExecutionContext): Boolean {
        logger.info("Resuming simple activity: $activityId")
        // In a real implementation, you would implement resume logic
        return true
    }
    
    override suspend fun getStatus(activityId: String, context: ExecutionContext): ActivityStatus? {
        // In a real implementation, you would track and return the actual status
        return null
    }
    
    override suspend fun getMetadata(activityId: String, context: ExecutionContext): Map<String, Any>? {
        return mapOf(
            "executor" to name,
            "version" to version,
            "executionTime" to System.currentTimeMillis()
        )
    }
    
    /**
     * Generates output based on input variables and schema
     */
    private fun generateOutput(input: Map<String, Any>, schema: ActivitySchema): Map<String, Any> {
        val output = mutableMapOf<String, Any>()
        
        // Generate output for each output variable defined in the schema
        schema.outputSchema.forEach { (name, definition) ->
            output[name] = when (definition.constraints?.dataType) {
                ShaclDataType.STRING -> generateStringOutput(name, input)
                ShaclDataType.INTEGER, ShaclDataType.DECIMAL, ShaclDataType.DOUBLE -> generateNumberOutput(name, input)
                ShaclDataType.BOOLEAN -> generateBooleanOutput(name, input)
                else -> generateDefaultOutput(name, input)
            }
        }
        
        // Add some default outputs if none are defined
        if (output.isEmpty()) {
            output["result"] = "Success"
            output["processedAt"] = System.currentTimeMillis()
            output["inputSize"] = input.size
        }
        
        return output
    }
    
    private fun generateStringOutput(name: String, input: Map<String, Any>): String {
        return when (name) {
            "message" -> "Hello, ${input["name"] ?: "World"}!"
            "status" -> "completed"
            "result" -> "success"
            else -> "Processed $name"
        }
    }
    
    private fun generateNumberOutput(name: String, input: Map<String, Any>): Number {
        return when (name) {
            "count" -> input["count"] as? Number ?: 1
            "total" -> input.values.size
            "processed" -> System.currentTimeMillis()
            else -> 0
        }
    }
    
    private fun generateBooleanOutput(name: String, input: Map<String, Any>): Boolean {
        return when (name) {
            "success" -> true
            "completed" -> true
            "valid" -> input.isNotEmpty()
            else -> false
        }
    }
    
    private fun generateDefaultOutput(name: String, input: Map<String, Any>): Any {
        return when (name) {
            "message" -> "Hello, ${input["name"] ?: "World"}!"
            "status" -> "completed"
            "result" -> "success"
            else -> "Processed $name"
        }
    }
} 