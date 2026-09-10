package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.entity.CalendarEventEntity
import com.example.data.local.entity.RecurringActivityEntity
import com.example.ui.theme.*
import com.example.util.NativeCalendarHelper
import java.text.SimpleDateFormat
import java.util.*

enum class CalendarViewMode {
    DAILY,
    WEEKLY,
    MONTHLY
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CalendarScreen(
    events: List<CalendarEventEntity>,
    recurringActivities: List<RecurringActivityEntity>,
    selectedDate: String,
    onSelectDate: (String) -> Unit,
    onAddEvent: (String, String, String, String, String, String, String, String, String, Boolean, Boolean, Context) -> Unit,
    onUpdateEvent: (CalendarEventEntity, Boolean, Context) -> Unit,
    onDeleteEvent: (CalendarEventEntity) -> Unit,
    onSyncEventToNative: (Context, CalendarEventEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var viewMode by remember { mutableStateOf(CalendarViewMode.MONTHLY) }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingEvent by remember { mutableStateOf<CalendarEventEntity?>(null) }

    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }
    val displayDateFormat = remember { SimpleDateFormat("EEE, MMMM d, yyyy", Locale.US) }
    val monthYearFormat = remember { SimpleDateFormat("MMMM yyyy", Locale.US) }

    val todayStr = remember { dateFormat.format(Date()) }

    // Calendar state for month navigation
    val selectedCal = remember(selectedDate) {
        Calendar.getInstance().apply {
            try {
                val parsed = dateFormat.parse(selectedDate)
                if (parsed != null) time = parsed
            } catch (e: Exception) {}
        }
    }

    var displayedMonth by remember { mutableIntStateOf(selectedCal.get(Calendar.MONTH)) }
    var displayedYear by remember { mutableIntStateOf(selectedCal.get(Calendar.YEAR)) }

    val displayedCalendar = remember(displayedMonth, displayedYear) {
        Calendar.getInstance().apply {
            set(Calendar.YEAR, displayedYear)
            set(Calendar.MONTH, displayedMonth)
            set(Calendar.DAY_OF_MONTH, 1)
        }
    }

    val daysInMonth = displayedCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfWeek = displayedCalendar.get(Calendar.DAY_OF_WEEK) // 1 = Sunday

    // Day of week string for selected date (e.g. "THU")
    val selectedDowCode = remember(selectedDate) {
        val cal = Calendar.getInstance().apply {
            try {
                val p = dateFormat.parse(selectedDate)
                if (p != null) time = p
            } catch (e: Exception) {}
        }
        when (cal.get(Calendar.DAY_OF_WEEK)) {
            Calendar.SUNDAY -> "SUN"
            Calendar.MONDAY -> "MON"
            Calendar.TUESDAY -> "TUE"
            Calendar.WEDNESDAY -> "WED"
            Calendar.THURSDAY -> "THU"
            Calendar.FRIDAY -> "FRI"
            Calendar.SATURDAY -> "SAT"
            else -> "SUN"
        }
    }

    // Routines active on selected date
    val routinesForSelectedDate = remember(recurringActivities, selectedDowCode) {
        recurringActivities.filter {
            it.isActive && (it.daysOfWeek.contains(selectedDowCode) || it.daysOfWeek.contains("ALL"))
        }.sortedBy { it.startTime }
    }

    // Events for selected date
    val eventsForSelectedDate = remember(events, selectedDate) {
        events.filter { it.eventDate == selectedDate }.sortedBy { it.startTime }
    }

    // Event dates set for month dots
    val eventDatesMap = remember(events) {
        events.groupBy { it.eventDate }
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    editingEvent = null
                    showAddDialog = true
                },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Add Event", fontWeight = FontWeight.Bold) },
                containerColor = AmberGold,
                contentColor = DeepObsidian,
                modifier = Modifier.testTag("fab_add_calendar_event")
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .testTag("calendar_screen")
        ) {
            // Header Bar with View Switcher & Actions
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(top = 8.dp, start = 16.dp, end = 16.dp, bottom = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Calendar & Schedules",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 20.sp
                                )
                            )
                            Text(
                                text = "Daily, weekly, and monthly views with reminders",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    NativeCalendarHelper.openDeviceCalendarAtDate(context, selectedDate)
                                },
                                modifier = Modifier.testTag("btn_open_device_calendar")
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.OpenInNew,
                                    contentDescription = "Open Device Native Calendar",
                                    tint = AmberGold
                                )
                            }

                            FilledTonalButton(
                                onClick = {
                                    val todayCal = Calendar.getInstance()
                                    displayedMonth = todayCal.get(Calendar.MONTH)
                                    displayedYear = todayCal.get(Calendar.YEAR)
                                    onSelectDate(todayStr)
                                },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("btn_jump_today")
                            ) {
                                Text("Today", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // View Mode Switcher: Daily | Weekly | Monthly
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3),
                            onClick = { viewMode = CalendarViewMode.DAILY },
                            selected = viewMode == CalendarViewMode.DAILY,
                            icon = { SegmentedButtonDefaults.Icon(active = viewMode == CalendarViewMode.DAILY) },
                            modifier = Modifier.testTag("viewmode_daily")
                        ) {
                            Text("Daily")
                        }
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3),
                            onClick = { viewMode = CalendarViewMode.WEEKLY },
                            selected = viewMode == CalendarViewMode.WEEKLY,
                            icon = { SegmentedButtonDefaults.Icon(active = viewMode == CalendarViewMode.WEEKLY) },
                            modifier = Modifier.testTag("viewmode_weekly")
                        ) {
                            Text("Weekly")
                        }
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3),
                            onClick = { viewMode = CalendarViewMode.MONTHLY },
                            selected = viewMode == CalendarViewMode.MONTHLY,
                            icon = { SegmentedButtonDefaults.Icon(active = viewMode == CalendarViewMode.MONTHLY) },
                            modifier = Modifier.testTag("viewmode_monthly")
                        ) {
                            Text("Monthly")
                        }
                    }
                }
            }

            // View Content Switcher
            when (viewMode) {
                CalendarViewMode.MONTHLY -> {
                    MonthlyCalendarView(
                        displayedCalendar = displayedCalendar,
                        displayedMonth = displayedMonth,
                        displayedYear = displayedYear,
                        daysInMonth = daysInMonth,
                        firstDayOfWeek = firstDayOfWeek,
                        selectedDate = selectedDate,
                        todayStr = todayStr,
                        eventDatesMap = eventDatesMap,
                        eventsForSelectedDate = eventsForSelectedDate,
                        routinesForSelectedDate = routinesForSelectedDate,
                        onPrevMonth = {
                            if (displayedMonth == 0) {
                                displayedMonth = 11
                                displayedYear--
                            } else {
                                displayedMonth--
                            }
                        },
                        onNextMonth = {
                            if (displayedMonth == 11) {
                                displayedMonth = 0
                                displayedYear++
                            } else {
                                displayedMonth++
                            }
                        },
                        onSelectDate = onSelectDate,
                        onEditEvent = {
                            editingEvent = it
                            showAddDialog = true
                        },
                        onDeleteEvent = onDeleteEvent,
                        onSyncEvent = { onSyncEventToNative(context, it) },
                        onAddEventClick = {
                            editingEvent = null
                            showAddDialog = true
                        }
                    )
                }
                CalendarViewMode.WEEKLY -> {
                    WeeklyCalendarView(
                        selectedDate = selectedDate,
                        todayStr = todayStr,
                        events = events,
                        recurringActivities = recurringActivities,
                        onSelectDate = onSelectDate,
                        onEditEvent = {
                            editingEvent = it
                            showAddDialog = true
                        },
                        onDeleteEvent = onDeleteEvent,
                        onSyncEvent = { onSyncEventToNative(context, it) }
                    )
                }
                CalendarViewMode.DAILY -> {
                    DailyCalendarView(
                        selectedDate = selectedDate,
                        todayStr = todayStr,
                        eventsForSelectedDate = eventsForSelectedDate,
                        routinesForSelectedDate = routinesForSelectedDate,
                        onSelectDate = onSelectDate,
                        onEditEvent = {
                            editingEvent = it
                            showAddDialog = true
                        },
                        onDeleteEvent = onDeleteEvent,
                        onSyncEvent = { onSyncEventToNative(context, it) },
                        onAddEventClick = {
                            editingEvent = null
                            showAddDialog = true
                        }
                    )
                }
            }
        }
    }

    // Add / Edit Event Dialog
    if (showAddDialog) {
        CustomEventDialog(
            initialEvent = editingEvent,
            defaultDate = selectedDate,
            onDismiss = {
                showAddDialog = false
                editingEvent = null
            },
            onSave = { title, desc, date, start, end, cat, color, loc, reminders, notify, syncNative ->
                if (editingEvent != null) {
                    onUpdateEvent(
                        editingEvent!!.copy(
                            title = title,
                            description = desc,
                            eventDate = date,
                            startTime = start,
                            endTime = end,
                            category = cat,
                            colorHex = color,
                            location = loc,
                            remindersMinutes = reminders,
                            notifyReminder = notify,
                            updatedAt = System.currentTimeMillis()
                        ),
                        syncNative,
                        context
                    )
                } else {
                    onAddEvent(
                        title, desc, date, start, end, cat, color, loc, reminders, notify, syncNative, context
                    )
                }
                showAddDialog = false
                editingEvent = null
            }
        )
    }
}

