package com.example.data.local

import androidx.room.*
import com.example.data.model.ActionRecordEntity
import com.example.data.model.AutomationEntity
import com.example.data.model.BatteryStatEntity
import com.example.data.model.MissionEntity
import com.example.data.model.NexusMemoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NexusDao {

    // Action Records & Audit Ledger
    @Query("SELECT * FROM action_records ORDER BY timestamp DESC")
    fun getAllActions(): Flow<List<ActionRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAction(action: ActionRecordEntity): Long

    @Query("UPDATE action_records SET state = :state, verificationProof = :proof, executionTimeMs = :executionTimeMs WHERE id = :id")
    suspend fun updateActionState(id: Long, state: String, proof: String, executionTimeMs: Long)

    @Query("SELECT COUNT(*) FROM action_records")
    suspend fun getActionCount(): Int

    // Nexus Memory & Knowledge Engine
    @Query("SELECT * FROM nexus_memory ORDER BY timestamp DESC")
    fun getAllMemory(): Flow<List<NexusMemoryEntity>>

    @Query("SELECT * FROM nexus_memory WHERE key LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%'")
    fun searchMemory(query: String): Flow<List<NexusMemoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: NexusMemoryEntity): Long

    @Delete
    suspend fun deleteMemory(memory: NexusMemoryEntity)

    // Automations
    @Query("SELECT * FROM automations ORDER BY id DESC")
    fun getAllAutomations(): Flow<List<AutomationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAutomation(automation: AutomationEntity): Long

    @Query("UPDATE automations SET isEnabled = :isEnabled WHERE id = :id")
    suspend fun setAutomationEnabled(id: Long, isEnabled: Boolean)

    @Delete
    suspend fun deleteAutomation(automation: AutomationEntity)

    // Battery Health Monitoring & High Drain Statistics
    @Query("SELECT * FROM battery_stats ORDER BY timestamp DESC")
    fun getAllBatteryStats(): Flow<List<BatteryStatEntity>>

    @Query("SELECT * FROM battery_stats WHERE isHighDrainAlert = 1 ORDER BY timestamp DESC")
    fun getHighDrainAlerts(): Flow<List<BatteryStatEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatteryStat(stat: BatteryStatEntity): Long

    @Query("DELETE FROM battery_stats WHERE id NOT IN (SELECT id FROM battery_stats ORDER BY timestamp DESC LIMIT 100)")
    suspend fun trimOldBatteryStats()

    // Mission Records & Procedural Memory
    @Query("SELECT * FROM mission_records ORDER BY createdTimestamp DESC")
    fun getAllMissions(): Flow<List<MissionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMission(mission: MissionEntity): Long

    @Query("SELECT COUNT(*) FROM mission_records WHERE state = 'COMPLETED'")
    suspend fun getCompletedMissionCount(): Int
}
