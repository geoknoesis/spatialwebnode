package com.geoknoesis.spatialweb.core.activity.model

import kotlinx.serialization.Serializable
import java.time.Instant

/**
 * Represents an instance of an activity execution.
 *
 * An Activity is created from an ActivitySchema and contains:
 * - Execution state and lifecycle information
 * - Input and output data
 * - Execution history and metadata
 * - Status tracking and error information
 */
@Serializable
data class Activity(
    /** Unique identifier for this activity instance */
    val id: String,
    
    /** Reference to the schema that defines this activity */
    val schemaId: String,
    
    /** Version of the schema used to create this activity */
    val schemaVersion: String,
    
    /** Input parameters for this activity execution */
    val input: Map<String, Any>,
    
    /** Current status of the activity */
    val status: ActivityStatus,
    
    /** User or system that created this activity */
    val createdBy: String,
    
    /** Optional correlation ID for tracking related activities */
    val correlationId: String? = null,
    
    /** When this activity was created */
    val createdAt: Instant = Instant.now(),
    
    /** When this activity was last updated */
    val updatedAt: Instant = Instant.now(),
    
    /** When execution started */
    val startedAt: Instant? = null,
    
    /** When execution completed (successfully or with error) */
    val completedAt: Instant? = null,
    
    /** Output data from the activity execution */
    val output: Map<String, Any>? = null,
    
    /** Error information if the activity failed */
    val error: ActivityError? = null,
    
    /** Execution progress (0.0 to 1.0) */
    val progress: Double = 0.0,
    
    /** Execution history and state transitions */
    val history: List<ActivityStateTransition> = emptyList(),
    
    /** Additional metadata for this activity instance */
    val metadata: Map<String, String> = emptyMap(),
    
    /** Retry count for failed executions */
    val retryCount: Int = 0,
    
    /** Maximum number of retries allowed */
    val maxRetries: Int = 3,
    
    /** Parent activity ID if this is a sub-activity */
    val parentActivityId: String? = null,
    
    /** Child activity IDs if this activity spawned sub-activities */
    val childActivityIds: List<String> = emptyList()
) {
    /**
     * Checks if the activity is in a terminal state
     */
    val isTerminal: Boolean
        get() = status in listOf(ActivityStatus.COMPLETED, ActivityStatus.FAILED, ActivityStatus.CANCELLED)
    
    /**
     * Checks if the activity can be retried
     */
    val canRetry: Boolean
        get() = status == ActivityStatus.FAILED && retryCount < maxRetries
    
    /**
     * Gets the execution duration if completed
     */
    val executionDuration: Long?
        get() = if (startedAt != null && completedAt != null) {
            completedAt.toEpochMilli() - startedAt.toEpochMilli()
        } else null
    
    /**
     * Creates a new state transition and updates the activity
     */
    fun transitionTo(newStatus: ActivityStatus, message: String? = null): Activity {
        val transition = ActivityStateTransition(
            fromStatus = status,
            toStatus = newStatus,
            timestamp = Instant.now(),
            message = message
        )
        
        return copy(
            status = newStatus,
            updatedAt = Instant.now(),
            history = history + transition,
            startedAt = if (newStatus == ActivityStatus.RUNNING && startedAt == null) Instant.now() else startedAt,
            completedAt = if (newStatus in listOf(ActivityStatus.COMPLETED, ActivityStatus.FAILED, ActivityStatus.CANCELLED)) Instant.now() else completedAt
        )
    }
    
    /**
     * Updates the activity with output data
     */
    fun withOutput(output: Map<String, Any>): Activity {
        return copy(
            output = output,
            updatedAt = Instant.now()
        )
    }
    
    /**
     * Updates the activity with error information
     */
    fun withError(error: ActivityError): Activity {
        return copy(
            error = error,
            updatedAt = Instant.now()
        )
    }
    
    /**
     * Updates the progress of the activity
     */
    fun withProgress(progress: Double): Activity {
        return copy(
            progress = progress.coerceIn(0.0, 1.0),
            updatedAt = Instant.now()
        )
    }
    
    /**
     * Increments the retry count
     */
    fun incrementRetry(): Activity {
        return copy(
            retryCount = retryCount + 1,
            updatedAt = Instant.now()
        )
    }
    
    /**
     * Adds a child activity reference
     */
    fun addChildActivity(childId: String): Activity {
        return copy(
            childActivityIds = childActivityIds + childId,
            updatedAt = Instant.now()
        )
    }
}

/**
 * Represents the status of an activity
 */
@Serializable
enum class ActivityStatus {
    /** Activity has been created but not yet started */
    CREATED,
    
    /** Activity is queued for execution */
    QUEUED,
    
    /** Activity is currently running */
    RUNNING,
    
    /** Activity has completed successfully */
    COMPLETED,
    
    /** Activity has failed */
    FAILED,
    
    /** Activity has been cancelled */
    CANCELLED,
    
    /** Activity is paused (can be resumed) */
    PAUSED,
    
    /** Activity is waiting for external input */
    WAITING
}

/**
 * Represents an error that occurred during activity execution
 */
@Serializable
data class ActivityError(
    /** Error code or type */
    val code: String,
    
    /** Human-readable error message */
    val message: String,
    
    /** Detailed error description */
    val details: String? = null,
    
    /** Stack trace or technical details */
    val stackTrace: String? = null,
    
    /** When the error occurred */
    val timestamp: Instant = Instant.now(),
    
    /** Additional error context */
    val context: Map<String, String> = emptyMap()
)

/**
 * Represents a state transition in an activity's lifecycle
 */
@Serializable
data class ActivityStateTransition(
    /** Previous status */
    val fromStatus: ActivityStatus,
    
    /** New status */
    val toStatus: ActivityStatus,
    
    /** When the transition occurred */
    val timestamp: Instant,
    
    /** Optional message describing the transition */
    val message: String? = null,
    
    /** Additional transition metadata */
    val metadata: Map<String, String> = emptyMap()
) 