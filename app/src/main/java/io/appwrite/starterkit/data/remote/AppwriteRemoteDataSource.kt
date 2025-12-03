package io.appwrite.starterkit.data.remote

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import io.appwrite.Client
import io.appwrite.ID
import io.appwrite.exceptions.AppwriteException
import io.appwrite.services.Databases
import io.appwrite.starterkit.constants.AppwriteConfig
import io.appwrite.starterkit.data.models.schedule.ClassEntry
import io.appwrite.starterkit.data.models.schedule.TaskEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Lightweight wrapper around the Appwrite Databases API used for syncing
 * timetable classes and tasks. All calls are wrapped inside [withContext]
 * blocks so that they can be invoked from view models without blocking
 * the main thread.
 */
class AppwriteRemoteDataSource(context: Context) {
    private val client: Client = Client(context)
        .setEndpoint(AppwriteConfig.APPWRITE_PUBLIC_ENDPOINT)
        .setProject(AppwriteConfig.APPWRITE_PROJECT_ID)

    private val databases = Databases(client)

    suspend fun fetchClasses(): List<ClassEntry> = withContext(Dispatchers.IO) {
        runCatching {
            val response = databases.listDocuments(
                databaseId = AppwriteConfig.APPWRITE_DATABASE_ID,
                collectionId = AppwriteConfig.APPWRITE_CLASSES_COLLECTION_ID
            )
            response.documents.mapNotNull { document ->
                document.data.toClassEntry(document.id)
            }
        }.getOrElse { emptyList() }
    }

    suspend fun fetchTasks(): List<TaskEntry> = withContext(Dispatchers.IO) {
        runCatching {
            val response = databases.listDocuments(
                databaseId = AppwriteConfig.APPWRITE_DATABASE_ID,
                collectionId = AppwriteConfig.APPWRITE_TASKS_COLLECTION_ID
            )
            response.documents.mapNotNull { document ->
                document.data.toTaskEntry(document.id)
            }
        }.getOrElse { emptyList() }
    }

    suspend fun upsertClass(entry: ClassEntry) = withContext(Dispatchers.IO) {
        val payload = entry.toRemotePayload()
        runCatching {
            databases.updateDocument(
                databaseId = AppwriteConfig.APPWRITE_DATABASE_ID,
                collectionId = AppwriteConfig.APPWRITE_CLASSES_COLLECTION_ID,
                documentId = entry.id,
                data = payload
            )
        }.recoverCatching { error ->
            if (error is AppwriteException && error.code == 404) {
                databases.createDocument(
                    databaseId = AppwriteConfig.APPWRITE_DATABASE_ID,
                    collectionId = AppwriteConfig.APPWRITE_CLASSES_COLLECTION_ID,
                    documentId = entry.id.ifEmpty { ID.unique() },
                    data = payload
                )
            } else {
                throw error
            }
        }.getOrNull()
    }

    suspend fun upsertTask(entry: TaskEntry) = withContext(Dispatchers.IO) {
        val payload = entry.toRemotePayload()
        runCatching {
            databases.updateDocument(
                databaseId = AppwriteConfig.APPWRITE_DATABASE_ID,
                collectionId = AppwriteConfig.APPWRITE_TASKS_COLLECTION_ID,
                documentId = entry.id,
                data = payload
            )
        }.recoverCatching { error ->
            if (error is AppwriteException && error.code == 404) {
                databases.createDocument(
                    databaseId = AppwriteConfig.APPWRITE_DATABASE_ID,
                    collectionId = AppwriteConfig.APPWRITE_TASKS_COLLECTION_ID,
                    documentId = entry.id.ifEmpty { ID.unique() },
                    data = payload
                )
            } else {
                throw error
            }
        }.getOrNull()
    }

    suspend fun deleteTask(taskId: String) = withContext(Dispatchers.IO) {
        runCatching {
            databases.deleteDocument(
                databaseId = AppwriteConfig.APPWRITE_DATABASE_ID,
                collectionId = AppwriteConfig.APPWRITE_TASKS_COLLECTION_ID,
                documentId = taskId
            )
        }
    }
}

private fun Map<String, Any?>.toClassEntry(id: String): ClassEntry? {
    val subject = this["subject"] as? String ?: return null
    val professor = this["professor"] as? String ?: ""
    val room = this["room"] as? String ?: ""
    val dayOfWeek = (this["dayOfWeek"] as? Number)?.toInt() ?: 0
    val startTime = this["startTime"] as? String ?: "09:00"
    val endTime = this["endTime"] as? String ?: "10:00"
    val accentColor = (this["accentColor"] as? Number)?.toInt() ?: 0xFF2563EB.toInt()
    return ClassEntry(
        id = id,
        subject = subject,
        professor = professor,
        room = room,
        dayOfWeek = dayOfWeek,
        startTime = startTime,
        endTime = endTime,
        accentColor = Color(accentColor)
    )
}

private fun Map<String, Any?>.toTaskEntry(id: String): TaskEntry? {
    val title = this["title"] as? String ?: return null
    val dueDate = this["dueDate"] as? String ?: "2025-01-01"
    val isCompleted = this["isCompleted"] as? Boolean ?: false
    val classId = this["classId"] as? String
    return TaskEntry(
        id = id,
        title = title,
        dueDate = dueDate,
        isCompleted = isCompleted,
        classId = classId
    )
}

private fun ClassEntry.toRemotePayload(): Map<String, Any?> = mapOf(
    "subject" to subject,
    "professor" to professor,
    "room" to room,
    "dayOfWeek" to dayOfWeek,
    "startTime" to startTime,
    "endTime" to endTime,
    "accentColor" to accentColor.toArgb()
)

private fun TaskEntry.toRemotePayload(): Map<String, Any?> = mapOf(
    "title" to title,
    "dueDate" to dueDate,
    "isCompleted" to isCompleted,
    "classId" to classId
)
