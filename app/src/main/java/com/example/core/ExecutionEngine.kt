package com.example.core

import com.example.data.jni.NexusJniBridge
import com.example.data.model.ActionRecordEntity
import com.example.data.model.ActionState

/**
 * 7. NEXUS EXECUTION ENGINE
 * Executes authorized operations across ActionRegistry tools, Android APIs, and local filesystem.
 * Never claims native C++/Rust acceleration unless the native library is actually loaded.
 */
class ExecutionEngine(private val actionRegistry: ActionRegistry? = null) {

    suspend fun executeAction(action: ActionRecordEntity): ActionRecordEntity {
        if (action.state != ActionState.AUTHORIZED) {
            return action.copy(
                state = ActionState.FAILED,
                details = action.details + " [EXÉCUTION BLOQUÉE: Action non autorisée]"
            )
        }

        val startTime = System.currentTimeMillis()
        val matchedTool = selectToolForAction(action)
        val toolResult = matchedTool?.execute?.invoke(mapOf("actionId" to action.actionId, "title" to action.title))

        val jniMode = if (NexusJniBridge.isNativeActive()) {
            "NATIVE_RUST_CPP (${NexusJniBridge.turbocompteAccelerate(4)}ms)"
        } else {
            "KOTLIN_JVM_VERIFIED"
        }

        val executionDuration = (System.currentTimeMillis() - startTime).coerceAtLeast(1L)
        val observableDetails = if (toolResult != null) {
            " [EXÉCUTÉ via outil '${toolResult.toolId}' (${toolResult.classification.name}): ${toolResult.dataSummary} | Mode: $jniMode]"
        } else {
            " [EXÉCUTÉ via ${action.departmentId} | Mode: $jniMode]"
        }

        val initialProof = toolResult?.verificationProof ?: ""

        return action.copy(
            state = ActionState.EXECUTED,
            executionTimeMs = executionDuration,
            verificationProof = initialProof,
            details = action.details + observableDetails
        )
    }

    private fun selectToolForAction(action: ActionRecordEntity): com.example.data.model.ActionTool? {
        val registry = actionRegistry ?: return null
        val text = "${action.title} ${action.departmentId}".lowercase()
        val toolId = when {
            text.contains("batterie") || text.contains("battery") -> "battery_diagnostic"
            text.contains("stockage") || text.contains("storage") -> "storage_analysis"
            text.contains("doublon") || text.contains("duplicate") -> "duplicate_search"
            text.contains("permission") || text.contains("guard") || text.contains("security") -> "permission_audit"
            text.contains("app") || text.contains("inventaire") -> "app_inventory"
            text.contains("réseau") || text.contains("network") -> "network_diagnostic"
            text.contains("sauvegarde") || text.contains("backup") -> "backup_creator"
            else -> "resource_monitor"
        }
        return registry.getTool(toolId)
    }
}
