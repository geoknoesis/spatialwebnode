# Activity Manager System

The Activity Manager is a comprehensive system for managing the lifecycle of activities in the Spatial Web Node. It provides a pluggable architecture for activity execution, persistence, and monitoring with SHACL-based validation.

## Overview

The Activity Manager system consists of several key components:

- **ActivityManager**: Central orchestrator that manages activity schemas, executors, and persistence
- **ActivitySchema**: Defines the structure and metadata for activities using variables and SHACL constraints
- **Activity**: Represents an instance of an activity execution
- **ActivityExecutor**: Plugin interface for executing activities
- **ActivityPersistence**: Pluggable backend for storing activity data
- **SHACL Constraints**: Rich validation using Shapes Constraint Language

## Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│  ActivityManager│    │ ActivitySchema  │    │   Activity      │
│                 │    │                 │    │                 │
│ - Schemas       │◄──►│ - Variables     │◄──►│ - Execution     │
│ - Executors     │    │ - SHACL         │    │ - State         │
│ - Persistence   │    │ - Metadata      │    │ - History       │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         ▼                       ▼                       ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│ActivityExecutor │    │ActivityPersistence│   │ ExecutionEvent  │
│                 │    │                 │    │                 │
│ - Execute       │    │ - RDF Store     │    │ - Started       │
│ - Validate      │    │ - Relational DB │    │ - Progress      │
│ - Control       │    │ - In-Memory     │    │ - Completed     │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

## Key Features

### 1. Pluggable Architecture
- **Activity Executors**: Plugin-based execution engines
- **Persistence Backends**: RDF store (primary), relational database, in-memory
- **Event System**: Real-time activity monitoring and notifications

### 2. Activity Lifecycle Management
- **Variable Definition**: Structured input/output variables with SHACL validation
- **Execution Control**: Start, pause, resume, cancel activities
- **State Tracking**: Complete execution history and state transitions
- **Progress Monitoring**: Real-time progress updates

### 3. SHACL-Based Validation
- **Rich Constraints**: Data types, ranges, patterns, and custom validation
- **Semantic Validation**: RDF-compatible constraint language
- **Flexible Rules**: Support for complex validation scenarios
- **Standard Compliance**: Based on W3C SHACL specification

### 4. Advanced Features
- **Correlation Tracking**: Link related activities
- **Retry Logic**: Automatic retry with configurable backoff
- **Resource Limits**: CPU, memory, and time constraints
- **Security Context**: User permissions and authentication
- **Statistics**: Comprehensive activity metrics and reporting

## Quick Start

### 1. Create an Activity Schema with SHACL Constraints

```kotlin
val schema = ActivitySchema(
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
        )
    ),
    constraints = ExecutionConstraints(
        maxExecutionTime = 30,
        allowParallel = true
    )
)
```

### 2. Implement an Activity Executor

```kotlin
class MyActivityExecutor : ActivityExecutor {
    override val pluginId: String = "my-executor"
    override val name: String = "My Activity Executor"
    override val version: String = "1.0.0"
    override val description: String = "My custom activity executor"
    
    override fun canExecute(schema: ActivitySchema): Boolean {
        return schema.executorPluginId == pluginId
    }
    
    override suspend fun execute(
        activity: Activity,
        schema: ActivitySchema,
        context: ExecutionContext
    ): Flow<ExecutionEvent> = flow {
        emit(ExecutionEvent.Started(activity.id))
        
        // Your business logic here
        val result = processActivity(activity.input)
        
        emit(ExecutionEvent.Completed(
            activityId = activity.id,
            output = result
        ))
    }
}
```

### 3. Set up the Activity Manager

```kotlin
// Create persistence backend
val persistence = InMemoryActivityPersistence()

// Create activity manager
val activityManager = ActivityManager(persistence)

// Register executor
val executor = MyActivityExecutor()
activityManager.registerExecutor(executor)

// Register schema
activityManager.registerSchema(schema)

// Start an activity
val activity = activityManager.startActivity(
    schemaId = "greeting-activity",
    input = mapOf("name" to "Alice"),
    createdBy = "user123"
)
```

## Components

### ActivitySchema

Defines the structure and metadata for activities:

- **Variables**: Input/output variable definitions with SHACL constraints
- **SHACL Validation**: Rich constraint language for data validation
- **Execution Constraints**: Time limits, resource constraints, retry configuration
- **Metadata**: Tags, categories, and additional information

