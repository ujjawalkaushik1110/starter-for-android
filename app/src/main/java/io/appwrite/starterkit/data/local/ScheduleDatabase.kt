package io.appwrite.starterkit.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import io.appwrite.starterkit.data.local.dao.ClassDao
import io.appwrite.starterkit.data.local.dao.TaskDao
import io.appwrite.starterkit.data.local.entities.ClassEntity
import io.appwrite.starterkit.data.local.entities.TaskEntity

@Database(
    entities = [ClassEntity::class, TaskEntity::class],
    version = 1,
    exportSchema = false
)
abstract class ScheduleDatabase : RoomDatabase() {
    abstract fun classDao(): ClassDao
    abstract fun taskDao(): TaskDao

    companion object {
        @Volatile
        private var INSTANCE: ScheduleDatabase? = null

        fun getInstance(context: Context): ScheduleDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    ScheduleDatabase::class.java,
                    "schedule.db"
                ).fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
