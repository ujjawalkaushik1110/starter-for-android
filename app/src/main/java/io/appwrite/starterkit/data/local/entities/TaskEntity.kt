package io.appwrite.starterkit.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey val id: String,
    val title: String,
    val dueDate: String,
    val isCompleted: Boolean,
    val classId: String?,
)
