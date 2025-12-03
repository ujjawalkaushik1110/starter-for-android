package io.appwrite.starterkit.viewmodels

import android.app.Application
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.appwrite.starterkit.data.models.schedule.ClassEntry
import io.appwrite.starterkit.data.models.schedule.ScheduleTab
import io.appwrite.starterkit.data.models.schedule.StudySuggestion
import io.appwrite.starterkit.data.models.schedule.TaskEntry
import io.appwrite.starterkit.data.models.schedule.WeekDay
import io.appwrite.starterkit.data.repository.ScheduleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ScheduleViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ScheduleRepository.getInstance(application)

    init {
        viewModelScope.launch { repository.refreshFromRemote() }
    }

    val classes: StateFlow<List<ClassEntry>> = repository.classes.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    val tasks: StateFlow<List<TaskEntry>> = repository.tasks.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    private val _suggestions = MutableStateFlow<List<StudySuggestion>>(emptyList())
    private val _activeTab = MutableStateFlow(ScheduleTab.TIMETABLE)

    val suggestions: StateFlow<List<StudySuggestion>> = _suggestions.asStateFlow()
    val activeTab: StateFlow<ScheduleTab> = _activeTab.asStateFlow()

    fun setActiveTab(tab: ScheduleTab) {
        _activeTab.value = tab
    }

    fun addClass(
        subject: String,
        professor: String,
        room: String,
        dayOfWeek: Int,
        startTime: String,
        endTime: String,
        accentColor: Int,
    ) {
        val entry = ClassEntry(
            subject = subject,
            professor = professor,
            room = room,
            dayOfWeek = dayOfWeek,
            startTime = startTime,
            endTime = endTime,
            accentColor = Color(accentColor)
        )
        viewModelScope.launch { repository.addClass(entry) }
    }

    fun addTask(
        title: String,
        dueDate: String,
        classId: String?,
    ) {
        val entry = TaskEntry(
            title = title,
            dueDate = dueDate,
            classId = classId,
        )
        viewModelScope.launch { repository.addTask(entry) }
    }

    fun toggleTaskCompletion(taskId: String) {
        viewModelScope.launch { repository.toggleTaskCompletion(taskId) }
    }

    fun clearCompletedTasks() {
        viewModelScope.launch { repository.clearCompletedTasks() }
    }

    fun syncWithRemote() {
        viewModelScope.launch { repository.refreshFromRemote() }
    }

    fun generateSuggestions() {
        val pendingTasks = tasks.value.filterNot { it.isCompleted }
        if (pendingTasks.isEmpty()) {
            _suggestions.value = emptyList()
            return
        }

        val orderedTasks = pendingTasks.sortedBy { it.dueDate }
        val classLookup = classes.value.associateBy { it.id }
        val generated = orderedTasks.take(5).mapIndexed { index, task ->
            val relatedClass = task.classId?.let { classLookup[it] }
            val day = relatedClass?.let { WeekDay.fromIndex(it.dayOfWeek) }
                ?: WeekDay.entries[index % WeekDay.entries.size]
            val start = relatedClass?.endTime ?: "16:00"
            val end = relatedClass?.let { advanceTime(it.endTime, 60) } ?: "17:00"
            StudySuggestion(
                subject = relatedClass?.subject ?: "Independent Study",
                task = task.title,
                day = day,
                startTime = start,
                endTime = end
            )
        }
        _suggestions.value = generated
    }

    fun dismissSuggestions() {
        _suggestions.value = emptyList()
    }

    private fun advanceTime(time: String, minutes: Int): String {
        val parts = time.split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: 0
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
        val total = hour * 60 + minute + minutes
        val normalized = total % (24 * 60)
        val h = normalized / 60
        val m = normalized % 60
        return String.format("%02d:%02d", h, m)
    }
}
