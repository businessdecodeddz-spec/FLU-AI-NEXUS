package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mission_records")
data class MissionEntity(
    @PrimaryKey
    val missionId: String,
    val objective: String,
    val state: String, // MissionState.name
    val autonomyMode: String, // AutonomyMode.name
    val taskCount: Int,
    val completedTaskCount: Int,
    val createdTimestamp: Long,
    val completedTimestamp: Long,
    val failureReason: String = "",
    val finalReport: String = "",
    val verificationSignature: String = ""
)
