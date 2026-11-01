package com.example.data.model

enum class RiskLevel {
    CRITICAL,
    HIGH,
    MEDIUM,
    LOW
}

data class AppRiskInfo(
    val packageName: String,
    val appName: String,
    val riskLevel: RiskLevel,
    val isSystemApp: Boolean = false,
    val dangerousPermissions: List<String>,
    val riskDescription: String,
    val suggestedAction: String, // e.g. "Désinstallation recommandée", "Gel du processus conseillé"
    val isFrozen: Boolean = false,
    val isIgnored: Boolean = false
)
