package com.geoknoesis.spatialweb.identity.vc

interface CredentialVerifier {
    suspend fun verify(credential: String): VerificationResult
}

sealed class VerificationResult {
    data class Success(val metadata: Map<String, Any?> = emptyMap()) : VerificationResult()
    data class Failure(val reason: String, val errors: List<String> = emptyList()) : VerificationResult()

    fun isSuccess(): Boolean = this is Success
    fun isFailure(): Boolean = this is Failure
}