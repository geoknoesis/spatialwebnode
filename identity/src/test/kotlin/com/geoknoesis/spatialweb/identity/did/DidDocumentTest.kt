package com.geoknoesis.spatialweb.identity.did

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class DidDocumentTest {

    private val fullDidJson = """
        {
          "@context": ["https://www.w3.org/ns/did/v1"],
          "id": "did:example:123456789abcdefghi",
          "controller": ["did:example:controller"],
          "alsoKnownAs": ["https://example.com/user/1234"],
          "verificationMethod": [
            {
              "id": "did:example:123456789abcdefghi#keys-1",
              "type": "Ed25519VerificationKey2018",
              "controller": "did:example:123456789abcdefghi",
              "publicKeyMultibase": "z6MkoH2...",
              "publicKeyJwk": {
                "kty": "OKP",
                "crv": "Ed25519",
                "x": "VCpo2LMLhn6iWku8MKvSLg2ZAoC-nlOyPVQaO3FxVeQ"
              }
            }
          ],
          "authentication": [
            {
              "type": "Reference",
              "ref": "did:example:123456789abcdefghi#keys-1"
            }
          ],
          "assertionMethod": [
            {
              "type": "Reference",
              "ref": "did:example:123456789abcdefghi#keys-1"
            }
          ],
          "keyAgreement": [
            {
              "type": "Method",
              "method": {
                "id": "did:example:123456789abcdefghi#key-agree-1",
                "type": "X25519KeyAgreementKey2019",
                "controller": "did:example:123456789abcdefghi",
                "publicKeyBase58": "GfH3Tn8U9o..."
              }
            }
          ],
          "capabilityInvocation": [
            {
              "type": "Reference",
              "ref": "did:example:123456789abcdefghi#keys-1"
            }
          ],
          "capabilityDelegation": [
            {
              "type": "Reference",
              "ref": "did:example:123456789abcdefghi#keys-1"
            }
          ],
          "service": [
            {
              "id": "#agent",
              "type": ["AgentService"],
              "serviceEndpoint": "https://agent.example.com/8377464",
              "profile": "hstp:relay"
            }
          ]
        }
    """.trimIndent()

    @Test
    fun `test complete DID document parsing`() {
        val doc = fullDidJson.toDidDocument()
        assertEquals("did:example:123456789abcdefghi", doc.id)
        assertEquals("did:example:controller", doc.controller?.first())
        assertEquals("https://example.com/user/1234", doc.alsoKnownAs?.first())
        assertEquals("z6MkoH2...", doc.verificationMethod?.first()?.publicKeyMultibase)

        val auth = doc.authentication?.first() as? VerificationRelationship.Reference
        assertNotNull(auth)
        assertEquals("did:example:123456789abcdefghi#keys-1", auth.ref)

        assertEquals(
            "did:example:123456789abcdefghi#keys-1",
            (doc.assertionMethod?.first() as? VerificationRelationship.Reference)?.ref
        )
        assertEquals(
            "did:example:123456789abcdefghi#keys-1",
            (doc.capabilityInvocation?.first() as? VerificationRelationship.Reference)?.ref
        )

        val service = doc.service?.first()
        assertNotNull(service)
        assertEquals("AgentService", service.type.first())
        assertEquals("hstp:relay", service.profile)
    }
}
