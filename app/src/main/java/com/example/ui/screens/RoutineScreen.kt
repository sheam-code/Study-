package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.entity.CalendarEventEntity
import com.example.data.local.entity.RecurringActivityEntity
import com.example.model.ScheduleRepository
import com.example.model.ScheduleSlot
import com.example.ui.theme.*
import com.example.util.NativeCalendarHelper
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineScreen(
    dayNumber: Int,
    previewDow: Int?,
    onSelectPreviewDow: (Int?) -> Unit,
    recurringActivities: List<RecurringActivityEntity>,
    calendarEvents: List<CalendarEventEntity>,
    onAddRecurringActivity: (String, String, String, String, String, String, String, String, String, Boolean, Context) -> Unit,
    onUpdateRecurringActivity: (RecurringActivityEntity) -> Unit,
    onToggleRecurringActivity: (RecurringActivityEntity) -> Unit,
    onDeleteRecurringActivity: (Long) -> Unit,
    onNavigateToCalendar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Today's Schedule & Events, 1: Manage Recurring Activities
    var showAddDialog by remember { mutableStateOf(false) }
    var editingActivity by remember { mutableStateOf<RecurringActivityEntity?>(null) }

    val realCalendar = remember { Calendar.getInstance() }
    val realDow = realCalendar.get(Calendar.DAY_OF_WEEK)
    val nowMinutes = realCalendar.get(Calendar.HOUR_OF_DAY) * 60 + realCalendar.get(Calendar.MINUTE)

    val activeDow = previewDow ?: realDow
    val isViewingRealToday = previewDow == null || previewDow == realDow

    val todayDateStr = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }

    val dowShortMap = mapOf(
        Calendar.SUNDAY to "SUN",
        Calendar.MONDAY to "MON",
        Calendar.TUESDAY to "TUE",
        Calendar.WEDNESDAY to "WED",
        Calendar.THURSDAY to "THU",
        Calendar.FRIDAY to "FRI",
        Calendar.SATURDAY to "SAT"
    )

    val currentDowCode = dowShortMap[activeDow] ?: "SUN"

    val weekDays = listOf(
        Calendar.SUNDAY to "Sun",
        Calendar.MONDAY to "Mon",
        Calendar.TUESDAY to "Tue",
        Calendar.WEDNESDAY to "Wed",
        Calendar.THURSDAY to "Thu",
        Calendar.FRIDAY to "Fri",
        Calendar.SATURDAY to "Sat"
    )

    val fullDayNames = mapOf(
        Calendar.SUNDAY to "Sunday",
        Calendar.MONDAY to "Monday",
        Calendar.TUESDAY to "Tuesday",
        Calendar.WEDNESDAY to "Wednesday",
        Calendar.THURSDAY to "Thursday",
        Calendar.FRIDAY to "Friday",
        Calendar.SATURDAY to "Saturday"
    )

    // Filter recurring activities for the active day of week
    val activeDayRoutines = remember(recurringActivities, currentDowCode) {
        recurringActivities.filter { activity ->
            activity.isActive && (activity.daysOfWeek.contains(currentDowCode) || activity.daysOfWeek.contains("ALL"))
        }.sortedBy { it.startTime }
    }

    // Filter calendar events for today (or preview date)
    val dayEvents = remember(calendarEvents, todayDateStr, activeDow, realDow) {
        if (activeDow == realDow) {
            calendarEvents.filter { it.eventDate == todayDateStr }
        } else {
            // Find events for the offset day
            val diff = activeDow - realDow
            val targetCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, diff) }
            val dateTarget = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(targetCal.time)
            calendarEvents.filter { it.eventDate == dateTarget }
        }
    }

    Scaffold(
        floatingActionButton = {
            if (selectedTab == 1) {
                ExtendedFloatingActionButton(
                    onClick = { showAddDialog = true },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("New Activity", fontWeight = FontWeight.Bold) },
                    containerColor = AmberGold,
                    contentColor = DeepObsidian,
                    modifier = Modifier.testTag("fab_add_recurring_activity")
                )
            }
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .testTag("routine_screen")
        ) {
            // Screen Header & Tab Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(top = 12.dp, start = 16.dp, end = 16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Routines & Schedules",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 20.sp
                                )
                            )
                            Text(
                                text = "Visual schedule timeline alongside scheduled events",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                        if (selectedTab == 0) {
                            FilledTonalButton(
                                onClick = { showAddDialog = true },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("btn_add_activity_header")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Activity", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.primary
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = {
                                Text(
                                    "Day Timeline & Events (${activeDayRoutines.size + dayEvents.size})",
                                    fontWeight = FontWeight.SemiBold
                                )
                            },
                            modifier = Modifier.testTag("tab_day_timeline")
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = {
                                Text(
                                    "Manage Recurring (${recurringActivities.size})",
                                    fontWeight = FontWeight.SemiBold
                                )
                            },
                            modifier = Modifier.testTag("tab_manage_recurring")
                        )
                    }
                }
            }

            val combinedItems = remember(activeDayRoutines, dayEvents) {
                val routineItems = activeDayRoutines.map {
                    TimelineEntry.Routine(it)
                }
                val eventItems = dayEvents.map {
                    TimelineEntry.Event(it)
                }
                (routineItems + eventItems).sortedBy { it.startTime }
            }

            if (selectedTab == 0) {
                // TODAY'S TIMELINE & EVENTS VIEW
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Week Strip Chips
                    item {
                        Column {
                            Text(
                                text = "SELECT DAY OF WEEK",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("week_strip_row")
                            ) {
                                itemsIndexed(weekDays) { _, (dowVal, dowShort) ->
                                    val isSelected = activeDow == dowVal
                                    val isToday = dowVal == realDow

                                    Surface(
                                        onClick = {
                                            if (dowVal == realDow) {
                                                onSelectPreviewDow(null)
                                            } else {
                                                onSelectPreviewDow(dowVal)
                                            }
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isSelected) {
                                            MaterialTheme.colorScheme.primaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.surface
                                        },
                                        border = if (isSelected) {
                                            CardDefaults.outlinedCardBorder().copy(
                                                brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary)
                                            )
                                        } else {
                                            CardDefaults.outlinedCardBorder()
                                        },
                                        modifier = Modifier
                                            .width(56.dp)
                                            .height(56.dp)
                                            .testTag("daychip_$dowShort")
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center,
                                            modifier = Modifier.fillMaxSize()
                                        ) {
                                            Text(
                                                text = dowShort,
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isSelected) {
                                                        MaterialTheme.colorScheme.onPrimaryContainer
                                                    } else {
                                                        MaterialTheme.colorScheme.onSurface
                                                    }
                                                )
                                            )
                                            if (isToday) {
                                                Box(
                                                    modifier = Modifier
                                                        .padding(top = 2.dp)
                                                        .size(4.dp)
                                                        .background(AmberGold, CircleShape)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Active Schedule Preset Banner
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = fullDayNames[activeDow] ?: "Schedule",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        if (isViewingRealToday) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = AmberGold.copy(alpha = 0.2f)
                                            ) {
                                                Text(
                                                    text = "TODAY",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        color = AmberGold,
                                                        fontWeight = FontWeight.ExtraBold
                                                    ),
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${activeDayRoutines.size} Recurring Activities • ${dayEvents.size} Calendar Events",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                }

                                TextButton(
                                    onClick = onNavigateToCalendar,
                                    colors = ButtonDefaults.textButtonColors(contentColor = AmberGold)
                                ) {
                                    Icon(Icons.Outlined.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Full Calendar", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }

                    // Combined Timeline Section Header
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "SCHEDULE & EVENTS TIMELINE",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                            Text(
                                text = "Chronological",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }

                    // Render Combined Timeline Items
                    if (combinedItems.isEmpty()) {
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
                                        imageVector = Icons.Outlined.EventAvailable,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(40.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "No routines or events for this day",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Add recurring activities or schedule new calendar events to plan your rhythm.",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(
                                        onClick = { showAddDialog = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = AmberGold, contentColor = DeepObsidian)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Add Recurring Activity")
                                    }
                                }
                            }
                        }
                    } else {
                        items(combinedItems) { item ->
                            when (item) {
                                is TimelineEntry.Routine -> {
                                    RoutineTimelineCard(
                                        routine = item.activity,
                                        isViewingRealToday = isViewingRealToday,
                                        nowMinutes = nowMinutes,
                                        onExportDeviceCalendar = {
                                            NativeCalendarHelper.insertRoutineToDeviceCalendar(context, item.activity)
                                        },
                                        onEdit = {
                                            editingActivity = item.activity
                                            showAddDialog = true
                                        }
                                    )
                                }
                                is TimelineEntry.Event -> {
                                    EventTimelineCard(
                                        event = item.event,
                                        onExportDeviceCalendar = {
                                            NativeCalendarHelper.insertEventToDeviceCalendar(context, item.event)
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Core Rules Reminder
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Lightbulb,
                                        contentDescription = null,
                                        tint = AmberGold,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "90-Day Challenge Anchor Rules",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "1. Blocks can shift by 15–30 min without breaking momentum.\n" +
                                            "2. Judge yourself by the week's total, never a single off-day.\n" +
                                            "3. If falling behind, shorten practice blocks before ever sacrificing sleep.",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        lineHeight = 18.sp
                                    )
                                )
                            }
                        }
                    }
                }
            } else {
                // MANAGE RECURRING ACTIVITIES VIEW
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = AmberGold.copy(alpha = 0.12f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Repeat,
                                    contentDescription = null,
                                    tint = AmberGold,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Recurring Habits & Fixed Slots",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = AmberGold
                                        )
                                    )
                                    Text(
                                        text = "Define recurring exercise, study blocks, lectures, and drills. They auto-populate into your daily schedule and can be synced with your device calendar.",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                }
                            }
                        }
                    }

                    if (recurringActivities.isEmpty()) {
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
                                        imageVector = Icons.Outlined.Schedule,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(40.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text("No recurring activities defined", fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "Tap '+ New Activity' to set up Morning Exercise, Study Blocks, or Batches.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    } else {
                        items(recurringActivities) { activity ->
                            RecurringActivityCard(
                                activity = activity,
                                onToggle = { onToggleRecurringActivity(activity) },
                                onEdit = {
                                    editingActivity = activity
                                    showAddDialog = true
                                },
                                onDelete = { onDeleteRecurringActivity(activity.id) },
                                onExportDevice = {
                                    NativeCalendarHelper.insertRoutineToDeviceCalendar(context, activity)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Add / Edit Recurring Activity Dialog
    if (showAddDialog) {
        RecurringActivityDialog(
            initialActivity = editingActivity,
            onDismiss = {
                showAddDialog = false
                editingActivity = null
            },
            onSave = { title, category, start, end, days, loc, color, rem, notes, syncDevice ->
                if (editingActivity != null) {
                    onUpdateRecurringActivity(
                        editingActivity!!.copy(
                            title = title,
                            category = category,
                            startTime = start,
                            endTime = end,
                            daysOfWeek = days,
                            location = loc,
                            colorHex = color,
                            remindersMinutes = rem,
                            notes = notes,
                            updatedAt = System.currentTimeMillis()
                        )
                    )
                    if (syncDevice) {
                        NativeCalendarHelper.insertRoutineToDeviceCalendar(context, editingActivity!!)
                    }
                } else {
                    onAddRecurringActivity(
                        title, category, start, end, days, loc, color, rem, notes, syncDevice, context
                    )
                }
                showAddDialog = false
                editingActivity = null
            }
        )
    }
}

// Sealed class for combining timeline items
sealed class TimelineEntry(val startTime: String) {
    data class Routine(val activity: RecurringActivityEntity) : TimelineEntry(activity.startTime)
    data class Event(val event: CalendarEventEntity) : TimelineEntry(event.startTime)
}

@Composable
fun RoutineTimelineCard(
    routine: RecurringActivityEntity,
    isViewingRealToday: Boolean,
    nowMinutes: Int,
    onExportDeviceCalendar: () -> Unit,
    onEdit: () -> Unit
) {
    val routineColor = remember(routine.colorHex) {
        try {
            Color(android.graphics.Color.parseColor(routine.colorHex))
        } catch (e: Exception) {
            AmberGold
        }
    }

    val isCurrentActiveSlot = remember(isViewingRealToday, nowMinutes, routine) {
        if (!isViewingRealToday) false
        else {
            val startParts = routine.startTime.split(":")
            val endParts = routine.endTime.split(":")
            if (startParts.size == 2 && endParts.size == 2) {
                val sMin = (startParts[0].toIntOrNull() ?: 0) * 60 + (startParts[1].toIntOrNull() ?: 0)
                val eMin = (endParts[0].toIntOrNull() ?: 0) * 60 + (endParts[1].toIntOrNull() ?: 0)
                nowMinutes in sMin until eMin
            } else false
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("timeline_routine_${routine.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentActiveSlot) {
                routineColor.copy(alpha = 0.15f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = if (isCurrentActiveSlot) {
            CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(routineColor)
            )
        } else {
            CardDefaults.outlinedCardBorder()
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Category colored left pill
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(52.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(routineColor)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = routineColor.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "ROUTINE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = routineColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = routine.category,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }

                    if (isCurrentActiveSlot) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = routineColor
                        ) {
                            Text(
                                text = "● ACTIVE NOW",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = routine.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${routine.startTime} – ${routine.endTime}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )

                    if (routine.location.isNotBlank()) {
                        Spacer(modifier = Modifier.width(10.dp))
                        Icon(
                            imageVector = Icons.Outlined.Place,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = routine.location,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            maxLines = 1
                        )
                    }
                }

                if (routine.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = routine.notes,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        ),
                        maxLines = 2
                    )
                }
            }

            IconButton(
                onClick = onExportDeviceCalendar,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.CalendarMonth,
                    contentDescription = "Export to device calendar",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun EventTimelineCard(
    event: CalendarEventEntity,
    onExportDeviceCalendar: () -> Unit
) {
    val eventColor = remember(event.colorHex) {
        try {
            Color(android.graphics.Color.parseColor(event.colorHex))
        } catch (e: Exception) {
            SteelBlue
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("timeline_event_${event.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = eventColor.copy(alpha = 0.08f)
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(eventColor.copy(alpha = 0.5f))
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(52.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(eventColor)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = eventColor
                        ) {
                            Text(
                                text = "CALENDAR EVENT",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = event.category,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = eventColor,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    if (event.remindersMinutes.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.NotificationsActive,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp),
                                    tint = AmberGold
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Reminders on",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.AccessTime,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${event.startTime} – ${event.endTime}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )

                    if (event.location.isNotBlank()) {
                        Spacer(modifier = Modifier.width(10.dp))
                        Icon(
                            imageVector = Icons.Outlined.Place,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = SteelBlue
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = event.location,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = SteelBlue,
                                fontWeight = FontWeight.Medium
                            ),
                            maxLines = 1
                        )
                    }
                }

                if (event.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = event.description,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        maxLines = 2
                    )
                }
            }

            IconButton(
                onClick = onExportDeviceCalendar,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.CalendarMonth,
                    contentDescription = "Sync to device calendar",
                    tint = AmberGold,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun RecurringActivityCard(
    activity: RecurringActivityEntity,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onExportDevice: () -> Unit
) {
    val categoryColor = remember(activity.colorHex) {
        try {
            Color(android.graphics.Color.parseColor(activity.colorHex))
        } catch (e: Exception) {
            AmberGold
        }
    }

    val daysList = listOf("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT")
    val activeDays = remember(activity.daysOfWeek) {
        activity.daysOfWeek.split(",").map { it.trim().uppercase() }.toSet()
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("recurring_card_${activity.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(categoryColor, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = activity.category,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = categoryColor,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    if (activity.location.isNotBlank()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "• 📍 ${activity.location}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            maxLines = 1
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = activity.isActive,
                        onCheckedChange = { onToggle() },
                        modifier = Modifier.testTag("switch_active_${activity.id}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = activity.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Schedule,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${activity.startTime} – ${activity.endTime}",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )

                if (activity.remindersMinutes.isNotBlank()) {
                    Spacer(modifier = Modifier.width(12.dp))
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp),
                        tint = AmberGold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${activity.remindersMinutes}m before",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }

            if (activity.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = activity.notes,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Active Days Pills
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                daysList.forEach { d ->
                    val isActive = activeDays.contains(d) || activeDays.contains("ALL")
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (isActive) {
                            AmberGold.copy(alpha = 0.2f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        }
                    ) {
                        Text(
                            text = d.take(1),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Normal,
                                color = if (isActive) AmberGold else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                fontSize = 10.sp
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                IconButton(onClick = onExportDevice, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Outlined.CalendarMonth,
                        contentDescription = "Sync to native calendar",
                        tint = AmberGold,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = "Edit activity",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Delete activity",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringActivityDialog(
    initialActivity: RecurringActivityEntity?,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String, String, String, String, String, Boolean) -> Unit
) {
    var title by remember { mutableStateOf(initialActivity?.title ?: "") }
    var category by remember { mutableStateOf(initialActivity?.category ?: "DEEP_STUDY") }
    var startTime by remember { mutableStateOf(initialActivity?.startTime ?: "07:00") }
    var endTime by remember { mutableStateOf(initialActivity?.endTime ?: "08:30") }
    var location by remember { mutableStateOf(initialActivity?.location ?: "") }
    var reminderMins by remember { mutableStateOf(initialActivity?.remindersMinutes ?: "15") }
    var notes by remember { mutableStateOf(initialActivity?.notes ?: "") }
    var syncToNative by remember { mutableStateOf(false) }

    val daysList = listOf("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT")
    val selectedDays = remember {
        mutableStateListOf<String>().apply {
            if (initialActivity != null) {
                addAll(initialActivity.daysOfWeek.split(",").map { it.trim().uppercase() })
            } else {
                addAll(listOf("SUN", "MON", "TUE", "WED", "THU"))
            }
        }
    }

    val categories = listOf(
        "DEEP_STUDY" to "#749ECF",
        "EXERCISE" to "#8FBF9D",
        "PRACTICE" to "#6FB9AE",
        "REVISION" to "#AC91D6",
        "COLLEGE" to "#DD9257",
        "BATCH" to "#E6B869",
        "MEAL" to "#D9822B",
        "SLEEP" to "#5C7080",
        "CUSTOM" to "#9E9E9E"
    )

    var selectedColor by remember {
        mutableStateOf(initialActivity?.colorHex ?: "#749ECF")
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .testTag("dialog_recurring_activity"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                Text(
                    text = if (initialActivity != null) "Edit Recurring Activity" else "New Recurring Activity",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Recurring habits and study blocks appear every selected day.",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )

                Spacer(modifier = Modifier.height(14.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Activity Title *") },
                            placeholder = { Text("e.g., Morning Exercise, SS1 Deep Focus Block") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_activity_title"),
                            singleLine = true
                        )
                    }

                    item {
                        Text("Category", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(categories) { (cat, colorHex) ->
                                val isSel = category == cat
                                FilterChip(
                                    selected = isSel,
                                    onClick = {
                                        category = cat
                                        selectedColor = colorHex
                                    },
                                    label = { Text(cat) },
                                    leadingIcon = {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .background(
                                                    Color(android.graphics.Color.parseColor(colorHex)),
                                                    CircleShape
                                                )
                                        )
                                    }
                                )
                            }
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = startTime,
                                onValueChange = { startTime = it },
                                label = { Text("Start Time") },
                                placeholder = { Text("07:00") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("input_activity_start_time"),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = endTime,
                                onValueChange = { endTime = it },
                                label = { Text("End Time") },
                                placeholder = { Text("08:30") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("input_activity_end_time"),
                                singleLine = true
                            )
                        }
                    }

                    item {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Days of the Week", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                                Row {
                                    TextButton(onClick = {
                                        selectedDays.clear()
                                        selectedDays.addAll(daysList)
                                    }) {
                                        Text("All", style = MaterialTheme.typography.labelSmall)
                                    }
                                    TextButton(onClick = {
                                        selectedDays.clear()
                                        selectedDays.addAll(listOf("SUN", "MON", "TUE", "WED", "THU"))
                                    }) {
                                        Text("Weekdays", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                daysList.forEach { d ->
                                    val isChecked = selectedDays.contains(d)
                                    Surface(
                                        onClick = {
                                            if (isChecked) selectedDays.remove(d) else selectedDays.add(d)
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isChecked) AmberGold else MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier.size(42.dp)
                                    ) {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier.fillMaxSize()
                                        ) {
                                            Text(
                                                text = d.take(2),
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isChecked) DeepObsidian else MaterialTheme.colorScheme.onSurface
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = location,
                            onValueChange = { location = it },
                            label = { Text("Location (Optional)") },
                            placeholder = { Text("e.g., Study Desk, Hall 201, Local Park") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_activity_location"),
                            singleLine = true
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = reminderMins,
                            onValueChange = { reminderMins = it },
                            label = { Text("Reminder (Minutes before start)") },
                            placeholder = { Text("e.g., 15") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("Notes / Instructions") },
                            placeholder = { Text("Focus areas, goals, or reminders for this slot") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2
                        )
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = syncToNative,
                                    onCheckedChange = { syncToNative = it }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Sync to Device Native Calendar",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                    )
                                    Text(
                                        text = "Exports recurring rule to Google Calendar/Samsung Calendar",
                                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isNotBlank() && selectedDays.isNotEmpty()) {
                                onSave(
                                    title.trim(),
                                    category,
                                    startTime.ifBlank { "07:00" },
                                    endTime.ifBlank { "08:30" },
                                    selectedDays.joinToString(","),
                                    location.trim(),
                                    selectedColor,
                                    reminderMins.ifBlank { "15" },
                                    notes.trim(),
                                    syncToNative
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AmberGold, contentColor = DeepObsidian),
                        enabled = title.isNotBlank() && selectedDays.isNotEmpty(),
                        modifier = Modifier.testTag("btn_save_recurring_activity")
                    ) {
                        Text("Save Activity", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
