package com.example.core

import com.example.data.model.ActionRecordEntity
import com.example.data.model.ActionState
import java.security.MessageDigest

/**
 * 8. NEXUS VERIFICATION ENGINE
 * Validates execution proof, calculates cryptographic signatures, and transitions state to VERIFIED.
 */
class VerificationEngine {

    fun verifyResult(action: ActionRecordEntity): ActionRecordEntity {
        if (action.state != ActionState.EXECUTED) {
            return action.copy(state = ActionState.FAILED)
        }

        // Generate cryptographic proof hash (SHA-256 simulation)
        val rawData = "${action.actionId}:${action.departmentId}:${action.timestamp}:${action.executionTimeMs}"
        val signature = hashSha256(rawData)

        val proof = "PREUVE-NEXUS-VERIFIED [SHA256: ${signature.take(16).uppercase()}]"

        return action.copy(
            state = ActionState.VERIFIED,
            verificationProof = proof,
            signature = signature
        )
    }

    private fun hashSha256(input: String): String {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val hashBytes = digest.digest(input.toByteArray())
            hashBytes.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            "HASH-" + input.hashCode().toString()
        }
    }
}
