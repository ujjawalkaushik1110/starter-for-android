package io.appwrite.starterkit.ui.schedule

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.appwrite.starterkit.data.models.schedule.ClassEntry
import io.appwrite.starterkit.data.models.schedule.ScheduleTab
import io.appwrite.starterkit.data.models.schedule.StudySuggestion
import io.appwrite.starterkit.data.models.schedule.TaskEntry
import io.appwrite.starterkit.data.models.schedule.WeekDay
import io.appwrite.starterkit.ui.theme.AppwriteStarterKitTheme
import io.appwrite.starterkit.viewmodels.ScheduleViewModel

@Composable
fun ScheduleWiseApp(
    viewModel: ScheduleViewModel = viewModel(),
) {
    val classes by viewModel.classes.collectAsState()
    val tasks by viewModel.tasks.collectAsState()
    val suggestions by viewModel.suggestions.collectAsState()
    val activeTab by viewModel.activeTab.collectAsState()

    var showClassDialog by rememberSaveable { mutableStateOf(false) }
    var showTaskDialog by rememberSaveable { mutableStateOf(false) }
    var showSuggestionDialog by rememberSaveable { mutableStateOf(false) }
    var darkMode by rememberSaveable { mutableStateOf(false) }

    AppwriteStarterKitTheme(darkTheme = darkMode) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surface,
                                MaterialTheme.colorScheme.background
                            )
                        )
                    )
            ) {
                ScheduleHeader(
                    darkMode = darkMode,
                    onToggleTheme = { darkMode = !darkMode }
                )
                ScheduleDashboard(
                    classes = classes,
                    tasks = tasks,
                    activeTab = activeTab,
                    onTabChange = viewModel::setActiveTab,
                    onToggleTask = viewModel::toggleTaskCompletion,
                    onRequestAddClass = { showClassDialog = true },
                    onRequestAddTask = { showTaskDialog = true },
                    onRequestSuggestions = {
                        viewModel.generateSuggestions()
                        showSuggestionDialog = true
                    }
                )
            }
        }
    }

    if (showClassDialog) {
        AddClassDialog(
            onDismiss = { showClassDialog = false },
            onSave = { subject, professor, room, day, start, end, color ->
                viewModel.addClass(
                    subject = subject,
                    professor = professor,
                    room = room,
                    dayOfWeek = day.index,
                    startTime = start,
                    endTime = end,
                    accentColor = color
                )
                showClassDialog = false
            }
        )
    }

    if (showTaskDialog) {
        AddTaskDialog(
            classes = classes,
            onDismiss = { showTaskDialog = false },
            onSave = { title, dueDate, classId ->
                viewModel.addTask(title, dueDate, classId)
                showTaskDialog = false
            }
        )
    }

    if (showSuggestionDialog) {
        StudySuggestionDialog(
            suggestions = suggestions,
            onDismiss = {
                viewModel.dismissSuggestions()
                showSuggestionDialog = false
            }
        )
    }
}

@Composable
private fun ScheduleHeader(
    darkMode: Boolean,
    onToggleTheme: () -> Unit,
) {
    Surface(tonalElevation = 2.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "SW",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "ScheduleWise",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Your smart timetable & tasks hub",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            ThemeToggle(
                darkMode = darkMode,
                onToggle = onToggleTheme
            )
        }
    }
}

@Composable
private fun ThemeToggle(
    darkMode: Boolean,
    onToggle: () -> Unit,
) {
    OutlinedButton(onClick = onToggle) {
        Crossfade(targetState = darkMode, label = "themeToggle") { dark ->
            Text(if (dark) "Dark" else "Light")
        }
    }
}

@Composable
private fun ScheduleDashboard(
    classes: List<ClassEntry>,
    tasks: List<TaskEntry>,
    activeTab: ScheduleTab,
    onTabChange: (ScheduleTab) -> Unit,
    onToggleTask: (String) -> Unit,
    onRequestAddClass: () -> Unit,
    onRequestAddTask: () -> Unit,
    onRequestSuggestions: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        ActionButtons(
            onAddClass = onRequestAddClass,
            onAddTask = onRequestAddTask,
            onRequestSuggestions = onRequestSuggestions
        )

        Spacer(modifier = Modifier.height(16.dp))

        ScheduleTabs(activeTab = activeTab, onTabChange = onTabChange)

        Spacer(modifier = Modifier.height(16.dp))

        when (activeTab) {
            ScheduleTab.TIMETABLE -> TimetableSection(classes = classes)
            ScheduleTab.TASKS -> TaskSection(
                tasks = tasks,
                classes = classes,
                onToggleTask = onToggleTask
            )
            ScheduleTab.CLASSES -> ClassSection(classes = classes)
        }
    }
}

