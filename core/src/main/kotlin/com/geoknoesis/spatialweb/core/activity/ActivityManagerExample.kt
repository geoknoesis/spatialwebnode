package com.geoknoesis.spatialweb.core.activity

import com.geoknoesis.spatialweb.core.activity.executor.ExecutionContext
import com.geoknoesis.spatialweb.core.activity.executor.ExecutionEvent
import com.geoknoesis.spatialweb.core.activity.executor.SimpleActivityExecutor
import com.geoknoesis.spatialweb.core.activity.model.*

import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

/**
 * Comprehensive example demonstrating the ActivityManager system.
 *
 * This example shows:
 * - Creating activity schemas with SHACL constraints
 * - Registering executors
 * - Starting and monitoring activities
 * - Handling events and lifecycle
 * - Querying and statistics
 */
object ActivityManagerExample {
    
    private val logger = LoggerFactory.getLogger(ActivityManagerExample::class.java)
    
    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        logger.info("Starting ActivityManager Example")
        
        // Create activity manager
        val activityManager = ActivityManager()
        
        // Add event listener for monitoring
        activityManager.addEventListener(createEventListener())
        
        // Register a simple executor
        val simpleExecutor = SimpleActivityExecutor()
        activityManager.registerExecutor(simpleExecutor)
        
        // Create activity schemas
        val schemas = createExampleSchemas()
        schemas.forEach { schema ->
            val success = activityManager.registerSchema(schema)
            logger.info("Registered schema ${schema.name}: $success")
        }
        
        // Start some activities
        val activities = mutableListOf<String>()
        
        // Start a greeting activity
        val greetingActivity = activityManager.startActivity(
            schemaId = "greeting-activity",
            input = mapOf("name" to "Alice", "greeting" to "Hello"),
            createdBy = "example-user",
            correlationId = "greeting-session-1"
        )
        greetingActivity?.let { activities.add(it.id) }
        
        // Start a data processing activity
        val processingActivity = activityManager.startActivity(
            schemaId = "data-processing",
            input = mapOf(
                "data" to listOf("item1", "item2", "item3"),
                "operation" to "transform",
                "batchSize" to 2
            ),
            createdBy = "example-user",
            correlationId = "processing-session-1"
        )
        processingActivity?.let { activities.add(it.id) }
        
        // Start a validation activity
        val validationActivity = activityManager.startActivity(
            schemaId = "validation-activity",
            input = mapOf(
                "data" to mapOf("field1" to "value1", "field2" to "value2"),
                "rules" to listOf("required", "non-empty")
            ),
            createdBy = "example-user",
            correlationId = "validation-session-1"
        )
        validationActivity?.let { activities.add(it.id) }
        
        // Wait for activities to complete
        logger.info("Waiting for activities to complete...")
        Thread.sleep(2000) // Wait 2 seconds for activities to complete
        
        // Query activities
        logger.info("Querying activities...")
        
        // Get all activities
        val allActivities = activityManager.searchActivities(
            ActivitySearchQuery(limit = 100)
        )
        logger.info("Total activities: ${allActivities.size}")
        
        // Get activities by status
        val completedActivities = activityManager.searchActivities(
            ActivitySearchQuery(status = ActivityStatus.COMPLETED)
        )
        logger.info("Completed activities: ${completedActivities.size}")
        
        // Get activities by correlation ID
        val greetingActivities = activityManager.searchActivities(
            ActivitySearchQuery(correlationId = "greeting-session-1")
        )
        logger.info("Greeting session activities: ${greetingActivities.size}")
        
        // Get statistics
        val statistics = activityManager.getStatistics()
        logger.info("Activity Statistics:")
        logger.info("  Total activities: ${statistics.totalActivities}")
        logger.info("  Success rate: ${"%.2f".format(statistics.successRate * 100)}%")
        logger.info("  Average execution time: ${statistics.averageExecutionTime}ms")
        logger.info("  Activities by status: ${statistics.activitiesByStatus}")
        
