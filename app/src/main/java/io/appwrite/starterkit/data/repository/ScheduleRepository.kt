package io.appwrite.starterkit.data.repository

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import io.appwrite.starterkit.data.local.ScheduleDatabase
import io.appwrite.starterkit.data.local.entities.ClassEntity
import io.appwrite.starterkit.data.local.entities.TaskEntity
import io.appwrite.starterkit.data.models.schedule.ClassEntry
import io.appwrite.starterkit.data.models.schedule.ScheduleSeedData
import io.appwrite.starterkit.data.models.schedule.TaskEntry
import io.appwrite.starterkit.data.remote.AppwriteRemoteDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class ScheduleRepository private constructor(context: Context) {
    private val database = ScheduleDatabase.getInstance(context)
    private val classDao = database.classDao()
    private val taskDao = database.taskDao()
    private val remote = AppwriteRemoteDataSource(context)

    val classes: Flow<List<ClassEntry>> = classDao.observeClasses().map { entities ->
        entities.map { it.toModel() }
    }

    val tasks: Flow<List<TaskEntry>> = taskDao.observeTasks().map { entities ->
        entities.map { it.toModel() }
    }

    suspend fun addClass(entry: ClassEntry) {
        classDao.upsert(entry.toEntity())
        remote.upsertClass(entry)
    }

    suspend fun addTask(entry: TaskEntry) {
        taskDao.upsert(entry.toEntity())
        remote.upsertTask(entry)
    }

    suspend fun toggleTaskCompletion(taskId: String) {
        val current = taskDao.findById(taskId) ?: return
        val updated = current.copy(isCompleted = !current.isCompleted)
        taskDao.upsert(updated)
        remote.upsertTask(updated.toModel())
    }

    suspend fun clearCompletedTasks() {
        val completedTasks = taskDao.getCompletedTasks()
        if (completedTasks.isEmpty()) return

        taskDao.clearCompleted()
        completedTasks.forEach { remote.deleteTask(it.id) }
    }

    suspend fun ensureSeedData() {
        refreshFromRemote()

        val hasData = classDao.countClasses() > 0 || taskDao.countTasks() > 0
        if (hasData) return

        val classEntities = ScheduleSeedData.sampleClasses.map { it.toEntity() }
        val taskEntities = ScheduleSeedData.sampleTasks.map { it.toEntity() }

        classDao.insertAll(classEntities)
        taskDao.insertAll(taskEntities)

        ScheduleSeedData.sampleClasses.forEach { remote.upsertClass(it) }
        ScheduleSeedData.sampleTasks.forEach { remote.upsertTask(it) }
    }

    suspend fun refreshFromRemote() {
        val remoteClasses = remote.fetchClasses()
        if (remoteClasses.isNotEmpty()) {
            classDao.insertAll(remoteClasses.map { it.toEntity() })
        }

        val remoteTasks = remote.fetchTasks()
        if (remoteTasks.isNotEmpty()) {
            taskDao.insertAll(remoteTasks.map { it.toEntity() })
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: ScheduleRepository? = null

        fun getInstance(context: Context): ScheduleRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ScheduleRepository(context.applicationContext).also { repo ->
                    INSTANCE = repo
                    CoroutineScope(Dispatchers.IO).launch {
                        repo.ensureSeedData()
                    }
                }
            }
        }
    }
}

private fun ClassEntity.toModel(): ClassEntry = ClassEntry(
    id = id,
    subject = subject,
    professor = professor,
    room = room,
    dayOfWeek = dayOfWeek,
    startTime = startTime,
    endTime = endTime,
    accentColor = Color(accentColor)
)

private fun TaskEntity.toModel(): TaskEntry = TaskEntry(
    id = id,
    title = title,
    dueDate = dueDate,
    isCompleted = isCompleted,
    classId = classId
)

private fun ClassEntry.toEntity(): ClassEntity = ClassEntity(
    id = id,
    subject = subject,
    professor = professor,
    room = room,
    dayOfWeek = dayOfWeek,
    startTime = startTime,
    endTime = endTime,
    accentColor = accentColor.toArgb()
)

private fun TaskEntry.toEntity(): TaskEntity = TaskEntity(
    id = id,
    title = title,
    dueDate = dueDate,
    isCompleted = isCompleted,
    classId = classId
)
