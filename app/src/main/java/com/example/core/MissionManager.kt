package com.example.core

import android.content.Context
import com.example.core.agentic.*
import com.example.data.local.NexusDao
import com.example.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull

/**
 * NEXUS MISSION MANAGER
 * Orchestrates the complete mission lifecycle:
 * CREATED -> ANALYZING -> PLANNING -> AWAITING_APPROVAL -> EXECUTING -> VERIFYING -> COMPLETED / FAILED / CANCELLED
 *
 * Shares the centralized ActionRegistry with NexusCore and persists every mission state transition
 * into Room (NexusDao) so missions survive process termination and require observable SHA-256 proofs.
 */
class MissionManager(
    context: Context,
    private val dao: NexusDao,
    val registry: ActionRegistry = ActionRegistry(context)
) {
    val perception = PerceptionEngine(context)
    val contextEngine = ContextEngine(dao)
    val research = ResearchEngine()
    val reasoning = ReasoningEngine(registry)
    val planning = PlanningEngine()
    val decision = DecisionEngineAgent()
    val policy = PolicyEngineAgent()
    val execution = ExecutionEngineAgent(registry)
    val verification = VerificationEngineAgent()
    val learning = LearningEngineAgent(dao)

    private val _activeMission = MutableStateFlow<Mission?>(null)
    val activeMission: StateFlow<Mission?> = _activeMission.asStateFlow()

    private val _missionHistory = MutableStateFlow<List<Mission>>(emptyList())
    val missionHistory: StateFlow<List<Mission>> = _missionHistory.asStateFlow()

    private val _autonomyMode = MutableStateFlow(AutonomyMode.SUPERVISED)
    val autonomyMode: StateFlow<AutonomyMode> = _autonomyMode.asStateFlow()

    fun setAutonomyMode(mode: AutonomyMode) {
        _autonomyMode.value = mode
    }

    /**
     * Restores persisted missions from Room DB so mission history and state survive process restarts.
     */
    suspend fun restoreMissionsFromDatabase(): List<Mission> {
        val entities = dao.getAllMissions().firstOrNull() ?: emptyList()
        val restored = entities.map { entity ->
            val parsedState = try {
                MissionState.valueOf(entity.state)
            } catch (_: Exception) {
                MissionState.COMPLETED
            }
            val parsedMode = try {
                AutonomyMode.valueOf(entity.autonomyMode)
            } catch (_: Exception) {
                AutonomyMode.SUPERVISED
            }
            Mission(
                missionId = entity.missionId,
                objective = entity.objective,
                state = parsedState,
                autonomyMode = parsedMode,
                tasks = emptyList(),
                createdTimestamp = entity.createdTimestamp,
                completedTimestamp = entity.completedTimestamp,
                failureReason = entity.failureReason,
                finalReport = entity.finalReport,
                verificationSignature = entity.verificationSignature
            )
        }
        _missionHistory.value = restored
        return restored
    }

    suspend fun startNewMission(objective: String): Mission {
        val missionId = "MSN-" + System.currentTimeMillis().toString().takeLast(6)
        val mode = _autonomyMode.value

        // 1. CREATED
        var mission = Mission(
            missionId = missionId,
            objective = objective,
            state = MissionState.CREATED,
            autonomyMode = mode
        )
        _activeMission.value = mission
        learning.recordMissionLearning(mission)

        // 2. ANALYZING (Perception + Context + Research)
        mission = mission.copy(state = MissionState.ANALYZING)
        _activeMission.value = mission
        perception.perceiveSystemState()
        contextEngine.getActiveContext()
        research.analyzeRequirement(objective)

        // 3. PLANNING (Reasoning + Task Graph Generation)
        mission = mission.copy(state = MissionState.PLANNING)
        _activeMission.value = mission
        val selectedTools = reasoning.selectToolsForObjective(objective)
        val tasks = planning.buildTaskGraph(objective, selectedTools)
        mission = mission.copy(tasks = tasks)
        _activeMission.value = mission
        learning.recordMissionLearning(mission)

        // 4. DECISION & POLICY CHECK
        val needsManualApproval = mode == AutonomyMode.ASSISTED ||
                (mode == AutonomyMode.SUPERVISED && tasks.any { it.isDestructive })

        if (needsManualApproval) {
            mission = mission.copy(state = MissionState.AWAITING_APPROVAL)
            _activeMission.value = mission
            learning.recordMissionLearning(mission)
            return mission
        }

        // 5. EXECUTE MISSION
        return executeMissionTasks(mission, userApproved = false)
    }

    suspend fun approveAndResumeMission(): Mission? {
        val current = _activeMission.value ?: return null
        if (current.state != MissionState.AWAITING_APPROVAL) return current
        return executeMissionTasks(current, userApproved = true)
    }

    suspend fun cancelActiveMission() {
        val current = _activeMission.value ?: return
        val cancelled = current.copy(
            state = MissionState.CANCELLED,
            failureReason = "Mission annulée manuellement par l'utilisateur."
        )
        _activeMission.value = cancelled
        _missionHistory.value = listOf(cancelled) + _missionHistory.value.filter { it.missionId != cancelled.missionId }
        learning.recordMissionLearning(cancelled)
    }

    private suspend fun executeMissionTasks(initialMission: Mission, userApproved: Boolean): Mission {
        var mission = initialMission.copy(state = MissionState.EXECUTING)
        _activeMission.value = mission
        learning.recordMissionLearning(mission)

        val updatedTasks = mission.tasks.toMutableList()
        var missionFailed = false
        var failReason = ""

        for (i in updatedTasks.indices) {
            val task = updatedTasks[i]
            val tool = registry.getTool(task.toolId)

            if (tool == null) {
                updatedTasks[i] = task.copy(
                    isFailed = true,
                    resultSummary = "Outil '${task.toolId}' introuvable dans ActionRegistry.",
                    verificationProof = "VERIFICATION_FAILED_NO_TOOL"
                )
                missionFailed = true
                failReason = "Outil requis '${task.toolId}' non disponible."
                break
            }

            // Policy check
            if (!policy.validatePolicy(tool, userApproved)) {
                updatedTasks[i] = task.copy(
                    isFailed = true,
                    resultSummary = "Bloqué par le Policy Engine : Autorisation utilisateur requise."
                )
                missionFailed = true
                failReason = "Action destructive '${task.title}' bloquée sans validation explicite."
                break
            }

            // Execute via ActionRegistry
            val result = execution.executeTask(task)
            val proof = verification.verifyResult(result)
            val isProofValid = result.success &&
                    proof.startsWith("VERIFIED-SHA256:") &&
                    !proof.startsWith("VERIFICATION_FAILED")

            if (!isProofValid) {
                updatedTasks[i] = task.copy(
                    isCompleted = false,
                    isFailed = true,
                    resultSummary = result.errorMessage ?: "Preuve observable absente ou invalide ($proof)",
                    verificationProof = proof
                )
                missionFailed = true
                failReason = result.errorMessage ?: "Échec de vérification de preuve observable pour '${task.title}'"
                break
            } else {
                updatedTasks[i] = task.copy(
                    isCompleted = true,
                    isFailed = false,
                    resultSummary = result.dataSummary,
                    verificationProof = proof
                )
            }

            mission = mission.copy(tasks = updatedTasks.toList(), currentTaskIndex = i + 1)
            _activeMission.value = mission
        }

        // 6. VERIFYING
        mission = mission.copy(state = MissionState.VERIFYING)
        _activeMission.value = mission

        val completedCount = updatedTasks.count { it.isCompleted && it.verificationProof.startsWith("VERIFIED-SHA256:") }
        val totalCount = updatedTasks.size
        val allVerifiedWithObservableProof = !missionFailed && totalCount > 0 && completedCount == totalCount

        val finalState = if (allVerifiedWithObservableProof) MissionState.COMPLETED else MissionState.FAILED
        val report = if (!allVerifiedWithObservableProof) {
            "Échec de la mission : ${failReason.ifBlank { "Preuve observable manquante ($completedCount/$totalCount tâches vérifiées)." }}"
        } else {
            "Mission accomplie ($completedCount/$totalCount étapes vérifiées avec preuve observable).\n" +
                    updatedTasks.joinToString("\n") { "• ${it.title}: ${it.resultSummary} [${it.verificationProof}]" }
        }

        val combinedProofs = updatedTasks.joinToString("|") { it.verificationProof }
        val signature = if (allVerifiedWithObservableProof) {
            val digest = java.security.MessageDigest.getInstance("SHA-256")
                .digest("${mission.missionId}:$combinedProofs".toByteArray())
                .joinToString("") { "%02x".format(it) }.take(16).uppercase()
            "MSN-VERIFIED-SHA256:$digest"
        } else {
            "MSN-FAILED-VERIFY"
        }

        val completedMission = mission.copy(
            state = finalState,
            tasks = updatedTasks,
            completedTimestamp = System.currentTimeMillis(),
            failureReason = if (allVerifiedWithObservableProof) "" else failReason.ifBlank { "Vérification incomplète" },
            finalReport = report,
            verificationSignature = signature
        )

        _activeMission.value = completedMission
        _missionHistory.value = listOf(completedMission) + _missionHistory.value.filter { it.missionId != completedMission.missionId }

        // 7. LEARNING ENGINE records in Room DB
        learning.recordMissionLearning(completedMission)

        return completedMission
    }
}
