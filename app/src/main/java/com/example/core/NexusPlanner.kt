package com.example.core

import com.example.data.model.ActionRecordEntity
import com.example.data.model.ActionState

/**
 * 4. NEXUS PLANNER
 * Converts goals or requests into a sequence of executable ActionRecord items.
 */
class NexusPlanner {

    data class Plan(
        val planId: String,
        val goal: String,
        val steps: List<ActionRecordEntity>,
        val estimatedDurationMs: Long
    )

    fun createPlan(goal: String, targetDepartment: String = "PERFORMANCE_ENGINE"): Plan {
        val planId = "PLAN-" + System.currentTimeMillis().toString().takeLast(6)
        val steps = mutableListOf<ActionRecordEntity>()

        when {
            goal.contains("OPTIMIZE_RAM") || goal.contains("RAM") -> {
                steps.add(
                    ActionRecordEntity(
                        actionId = "$planId-01",
                        title = "Audit des Processus en Arrière-Plan",
                        departmentId = "PERFORMANCE_ENGINE",
                        engineId = "PLANNER",
                        details = "Inspection de l'utilisation mémoire des 20 départements",
                        state = ActionState.PROPOSED,
                        isDestructive = false
                    )
                )
                steps.add(
                    ActionRecordEntity(
                        actionId = "$planId-02",
                        title = "Accélération Turbocompte & Auto-Trim",
                        departmentId = "PERFORMANCE_ENGINE",
                        engineId = "EXECUTION",
                        details = "Exécution de l'algorithme Turbocompte pour compacter la RAM inactive",
                        state = ActionState.PROPOSED,
                        isDestructive = false
                    )
                )
            }
            goal.contains("CLEAN_STORAGE") || goal.contains("STORAGE") -> {
                steps.add(
                    ActionRecordEntity(
                        actionId = "$planId-01",
                        title = "Scan des Fichiers Temporaires & Doublons",
                        departmentId = "STORAGE_INTELLIGENCE",
                        engineId = "PLANNER",
                        details = "Recherche de caches expirés et fichiers isolés > 50 MB",
                        state = ActionState.PROPOSED,
                        isDestructive = false
                    )
                )
                steps.add(
                    ActionRecordEntity(
                        actionId = "$planId-02",
                        title = "Purge Sécurisée du Cache Applicatif",
                        departmentId = "STORAGE_INTELLIGENCE",
                        engineId = "EXECUTION",
                        details = "Suppression des fichiers temporaires résiduels autorisés",
                        state = ActionState.PROPOSED,
                        isDestructive = true
                    )
                )
            }
            goal.contains("SECURITY_SCAN") || goal.contains("SECURITY") -> {
                steps.add(
                    ActionRecordEntity(
                        actionId = "$planId-01",
                        title = "Vérification d'Intégrité System Guard",
                        departmentId = "SYSTEM_GUARD",
                        engineId = "POLICY",
                        details = "Contrôle des hashs de binaire système et de l'état SEAndroid",
                        state = ActionState.PROPOSED,
                        isDestructive = false
                    )
                )
                steps.add(
                    ActionRecordEntity(
                        actionId = "$planId-02",
                        title = "Audit des Connexions Cyber Defense",
                        departmentId = "CYBER_DEFENSE",
                        engineId = "EXECUTION",
                        details = "Inspection des sockets actives et du pare-feu applicatif",
                        state = ActionState.PROPOSED,
                        isDestructive = false
                    )
                )
            }
            else -> {
                steps.add(
                    ActionRecordEntity(
                        actionId = "$planId-01",
                        title = "Analyse Générique de l'Opération: $goal",
                        departmentId = targetDepartment,
                        engineId = "PLANNER",
                        details = "Évaluation initiale des dépendances et de la faisabilité",
                        state = ActionState.PROPOSED,
                        isDestructive = false
                    )
                )
            }
        }

        return Plan(planId, goal, steps, steps.size * 250L)
    }
}
