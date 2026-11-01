package com.example.data.local

import android.content.Context
import androidx.room.*
import com.example.data.model.ActionRecordEntity
import com.example.data.model.ActionState
import com.example.data.model.AutomationEntity
import com.example.data.model.BatteryStatEntity
import com.example.data.model.MissionEntity
import com.example.data.model.NexusMemoryEntity

class ActionStateConverter {
    @TypeConverter
    fun fromActionState(state: ActionState): String = state.name

    @TypeConverter
    fun toActionState(value: String): ActionState = try {
        ActionState.valueOf(value)
    } catch (e: Exception) {
        ActionState.PROPOSED
    }
}

@Database(
    entities = [ActionRecordEntity::class, NexusMemoryEntity::class, AutomationEntity::class, BatteryStatEntity::class, MissionEntity::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(ActionStateConverter::class)
abstract class NexusDatabase : RoomDatabase() {

    abstract fun nexusDao(): NexusDao

    companion object {
        @Volatile
        private var INSTANCE: NexusDatabase? = null

        fun getDatabase(context: Context): NexusDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NexusDatabase::class.java,
                    "nexus_system_database.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
