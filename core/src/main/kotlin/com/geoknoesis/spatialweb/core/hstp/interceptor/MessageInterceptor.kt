package com.geoknoesis.spatialweb.core.hstp.interceptor

import com.geoknoesis.spatialweb.core.hstp.engine.MessageContext

/**
 * Represents an interceptor in the HSTP framework that processes messages during their lifecycle.
 *
 * This interface defines a contract for intercepting and potentially modifying a `MessageContext`
 * object during message transmission. Implementations can execute additional logic before and after
 * delegating the processing to the next interceptor in the chain.
 *
 * Interceptors are executed in the order they are added to the pipeline. Each interceptor can decide
 * whether to pass control to the next interceptor or terminate the chain.
 *
 * @see MessageContext
 * @see HSTPPipeline
 */
interface MessageInterceptor {
    suspend fun intercept(context: MessageContext, next: suspend (MessageContext) -> Unit)
}