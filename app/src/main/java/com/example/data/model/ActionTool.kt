package com.example.data.model

data class ActionTool(
    val id: String,
    val name: String,
    val description: String,
    val requiredPermissions: List<String> = emptyList(),
    val riskLevel: RiskLevel = RiskLevel.LOW,
    val isDestructive: Boolean = false,
    val execute: suspend (Map<String, Any>) -> ActionResult
)

data class ActionResult(
    val toolId: String,
    val success: Boolean,
    val isAvailable: Boolean = true,
    val dataSummary: String,
    val errorMessage: String? = null,
    val verificationProof: String = "VERIFIED-OK",
    val executionTimeMs: Long = 120L
)
