package com.example.core

import com.example.data.local.NexusDao
import com.example.data.model.NexusMemoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * 3. NEXUS MEMORY
 * Contextual and persistent memory engine backed by Room local database.
 */
class NexusMemory(private val dao: NexusDao) {

    fun getAllMemory(): Flow<List<NexusMemoryEntity>> = dao.getAllMemory()

    fun searchMemory(query: String): Flow<List<NexusMemoryEntity>> = dao.searchMemory(query)

    suspend fun saveMemorySnippet(key: String, category: String, content: String, importance: Int = 1, department: String = "CORE"): Long {
        val entity = NexusMemoryEntity(
            key = key,
            category = category,
            content = content,
            importance = importance,
            sourceDepartment = department
        )
        return dao.insertMemory(entity)
    }

    suspend fun deleteMemorySnippet(memory: NexusMemoryEntity) {
        dao.deleteMemory(memory)
    }
}
