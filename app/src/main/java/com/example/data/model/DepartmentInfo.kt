package com.example.data.model

import androidx.compose.ui.graphics.vector.ImageVector

data class DepartmentInfo(
    val id: String,
    val name: String,
    val description: String,
    val iconName: String,
    val isOperational: Boolean = true,
    val loadPercentage: Float = 15f,
    val activeTasks: Int = 1,
    val capabilities: List<String>
)

data class EngineInfo(
    val id: String,
    val name: String,
    val role: String,
    val status: String = "ACTIVE",
    val activityMs: Long = 12
)
