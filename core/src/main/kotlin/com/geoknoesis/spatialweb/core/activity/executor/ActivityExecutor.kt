package com.geoknoesis.spatialweb.core.activity.executor

import com.geoknoesis.spatialweb.core.activity.model.Activity
import com.geoknoesis.spatialweb.core.activity.model.ActivitySchema
import com.geoknoesis.spatialweb.core.activity.model.ActivityStatus
import kotlinx.coroutines.flow.Flow

/**
 * Interface for activity execution plugins.
 *
 * ActivityExecutors are responsible for:
 * - Validating activity schemas and inputs
 * - Executing the actual business logic
 * - Managing execution state and progress
 * - Handling errors and retries
 * - Providing execution metadata
 */
interface ActivityExecutor {
    
    /**
     * Unique identifier for this executor plugin
     */
    val pluginId: String
    
    /**
     * Human-readable name of this executor
     */
    val name: String
    
    /**
     * Version of this executor
     */
    val version: String
    
    /**
     * Description of what this executor does
     */
    val description: String
    
    /**
     * Validates that this executor can handle the given activity schema
     */
    fun canExecute(schema: ActivitySchema): Boolean
    
    /**
     * Validates the input parameters for an activity
     */
    fun validateInput(schema: ActivitySchema, input: Map<String, Any>): ValidationResult
    
    /**
     * Executes an activity asynchronously
     *
     * @param activity The activity to execute
     * @param schema The schema that defines the activity
     * @param context Execution context containing runtime information
     * @return Flow of execution events that can be used to track progress
     */
    suspend fun execute(
        activity: Activity,
        schema: ActivitySchema,
        context: ExecutionContext
    ): Flow<ExecutionEvent>
    
    /**
     * Cancels a running activity
     */
    suspend fun cancel(activityId: String, context: ExecutionContext): Boolean
    
    /**
     * Pauses a running activity
     */
    suspend fun pause(activityId: String, context: ExecutionContext): Boolean
    
    /**
     * Resumes a paused activity
     */
    suspend fun resume(activityId: String, context: ExecutionContext): Boolean
    
    /**
     * Gets the current status of an activity
     */
    suspend fun getStatus(activityId: String, context: ExecutionContext): ActivityStatus?
    
    /**
     * Gets execution metadata for an activity
     */
    suspend fun getMetadata(activityId: String, context: ExecutionContext): Map<String, Any>?
}

/**
 * Context information passed to activity executors during execution
 */
data class ExecutionContext(
    /** Unique execution ID */
    val executionId: String,
    
    /** User or system that initiated the execution */
    val initiatedBy: String,
    
    /** Correlation ID for tracking related executions */
    val correlationId: String? = null,
    
    /** Execution environment information */
    val environment: Map<String, String> = emptyMap(),
    
    /** Security context and permissions */
    val securityContext: SecurityContext? = null,
    
    /** Resource limits and constraints */
    val resourceLimits: ResourceLimits? = null,
    
    /** Additional execution parameters */
    val parameters: Map<String, Any> = emptyMap()
)

/**
 * Security context for activity execution
 */
data class SecurityContext(
    /** User ID */
    val userId: String? = null,
    
    /** User roles */
    val roles: List<String> = emptyList(),
    
    /** Permissions */
    val permissions: List<String> = emptyList(),
    
    /** Authentication token */
    val authToken: String? = null,
    
    /** Additional security metadata */
    val metadata: Map<String, String> = emptyMap()
)

/**
 * Resource limits for activity execution
 */
data class ResourceLimits(
    /** Maximum CPU usage (percentage) */
    val maxCpuUsage: Double? = null,
    
    /** Maximum memory usage (MB) */
    val maxMemoryUsage: Long? = null,
    
    /** Maximum execution time (seconds) */
    val maxExecutionTime: Long? = null,
    
    /** Maximum disk usage (MB) */
    val maxDiskUsage: Long? = null,
    
    /** Maximum network bandwidth (KB/s) */
    val maxNetworkBandwidth: Long? = null
)

/**
 * Events that occur during activity execution
 */
sealed class ExecutionEvent {
    /** Execution has started */
    data class Started(
        val activityId: String,
        val timestamp: Long = System.currentTimeMillis()
    ) : ExecutionEvent()
    
    /** Progress update */
    data class Progress(
        val activityId: String,
        val progress: Double, // 0.0 to 1.0
        val message: String? = null,
        val timestamp: Long = System.currentTimeMillis()
    ) : ExecutionEvent()
    
    /** Output data produced */
    data class Output(
        val activityId: String,
        val output: Map<String, Any>,
        val timestamp: Long = System.currentTimeMillis()
    ) : ExecutionEvent()
    
    /** Execution completed successfully */
    data class Completed(
        val activityId: String,
        val output: Map<String, Any>? = null,
        val duration: Long? = null,
        val timestamp: Long = System.currentTimeMillis()
    ) : ExecutionEvent()
    
    /** Execution failed */
    data class Failed(
        val activityId: String,
        val error: String,
        val details: String? = null,
        val timestamp: Long = System.currentTimeMillis()
    ) : ExecutionEvent()
    
    /** Execution was cancelled */
    data class Cancelled(
        val activityId: String,
        val reason: String? = null,
        val timestamp: Long = System.currentTimeMillis()
    ) : ExecutionEvent()
    
    /** Execution was paused */
    data class Paused(
        val activityId: String,
        val reason: String? = null,
        val timestamp: Long = System.currentTimeMillis()
    ) : ExecutionEvent()
    
    /** Execution was resumed */
    data class Resumed(
        val activityId: String,
        val timestamp: Long = System.currentTimeMillis()
    ) : ExecutionEvent()
    
    /** Sub-activity created */
    data class SubActivityCreated(
        val parentActivityId: String,
        val childActivityId: String,
        val childSchemaId: String,
        val timestamp: Long = System.currentTimeMillis()
    ) : ExecutionEvent()
}

/**
 * Result of validation operations
 */
data class ValidationResult(
    val isValid: Boolean,
    val errors: List<String> = emptyList(),
    val warnings: List<String> = emptyList()
) {
    companion object {
        fun success() = ValidationResult(true)
        fun failure(vararg errors: String) = ValidationResult(false, errors.toList())
        fun failure(errors: List<String>) = ValidationResult(false, errors)
    }
} 