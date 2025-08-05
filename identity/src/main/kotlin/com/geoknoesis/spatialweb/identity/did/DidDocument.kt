package com.geoknoesis.spatialweb.identity.did

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.time.Instant

/**
 * Represents a Decentralized Identifier (DID) Document as per the W3C DID Core Specification.
 *
 * A DID Document is a JSON-LD document that contains information about a DID, such as the
 * public keys, authentication methods, authorization endpoints, and other metadata. This class
 * provides a structured representation of a DID Document with its required and optional fields.
 *
 * @property context The "@context" field of the DID document, providing the JSON-LD context(s).
 * @property id The unique identifier (DID) associated with this DID Document.
 * @property controller A list of DIDs or entities that are capable of exercising control over the DID subject.
 * @property alsoKnownAs A list of other identifiers or URLs that are associated with the DID subject.
 *
 * @property verificationMethod A list of verification methods (e.g., cryptographic keys) that can be used
 * to prove control of the DID or authenticate operations on its behalf.
 *
 * @property authentication A list of verification relationships that specify which verification methods
 * can be used for DID authentication.
 * @property assertionMethod A list of verification relationships for asserting claims about the DID subject.
 * @property keyAgreement A list of verification relationships used for key exchange operations, enabling
 * encrypted communication.
 * @property capabilityInvocation A list of verification relationships for invoking capabilities, typically
 * related to delegation and control operations.
 * @property capabilityDelegation A list of verification relationships for delegating capabilities
 * to other controllers or entities.
 *
 * @property service A list of service endpoints associated with the DID document that provide references
 * to external services or metadata.
 */
@Serializable
data class DidDocument(
    val context: List<String>? = null,
    val id: String,
    val controller: List<String>? = null,
    val alsoKnownAs: List<String>? = null,

    val verificationMethod: List<VerificationMethod>? = null,

    val authentication: List<VerificationRelationship>? = null,
    val assertionMethod: List<VerificationRelationship>? = null,
    val keyAgreement: List<VerificationRelationship>? = null,
    val capabilityInvocation: List<VerificationRelationship>? = null,
    val capabilityDelegation: List<VerificationRelationship>? = null,

    val service: List<ServiceEndpoint>? = null
)

/**
 * Represents a verification method used within a DID Document.
 *
 * This class models the properties of a verification method as specified
 * in the W3C DID Core specification. It includes a variety of public key representations
 * (e.g., Multibase, JWK, Base58, Hex, PEM) to ensure compatibility with different
 * cryptographic systems and formats.
 *
 * Properties:
 * - `id` is the unique identifier for the verification method.
 * - `type` specifies the type of cryptographic method used (e.g., Ed25519VerificationKey2020).
 * - `controller` refers to the DID that controls this verification method.
 * - `publicKeyMultibase` is an optional property representing the public key in Multibase encoding.
 * - `publicKeyJwk` is an optional property representing the public key in JSON Web Key (JWK) format.
 * - `publicKeyBase58` is an optional property representing the public key in Base58 encoding.
 * - `publicKeyHex` is an optional property representing the public key in hexadecimal encoding.
 * - `publicKeyPem` is an optional property representing the public key in PEM format.
 *
 * This class is serializable using Kotlin serialization.
 */
@Serializable
data class VerificationMethod(
    val id: String,
    val type: String,
    val controller: String,
    val publicKeyMultibase: String? = null,
    val publicKeyJwk: JsonObject? = null,
    val publicKeyBase58: String? = null,
    val publicKeyHex: String? = null,
    val publicKeyPem: String? = null
)

/**
 * Represents a relationship used for verification purposes.
 *
 * A `VerificationRelationship` can either reference an identifier or contain a detailed
 * verification method. This sealed class provides a way to define distinct types of
 * verification relationships to ensure compatibility with DID standards and practices.
 *
 * Subclasses:
 * - `Reference`: Contains a reference string representing a relationship identifier.
 * - `Method`: Contains a `VerificationMethod` object that defines a verification strategy.
 */
@Serializable
sealed class VerificationRelationship {
    /**
     * Represents a reference-based verification relationship.
     *
     * This class is a specific implementation of the `VerificationRelationship` sealed class,
     * used to define relationships in which verification is established via a reference.
     *
     * @property ref The reference string used for verification.
     */
    @Serializable
    data class Reference(val ref: String) : VerificationRelationship()

    /**
     * Represents a specific type of verification relationship within a DID Document.
     *
     * This class models a relationship that references a `VerificationMethod` directly,
     * which is commonly used to link cryptographic keys or similar verification capabilities
     * to the DID Document for purposes such as authentication, key agreement, or assertion.
     *
     * @property method The verification method associated with this relationship
     */
    @Serializable
    data class Method(val method: VerificationMethod) : VerificationRelationship()
}

/**
 * Represents a service endpoint as part of a verifiable credential or DID document.
 *
 * This data class encapsulates details of a service endpoint, including its unique identifier,
 * the types of services it supports, the endpoint URI, an optional profile, and any additional
 * properties associated with the service.
 *
 * Key attributes:
 * - `id`: A unique string identifier for the service endpoint.
 * - `type`: A list of service types associated with this endpoint.
 * - `serviceEndpoint`: The URI or URL representing where the service can be accessed.
 * - `profile`: An optional string describing the profile of the service.
 * - `additionalProperties`: A map for storing extra properties as key-value pairs, supporting JSON structures.
 */
@Serializable
data class ServiceEndpoint(
    val id: String,
    val type: List<String>,
    val serviceEndpoint: String,
    val profile: String? = null,
    val additionalProperties: Map<String, JsonElement?> = emptyMap()
)



private val jsonFormat = Json { ignoreUnknownKeys = true }
/**
 * Extension function to convert a JsonElement (from Walt.id) to a typed DidDocument.
 */
fun JsonElement.toDidDocument(): DidDocument {
    return jsonFormat.decodeFromJsonElement(DidDocument.serializer(), this)
}

/**
 * Extension function to convert a JSON string directly to a typed DidDocument.
 */
fun String.toDidDocument(): DidDocument {
    return jsonFormat.decodeFromString(this)
}


fun JsonElement.extractDidDocumentMetadata(): DidDocumentManager.Metadata {
    val obj = this.jsonObject
    val meta = obj["didDocumentMetadata"]?.jsonObject

    val updated = meta?.get("updated")?.jsonPrimitive?.contentOrNull?.let(Instant::parse)
    val versionId = meta?.get("versionId")?.jsonPrimitive?.contentOrNull
    val deactivated = meta?.get("deactivated")?.jsonPrimitive?.booleanOrNull ?: false
    val fetchedAt = Instant.now()

    return DidDocumentManager.Metadata(
        updated = updated,
        versionId = versionId,
        deactivated = deactivated,
        fetchedAt = fetchedAt
    )
}