package com.example.core

import android.content.Context
import com.example.core.agentic.*
import com.example.data.local.NexusDao
import com.example.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.security.MessageDigest

/**
 "MISSION MANAGER"
 Autonomous Agentic Mission Manager.
 Decomposes goals into subtasks, resolves tool dependencies, manages lifecycle states:
 CREATED -> ANALYZING -> PLANNING -> AWAITING_APPROVAL -> EXECUTING -> VERIFYING -> COMPLETED / FAILED / CANCELLED.
 */
class MissionManager(
    private val context: Context,
    private val dao: NexusDao
) {

    val registry = ActionRegistry(context)

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

    private val _missionLogs = MutableStateFlow<List<String>>(emptyList())
    val missionLogs: StateFlow<List<String>> = _missionLogs.asStateFlow()

    suspend fun createAndRunMission(
        objective: String,
        mode: AutonomyMode = AutonomyMode.SUPERVISED,
        userApproved: Boolean = false
    ): Mission {
        val missionId = "MIS-" + System.currentTimeMillis().toString().takeLast(6)
        var mission = Mission(
            missionId = missionId,
            objective = objective,
            state = MissionState.CREATED,
            autonomyMode = mode
        )
        _activeMission.value = mission
        log("MISSION CREATED [$missionId]: '$objective' (Mode: ${mode.name})")

        // 1. ANALYZING Phase
        mission = mission.copy(state = MissionState.ANALYZING)
        _activeMission.value = mission
        log("ANALYZING: Inspection du contexte système et recherche d'outils...")

        perception.perceiveSystemState()
        research.analyzeRequirement(objective)

        val selectedTools = reasoning.selectToolsForObjective(objective)

        // 2. PLANNING Phase
        mission = mission.copy(state = MissionState.PLANNING)
        _activeMission.value = mission
        log("PLANNING: Construction du graphe de dépendance (${selectedTools.size} outils sélectionnés)...")

        val subtasks = planning.buildTaskGraph(objective, selectedTools)
        mission = mission.copy(tasks = subtasks)
        _activeMission.value = mission

        // Check if approval is needed based on AutonomyMode or Destructive tasks
        val needsApproval = subtasks.any { it.isDestructive } ||
                mode == AutonomyMode.ASSISTED ||
                (mode == AutonomyMode.SUPERVISED && !userApproved)

        if (needsApproval && !userApproved) {
            mission = mission.copy(state = MissionState.AWAITING_APPROVAL)
            _activeMission.value = mission
            log("AWAITING_APPROVAL: Mission en attente de la validation utilisateur pour exécution.")
            return mission
        }

        // 3. EXECUTING Phase
        mission = mission.copy(state = MissionState.EXECUTING)
        _activeMission.value = mission
        log("EXECUTING: Lancement de l'exécution séquentielle des sous-tâches...")

        val updatedTasks = mutableListOf<MissionTask>()
        var hasError = false

        for ((index, task) in subtasks.withIndex()) {
            log("EXECUTING TASK [${task.taskId}]: ${task.title}")
            val result = execution.executeTask(task)

            if (result.success) {
                val proof = verification.verifyResult(result)
                val completedTask = task.copy(
                    isCompleted = true,
                    resultSummary = result.dataSummary,
                    verificationProof = proof
                )
                updatedTasks.add(completedTask)
                mission = mission.copy(tasks = updatedTasks.toList(), currentTaskIndex = index + 1)
                _activeMission.value = mission
                log("TASK COMPLETED [${task.taskId}]: ${result.dataSummary} ($proof)")
            } else {
                hasError = true
                val errSummary = result.errorMessage ?: "Erreur inconnue lors de l'exécution de la tâche"
                val failedTask = task.copy(
                    isFailed = true,
                    resultSummary = errSummary
                )
                updatedTasks.add(failedTask)
                mission = mission.copy(
                    tasks = updatedTasks.toList(),
                    state = MissionState.FAILED,
                    failureReason = errSummary
                )
                _activeMission.value = mission
                log("TASK FAILED [${task.taskId}]: $errSummary")
                break
            }
        }

        if (hasError) {
            learning.recordMissionLearning(mission)
            return mission
        }

        // 4. VERIFYING Phase
        mission = mission.copy(state = MissionState.VERIFYING)
        _activeMission.value = mission
        log("VERIFYING: Évaluation globale de la mission et signature du rapport final...")

        val reportBuilder = StringBuilder()
        reportBuilder.append("RAPPORT DE MISSION FINALE [$missionId]\n")
        reportBuilder.append("• Objectif: $objective\n")
        reportBuilder.append("• Mode: ${mode.name}\n")
        reportBuilder.append("• Tâches Exécutées: ${updatedTasks.size}/${subtasks.size}\n\n")
        updatedTasks.forEach { t ->
            reportBuilder.append("✓ [${t.taskId}] ${t.resultSummary}\n  (Preuve: ${t.verificationProof})\n")
        }

        val rawSignature = "$missionId:$objective:${updatedTasks.size}:${System.currentTimeMillis()}"
        val finalSignature = "SHA256-MISSION:" + hashSha256(rawSignature).take(16).uppercase()

        mission = mission.copy(
            state = MissionState.COMPLETED,
            completedTimestamp = System.currentTimeMillis(),
            finalReport = reportBuilder.toString(),
            verificationSignature = finalSignature
        )
        _activeMission.value = mission
        log("MISSION COMPLETED [$missionId]: Tout les objectifs ont été vérifiés. Signature: $finalSignature")

        // 5. LEARNING Phase
        learning.recordMissionLearning(mission)

        return mission
    }

    suspend fun approveAndResumeMission(userApproved: Boolean): Mission? {
        val current = _activeMission.value ?: return null
        if (current.state != MissionState.AWAITING_APPROVAL) return current

        return if (userApproved) {
            log("USER APPROVED: Reprise de la mission [${current.missionId}]...")
            createAndRunMission(current.objective, current.autonomyMode, userApproved = true)
        } else {
            val cancelled = current.copy(
                state = MissionState.CANCELLED,
                failureReason = "Annulée par l'utilisateur"
            )
            _activeMission.value = cancelled
            log("USER CANCELLED: Mission [${current.missionId}] annulée.")
            learning.recordMissionLearning(cancelled)
            cancelled
        }
    }

    private fun log(message: String) {
        _missionLogs.value = _missionLogs.value + message
    }

    private fun hashSha256(input: String): String {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val hashBytes = digest.digest(input.toByteArray())
            hashBytes.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            "HASH-" + input.hashCode()
        }
    }
}