### VariableDefinition

Represents a variable in an activity schema:

- **ID**: Unique identifier for the variable
- **Name**: Human-readable variable name
- **Description**: Detailed description of the variable
- **SHACL Constraints**: Validation rules using SHACL property shapes

### SHACL Property Shapes

Rich validation constraints based on the Shapes Constraint Language:

```kotlin
val constraints = ShaclPropertyShape(
    path = "email",
    dataType = ShaclDataType.STRING,
    required = true,
    pattern = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
    minLength = 5,
    maxLength = 254,
    message = "Must be a valid email address"
)
```

### Activity

Represents an activity execution instance:

- **State Management**: CREATED, QUEUED, RUNNING, COMPLETED, FAILED, etc.
- **Progress Tracking**: 0.0 to 1.0 progress indicator
- **Execution History**: Complete state transition history
- **Input/Output Data**: Activity variables and results
- **Error Handling**: Detailed error information and stack traces

### ActivityExecutor

Plugin interface for executing activities:

- **Execution**: Main activity execution logic
- **Validation**: Input variable validation using SHACL
- **Control**: Pause, resume, cancel operations
- **Monitoring**: Status and metadata retrieval

### ActivityPersistence

Pluggable backend for data storage:

- **RDF Store**: Primary storage for semantic data
- **Relational Database**: Traditional SQL storage
- **In-Memory**: Fast storage for testing and development
- **File Storage**: Persistent file-based storage

## SHACL Constraints

### Data Types

```kotlin
enum class ShaclDataType {
    STRING,      // xsd:string
    INTEGER,     // xsd:integer
    DECIMAL,     // xsd:decimal
    DOUBLE,      // xsd:double
    BOOLEAN,     // xsd:boolean
    DATE,        // xsd:date
    DATETIME,    // xsd:dateTime
    TIME,        // xsd:time
    URI,         // xsd:anyURI
    LANG_STRING  // rdf:langString
}
```

### Common Constraints

```kotlin
// String constraints
ShaclPropertyShape(
    path = "name",
    dataType = ShaclDataType.STRING,
    required = true,
    minLength = 1,
    maxLength = 100,
    pattern = "^[a-zA-Z ]+$"
)

// Numeric constraints
ShaclPropertyShape(
    path = "age",
    dataType = ShaclDataType.INTEGER,
    minInclusive = 0.0,
    maxInclusive = 150.0
)

// Collection constraints
ShaclPropertyShape(
    path = "tags",
    minCount = 1,
    maxCount = 10
)

// Allowed values
ShaclPropertyShape(
    path = "status",
    dataType = ShaclDataType.STRING,
    allowedValues = listOf("active", "inactive", "pending")
)
```

### Complex Validation

```kotlin
// Email validation
val emailConstraints = ShaclPropertyShape(
    path = "email",
    dataType = ShaclDataType.STRING,
    required = true,
    pattern = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
    message = "Must be a valid email address"
)

// URL validation
val urlConstraints = ShaclPropertyShape(
    path = "website",
    dataType = ShaclDataType.URI,
    pattern = "^https?://.*"
)

// Date range validation
val dateConstraints = ShaclPropertyShape(
    path = "birthDate",
    dataType = ShaclDataType.DATE,
    maxInclusive = "2024-01-01"
)
```

## Persistence Backends

### RDF Store (Primary)

The primary persistence backend uses RDF (Resource Description Framework) for semantic data storage:

```kotlin
// Configuration for RDF store
val rdfConfig = PersistenceConfig(
    type = PersistenceType.RDF,
    connection = ConnectionConfig(
        url = "http://localhost:8080/rdf4j-server/repositories/activities",
        username = "admin",
        password = "password"
    )
)
```

### Relational Database

Traditional SQL database support:

```kotlin
val dbConfig = PersistenceConfig(
    type = PersistenceType.RELATIONAL,
    connection = ConnectionConfig(
        url = "jdbc:postgresql://localhost:5432/activities",
        username = "postgres",
        password = "password"
    ),
    pool = PoolConfig(
        minConnections = 5,
        maxConnections = 20
    )
)
```

### In-Memory Storage

Fast storage for testing and development:

```kotlin
val inMemoryPersistence = InMemoryActivityPersistence()
```

