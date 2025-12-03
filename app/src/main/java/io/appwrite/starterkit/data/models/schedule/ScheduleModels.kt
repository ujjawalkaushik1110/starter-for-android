package io.appwrite.starterkit.data.models.schedule

import androidx.compose.ui.graphics.Color
import java.util.UUID

/**
 * Immutable representations of the entities shown inside the ScheduleWise UI.
 */
data class ClassEntry(
    val id: String = UUID.randomUUID().toString(),
    val subject: String,
    val professor: String,
    val room: String,
    val dayOfWeek: Int,
    val startTime: String,
    val endTime: String,
    val accentColor: Color,
)

data class TaskEntry(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val dueDate: String,
    val isCompleted: Boolean = false,
    val classId: String? = null,
)

data class StudySuggestion(
    val id: String = UUID.randomUUID().toString(),
    val subject: String,
    val task: String,
    val day: WeekDay,
    val startTime: String,
    val endTime: String,
)

enum class ScheduleTab { TIMETABLE, TASKS, CLASSES }

enum class WeekDay(val label: String, val index: Int) {
    MONDAY("Mon", 0),
    TUESDAY("Tue", 1),
    WEDNESDAY("Wed", 2),
    THURSDAY("Thu", 3),
    FRIDAY("Fri", 4),
    SATURDAY("Sat", 5),
    SUNDAY("Sun", 6);

    companion object {
        fun fromIndex(index: Int): WeekDay = entries.firstOrNull { it.index == index } ?: MONDAY
    }
}

object ScheduleSeedData {
    val sampleClasses = listOf(
        ClassEntry(
            subject = "Mathematics",
            professor = "Dr. Patel",
            room = "Room 204",
            dayOfWeek = WeekDay.MONDAY.index,
            startTime = "09:00",
            endTime = "10:30",
            accentColor = Color(0xFF2563EB)
        ),
        ClassEntry(
            subject = "Physics",
            professor = "Prof. Ahuja",
            room = "Lab 2",
            dayOfWeek = WeekDay.TUESDAY.index,
            startTime = "11:00",
            endTime = "12:00",
            accentColor = Color(0xFFF97316)
        ),
        ClassEntry(
            subject = "Design Thinking",
            professor = "Ms. Flores",
            room = "Studio",
            dayOfWeek = WeekDay.WEDNESDAY.index,
            startTime = "14:00",
            endTime = "15:30",
            accentColor = Color(0xFF0EA5E9)
        )
    )

    val sampleTasks = listOf(
        TaskEntry(
            title = "Math assignment: Linear Algebra",
            dueDate = "2025-12-10",
            classId = sampleClasses.first().id
        ),
        TaskEntry(
            title = "Physics lab report",
            dueDate = "2025-12-11",
            classId = sampleClasses[1].id
        ),
        TaskEntry(
            title = "Create mood board",
            dueDate = "2025-12-08",
            classId = sampleClasses[2].id
        )
    )
}
