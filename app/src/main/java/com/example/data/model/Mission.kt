package com.example.data.model

enum class AutonomyMode {
    ASSISTED,    // AI analyzes and proposes plans only.
    SUPERVISED,  // AI plans and executes after explicit user approval.
    AUTONOMOUS   // AI executes pre-authorized, non-destructive, reversible operations automatically.
}

enum class MissionState {
    CREATED,
    ANALYZING,
    PLANNING,
    AWAITING_APPROVAL,
    EXECUTING,
    VERIFYING,
    COMPLETED,
    FAILED,
    CANCELLED
}

data class MissionTask(
    val taskId: String,
    val title: String,
    val toolId: String,
    val parameters: Map<String, String> = emptyMap(),
    val isCompleted: Boolean = false,
    val isFailed: Boolean = false,
    val isDestructive: Boolean = false,
    val resultSummary: String = "",
    val verificationProof: String = ""
)

data class Mission(
    val missionId: String,
    val objective: String,
    val state: MissionState,
    val autonomyMode: AutonomyMode,
    val tasks: List<MissionTask> = emptyList(),
    val currentTaskIndex: Int = 0,
    val retryCount: Int = 0,
    val createdTimestamp: Long = System.currentTimeMillis(),
    val completedTimestamp: Long = 0,
    val failureReason: String = "",
    val finalReport: String = "",
    val verificationSignature: String = ""
)
