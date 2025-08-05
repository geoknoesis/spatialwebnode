package com.geoknoesis.spatialweb.core.hstp.interceptor

import com.geoknoesis.spatialweb.core.hstp.engine.MessageContext
import org.slf4j.LoggerFactory

class LoggingInterceptor : MessageInterceptor {

    private val logger = LoggerFactory.getLogger(LoggingInterceptor::class.java)

    override suspend fun intercept(
        context: MessageContext,
        next: suspend (MessageContext) -> Unit
    ) {
        val header = context.message.header
        logger.info("🟢 Received HSTP message: operation='${header.operation}', from='${header.source}', to='${header.destination}'")

        try {
            next(context)
            logger.info("✅ Successfully processed operation='${header.operation}'")
        } catch (ex: Exception) {
            logger.error("❌ Failed processing operation='${header.operation}': ${ex.message}", ex)
            throw ex
        }
    }
}