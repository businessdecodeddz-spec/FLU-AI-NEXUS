package com.example

import com.example.core.monetization.NexusMonetizationManager
import com.example.core.monetization.NexusTier
import org.junit.Assert.*
import org.junit.Test

class MonetizationUnitTest {

    @Test
    fun testMonetizationInitialStateAndTierSelection() {
        val manager = NexusMonetizationManager()
        val initialAccount = manager.accountState.value

        assertEquals(NexusTier.PREMIUM, initialAccount.currentTier)
        assertEquals(10, manager.storeAgents.value.size)

        // Test non-negotiable free security features unlocked across all tiers
        assertTrue(manager.isFeatureUnlocked("CORE_SECURITY_DIAG"))
        assertTrue(manager.isFeatureUnlocked("PERMISSION_CONTROL"))
        assertTrue(manager.isFeatureUnlocked("AUTONOMY_KILL_SWITCH"))

        // Test tier switching
        manager.selectTier(NexusTier.DEVELOPER)
        assertEquals(NexusTier.DEVELOPER, manager.accountState.value.currentTier)
        assertTrue(manager.isFeatureUnlocked("AUTONOMOUS_AGENT"))
    }

    @Test
    fun testAgentStoreAcquisition() {
        val manager = NexusMonetizationManager()
        val legalAgent = manager.storeAgents.value.first { it.agentId == "AGT-LEG" }
        assertFalse(legalAgent.isAcquired)

        val success = manager.acquireAgent("AGT-LEG")
        assertTrue(success)

        val updatedAgent = manager.storeAgents.value.first { it.agentId == "AGT-LEG" }
        assertTrue(updatedAgent.isAcquired)
    }
}
