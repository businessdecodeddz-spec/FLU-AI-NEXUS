package com.example.core

import com.example.data.local.NexusDao
import com.example.data.model.ActionRecordEntity
import com.example.data.model.ActionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 1. NEXUS CORE
 * Master Orchestrator coordinating all 10 Core Engines and 20 Departments.
 * Enforces the strict action lifecycle: PROPOSED -> AUTHORIZED -> EXECUTED -> VERIFIED.
 */
class NexusCore(val dao: NexusDao, val context: android.content.Context? = null) {

    val registry = ServiceRegistry()
    val actionRegistry = ActionRegistry()
    val cognitive = CognitiveEngine()
    val memory = NexusMemory(dao)
    val planner = NexusPlanner()
    val decision = DecisionEngine()
    val policy = PolicyEngine()
    val execution = ExecutionEngine()
    val verification = VerificationEngine()
    val learning = LearningEngine()
    val redTeam = RedTeamEngine()
    val metaAgent = com.example.core.agentic.MetaAgent(dao, registry)
    val storageGovernor = NexusStorageGovernor(dao)
    val predictiveBrain = PredictiveBrain(dao)
    val digitalTwin = DigitalTwin()
    val scientificDebate = ScientificDebateEngine()
    val cyberLab = CyberLab5Units()
    val universalDeveloper = UniversalDeveloper()
    val compatibilityTestEngine = com.example.core.adaptive.NexusCompatibilityTestEngine()
    val adaptiveEngine: com.example.core.adaptive.NexusAdaptiveEngine? = context?.let { com.example.core.adaptive.NexusAdaptiveEngine(it) }
    val monetizationManager = com.example.core.monetization.NexusMonetizationManager()

    private val _systemStatus = MutableStateFlow("NEXUS CORE: OPÉRATIONNEL (Intégrité 100%)")
    val systemStatus: StateFlow<String> = _systemStatus.asStateFlow()

    private val _cpuLoad = MutableStateFlow(14f)
    val cpuLoad: StateFlow<Float> = _cpuLoad.asStateFlow()

    private val _ramUsageMb = MutableStateFlow(1850)
    val ramUsageMb: StateFlow<Int> = _ramUsageMb.asStateFlow()

    suspend fun executePipeline(
        goal: String,
        targetDepartment: String = "PERFORMANCE_ENGINE",
        userApproval: Boolean = false
    ): ActionRecordEntity {
        // Step 1: PLANNER creates proposed action
        val plan = planner.createPlan(goal, targetDepartment)
        val initialAction = plan.steps.firstOrNull() ?: ActionRecordEntity(
            actionId = "ACT-" + System.currentTimeMillis().toString().takeLast(6),
            title = goal,
            departmentId = targetDepartment,
            engineId = "CORE",
            details = "Exécution directe de la commande",
            state = ActionState.PROPOSED
        )

        // Save proposed action to database
        val savedId = dao.insertAction(initialAction)

        // Step 2: DECISION ENGINE evaluates risk
        val assessment = decision.evaluateAction(initialAction, currentBatteryLevel = 85, availableRamMb = 1200)

        if (!assessment.isFeasible) {
            val failedAction = initialAction.copy(
                id = savedId,
                state = ActionState.FAILED,
                details = initialAction.details + " [ÉCHEC DÉCISION: ${assessment.recommendation}]"
            )
            dao.insertAction(failedAction)
            return failedAction
        }

        // Step 3: POLICY ENGINE checks authorization
        val authorizedAction = policy.authorizeAction(initialAction.copy(id = savedId), userApproval)

        if (authorizedAction.state == ActionState.CANCELLED) {
            dao.insertAction(authorizedAction)
            return authorizedAction
        }

        // Step 4: EXECUTION ENGINE runs authorized task
        val executedAction = execution.executeAction(authorizedAction)

        // Step 5: VERIFICATION ENGINE validates proof & hashes result
        val verifiedAction = verification.verifyResult(executedAction)

        // Save final verified state to Room DB
        dao.insertAction(verifiedAction)

        // Step 6: LEARNING ENGINE updates system status
        _systemStatus.value = "NEXUS CORE: Action '${verifiedAction.title}' vérifiée et journalisée."

        return verifiedAction
    }
}
