package io.appwrite.starterkit.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import io.appwrite.starterkit.data.local.entities.ClassEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ClassDao {
    @Query("SELECT * FROM classes ORDER BY dayOfWeek, startTime")
    fun observeClasses(): Flow<List<ClassEntity>>

    @Upsert
    suspend fun upsert(entity: ClassEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<ClassEntity>)

    @Query("SELECT COUNT(*) FROM classes")
    suspend fun countClasses(): Int
}
