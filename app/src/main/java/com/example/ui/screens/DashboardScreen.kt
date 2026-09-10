package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.ChecklistEntity
import com.example.data.local.entity.ErrorLogEntity
import com.example.data.local.entity.TaskEntity
import com.example.model.ScheduleRepository
import com.example.model.ScheduleSlot
import com.example.ui.theme.*
import java.util.Calendar

@Composable
fun DashboardScreen(
    dayNumber: Int,
    daysLeft: Int,
    percentDone: Int,
    streak: Int,
    checklist: ChecklistEntity,
    errorLogs: List<ErrorLogEntity>,
    upcomingTasks: List<TaskEntity>,
    onToggleChecklist: (String, Boolean) -> Unit,
    onAddErrorLog: (String) -> Unit,
    onDeleteErrorLog: (Long) -> Unit,
    onNavigateToRoutine: () -> Unit,
    onNavigateToTasks: () -> Unit,
    modifier: Modifier = Modifier
) {
    var errorInput by remember { mutableStateOf("") }

    // Breathing Dot Animation
    val infiniteTransition = rememberInfiniteTransition(label = "breath")
    val breathScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathScale"
    )
    val breathAlpha by infiniteTransition.animateFloat(
        initialValue = 0.55f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathAlpha"
    )

    // Current active slot computation
    val now = Calendar.getInstance()
    val dow = now.get(Calendar.DAY_OF_WEEK)
    val currentMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
    val todaySchedule = ScheduleRepository.getScheduleForDow(dow)

    val activeSlotIndex = todaySchedule.slots.indexOfLast { it.startMinutesOfDay <= currentMinutes }
    val currentSlot: ScheduleSlot? = if (activeSlotIndex >= 0) todaySchedule.slots[activeSlotIndex] else todaySchedule.slots.firstOrNull()
    val nextSlot: ScheduleSlot? = if (activeSlotIndex >= 0 && activeSlotIndex < todaySchedule.slots.lastIndex) todaySchedule.slots[activeSlotIndex + 1] else null

    val completedCount = listOf(checklist.ssDone, checklist.errorlogDone, checklist.sleepDone, checklist.runDone).count { it }
    val allCompleted = completedCount == 4

    val quote = ScheduleRepository.QUOTES[((dayNumber % ScheduleRepository.QUOTES.size) + ScheduleRepository.QUOTES.size) % ScheduleRepository.QUOTES.size]

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Hero Section
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("hero_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    // Breathing pulse dot
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .scale(breathScale)
                                .clip(CircleShape)
                                .background(AmberGold.copy(alpha = breathAlpha))
                        )
                        Text(
                            text = "Today is",
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Day $dayNumber",
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontWeight = FontWeight.Light,
                                letterSpacing = (-1).sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Text(
                            text = "of 90",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Progress Track
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction = (percentDone / 100f).coerceIn(0.01f, 1f))
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(SageGreen, AmberGold)
                                    )
                                )
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Stat Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "$daysLeft",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Text(
                                text = "days left",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "$percentDone%",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Text(
                                text = "of the way there",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Quote
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(44.dp)
                                .background(AmberGold, RoundedCornerShape(2.dp))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "“$quote”",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontStyle = FontStyle.Italic,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 20.sp
                            )
                        )
                    }
                }
            }
        }

        // Live Active Routine Block Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToRoutine() }
                    .testTag("active_routine_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = SteelBlue.copy(alpha = 0.12f)
                ),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(currentSlot?.category?.color ?: SteelBlue)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "NOW SCHEDULED • ${currentSlot?.formatTime() ?: "--"}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = SteelBlue,
                                letterSpacing = 0.5.sp
                            )
                        )
                        Text(
                            text = currentSlot?.label ?: "Routine on break",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        if (nextSlot != null) {
                            Text(
                                text = "Next at ${nextSlot.formatTime()}: ${nextSlot.label}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "View routine",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Checklist Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("checklist_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (allCompleted) SageGreen.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
                ),
                border = if (allCompleted) {
                    CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(SageGreen, SageGreen)))
                } else {
                    CardDefaults.outlinedCardBorder()
                }
            ) {
                Column(
                    modifier = Modifier.padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Today's checklist",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (streak > 0) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = AmberGold.copy(alpha = 0.18f)
                                ) {
                                    Text(
                                        text = "🔥 $streak-day streak",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = AmberGold,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                            Text(
                                text = "$completedCount/4",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    Text(
                        text = "The 4 things that matter more than covering more chapters.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                    )

                    // 4 Items
                    ChecklistRow(
                        label = "All scheduled SS blocks done",
                        checked = checklist.ssDone,
                        onToggle = { onToggleChecklist("ss", checklist.ssDone) },
                        testTag = "check_item_ss"
                    )
                    ChecklistRow(
                        label = "Error log updated",
                        checked = checklist.errorlogDone,
                        onToggle = { onToggleChecklist("errorlog", checklist.errorlogDone) },
                        testTag = "check_item_errorlog"
                    )
                    ChecklistRow(
                        label = "Slept close to the target hours",
                        checked = checklist.sleepDone,
                        onToggle = { onToggleChecklist("sleep", checklist.sleepDone) },
                        testTag = "check_item_sleep"
                    )
                    ChecklistRow(
                        label = "Run / exercise done",
                        checked = checklist.runDone,
                        onToggle = { onToggleChecklist("run", checklist.runDone) },
                        testTag = "check_item_run"
                    )

                    AnimatedVisibility(visible = allCompleted) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SageGreen.copy(alpha = 0.18f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = SageGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Nice — everything's done for today.",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = SageGreen,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // Error Log Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("error_log_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Error log",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    Text(
                        text = "Weak topics, kept locally and synced with your devices.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = errorInput,
                            onValueChange = { errorInput = it },
                            placeholder = { Text("e.g. Rotational dynamics — torque") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("error_log_input"),
                            shape = RoundedCornerShape(10.dp)
                        )
                        Button(
                            onClick = {
                                if (errorInput.isNotBlank()) {
                                    onAddErrorLog(errorInput)
                                    errorInput = ""
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AmberGold,
                                contentColor = Color(0xFF211823)
                            ),
                            modifier = Modifier
                                .height(56.dp)
                                .testTag("error_log_add_button")
                        ) {
                            Text("Add", fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Tags flow
                    if (errorLogs.isEmpty()) {
                        Text(
                            text = "Nothing logged yet — add weak topics as you find them.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontStyle = FontStyle.Italic,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            errorLogs.take(5).forEach { item ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                    border = CardDefaults.outlinedCardBorder(),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = item.topic,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            )
                                            if (item.note.isNotBlank()) {
                                                Text(
                                                    text = item.note,
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                )
                                            }
                                        }
                                        IconButton(
                                            onClick = { onDeleteErrorLog(item.id) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Remove topic",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Upcoming Deadlines Overview
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("upcoming_tasks_card"),
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
                        Text(
                            text = "Upcoming Deadlines",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        TextButton(
                            onClick = onNavigateToTasks,
                            modifier = Modifier.testTag("view_all_tasks_button")
                        ) {
                            Text("View all (${upcomingTasks.size})")
                        }
                    }

                    if (upcomingTasks.isEmpty()) {
                        Text(
                            text = "No pending deadlines. Good job staying on track!",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontStyle = FontStyle.Italic
                            ),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            upcomingTasks.take(3).forEach { task ->
                                val nowMs = System.currentTimeMillis()
                                val diffHours = (task.deadlineEpochMs - nowMs) / (3600 * 1000)
                                val urgencyColor = when {
                                    diffHours < 0 -> Color(0xFFEF5350) // Overdue
                                    diffHours < 12 -> Terracotta
                                    task.priority == "HIGH" -> AmberGold
                                    else -> SteelBlue
                                }

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(urgencyColor)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = task.title,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = FontWeight.Medium
                                                )
                                            )
                                            Text(
                                                text = "${task.subject} • ${if (diffHours < 0) "Overdue" else "Due in ${diffHours}h"}",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = urgencyColor
                                                )
                                            )
                                        }
                                        if (task.reminderEnabled) {
                                            Icon(
                                                imageVector = Icons.Default.NotificationsActive,
                                                contentDescription = "Reminder enabled",
                                                tint = AmberGold,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = if (daysLeft > 0) "$daysLeft days to go. One day at a time." else "The 90 days are complete.",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun ChecklistRow(
    label: String,
    checked: Boolean,
    onToggle: () -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onToggle() }
            .padding(vertical = 6.dp, horizontal = 4.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = { onToggle() },
            colors = CheckboxDefaults.colors(
                checkedColor = SageGreen,
                checkmarkColor = Color(0xFF1D1712),
                uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = if (checked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                textDecoration = if (checked) TextDecoration.LineThrough else TextDecoration.None
            )
        )
    }
}