@Composable
private fun ActionButtons(
    onAddClass: () -> Unit,
    onAddTask: () -> Unit,
    onRequestSuggestions: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(onClick = onAddClass, modifier = Modifier.weight(1f)) {
            Text("Add Class")
        }
        OutlinedButton(onClick = onAddTask, modifier = Modifier.weight(1f)) {
            Text("Add Task")
        }
        OutlinedButton(onClick = onRequestSuggestions, modifier = Modifier.weight(1f)) {
            Text("Study Plan")
        }
    }
}

@Composable
private fun ScheduleTabs(activeTab: ScheduleTab, onTabChange: (ScheduleTab) -> Unit) {
    val tabs = listOf(
        ScheduleTab.TIMETABLE to "Timetable",
        ScheduleTab.TASKS to "Tasks",
        ScheduleTab.CLASSES to "All Classes"
    )
    TabRow(selectedTabIndex = tabs.indexOfFirst { it.first == activeTab }) {
        tabs.forEachIndexed { index, pair ->
            Tab(
                selected = activeTab == pair.first,
                onClick = { onTabChange(pair.first) },
                text = { Text(pair.second) },
                selectedContentColor = MaterialTheme.colorScheme.primary,
                unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TimetableSection(classes: List<ClassEntry>) {
    if (classes.isEmpty()) {
        EmptyState(message = "No classes scheduled yet. Add one to get started.")
        return
    }
    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items(WeekDay.entries) { day ->
            val dayClasses = classes.filter { it.dayOfWeek == day.index }
            TimetableDayCard(day, dayClasses)
        }
    }
}

@Composable
private fun TimetableDayCard(day: WeekDay, entries: List<ClassEntry>) {
    Card(
        modifier = Modifier
            .size(width = 220.dp, height = 260.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = day.label, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            if (entries.isEmpty()) {
                Text(
                    text = "No sessions",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                entries.sortedBy { it.startTime }.forEach { entry ->
                    ClassBlock(entry)
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
private fun ClassBlock(entry: ClassEntry) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(entry.accentColor.copy(alpha = 0.15f))
            .padding(12.dp)
    ) {
        Text(text = entry.subject, fontWeight = FontWeight.SemiBold)
        Text(
            text = "${entry.startTime} - ${entry.endTime}",
            style = MaterialTheme.typography.bodySmall
        )
        Text(
            text = "${entry.professor} · ${entry.room}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun TaskSection(
    tasks: List<TaskEntry>,
    classes: List<ClassEntry>,
    onToggleTask: (String) -> Unit,
) {
    if (tasks.isEmpty()) {
        EmptyState(message = "No tasks yet. Add your first assignment.")
        return
    }
    val classLookup = remember(classes) { classes.associateBy { it.id } }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(tasks, key = { it.id }) { task ->
            TaskRow(task = task, classLookup = classLookup, onToggle = onToggleTask)
        }
    }
}

@Composable
private fun TaskRow(
    task: TaskEntry,
    classLookup: Map<String, ClassEntry>,
    onToggle: (String) -> Unit,
) {
    Card(shape = RoundedCornerShape(18.dp), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = task.isCompleted, onCheckedChange = { onToggle(task.id) })
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Due ${task.dueDate}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                task.classId?.let { classLookup[it]?.subject }?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Icon(
                imageVector = Icons.Outlined.CheckCircle,
                contentDescription = null,
                tint = if (task.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
private fun ClassSection(classes: List<ClassEntry>) {
    if (classes.isEmpty()) {
        EmptyState(message = "Add classes to see them here.")
        return
    }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(classes, key = { it.id }) { entry ->
            ClassCard(entry)
        }
    }
}

@Composable
private fun ClassCard(entry: ClassEntry) {
    Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Canvas(modifier = Modifier.size(12.dp)) {
                    drawCircle(color = entry.accentColor)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = entry.subject, style = MaterialTheme.typography.titleMedium)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = entry.professor, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = entry.room, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = "${WeekDay.fromIndex(entry.dayOfWeek).label} · ${entry.startTime} - ${entry.endTime}")
        }
    }
}

@Composable
private fun EmptyState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = message, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun AddClassDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, String, WeekDay, String, String, Int) -> Unit,
) {
    var subject by rememberSaveable { mutableStateOf("") }
    var professor by rememberSaveable { mutableStateOf("") }
    var room by rememberSaveable { mutableStateOf("") }
    var startTime by rememberSaveable { mutableStateOf("09:00") }
    var endTime by rememberSaveable { mutableStateOf("10:00") }
    var selectedDay by rememberSaveable { mutableStateOf(WeekDay.MONDAY) }
    val accentOptions = listOf(
        0xFF2563EB.toInt(),
        0xFFF97316.toInt(),
        0xFF0EA5E9.toInt(),
        0xFF9333EA.toInt(),
        0xFF10B981.toInt()
    )
    var selectedColor by rememberSaveable { mutableStateOf(accentOptions.first()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            val enableSave = subject.isNotBlank() && professor.isNotBlank()
            TextButton(onClick = {
                onSave(subject, professor, room, selectedDay, startTime, endTime, selectedColor)
            }, enabled = enableSave) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        title = { Text("Add class") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = subject, onValueChange = { subject = it }, label = { Text("Subject") })
                OutlinedTextField(value = professor, onValueChange = { professor = it }, label = { Text("Professor") })
                OutlinedTextField(value = room, onValueChange = { room = it }, label = { Text("Room") })
                DayDropdown(selectedDay = selectedDay, onSelect = { selectedDay = it })
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        modifier = Modifier.weight(1f),
                        value = startTime,
                        onValueChange = { startTime = it },
                        label = { Text("Start") }
                    )
                    OutlinedTextField(
                        modifier = Modifier.weight(1f),
                        value = endTime,
                        onValueChange = { endTime = it },
                        label = { Text("End") }
                    )
                }
                ColorSelector(options = accentOptions, selected = selectedColor, onSelect = { selectedColor = it })
            }
        }
    )
}

@Composable
private fun AddTaskDialog(
    classes: List<ClassEntry>,
    onDismiss: () -> Unit,
    onSave: (String, String, String?) -> Unit,
) {
    var title by rememberSaveable { mutableStateOf("") }
    var dueDate by rememberSaveable { mutableStateOf("2025-12-10") }
    var selectedClassId by rememberSaveable { mutableStateOf<String?>(null) }
    var classMenuExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            val enableSave = title.isNotBlank() && dueDate.isNotBlank()
            TextButton(onClick = { onSave(title, dueDate, selectedClassId) }, enabled = enableSave) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        title = { Text("Add task") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") })
                OutlinedTextField(value = dueDate, onValueChange = { dueDate = it }, label = { Text("Due date") })
                Box {
                    OutlinedTextField(
                        value = selectedClassId?.let { id -> classes.firstOrNull { it.id == id }?.subject } ?: "",
                        onValueChange = {},
                        label = { Text("Linked class (optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        trailingIcon = {
                            TextButton(onClick = { classMenuExpanded = true }) { Text("Choose") }
                        }
                    )
                    DropdownMenu(expanded = classMenuExpanded, onDismissRequest = { classMenuExpanded = false }) {
                        classes.forEach { entry ->
                            DropdownMenuItem(
                                text = { Text(entry.subject) },
                                onClick = {
                                    selectedClassId = entry.id
                                    classMenuExpanded = false
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("No class") },
                            onClick = {
                                selectedClassId = null
                                classMenuExpanded = false
                            }
                        )
                    }
                }
            }
        }
    )
}

@Composable
private fun StudySuggestionDialog(
    suggestions: List<StudySuggestion>,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        },
        title = { Text("Suggested study sessions") },
        text = {
            if (suggestions.isEmpty()) {
                Text("You're all caught up! Add tasks to receive study plans.")
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    suggestions.forEach { suggestion ->
                        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant)) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(text = suggestion.subject, fontWeight = FontWeight.SemiBold)
                                Text(text = suggestion.task, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(text = "${suggestion.day.label} · ${suggestion.startTime} - ${suggestion.endTime}")
                            }
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun DayDropdown(selectedDay: WeekDay, onSelect: (WeekDay) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedTextField(
            value = selectedDay.label,
            onValueChange = {},
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            label = { Text("Day of week") },
            trailingIcon = { TextButton(onClick = { expanded = true }) { Text("Pick") } }
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            WeekDay.entries.forEach { day ->
                DropdownMenuItem(
                    text = { Text(day.label) },
                    onClick = {
                        onSelect(day)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun ColorSelector(options: List<Int>, selected: Int, onSelect: (Int) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        options.forEach { colorValue ->
            val color = Color(colorValue)
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.9f))
                    .border(
                        width = if (selected == colorValue) 3.dp else 1.dp,
                        color = if (selected == colorValue) MaterialTheme.colorScheme.primary else Color.Transparent,
                        shape = CircleShape
                    )
                    .clickable { onSelect(colorValue) }
            )
        }
    }
}
