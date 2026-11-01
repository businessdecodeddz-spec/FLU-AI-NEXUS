package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "automations")
data class AutomationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val triggerCondition: String, // e.g. "BATTERY_LOW", "HIGH_RAM_USAGE", "UNKNOWN_WIFI"
    val targetDepartment: String,
    val actionCommand: String,
    val isEnabled: Boolean = true,
    val executionCount: Int = 0,
    val lastExecutedTime: Long = 0
)