// ---------------------------------------------------------------------------
// MONTHLY VIEW
// ---------------------------------------------------------------------------
@Composable
fun MonthlyCalendarView(
    displayedCalendar: Calendar,
    displayedMonth: Int,
    displayedYear: Int,
    daysInMonth: Int,
    firstDayOfWeek: Int,
    selectedDate: String,
    todayStr: String,
    eventDatesMap: Map<String, List<CalendarEventEntity>>,
    eventsForSelectedDate: List<CalendarEventEntity>,
    routinesForSelectedDate: List<RecurringActivityEntity>,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onSelectDate: (String) -> Unit,
    onEditEvent: (CalendarEventEntity) -> Unit,
    onDeleteEvent: (CalendarEventEntity) -> Unit,
    onSyncEvent: (CalendarEventEntity) -> Unit,
    onAddEventClick: () -> Unit
) {
    val monthFormat = remember { SimpleDateFormat("MMMM yyyy", Locale.US) }
    val dayNames = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Month Navigation Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onPrevMonth, modifier = Modifier.testTag("btn_prev_month")) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Month")
                        }
                        Text(
                            text = monthFormat.format(displayedCalendar.time),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        IconButton(onClick = onNextMonth, modifier = Modifier.testTag("btn_next_month")) {
                            Icon(Icons.Default.ChevronRight, contentDescription = "Next Month")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Day Header
                    Row(modifier = Modifier.fillMaxWidth()) {
                        dayNames.forEach { dayName ->
                            Text(
                                text = dayName,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Grid of Days
                    val totalSlots = ((firstDayOfWeek - 1) + daysInMonth + 6) / 7 * 7
                    val weeks = totalSlots / 7

                    for (w in 0 until weeks) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            for (d in 1..7) {
                                val slotIndex = w * 7 + d
                                val dayNum = slotIndex - (firstDayOfWeek - 1)

                                if (dayNum in 1..daysInMonth) {
                                    val dateStr = String.format(
                                        Locale.US,
                                        "%04d-%02d-%02d",
                                        displayedYear,
                                        displayedMonth + 1,
                                        dayNum
                                    )
                                    val isSelected = dateStr == selectedDate
                                    val isToday = dateStr == todayStr
                                    val dayEvents = eventDatesMap[dateStr] ?: emptyList()

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .padding(2.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                when {
                                                    isSelected -> MaterialTheme.colorScheme.primaryContainer
                                                    isToday -> AmberGold.copy(alpha = 0.15f)
                                                    else -> Color.Transparent
                                                }
                                            )
                                            .border(
                                                width = if (isSelected) 1.5.dp else if (isToday) 1.dp else 0.dp,
                                                color = if (isSelected) AmberGold else if (isToday) AmberGold.copy(alpha = 0.5f) else Color.Transparent,
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .clickable { onSelectDate(dateStr) }
                                            .testTag("calendar_day_$dateStr"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text = "$dayNum",
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isSelected) {
                                                        MaterialTheme.colorScheme.onPrimaryContainer
                                                    } else if (isToday) {
                                                        AmberGold
                                                    } else {
                                                        MaterialTheme.colorScheme.onSurface
                                                    }
                                                )
                                            )
                                            if (dayEvents.isNotEmpty()) {
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                                                    modifier = Modifier.padding(top = 2.dp)
                                                ) {
                                                    dayEvents.take(3).forEach { ev ->
                                                        val dotColor = try {
                                                            Color(android.graphics.Color.parseColor(ev.colorHex))
                                                        } catch (e: Exception) {
                                                            AmberGold
                                                        }
                                                        Box(
                                                            modifier = Modifier
                                                                .size(4.dp)
                                                                .background(dotColor, CircleShape)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Selected Date Schedule Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SCHEDULE FOR $selectedDate",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Text(
                        text = "${eventsForSelectedDate.size} Events • ${routinesForSelectedDate.size} Routine Blocks",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }

                FilledTonalButton(
                    onClick = onAddEventClick,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Event", style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        // Selected Date Items
        if (eventsForSelectedDate.isEmpty() && routinesForSelectedDate.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.EventBusy,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No events or routine blocks on this day",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap 'Add Event' above to schedule an exam, class, or milestone.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        } else {
            // Events first
            items(eventsForSelectedDate) { event ->
                CalendarEventCard(
                    event = event,
                    onEdit = { onEditEvent(event) },
                    onDelete = { onDeleteEvent(event) },
                    onSyncNative = { onSyncEvent(event) }
                )
            }

            // Recurring Routines for this day
            items(routinesForSelectedDate) { routine ->
                RoutineDaySummaryCard(routine = routine)
            }
        }
    }
}

// ---------------------------------------------------------------------------
// WEEKLY VIEW
// ---------------------------------------------------------------------------
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WeeklyCalendarView(
    selectedDate: String,
    todayStr: String,
    events: List<CalendarEventEntity>,
    recurringActivities: List<RecurringActivityEntity>,
    onSelectDate: (String) -> Unit,
    onEditEvent: (CalendarEventEntity) -> Unit,
    onDeleteEvent: (CalendarEventEntity) -> Unit,
    onSyncEvent: (CalendarEventEntity) -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }
    val dayOfWeekFormat = remember { SimpleDateFormat("EEE", Locale.US) }
    val dayNumberFormat = remember { SimpleDateFormat("d", Locale.US) }

    // Compute the 7 days of the selected date's week (starting Sunday)
    val weekDays = remember(selectedDate) {
        val cal = Calendar.getInstance().apply {
            try {
                val p = dateFormat.parse(selectedDate)
                if (p != null) time = p
            } catch (e: Exception) {}
        }
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        (0 until 7).map {
            val dStr = dateFormat.format(cal.time)
            val dow = dayOfWeekFormat.format(cal.time)
            val dNum = dayNumberFormat.format(cal.time)
            val dowCode = when (cal.get(Calendar.DAY_OF_WEEK)) {
                Calendar.SUNDAY -> "SUN"
                Calendar.MONDAY -> "MON"
                Calendar.TUESDAY -> "TUE"
                Calendar.WEDNESDAY -> "WED"
                Calendar.THURSDAY -> "THU"
                Calendar.FRIDAY -> "FRI"
                Calendar.SATURDAY -> "SAT"
                else -> "SUN"
            }
            cal.add(Calendar.DAY_OF_YEAR, 1)
            Triple(dStr, "$dow $dNum", dowCode)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Week Days Selector Strip
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "WEEK AT A GLANCE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        weekDays.forEach { (dStr, label, _) ->
                            val isSelected = dStr == selectedDate
                            val isToday = dStr == todayStr
                            val dayEventsCount = events.count { it.eventDate == dStr }

                            Surface(
                                onClick = { onSelectDate(dStr) },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                border = if (isSelected) CardDefaults.outlinedCardBorder().copy(
                                    brush = androidx.compose.ui.graphics.SolidColor(AmberGold)
                                ) else null,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 2.dp)
                                    .height(60.dp)
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = label.split(" ")[0],
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 10.sp
                                        )
                                    )
                                    Text(
                                        text = label.split(" ")[1],
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) AmberGold else MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                    if (dayEventsCount > 0) {
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
        }

        // Full 7-Day Timetable Cards
        weekDays.forEach { (dStr, label, dowCode) ->
            val dayEvents = events.filter { it.eventDate == dStr }.sortedBy { it.startTime }
            val dayRoutines = recurringActivities.filter {
                it.isActive && (it.daysOfWeek.contains(dowCode) || it.daysOfWeek.contains("ALL"))
            }.sortedBy { it.startTime }
            val isSelectedDay = dStr == selectedDate

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectDate(dStr) }
                        .testTag("weekly_day_card_$dStr"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelectedDay) {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        } else {
                            MaterialTheme.colorScheme.surface
                        }
                    ),
                    border = if (isSelectedDay) {
                        CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(AmberGold)
                        )
                    } else {
                        CardDefaults.outlinedCardBorder()
                    }
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelectedDay) AmberGold else MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                if (dStr == todayStr) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = AmberGold.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = "TODAY",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = AmberGold,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 9.sp
                                            ),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            Text(
                                text = "${dayEvents.size} events • ${dayRoutines.size} routine slots",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (dayEvents.isEmpty() && dayRoutines.isEmpty()) {
                            Text(
                                text = "Free day / No scheduled items",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            )
                        } else {
                            // Compact chips preview
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                dayEvents.forEach { ev ->
                                    val evColor = try {
                                        Color(android.graphics.Color.parseColor(ev.colorHex))
                                    } catch (e: Exception) {
                                        SteelBlue
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = evColor.copy(alpha = 0.15f),
                                        border = CardDefaults.outlinedCardBorder().copy(
                                            brush = androidx.compose.ui.graphics.SolidColor(evColor.copy(alpha = 0.5f))
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(modifier = Modifier.size(6.dp).background(evColor, CircleShape))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "${ev.startTime} ${ev.title}",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 11.sp
                                                )
                                            )
                                        }
                                    }
                                }

                                dayRoutines.take(4).forEach { rt ->
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "${rt.startTime} ${rt.title}",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    fontSize = 11.sp
                                                )
                                            )
                                        }
                                    }
                                }

                                if (dayRoutines.size > 4) {
                                    Text(
                                        text = "+${dayRoutines.size - 4} more",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 10.sp
                                        ),
                                        modifier = Modifier.padding(top = 4.dp)
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

// ---------------------------------------------------------------------------
// DAILY VIEW
// ---------------------------------------------------------------------------
@Composable
fun DailyCalendarView(
    selectedDate: String,
    todayStr: String,
    eventsForSelectedDate: List<CalendarEventEntity>,
    routinesForSelectedDate: List<RecurringActivityEntity>,
    onSelectDate: (String) -> Unit,
    onEditEvent: (CalendarEventEntity) -> Unit,
    onDeleteEvent: (CalendarEventEntity) -> Unit,
    onSyncEvent: (CalendarEventEntity) -> Unit,
    onAddEventClick: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }
    val displayFormat = remember { SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.US) }

    val formattedDateTitle = remember(selectedDate) {
        try {
            val p = dateFormat.parse(selectedDate)
            if (p != null) displayFormat.format(p) else selectedDate
        } catch (e: Exception) {
            selectedDate
        }
    }

    val combined = remember(eventsForSelectedDate, routinesForSelectedDate) {
        val ev = eventsForSelectedDate.map { TimelineEntry.Event(it) }
        val rt = routinesForSelectedDate.map { TimelineEntry.Routine(it) }
        (ev + rt).sortedBy { it.startTime }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Day Date Header & Day Switcher
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            val cal = Calendar.getInstance().apply {
                                try { time = dateFormat.parse(selectedDate)!! } catch (e: Exception) {}
                                add(Calendar.DAY_OF_YEAR, -1)
                            }
                            onSelectDate(dateFormat.format(cal.time))
                        }
                    ) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Day")
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = formattedDateTitle,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        if (selectedDate == todayStr) {
                            Text(
                                text = "TODAY'S SCHEDULE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = AmberGold,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.sp
                                )
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            val cal = Calendar.getInstance().apply {
                                try { time = dateFormat.parse(selectedDate)!! } catch (e: Exception) {}
                                add(Calendar.DAY_OF_YEAR, 1)
                            }
                            onSelectDate(dateFormat.format(cal.time))
                        }
                    ) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Next Day")
                    }
                }
            }
        }

        // Summary Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "COMBINED DAY TIMELINE",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Text(
                    text = "${eventsForSelectedDate.size + routinesForSelectedDate.size} Total Blocks",
                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }
        }

        // Unified Chronological Timeline
        if (combined.isEmpty()) {
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
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("No events or routines scheduled for this day", fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = onAddEventClick,
                            colors = ButtonDefaults.buttonColors(containerColor = AmberGold, contentColor = DeepObsidian)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Schedule Custom Event")
                        }
                    }
                }
            }
        } else {
            items(combined) { entry ->
                when (entry) {
                    is TimelineEntry.Event -> {
                        CalendarEventCard(
                            event = entry.event,
                            onEdit = { onEditEvent(entry.event) },
                            onDelete = { onDeleteEvent(entry.event) },
                            onSyncNative = { onSyncEvent(entry.event) }
                        )
                    }
                    is TimelineEntry.Routine -> {
                        RoutineDaySummaryCard(routine = entry.activity)
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// EVENT & ROUTINE CARDS
// ---------------------------------------------------------------------------
@Composable
fun CalendarEventCard(
    event: CalendarEventEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onSyncNative: () -> Unit
) {
    val eventColor = remember(event.colorHex) {
        try {
            Color(android.graphics.Color.parseColor(event.colorHex))
        } catch (e: Exception) {
            SteelBlue
        }
    }

    val remindersList = remember(event.remindersMinutes) {
        event.remindersMinutes.split(",").mapNotNull { it.trim().toIntOrNull() }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("event_card_${event.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(eventColor.copy(alpha = 0.6f))
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
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
                            text = event.category,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    if (event.location.isNotBlank()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Place,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = SteelBlue
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = event.location,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = SteelBlue,
                                    fontWeight = FontWeight.Medium
                                ),
                                maxLines = 1
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onSyncNative,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CalendarMonth,
                            contentDescription = "Sync to native calendar",
                            tint = AmberGold,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = "Edit event",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = "Delete event",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = event.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )

            if (event.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = event.description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.AccessTime,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${event.startTime} – ${event.endTime}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }

                if (remindersList.isNotEmpty() && event.notifyReminder) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.NotificationsActive,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = AmberGold
                        )
                        val labels = remindersList.map { m ->
                            when {
                                m == 0 -> "0m"
                                m < 60 -> "${m}m"
                                m < 1440 -> "${m / 60}h"
                                else -> "${m / 1440}d"
                            }
                        }.joinToString(", ")
                        Text(
                            text = "Reminders: $labels",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = AmberGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RoutineDaySummaryCard(routine: RecurringActivityEntity) {
    val routineColor = remember(routine.colorHex) {
        try {
            Color(android.graphics.Color.parseColor(routine.colorHex))
        } catch (e: Exception) {
            AmberGold
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        ),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(routineColor, CircleShape)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "ROUTINE • ${routine.category}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = routineColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp
                        )
                    )
                    if (routine.location.isNotBlank()) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "📍 ${routine.location}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp
                            ),
                            maxLines = 1
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = routine.title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            }
            Text(
                text = "${routine.startTime}–${routine.endTime}",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

// ---------------------------------------------------------------------------
// ADD / EDIT CUSTOM EVENT DIALOG WITH MULTIPLE REMINDERS & LOCATION
// ---------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CustomEventDialog(
    initialEvent: CalendarEventEntity?,
    defaultDate: String,
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        desc: String,
        date: String,
        start: String,
        end: String,
        cat: String,
        color: String,
        loc: String,
        reminders: String,
        notify: Boolean,
        syncNative: Boolean
    ) -> Unit
) {
    var title by remember { mutableStateOf(initialEvent?.title ?: "") }
    var description by remember { mutableStateOf(initialEvent?.description ?: "") }
    var eventDate by remember { mutableStateOf(initialEvent?.eventDate ?: defaultDate) }
    var startTime by remember { mutableStateOf(initialEvent?.startTime ?: "09:00") }
    var endTime by remember { mutableStateOf(initialEvent?.endTime ?: "10:30") }
    var location by remember { mutableStateOf(initialEvent?.location ?: "") }
    var category by remember { mutableStateOf(initialEvent?.category ?: "EXAM") }
    var notifyReminder by remember { mutableStateOf(initialEvent?.notifyReminder ?: true) }
    var syncToNativeCalendar by remember { mutableStateOf(false) }

    // Multiple Reminders selection
    val availableReminderOptions = listOf(
        0 to "At event start",
        15 to "15 min before",
        30 to "30 min before",
        60 to "1 hour before",
        120 to "2 hours before",
        1440 to "1 day before"
    )

    val selectedReminders = remember {
        mutableStateListOf<Int>().apply {
            if (initialEvent != null && initialEvent.remindersMinutes.isNotBlank()) {
                val parsed = initialEvent.remindersMinutes.split(",").mapNotNull { it.trim().toIntOrNull() }
                addAll(parsed)
            } else {
                addAll(listOf(15, 60))
            }
        }
    }

    val categories = listOf(
        "EXAM" to "#EF5350",
        "MOCK_TEST" to "#AC91D6",
        "DEADLINE" to "#E6B869",
        "COLLEGE" to "#749ECF",
        "BATCH" to "#DD9257",
        "MILESTONE" to "#8FBF9D",
        "PERSONAL" to "#9E9E9E"
    )

    var selectedColor by remember {
        mutableStateOf(initialEvent?.colorHex ?: "#EF5350")
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .testTag("dialog_custom_event"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                Text(
                    text = if (initialEvent != null) "Edit Custom Event" else "Add Custom Event",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Specify date, times, location, multiple reminders, and native calendar sync.",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )

                Spacer(modifier = Modifier.height(14.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Event Title *") },
                            placeholder = { Text("e.g., Physics Term Exam, Calculus Batch") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_event_title"),
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
                        OutlinedTextField(
                            value = eventDate,
                            onValueChange = { eventDate = it },
                            label = { Text("Date (YYYY-MM-DD) *") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_event_date"),
                            singleLine = true
                        )
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
                                placeholder = { Text("09:00") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("input_event_start_time"),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = endTime,
                                onValueChange = { endTime = it },
                                label = { Text("End Time") },
                                placeholder = { Text("10:30") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("input_event_end_time"),
                                singleLine = true
                            )
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = location,
                            onValueChange = { location = it },
                            label = { Text("Location") },
                            placeholder = { Text("e.g., Hall 402, Science Complex, Home Desk") },
                            leadingIcon = {
                                Icon(Icons.Outlined.Place, contentDescription = null, tint = SteelBlue)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_event_location"),
                            singleLine = true
                        )
                    }

                    // Multiple Reminders Selection
                    item {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Multiple Reminders",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Switch(
                                    checked = notifyReminder,
                                    onCheckedChange = { notifyReminder = it }
                                )
                            }

                            if (notifyReminder) {
                                Spacer(modifier = Modifier.height(6.dp))
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    availableReminderOptions.forEach { (mins, label) ->
                                        val isChecked = selectedReminders.contains(mins)
                                        FilterChip(
                                            selected = isChecked,
                                            onClick = {
                                                if (isChecked) {
                                                    selectedReminders.remove(mins)
                                                } else {
                                                    selectedReminders.add(mins)
                                                }
                                            },
                                            label = { Text(label, fontSize = 11.sp) },
                                            leadingIcon = if (isChecked) {
                                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp)) }
                                            } else null
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Description / Notes") },
                            placeholder = { Text("Topics covered, materials to bring, or objectives") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2
                        )
                    }

                    // Native Calendar Sync Checkbox
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = syncToNativeCalendar,
                                    onCheckedChange = { syncToNativeCalendar = it },
                                    modifier = Modifier.testTag("checkbox_sync_device_calendar")
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Add to Device Native Calendar",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                    )
                                    Text(
                                        text = "Directly triggers Android Google Calendar / Samsung Calendar",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
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
                            if (title.isNotBlank() && eventDate.isNotBlank()) {
                                val remindersStr = selectedReminders.sorted().joinToString(",")
                                onSave(
                                    title.trim(),
                                    description.trim(),
                                    eventDate.trim(),
                                    startTime.ifBlank { "09:00" },
                                    endTime.ifBlank { "10:00" },
                                    category,
                                    selectedColor,
                                    location.trim(),
                                    remindersStr,
                                    notifyReminder,
                                    syncToNativeCalendar
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AmberGold, contentColor = DeepObsidian),
                        enabled = title.isNotBlank() && eventDate.isNotBlank(),
                        modifier = Modifier.testTag("btn_save_event")
                    ) {
                        Text("Save Event", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
