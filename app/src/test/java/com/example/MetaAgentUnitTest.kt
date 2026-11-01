package com.example

import com.example.core.ActionRegistry
import com.example.core.ServiceRegistry
import com.example.core.agentic.MetaAgent
import com.example.core.agentic.MetaAgentStage
import com.example.data.local.NexusDao
import com.example.data.model.ActionRecordEntity
import com.example.data.model.AutomationEntity
import com.example.data.model.NexusMemoryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class MetaAgentUnitTest {

    @Test
    fun metaAgent_executes9StepSynthesisFlowAndAwaitsUserAuthorization(): Unit = runBlocking {
        val serviceRegistry = ServiceRegistry()
        val actionRegistry = ActionRegistry()
        val metaAgent = MetaAgent(
            dao = FakeNexusDao(),
            serviceRegistry = serviceRegistry
        )

        val objective = "Capturer des données de vision nocturne caméra"

        // Process capability gap
        val proposal = metaAgent.processUnfulfilledObjective(objective, actionRegistry)

        assertNotNull("Meta-Agent proposal should not be null", proposal)
        assertEquals(MetaAgentStage.AWAITING_USER_VALIDATION, metaAgent.currentStage.value)
        assertEquals(MetaAgentStage.AWAITING_USER_VALIDATION, proposal?.stage)
        assertTrue("Proposal should pass Android security model audit", proposal?.auditReport?.passedAndroidSecurityModel == true)
        assertTrue("Proposal must require explicit user authorization", proposal?.stage == MetaAgentStage.AWAITING_USER_VALIDATION)

        // Approve and integrate
        val proposalId = proposal!!.proposalId
        val integrated = metaAgent.approveAndIntegrateModule(proposalId, actionRegistry)

        assertTrue("Module should be integrated successfully", integrated)
        assertEquals(MetaAgentStage.INTEGRATED, metaAgent.currentStage.value)
        assertNotNull("Dynamic tool should be registered in ActionRegistry", actionRegistry.getTool(proposalId.lowercase()))
    }
}

// Fake DAO implementation for JVM Unit Testing
private class FakeNexusDao : NexusDao {
    override suspend fun insertAction(action: ActionRecordEntity): Long = 1L
    override fun getAllActions(): Flow<List<ActionRecordEntity>> = flowOf(emptyList())
    override suspend fun updateActionState(id: Long, state: String, proof: String, executionTimeMs: Long) {}
    override suspend fun getActionCount(): Int = 0

    override fun getAllMemory(): Flow<List<NexusMemoryEntity>> = flowOf(emptyList())
    override fun searchMemory(query: String): Flow<List<NexusMemoryEntity>> = flowOf(emptyList())
    override suspend fun insertMemory(memory: NexusMemoryEntity): Long = 1L
    override suspend fun deleteMemory(memory: NexusMemoryEntity) {}

    override fun getAllAutomations(): Flow<List<AutomationEntity>> = flowOf(emptyList())
    override suspend fun insertAutomation(automation: AutomationEntity): Long = 1L
    override suspend fun setAutomationEnabled(id: Long, isEnabled: Boolean) {}
    override suspend fun deleteAutomation(automation: AutomationEntity) {}

    override fun getAllBatteryStats(): Flow<List<com.example.data.model.BatteryStatEntity>> = flowOf(emptyList())
    override fun getHighDrainAlerts(): Flow<List<com.example.data.model.BatteryStatEntity>> = flowOf(emptyList())
    override suspend fun insertBatteryStat(stat: com.example.data.model.BatteryStatEntity): Long = 1L
    override suspend fun trimOldBatteryStats() {}
    override fun getAllMissions(): Flow<List<com.example.data.model.MissionEntity>> = flowOf(emptyList())
    override suspend fun insertMission(mission: com.example.data.model.MissionEntity): Long = 1L
    override suspend fun getCompletedMissionCount(): Int = 0
}
