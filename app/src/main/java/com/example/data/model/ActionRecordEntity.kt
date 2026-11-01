package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "action_records")
data class ActionRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val actionId: String,
    val title: String,
    val departmentId: String,
    val engineId: String,
    val details: String,
    val state: ActionState,
    val isDestructive: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val verificationProof: String = "",
    val executionTimeMs: Long = 0,
    val signature: String = ""
)
