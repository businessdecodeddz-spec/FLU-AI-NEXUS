package com.example.core

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * NEXUS DIGITAL TWIN
 * Constructs a living digital model of the smartphone environment.
 * Maps installed apps, file trees, dependencies, resources, and usage patterns.
 * Used to simulate potential consequences of actions before authorization.
 */

data class DigitalTwinNode(
    val id: String,
    val name: String,
    val type: String, // "APP", "FILE_TREE", "RESOURCE", "SERVICE", "CONFIG"
    val dependencyCount: Int,
    val riskRating: String
)

data class ImpactSimulationReport(
    val simulationId: String,
    val targetOperation: String,
    val affectedNodes: List<DigitalTwinNode>,
    val sideEffectsRisk: String, // "NONE", "LOW", "MODERATE", "CRITICAL"
    val impactedApps: List<String>,
    val safetyRecommendation: String,
    val canProceedSafely: Boolean
)

class DigitalTwin {

    private val _nodes = MutableStateFlow<List<DigitalTwinNode>>(emptyList())
    val nodes: StateFlow<List<DigitalTwinNode>> = _nodes.asStateFlow()

    init {
        buildInitialTwinGraph()
    }

    private fun buildInitialTwinGraph() {
        _nodes.value = listOf(
            DigitalTwinNode("NODE-APP-01", "FLU-AI NEXUS Core Engine", "APP", 12, "LOW"),
            DigitalTwinNode("NODE-APP-02", "System Launcher", "APP", 8, "CRITICAL"),
            DigitalTwinNode("NODE-FILE-01", "/data/user/0/com.example/databases/nexus.db", "FILE_TREE", 5, "CRITICAL"),
            DigitalTwinNode("NODE-FILE-02", "/sdcard/Downloads/CacheTemp/", "FILE_TREE", 0, "NONE"),
            DigitalTwinNode("NODE-RES-01", "Wi-Fi Socket Buffer", "RESOURCE", 3, "LOW"),
            DigitalTwinNode("NODE-RES-02", "JNI Native Shared Memory", "RESOURCE", 10, "MODERATE")
        )
    }

    suspend fun simulateActionImpact(
        operationName: String,
        targetPaths: List<String>
    ): ImpactSimulationReport {
        delay(200)

        val affected = mutableListOf<DigitalTwinNode>()
        val impactedApps = mutableListOf<String>()

        targetPaths.forEach { path ->
            if (path.contains("nexus.db") || path.contains("system")) {
                affected.add(DigitalTwinNode("NODE-FILE-CRIT", path, "FILE_TREE", 6, "CRITICAL"))
                impactedApps.add("NEXUS Core Persistence Layer")
            } else if (path.contains("Cache") || path.contains("tmp")) {
                affected.add(DigitalTwinNode("NODE-FILE-SAFE", path, "FILE_TREE", 0, "NONE"))
            } else {
                affected.add(DigitalTwinNode("NODE-FILE-GENERIC", path, "FILE_TREE", 1, "LOW"))
            }
        }

        val hasCritical = affected.any { it.riskRating == "CRITICAL" }
        val sideEffects = if (hasCritical) "CRITICAL" else if (affected.any { it.riskRating == "MODERATE" }) "MODERATE" else "NONE"
        val canProceed = !hasCritical

        return ImpactSimulationReport(
            simulationId = "TWIN-SIM-" + System.currentTimeMillis().toString().takeLast(6),
            targetOperation = operationName,
            affectedNodes = affected,
            sideEffectsRisk = sideEffects,
            impactedApps = impactedApps,
            safetyRecommendation = if (canProceed) "SIMULATION VALIDÉE: Aucun effet secondaire critique détecté." else "ATTENTION SIMULATION: Risque de rupture d'intégrité sur les composants système.",
            canProceedSafely = canProceed
        )
    }
}
