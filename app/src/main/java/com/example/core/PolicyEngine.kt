package com.example.core

import com.example.data.model.ActionRecordEntity
import com.example.data.model.ActionState

/**
 * 6. NEXUS POLICY ENGINE
 * Security, permission verification, and authorization management.
 */
class PolicyEngine {

    fun checkPolicy(action: ActionRecordEntity, userGrantedApproval: Boolean = false): Boolean {
        // Destructive operations strictly require user authorization
        if (action.isDestructive && !userGrantedApproval) {
            return false
        }
        return true
    }

    fun authorizeAction(action: ActionRecordEntity, userGrantedApproval: Boolean = false): ActionRecordEntity {
        val allowed = checkPolicy(action, userGrantedApproval)
        return if (allowed) {
            action.copy(state = ActionState.AUTHORIZED)
        } else {
            action.copy(
                state = ActionState.CANCELLED,
                details = action.details + " [REFUSÉ PAR POLICY ENGINE: Autorisation utilisateur manquante]"
            )
        }
    }
}
