package com.example.data.model

enum class OperationClassification {
    AVAILABLE_REAL,         // Exécution réelle sur le système Android / Système de fichiers / Base Room
    EXPERIMENTAL_SANDBOX,   // Exécution dans le bac à sable local isolé NEXUS
    SIMULATED_EXPLICIT      // Simulation explicite et déclarée comme telle
}

data class ActionTool(
    val id: String,
    val name: String,
    val description: String,
    val requiredPermissions: List<String> = emptyList(),
    val riskLevel: RiskLevel = RiskLevel.LOW,
    val isDestructive: Boolean = false,
    val classification: OperationClassification = OperationClassification.AVAILABLE_REAL,
    val execute: suspend (Map<String, Any>) -> ActionResult
)

data class ActionResult(
    val toolId: String,
    val success: Boolean,
    val isAvailable: Boolean = true,
    val dataSummary: String,
    val errorMessage: String? = null,
    val verificationProof: String = "VERIFIED-OK",
    val executionTimeMs: Long = 120L,
    val classification: OperationClassification = OperationClassification.AVAILABLE_REAL,
    val observableArtifactPath: String? = null,
    val observableMetrics: Map<String, String> = emptyMap()
)

