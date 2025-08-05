package com.geoknoesis.spatialweb.core.hstp.engine

import com.geoknoesis.spatialweb.core.hstp.interceptor.MessageInterceptor
import com.geoknoesis.spatialweb.core.hstp.model.HSTPMessage
import com.geoknoesis.spatialweb.core.hstp.operation.OperationManager

/**
 * Represents the HSTP message processing pipeline.
 *
 * The pipeline sequentially applies a list of message interceptors,
 * and finally dispatches the message to an operation handler resolved from the engine.
 */
class HSTPPipeline(
    private val interceptors: List<MessageInterceptor>,
    private val engine: HSTPEngine
) {
    suspend fun run(message: HSTPMessage) {
        val context = MessageContext(message, engine)
        execute(0, context)(context)
    }

    private fun execute(index: Int, context: MessageContext): suspend (MessageContext) -> Unit {
        return if (index < interceptors.size) {
            { ctx -> interceptors[index].intercept(ctx, execute(index + 1, ctx)) }
        } else {
            { ctx ->
                val operation = ctx.message.header.operation
                val handler = engine.operationManager.resolve(operation)
                    ?: error("No OperationHandler found for operation: $operation")
                handler.handle(ctx)
            }
        }
    }
}

/**
 * Builder for constructing an [HSTPPipeline] instance using a declarative DSL.
 *
 * Allows configuration of:
 * - Interceptors that are applied in sequence.
 * - The [OperationManager] used to dispatch the final operation.
 */
class HSTPPipelineBuilder(private val engine: HSTPEngine) {
    private val interceptors = mutableListOf<MessageInterceptor>()

    fun use(interceptor: MessageInterceptor) {
        interceptors += interceptor
    }

    fun build(): HSTPPipeline {
        return HSTPPipeline(interceptors, engine)
    }
}

/**
 * DSL entry point for building an [HSTPPipeline].
 *
 * @param engine The HSTP engine providing shared components like [OperationManager].
 * @param init   Configuration block to register interceptors.
 * @return A fully configured [HSTPPipeline].
 */
fun hstpPipeline(engine: HSTPEngine, init: HSTPPipelineBuilder.() -> Unit): HSTPPipeline {
    return HSTPPipelineBuilder(engine).apply(init).build()
}
