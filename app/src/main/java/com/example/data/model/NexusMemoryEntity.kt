package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "nexus_memory")
data class NexusMemoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val key: String,
    val category: String, // SYSTEM, USER_PREF, KNOWLEDGE, INCIDENT, TELEMETRY
    val content: String,
    val importance: Int = 1, // 1 to 5
    val timestamp: Long = System.currentTimeMillis(),
    val sourceDepartment: String = "CORE"
)