        // Show activity details
        activities.forEach { activityId ->
            val activity = activityManager.getActivity(activityId)
            activity?.let {
                logger.info("Activity ${it.id}:")
                logger.info("  Schema: ${it.schemaId}")
                logger.info("  Status: ${it.status}")
                logger.info("  Created by: ${it.createdBy}")
                logger.info("  Progress: ${"%.1f".format(it.progress * 100)}%")
                logger.info("  Duration: ${it.executionDuration}ms")
                logger.info("  Output: ${it.output}")
                if (it.error != null) {
                    logger.info("  Error: ${it.error.message}")
                }
            }
        }
        
        // Show available schemas and executors
        logger.info("Available schemas:")
        activityManager.getAllSchemas().forEach { schema ->
            logger.info("  ${schema.name} (${schema.id}) - ${schema.description}")
        }
        
        logger.info("Available executors:")
        activityManager.getAllExecutors().forEach { executor ->
            logger.info("  ${executor.name} (${executor.pluginId}) - ${executor.description}")
        }
        
        // Shutdown
        activityManager.shutdown()
        logger.info("ActivityManager Example completed")
    }
    
    /**
     * Creates example activity schemas with SHACL constraints
     */
    private fun createExampleSchemas(): List<ActivitySchema> {
        return listOf(
            // Greeting activity schema
            ActivitySchema(
                id = "greeting-activity",
                name = "Greeting Activity",
                description = "Generates a personalized greeting message",
                version = "1.0.0",
                category = "communication",
                executorPluginId = "simple-executor",
                inputSchema = mapOf(
                    "name" to VariableDefinition(
                        id = "name-var",
                        name = "name",
                        description = "Name of the person to greet",
                        constraints = ShaclPropertyShape(
                            path = "name",
                            dataType = ShaclDataType.STRING,
                            required = true,
                            minLength = 1,
                            maxLength = 100
                        )
                    ),
                    "greeting" to VariableDefinition(
                        id = "greeting-var",
                        name = "greeting",
                        description = "Greeting type",
                        constraints = ShaclPropertyShape(
                            path = "greeting",
                            dataType = ShaclDataType.STRING,
                            required = false,
                            allowedValues = listOf("Hello", "Hi", "Good morning", "Good afternoon", "Good evening")
                        )
                    )
                ),
                outputSchema = mapOf(
                    "message" to VariableDefinition(
                        id = "message-var",
                        name = "message",
                        description = "Generated greeting message",
                        constraints = ShaclPropertyShape(
                            path = "message",
                            dataType = ShaclDataType.STRING,
                            minLength = 1
                        )
                    ),
                    "timestamp" to VariableDefinition(
                        id = "timestamp-var",
                        name = "timestamp",
                        description = "When the greeting was generated",
                        constraints = ShaclPropertyShape(
                            path = "timestamp",
                            dataType = ShaclDataType.INTEGER
                        )
                    )
                ),
                constraints = ExecutionConstraints(
                    maxExecutionTime = 30,
                    allowParallel = true
                ),
                metadata = mapOf(
                    "tags" to "greeting,communication,user-friendly"
                )
            ),
            
            // Data processing activity schema
            ActivitySchema(
                id = "data-processing",
                name = "Data Processing Activity",
                description = "Processes a list of data items",
                version = "1.0.0",
                category = "data-processing",
                executorPluginId = "simple-executor",
                inputSchema = mapOf(
                    "data" to VariableDefinition(
                        id = "data-var",
                        name = "data",
                        description = "Data items to process",
                        constraints = ShaclPropertyShape(
                            path = "data",
                            required = true,
                            minCount = 1,
                            maxCount = 1000
                        )
                    ),
                    "operation" to VariableDefinition(
                        id = "operation-var",
                        name = "operation",
                        description = "Type of operation to perform",
                        constraints = ShaclPropertyShape(
                            path = "operation",
                            dataType = ShaclDataType.STRING,
                            required = true,
                            allowedValues = listOf("transform", "filter", "aggregate", "sort")
                        )
                    ),
                    "batchSize" to VariableDefinition(
                        id = "batch-size-var",
                        name = "batchSize",
                        description = "Number of items to process in each batch",
                        constraints = ShaclPropertyShape(
                            path = "batchSize",
                            dataType = ShaclDataType.INTEGER,
                            minInclusive = 1.0,
                            maxInclusive = 100.0
                        )
                    )
                ),
                outputSchema = mapOf(
                    "processedCount" to VariableDefinition(
                        id = "processed-count-var",
                        name = "processedCount",
                        description = "Number of items processed",
                        constraints = ShaclPropertyShape(
                            path = "processedCount",
                            dataType = ShaclDataType.INTEGER,
                            minInclusive = 0.0
                        )
                    ),
                    "results" to VariableDefinition(
                        id = "results-var",
                        name = "results",
                        description = "Processing results",
                        constraints = ShaclPropertyShape(
                            path = "results"
                        )
                    ),
                    "summary" to VariableDefinition(
                        id = "summary-var",
                        name = "summary",
                        description = "Processing summary",
                        constraints = ShaclPropertyShape(
                            path = "summary"
                        )
                    )
                ),
                constraints = ExecutionConstraints(
                    maxExecutionTime = 300,
                    allowParallel = true,
                    retryConfig = RetryConfig(maxRetries = 3)
                ),
                metadata = mapOf(
                    "tags" to "data-processing,batch,transform"
                )
            ),
            
            // Validation activity schema
            ActivitySchema(
                id = "validation-activity",
                name = "Data Validation Activity",
                description = "Validates data against specified rules",
                version = "1.0.0",
                category = "validation",
                executorPluginId = "simple-executor",
                inputSchema = mapOf(
                    "data" to VariableDefinition(
                        id = "data-var",
                        name = "data",
                        description = "Data to validate",
                        constraints = ShaclPropertyShape(
                            path = "data",
                            required = true
                        )
                    ),
                    "rules" to VariableDefinition(
                        id = "rules-var",
                        name = "rules",
                        description = "Validation rules to apply",
                        constraints = ShaclPropertyShape(
                            path = "rules",
                            required = true,
                            minCount = 1,
                            allowedValues = listOf("required", "non-empty", "email", "url", "numeric")
                        )
                    )
                ),
                outputSchema = mapOf(
                    "valid" to VariableDefinition(
                        id = "valid-var",
                        name = "valid",
                        description = "Whether the data is valid",
                        constraints = ShaclPropertyShape(
                            path = "valid",
                            dataType = ShaclDataType.BOOLEAN
                        )
                    ),
                    "errors" to VariableDefinition(
                        id = "errors-var",
                        name = "errors",
                        description = "Validation errors found",
                        constraints = ShaclPropertyShape(
                            path = "errors"
                        )
                    ),
                    "validatedAt" to VariableDefinition(
                        id = "validated-at-var",
                        name = "validatedAt",
                        description = "When validation was performed",
                        constraints = ShaclPropertyShape(
                            path = "validatedAt",
                            dataType = ShaclDataType.INTEGER
                        )
                    )
                ),
                constraints = ExecutionConstraints(
                    maxExecutionTime = 60,
                    allowParallel = true
                ),
                metadata = mapOf(
                    "tags" to "validation,data-quality,error-checking"
                )
            )
        )
    }
    
    /**
     * Creates an event listener for monitoring activity events
     */
    private fun createEventListener(): ActivityEventListener {
        return object : ActivityEventListener {
            override fun onSchemaRegistered(schema: ActivitySchema) {
                logger.info("📋 Schema registered: ${schema.name} (${schema.id})")
            }
            
            override fun onActivityStarted(activity: Activity) {
                logger.info("🚀 Activity started: ${activity.id} (${activity.schemaId})")
            }
            
            override fun onActivityCompleted(activity: Activity) {
                logger.info("✅ Activity completed: ${activity.id} in ${activity.executionDuration}ms")
            }
            
            override fun onActivityFailed(activity: Activity) {
                logger.error("❌ Activity failed: ${activity.id} - ${activity.error?.message}")
            }
            
            override fun onActivityEvent(activity: Activity, event: ExecutionEvent) {
                when (event) {
                    is ExecutionEvent.Progress -> {
                        logger.debug("📊 Progress: ${activity.id} - ${"%.1f".format(event.progress * 100)}%")
                    }
                    is ExecutionEvent.Output -> {
                        logger.debug("📤 Output: ${activity.id} - ${event.output.size} items")
                    }
                    else -> {
                        logger.debug("📡 Event: ${activity.id} - ${event::class.simpleName}")
                    }
                }
            }
        }
    }
} 