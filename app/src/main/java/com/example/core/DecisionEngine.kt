package com.example.core

import com.example.data.model.ActionRecordEntity

/**
 * 5. NEXUS DECISION ENGINE
 * Autonomous decision matrix evaluating action risk, battery state, memory availability,
 * and selecting whether an action requires explicit user authorization or automatic execution.
 */
class DecisionEngine {

    data class Assessment(
        val isFeasible: Boolean,
        val riskScore: Int, // 0 to 100
        val requiresUserApproval: Boolean,
        val recommendation: String
    )

    fun evaluateAction(action: ActionRecordEntity, currentBatteryLevel: Int, availableRamMb: Int): Assessment {
        var riskScore = 10
        var requiresApproval = action.isDestructive

        if (action.isDestructive) {
            riskScore += 50
        }

        if (currentBatteryLevel < 15) {
            riskScore += 20
        }

        if (availableRamMb < 400) {
            riskScore += 15
        }

        val feasible = riskScore < 85
        val rec = when {
            riskScore >= 85 -> "ACTION DÉCONSEILLÉE: Risque élevé ($riskScore/100). Ressources insuffisantes."
            requiresApproval -> "AUTORISATION REQUISE: Opération sensible ($riskScore/100). Confirmation requise."
            else -> "ACTION VALIDÉE: Risque faible ($riskScore/100). Exécution automatique autorisée."
        }

        return Assessment(
            isFeasible = feasible,
            riskScore = riskScore,
            requiresUserApproval = requiresApproval,
            recommendation = rec
        )
    }
}
