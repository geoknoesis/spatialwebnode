package com.geoknoesis.spatialweb.core.activity

import com.geoknoesis.spatialweb.core.activity.executor.ActivityExecutor
import com.geoknoesis.spatialweb.core.activity.executor.ExecutionContext
import com.geoknoesis.spatialweb.core.activity.executor.ExecutionEvent
import com.geoknoesis.spatialweb.core.activity.model.Activity
import com.geoknoesis.spatialweb.core.activity.model.ActivitySchema
import com.geoknoesis.spatialweb.core.activity.model.ActivityStatus

import com.geoknoesis.spatialweb.core.activity.validation.ValidatorManager
import com.geoknoesis.spatialweb.core.activity.validation.ValidatorManagerConfig
import com.geoknoesis.spatialweb.core.activity.validation.ValidationContext
import com.geoknoesis.spatialweb.core.activity.validation.ValidationPhase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import java.time.Instant
import java.util.ServiceLoader
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Manages the complete lifecycle of activities, including schemas and execution.
 *
 * The ActivityManager is the central orchestrator that:
 * - Manages activity schemas (templates)
 * - Coordinates activity executors (plugins)
 * - Manages activity lifecycle and state transitions
 * - Provides monitoring and statistics
 */
class ActivityManager(
    private val config: ActivityManagerConfig = ActivityManagerConfig()
) {
    
    private val logger = LoggerFactory.getLogger(ActivityManager::class.java)
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    // Registry of available executors
    private val executors = ConcurrentHashMap<String, ActivityExecutor>()
    
    // Registry of activity schemas
    private val schemas = ConcurrentHashMap<String, ActivitySchema>()
    
    // Active activity executions
    private val activeExecutions = ConcurrentHashMap<String, ActivityExecution>()
    
    // Validator manager for pluggable validation
    private val validatorManager = ValidatorManager(config.validatorConfig)
    
    // Event listeners
    private val eventListeners = mutableListOf<ActivityEventListener>()
    
    init {
        // Note: initialize() is called separately after construction
    }
    
    /**
     * Initializes the activity manager
     */
    suspend fun initialize() {
        logger.info("Initializing ActivityManager...")
        
        // Load executors via SPI
        loadExecutors()
        
        logger.info("ActivityManager initialized successfully")
    }
    
    /**
     * Loads activity executors using Java Service Provider Interface
     */
    private fun loadExecutors() {
        val loadedExecutors = ServiceLoader.load(ActivityExecutor::class.java)
        loadedExecutors.forEach { executor ->
            registerExecutor(executor)
        }
        logger.info("Loaded ${executors.size} activity executors")
    }
    

    
    /**
     * Registers an activity executor
     */
    fun registerExecutor(executor: ActivityExecutor) {
        executors[executor.pluginId] = executor
        logger.info("Registered executor: ${executor.name} (${executor.pluginId})")
    }
    
    /**
     * Unregisters an activity executor
     */
    fun unregisterExecutor(pluginId: String) {
        executors.remove(pluginId)
        logger.info("Unregistered executor: $pluginId")
    }
    
    /**
     * Registers an activity schema
     */
    suspend fun registerSchema(schema: ActivitySchema): Boolean {
        // Validate that the executor exists
        if (!executors.containsKey(schema.executorPluginId)) {
            logger.error("Executor not found for schema ${schema.id}: ${schema.executorPluginId}")
            return false
        }
        
        // Validate schema using pluggable validators
        val validation = validatorManager.validateSchema(schema, ValidationContext(
            phase = ValidationPhase.SCHEMA,
            data = mapOf("operation" to "register")
        ))
        if (!validation.isValid) {
            logger.error("Schema validation failed: ${validation.errors.joinToString(", ") { it.message }}")
            return false
        }
        
        // Schema is already added to local registry
        
        // Add to local registry
        schemas[schema.id] = schema
        logger.info("Registered schema: ${schema.name} (${schema.id})")
        
        // Notify listeners
        notifySchemaRegistered(schema)
        
        return true
    }
    
    /**
     * Unregisters an activity schema
     */
    suspend fun unregisterSchema(schemaId: String): Boolean {
        val schema = schemas.remove(schemaId)
        if (schema != null) {
            // Schema removed from local registry
            logger.info("Unregistered schema: $schemaId")
            notifySchemaUnregistered(schema)
            return true
        }
        return false
    }
    
    /**
     * Creates and starts an activity execution
     */
    suspend fun startActivity(
        schemaId: String,
        input: Map<String, Any>,
        createdBy: String,
        correlationId: String? = null,
        context: ExecutionContext? = null
    ): Activity? {
        val schema = schemas[schemaId] ?: run {
            logger.error("Schema not found: $schemaId")
            return null
        }
        
        val executor = executors[schema.executorPluginId] ?: run {
            logger.error("Executor not found: ${schema.executorPluginId}")
            return null
        }
        
        // Validate input using pluggable validators
        val inputValidation = validatorManager.validateInput(schema, input, ValidationContext(
            phase = ValidationPhase.INPUT,
            data = mapOf("operation" to "start", "createdBy" to createdBy)
        ))
        if (!inputValidation.isValid) {
            logger.error("Input validation failed: ${inputValidation.errors.joinToString(", ") { it.message }}")
            return null
        }
        
        // Create activity instance
        val activity = schema.createActivity(input, createdBy, correlationId)
        
        // Activity is managed in memory
        
        // Create execution context
        val executionContext = context ?: ExecutionContext(
            executionId = UUID.randomUUID().toString(),
            initiatedBy = createdBy,
            correlationId = correlationId
        )
        
        // Start execution
        val execution = ActivityExecution(activity, schema, executor, executionContext)
        activeExecutions[activity.id] = execution
        
        // Launch execution in background
        scope.launch {
            try {
                executeActivity(execution)
            } catch (e: Exception) {
                logger.error("Activity execution failed: ${activity.id}", e)
                handleExecutionError(execution, e)
            }
        }
        
        logger.info("Started activity: ${activity.id} (${schema.name})")
        notifyActivityStarted(activity)
        
        return activity
    }
    
    /**
     * Executes an activity and handles the execution flow
     */
    private suspend fun executeActivity(execution: ActivityExecution) {
        val activity = execution.activity
        val schema = execution.schema
        val executor = execution.executor
        val context = execution.context
        
        try {
            // Update status to RUNNING
            val runningActivity = activity.transitionTo(ActivityStatus.RUNNING)
            updateActivity(runningActivity)
            
            // Execute the activity
            executor.execute(runningActivity, schema, context)
                .onEach { event ->
                    handleExecutionEvent(execution, event)
                }
                .collect()
                
        } finally {
            // Clean up execution
            activeExecutions.remove(activity.id)
        }
    }
    
    /**
     * Handles execution events from the executor
     */
    private suspend fun handleExecutionEvent(execution: ActivityExecution, event: ExecutionEvent) {
        val activity = execution.activity
        
        when (event) {
            is ExecutionEvent.Started -> {
                logger.debug("Activity started: ${activity.id}")
                notifyActivityEvent(activity, event)
            }
            
            is ExecutionEvent.Progress -> {
                val updatedActivity = activity.withProgress(event.progress)
                updateActivity(updatedActivity)
                notifyActivityEvent(updatedActivity, event)
            }
            
            is ExecutionEvent.Output -> {
                val updatedActivity = activity.withOutput(event.output)
                updateActivity(updatedActivity)
                notifyActivityEvent(updatedActivity, event)
            }
            
            is ExecutionEvent.Completed -> {
                val completedActivity = activity
                    .transitionTo(ActivityStatus.COMPLETED)
                    .withOutput(event.output ?: emptyMap())
                updateActivity(completedActivity)
                notifyActivityCompleted(completedActivity)
            }
            
            is ExecutionEvent.Failed -> {
                val failedActivity = activity
                    .transitionTo(ActivityStatus.FAILED)
                    .withError(com.geoknoesis.spatialweb.core.activity.model.ActivityError(
                        code = "EXECUTION_FAILED",
                        message = event.error,
                        details = event.details
                    ))
                updateActivity(failedActivity)
                notifyActivityFailed(failedActivity)
            }
            
            is ExecutionEvent.Cancelled -> {
                val cancelledActivity = activity.transitionTo(ActivityStatus.CANCELLED)
                updateActivity(cancelledActivity)
                notifyActivityCancelled(cancelledActivity)
            }
            
            is ExecutionEvent.Paused -> {
                val pausedActivity = activity.transitionTo(ActivityStatus.PAUSED)
                updateActivity(pausedActivity)
                notifyActivityEvent(activity, event)
            }
            
            is ExecutionEvent.Resumed -> {
                val resumedActivity = activity.transitionTo(ActivityStatus.RUNNING)
                updateActivity(resumedActivity)
                notifyActivityEvent(activity, event)
            }
            
            is ExecutionEvent.SubActivityCreated -> {
                notifyActivityEvent(activity, event)
            }
        }
    }
    
    /**
     * Handles execution errors
     */
    private suspend fun handleExecutionError(execution: ActivityExecution, error: Exception) {
        val activity = execution.activity
        val failedActivity = activity
            .transitionTo(ActivityStatus.FAILED)
            .withError(com.geoknoesis.spatialweb.core.activity.model.ActivityError(
                code = "EXECUTION_ERROR",
                message = error.message ?: "Unknown error",
                details = error.toString(),
                stackTrace = error.stackTraceToString()
            ))
        updateActivity(failedActivity)
        notifyActivityFailed(failedActivity)
    }
    
    /**
     * Updates an activity in memory
     */
    private suspend fun updateActivity(activity: Activity) {
        // Activity is updated in memory during execution
    }
    
    /**
     * Cancels a running activity
     */
    suspend fun cancelActivity(activityId: String, reason: String? = null): Boolean {
        val execution = activeExecutions[activityId] ?: return false
        
        return try {
            val cancelled = execution.executor.cancel(activityId, execution.context)
            if (cancelled) {
                logger.info("Activity cancelled: $activityId")
            }
            cancelled
        } catch (e: Exception) {
            logger.error("Failed to cancel activity: $activityId", e)
            false
        }
    }
    
    /**
     * Pauses a running activity
     */
    suspend fun pauseActivity(activityId: String, reason: String? = null): Boolean {
        val execution = activeExecutions[activityId] ?: return false
        
        return try {
            val paused = execution.executor.pause(activityId, execution.context)
            if (paused) {
                logger.info("Activity paused: $activityId")
            }
            paused
        } catch (e: Exception) {
            logger.error("Failed to pause activity: $activityId", e)
            false
        }
    }
    
    /**
     * Resumes a paused activity
     */
    suspend fun resumeActivity(activityId: String): Boolean {
        val execution = activeExecutions[activityId] ?: return false
        
        return try {
            val resumed = execution.executor.resume(activityId, execution.context)
            if (resumed) {
                logger.info("Activity resumed: $activityId")
            }
            resumed
        } catch (e: Exception) {
            logger.error("Failed to resume activity: $activityId", e)
            false
        }
    }
    
    /**
     * Gets an activity by ID
     */
    suspend fun getActivity(activityId: String): Activity? {
        // Activities are managed in memory during execution
        return null
    }
    
    /**
     * Gets a schema by ID
     */
    fun getSchema(schemaId: String): ActivitySchema? {
        return schemas[schemaId]
    }
    
    /**
     * Gets all schemas
     */
    fun getAllSchemas(): List<ActivitySchema> {
        return schemas.values.toList()
    }
    
    /**
     * Gets all executors
     */
    fun getAllExecutors(): List<ActivityExecutor> {
        return executors.values.toList()
    }
    
    /**
     * Searches activities
     */
    suspend fun searchActivities(query: ActivitySearchQuery): List<Activity> {
        // Activities are managed in memory during execution
        return emptyList()
    }
    
    /**
     * Gets activity statistics
     */
    suspend fun getStatistics(): ActivityStatistics {
        // Return empty statistics since activities are managed in memory
        return ActivityStatistics(
            totalActivities = 0,
            activeActivities = activeExecutions.size,
            completedActivities = 0,
            failedActivities = 0,
            averageExecutionTime = 0.0
        )
    }
    
    /**
     * Adds an event listener
     */
    fun addEventListener(listener: ActivityEventListener) {
        eventListeners.add(listener)
    }
    
    /**
     * Removes an event listener
     */
    fun removeEventListener(listener: ActivityEventListener) {
        eventListeners.remove(listener)
    }
    
    /**
     * Notifies listeners of schema registration
     */
    private fun notifySchemaRegistered(schema: ActivitySchema) {
        eventListeners.forEach { it.onSchemaRegistered(schema) }
    }
    
    /**
     * Notifies listeners of schema unregistration
     */
    private fun notifySchemaUnregistered(schema: ActivitySchema) {
        eventListeners.forEach { it.onSchemaUnregistered(schema) }
    }
    
    /**
     * Notifies listeners of activity start
     */
    private fun notifyActivityStarted(activity: Activity) {
        eventListeners.forEach { it.onActivityStarted(activity) }
    }
    
    /**
     * Notifies listeners of activity completion
     */
    private fun notifyActivityCompleted(activity: Activity) {
        eventListeners.forEach { it.onActivityCompleted(activity) }
    }
    
    /**
     * Notifies listeners of activity failure
     */
    private fun notifyActivityFailed(activity: Activity) {
        eventListeners.forEach { it.onActivityFailed(activity) }
    }
    
    /**
     * Notifies listeners of activity cancellation
     */
    private fun notifyActivityCancelled(activity: Activity) {
        eventListeners.forEach { it.onActivityCancelled(activity) }
    }
    
    /**
     * Notifies listeners of activity events
     */
    private fun notifyActivityEvent(activity: Activity, event: ExecutionEvent) {
        eventListeners.forEach { it.onActivityEvent(activity, event) }
    }
    
    /**
     * Shuts down the activity manager
     */
    suspend fun shutdown() {
        logger.info("Shutting down ActivityManager...")
        
        // Cancel all active executions
        activeExecutions.values.forEach { execution ->
            try {
                execution.executor.cancel(execution.activity.id, execution.context)
            } catch (e: Exception) {
                logger.warn("Failed to cancel activity: ${execution.activity.id}", e)
            }
        }
        

        
        // Cancel coroutine scope
        scope.cancel()
        
        logger.info("ActivityManager shutdown complete")
    }
}

