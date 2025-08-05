package com.geoknoesis.spatialweb.core.hstp.operation

import com.geoknoesis.spatialweb.core.hstp.engine.MessageContext

/**
 * Interface defining a contract for managing specific operations in the system.
 *
 * `OperationHandler` is designed to handle specific operation types identified by
 * their operation name. Implementations of this interface provide necessary logic to
 * process incoming message contexts corresponding to the operation they manage.
 */
interface OperationHandler {
    /**
     * Represents the operation type that this handler is responsible for managing.
     *
     * This property provides a specific identifier for the operation handled
     * by the implementation of the `OperationHandler` interface. It is used
     * to distinguish between various operations and ensure the appropriate
     * handler is invoked for a given context.
     */
    val operation: String
    /**
     * Handles an incoming message context as part of an operation.
     *
     * @param context The message context containing DID documents and related information
     */
    suspend fun handle(context: MessageContext)
}

