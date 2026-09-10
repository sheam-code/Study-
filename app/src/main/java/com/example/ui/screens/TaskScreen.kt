package com.example.ui.screens

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.TaskEntity
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskScreen(
    tasks: List<TaskEntity>,
    onToggleTask: (TaskEntity) -> Unit,
    onDeleteTask: (Long) -> Unit,
    onAddTask: (String, String, String, Long, String, Int) -> Unit,
    onTestNotification: () -> Unit,
    modifier: Modifier = Modifier
) {
    var filterStatus by remember { mutableStateOf("PENDING") } // ALL, PENDING, COMPLETED, HIGH_PRIORITY
    var selectedSubject by remember { mutableStateOf("ALL") }
    var showAddDialog by remember { mutableStateOf(false) }

    val subjects = listOf("ALL", "Physics", "Chemistry", "Biology", "Math", "English", "Bangla", "ICT", "General")

    val filteredTasks = remember(tasks, filterStatus, selectedSubject) {
        tasks.filter { task ->
            val statusMatch = when (filterStatus) {
                "ALL" -> true
                "PENDING" -> !task.isCompleted
                "COMPLETED" -> task.isCompleted
                "HIGH_PRIORITY" -> !task.isCompleted && task.priority == "HIGH"
                else -> true
            }
            val subjectMatch = if (selectedSubject == "ALL") true else task.subject.equals(selectedSubject, ignoreCase = true)
            statusMatch && subjectMatch
        }
    }

    val totalCount = tasks.size
    val pendingCount = tasks.count { !it.isCompleted }
    val completedCount = tasks.count { it.isCompleted }
    val highPriorityCount = tasks.count { !it.isCompleted && it.priority == "HIGH" }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = AmberGold,
                contentColor = Color(0xFF211823),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("add_task_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Task")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .testTag("task_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Dashboard Metrics Header Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("task_metrics_card"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Task Tracking Dashboard",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = "Upcoming deadlines with auto push reminders",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                            IconButton(
                                onClick = onTestNotification,
                                modifier = Modifier.testTag("test_push_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.NotificationsActive,
                                    contentDescription = "Test Notification",
                                    tint = AmberGold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Stats Grid
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            MetricChip(
                                title = "Pending",
                                count = "$pendingCount",
                                color = AmberGold,
                                modifier = Modifier.weight(1f)
                            )
                            MetricChip(
                                title = "High Priority",
                                count = "$highPriorityCount",
                                color = Terracotta,
                                modifier = Modifier.weight(1f)
                            )
                            MetricChip(
                                title = "Completed",
                                count = "$completedCount",
                                color = SageGreen,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Notification Banner Info
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SteelBlue.copy(alpha = 0.12f),
                    border = CardDefaults.outlinedCardBorder(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Alarm,
                            contentDescription = null,
                            tint = SteelBlue,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Push reminders trigger before each deadline even when offline.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Medium
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = onTestNotification) {
                            Text("Test", color = SteelBlue, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Status Filter Chips
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = filterStatus == "PENDING",
                        onClick = { filterStatus = "PENDING" },
                        label = { Text("Pending ($pendingCount)") },
                        modifier = Modifier.testTag("filter_pending")
                    )
                    FilterChip(
                        selected = filterStatus == "HIGH_PRIORITY",
                        onClick = { filterStatus = "HIGH_PRIORITY" },
                        label = { Text("High Priority ($highPriorityCount)") },
                        modifier = Modifier.testTag("filter_high_priority")
                    )
                    FilterChip(
                        selected = filterStatus == "COMPLETED",
                        onClick = { filterStatus = "COMPLETED" },
                        label = { Text("Done ($completedCount)") },
                        modifier = Modifier.testTag("filter_completed")
                    )
                    FilterChip(
                        selected = filterStatus == "ALL",
                        onClick = { filterStatus = "ALL" },
                        label = { Text("All ($totalCount)") },
                        modifier = Modifier.testTag("filter_all")
                    )
                }
            }

            // Subject Filter Row
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(subjects) { subj ->
                        val isSelected = selectedSubject == subj
                        SuggestionChip(
                            onClick = { selectedSubject = subj },
                            label = { Text(subj) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                                labelColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                }
            }

            // Tasks List
            if (filteredTasks.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "No tasks found for selected filters.",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                }
            } else {
                items(filteredTasks, key = { it.id }) { task ->
                    TaskCard(
                        task = task,
                        onToggle = { onToggleTask(task) },
                        onDelete = { onDeleteTask(task.id) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddTaskDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { title, desc, subj, deadline, priority, reminderMins ->
                onAddTask(title, desc, subj, deadline, priority, reminderMins)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun MetricChip(
    title: String,
    count: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.14f),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp
                )
            )
        }
    }
}

@Composable
private fun TaskCard(
    task: TaskEntity,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val nowMs = System.currentTimeMillis()
    val diffHours = (task.deadlineEpochMs - nowMs) / (3600 * 1000)
    val formattedDeadline = remember(task.deadlineEpochMs) {
        SimpleDateFormat("MMM d, h:mm a", Locale.US).format(Date(task.deadlineEpochMs))
    }

    val urgencyTag = when {
        task.isCompleted -> "Completed"
        diffHours < 0 -> "Overdue"
        diffHours < 24 -> "Due in ${diffHours}h"
        else -> "Due in ${diffHours / 24}d"
    }

    val priorityColor = when (task.priority) {
        "HIGH" -> Terracotta
        "MEDIUM" -> AmberGold
        else -> SageGreen
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("task_item_${task.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (task.isCompleted) {
                MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = SageGreen,
                    checkmarkColor = Color(0xFF1D1712),
                    uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier.padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = task.subject,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontSize = 10.sp
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = priorityColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "${task.priority} PRIORITY",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = priorityColor,
                                fontSize = 10.sp
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                    )
                )

                if (task.description.isNotBlank()) {
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Event,
                            contentDescription = null,
                            tint = if (diffHours < 12 && !task.isCompleted) Terracotta else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "$formattedDeadline ($urgencyTag)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (diffHours < 12 && !task.isCompleted) Terracotta else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }

                    if (task.reminderEnabled && !task.isCompleted) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = AmberGold,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "Alarm ${task.reminderMinutesBefore}m",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = AmberGold,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete task",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddTaskDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, Long, String, Int) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("Physics") }
    var priority by remember { mutableStateOf("HIGH") }
    var dueInDays by remember { mutableStateOf(1) } // 0=today, 1=tomorrow, 2=in 2 days, 7=in a week
    var reminderOffset by remember { mutableStateOf(60) } // mins before

    val subjects = listOf("Physics", "Chemistry", "Biology", "Math", "English", "Bangla", "ICT", "General")
    val priorities = listOf("HIGH", "MEDIUM", "LOW")
    val offsets = listOf(15, 30, 60, 120, 1440)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add Study Task & Deadline",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title *") },
                    placeholder = { Text("e.g. Physics SS4 Problem Set 1–25") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("add_task_title_input")
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Details & Chapter Topics") },
                    placeholder = { Text("e.g. Rotational Dynamics & torque questions") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Subject",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(subjects) { s ->
                        FilterChip(
                            selected = subject == s,
                            onClick = { subject = s },
                            label = { Text(s) }
                        )
                    }
                }

                Text(
                    text = "Priority Level",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    priorities.forEach { p ->
                        FilterChip(
                            selected = priority == p,
                            onClick = { priority = p },
                            label = { Text(p) }
                        )
                    }
                }

                Text(
                    text = "Deadline Timeline",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(0 to "Today", 1 to "Tomorrow", 3 to "3 Days", 7 to "1 Week").forEach { (days, label) ->
                        FilterChip(
                            selected = dueInDays == days,
                            onClick = { dueInDays = days },
                            label = { Text(label) }
                        )
                    }
                }

                Text(
                    text = "Push Notification Reminder",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    offsets.forEach { mins ->
                        val label = if (mins >= 60) "${mins / 60}h before" else "${mins}m before"
                        FilterChip(
                            selected = reminderOffset == mins,
                            onClick = { reminderOffset = mins },
                            label = { Text(label) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val cal = Calendar.getInstance()
                        cal.add(Calendar.DAY_OF_YEAR, dueInDays)
                        cal.set(Calendar.HOUR_OF_DAY, 21) // 9:00 PM default evening deadline
                        cal.set(Calendar.MINUTE, 0)
                        cal.set(Calendar.SECOND, 0)
                        onConfirm(title, description, subject, cal.timeInMillis, priority, reminderOffset)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AmberGold, contentColor = Color(0xFF211823)),
                modifier = Modifier.testTag("confirm_add_task_btn")
            ) {
                Text("Create Task", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
