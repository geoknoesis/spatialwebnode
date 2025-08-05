package com.geoknoesis.spatialweb.core.hstp.operation

import java.util.ServiceLoader

/**
 * Manages the registration and retrieval of operation handlers for specific operation types.
 *
 * This class is responsible for maintaining a collection of `OperationHandler` instances,
 * which are registered based on the operation type they handle. Handlers can be registered
 * individually, in bulk, or dynamically loaded using Java SPI.
 *
 * The `OperationManager` allows for the resolution of a handler by its associated operation
 * type, enabling dynamic dispatch based on operation identifiers.
 */
class OperationManager {
    private val handlers = mutableMapOf<String, OperationHandler>()

    /**
     * Registers an operation handler for its corresponding operation type.
     *
     * This method associates the provided `OperationHandler` instance with its operation type
     * in the internal registry of handlers. If a handler for the same operation type already exists,
     * it will be replaced with the new handler.
     *
     * @param handler The `OperationHandler` instance to be registered.
     */
    fun register(handler: OperationHandler) {
        handlers[handler.operation] = handler
    }

    /**
     * Registers multiple operation handlers for their respective operation types.
     *
     * This method iterates through the provided list of `OperationHandler` instances
     * and registers each handler using the `register` method. It is useful for
     * bulk registration of handlers to manage various operations within the system.
     *
     * @param handlers A list of `OperationHandler` instances to be registered.
     */
    fun registerAll(handlers: List<OperationHandler>) {
        handlers.forEach { register(it) }
    }

    /**
     * Dynamically loads and registers operation handlers using the Java Service Provider Interface (SPI).
     *
     * This method utilizes `ServiceLoader` to discover implementations of the `OperationHandler` interface
     * available on the classpath. Each discovered handler is subsequently registered using the `register` method.
     *
     * The primary purpose of this method is to enable dynamic plugin-based extension of operation handlers
     * without requiring explicit instantiation or manual registration of each handler.
     */
    fun scanPlugins() {
        val loaded = ServiceLoader.load(OperationHandler::class.java)
        loaded.forEach { register(it) }
    }

    /**
     * Resolves an operation handler for a given operation type.
     *
     * This function retrieves the `OperationHandler` associated with the specified
     * operation type from the internal registry of handlers. If no handler is found for the
     * provided operation, it returns `null`.
     *
     * @param operation The operation type identifier used to retrieve the corresponding handler.
     * @return The `OperationHandler` corresponding to the given operation type, or `null` if no handler is registered.
     */
    fun resolve(operation: String): OperationHandler? = handlers[operation]
}