/**
 * Configuration for the ActivityManager
 */
data class ActivityManagerConfig(
    /** Maximum number of concurrent activities */
    val maxConcurrentActivities: Int = 100,
    
    /** Activity execution timeout in seconds */
    val defaultExecutionTimeout: Long = 300,
    
    /** Whether to enable automatic retries */
    val enableAutoRetry: Boolean = true,
    
    /** Maximum number of retries */
    val maxRetries: Int = 3,
    
    /** Retry delay in milliseconds */
    val retryDelay: Long = 1000,
    
    /** Whether to enable activity monitoring */
    val enableMonitoring: Boolean = true,
    
    /** Monitoring interval in milliseconds */
    val monitoringInterval: Long = 5000,
    
    /** Validator manager configuration */
    val validatorConfig: ValidatorManagerConfig = ValidatorManagerConfig()
)

/**
 * Represents an active activity execution
 */
private data class ActivityExecution(
    val activity: Activity,
    val schema: ActivitySchema,
    val executor: ActivityExecutor,
    val context: ExecutionContext
)

/**
 * Interface for activity event listeners
 */
interface ActivityEventListener {
    fun onSchemaRegistered(schema: ActivitySchema) {}
    fun onSchemaUnregistered(schema: ActivitySchema) {}
    fun onActivityStarted(activity: Activity) {}
    fun onActivityCompleted(activity: Activity) {}
    fun onActivityFailed(activity: Activity) {}
    fun onActivityCancelled(activity: Activity) {}
    fun onActivityEvent(activity: Activity, event: ExecutionEvent) {}
}

/**
 * Simple activity search query
 */
data class ActivitySearchQuery(
    val status: ActivityStatus? = null,
    val createdBy: String? = null,
    val correlationId: String? = null,
    val limit: Int = 100,
    val offset: Int = 0
)

/**
 * Activity statistics
 */
data class ActivityStatistics(
    val totalActivities: Int,
    val activeActivities: Int,
    val completedActivities: Int,
    val failedActivities: Int,
    val averageExecutionTime: Double,
    val successRate: Double = if (totalActivities > 0) completedActivities.toDouble() / totalActivities else 0.0,
    val activitiesByStatus: Map<String, Int> = emptyMap()
) 