## Event System

The Activity Manager provides a comprehensive event system for monitoring:

```kotlin
activityManager.addEventListener(object : ActivityEventListener {
    override fun onActivityStarted(activity: Activity) {
        println("Activity started: ${activity.id}")
    }
    
    override fun onActivityCompleted(activity: Activity) {
        println("Activity completed: ${activity.id}")
    }
    
    override fun onActivityFailed(activity: Activity) {
        println("Activity failed: ${activity.id}")
    }
    
    override fun onActivityEvent(activity: Activity, event: ExecutionEvent) {
        when (event) {
            is ExecutionEvent.Progress -> {
                println("Progress: ${event.progress * 100}%")
            }
            is ExecutionEvent.Output -> {
                println("Output: ${event.output}")
            }
        }
    }
})
```

## Querying and Statistics

### Search Activities

```kotlin
// Search by status
val runningActivities = activityManager.searchActivities(
    ActivitySearchQuery(status = ActivityStatus.RUNNING)
)

// Search by time range
val recentActivities = activityManager.searchActivities(
    ActivitySearchQuery(
        fromTime = Instant.now().minus(Duration.ofHours(24)),
        toTime = Instant.now()
    )
)

// Search by correlation ID
val relatedActivities = activityManager.searchActivities(
    ActivitySearchQuery(correlationId = "session-123")
)
```

### Get Statistics

```kotlin
val stats = activityManager.getStatistics()
println("Total activities: ${stats.totalActivities}")
println("Success rate: ${stats.successRate * 100}%")
println("Average execution time: ${stats.averageExecutionTime}ms")
```

## Configuration

### ActivityManagerConfig

```kotlin
val config = ActivityManagerConfig(
    maxConcurrentActivities = 100,
    defaultExecutionTimeout = 300,
    enableAutoRetry = true,
    maxRetries = 3,
    retryDelay = 1000,
    enableMonitoring = true,
    monitoringInterval = 5000
)
```

### Execution Constraints

```kotlin
val constraints = ExecutionConstraints(
    maxExecutionTime = 300, // seconds
    maxMemoryUsage = 512, // MB
    allowParallel = true,
    requiredPermissions = listOf("activity:execute"),
    retryConfig = RetryConfig(
        maxRetries = 3,
        retryDelay = 1000,
        backoffMultiplier = 2.0
    ),
    timeoutConfig = TimeoutConfig(
        connectionTimeout = 30000,
        readTimeout = 60000,
        executionTimeout = 300000
    )
)
```

## Best Practices

### 1. Schema Design
- Use descriptive variable names and descriptions
- Define clear SHACL constraints for validation
- Set appropriate execution constraints and timeouts
- Add meaningful metadata and tags

### 2. SHACL Constraints
- Use appropriate data types for variables
- Define meaningful validation patterns
- Set reasonable min/max values
- Provide helpful error messages

### 3. Executor Implementation
- Implement proper error handling
- Provide meaningful progress updates
- Support cancellation and pause/resume
- Validate inputs using SHACL constraints

### 4. Persistence
- Choose the right backend for your use case
- Configure connection pools appropriately
- Monitor performance and health
- Implement backup and recovery strategies

### 5. Monitoring
- Use event listeners for real-time monitoring
- Implement proper logging
- Track key metrics and statistics
- Set up alerts for failures and timeouts

## Examples

See `ActivityManagerExample.kt` for a comprehensive example demonstrating:

- Schema creation with SHACL constraints
- Executor implementation and registration
- Activity execution and monitoring
- Querying and statistics
- Event handling

## Integration

The Activity Manager integrates with the broader Spatial Web Node architecture:

- **HSTP Engine**: Activities can be triggered by HSTP messages
- **Transport Layer**: Activities can communicate over various transport protocols
- **Identity System**: Activities can use DID-based authentication
- **Monitoring**: Integration with logging and metrics systems

## Future Enhancements

- **Workflow Engine**: Support for complex activity workflows
- **Distributed Execution**: Multi-node activity execution
- **Advanced Scheduling**: Cron-like scheduling and dependencies
- **Resource Management**: Advanced resource allocation and monitoring
- **Security**: Enhanced security and access control
- **Analytics**: Advanced analytics and reporting capabilities
- **SHACL Extensions**: Support for more advanced SHACL features 