package com.example

import com.example.data.jni.NexusJniBridge
import com.example.data.model.ActionRecordEntity
import com.example.data.model.ActionState
import com.example.core.DecisionEngine
import com.example.core.PolicyEngine
import com.example.core.VerificationEngine
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun nexusJniBridge_fallbackIsActive() {
        assertNotNull(NexusJniBridge.getBridgeStatus())
        assertTrue(NexusJniBridge.getBridgeStatus().contains("JNI READY") || NexusJniBridge.getBridgeStatus().contains("ACTIVE"))
    }

    @Test
    fun policyEngine_requiresAuthorizationForDestructiveActions() {
        val policy = PolicyEngine()
        val action = ActionRecordEntity(
            actionId = "TEST-01",
            title = "Purger le stockage",
            departmentId = "STORAGE_INTELLIGENCE",
            engineId = "TEST",
            details = "Test purge",
            state = ActionState.PROPOSED,
            isDestructive = true
        )

        val unapprovedResult = policy.authorizeAction(action, userGrantedApproval = false)
        assertEquals(ActionState.CANCELLED, unapprovedResult.state)

        val approvedResult = policy.authorizeAction(action, userGrantedApproval = true)
        assertEquals(ActionState.AUTHORIZED, approvedResult.state)
    }

    @Test
    fun verificationEngine_generatesProofHash() {
        val verifier = VerificationEngine()
        val executedAction = ActionRecordEntity(
            actionId = "TEST-02",
            title = "Test Action",
            departmentId = "PERFORMANCE_ENGINE",
            engineId = "EXECUTION",
            details = "Test execution",
            state = ActionState.EXECUTED,
            executionTimeMs = 120
        )

        val verified = verifier.verifyResult(executedAction)
        assertEquals(ActionState.VERIFIED, verified.state)
        assertTrue(verified.verificationProof.contains("SHA256"))
    }
}
