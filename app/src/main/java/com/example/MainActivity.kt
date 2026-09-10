package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.AppBottomNav
import com.example.ui.components.AppDestination
import com.example.ui.components.AppTopBar
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: MainViewModel = viewModel()
            val isDarkTheme by viewModel.isDarkTheme.collectAsState()

            MyApplicationTheme(darkTheme = isDarkTheme) {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Runtime Permission for Push Notifications (Android 13+)
    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else true
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // Navigation State
    var currentDestination by remember { mutableStateOf(AppDestination.DASHBOARD) }
    var showSyncSettings by remember { mutableStateOf(false) }

    // State Collection
    val currentDay by viewModel.currentDayNumber.collectAsState()
    val daysLeft by viewModel.daysLeft.collectAsState()
    val percentDone by viewModel.percentDone.collectAsState()
    val phaseInfo by viewModel.currentPhaseInfo.collectAsState()
    val previewDow by viewModel.previewDow.collectAsState()
    val todayChecklist by viewModel.todayChecklist.collectAsState()
    val streak by viewModel.currentStreak.collectAsState()
    val errorLogs by viewModel.errorLogs.collectAsState()
    val allTasks by viewModel.allTasks.collectAsState()
    val upcomingTasks by viewModel.upcomingTasks.collectAsState()
    val studyMaterials by viewModel.studyMaterials.collectAsState()
    val allNotes by viewModel.allNotes.collectAsState()
    val allEvents by viewModel.allEvents.collectAsState()
    val allRecurringActivities by viewModel.allRecurringActivities.collectAsState()
    val selectedCalendarDate by viewModel.selectedCalendarDate.collectAsState()

    val syncStatus by viewModel.syncManager.syncStatus.collectAsState()
    val lastSyncTime by viewModel.syncManager.lastSyncTime.collectAsState()
    val deviceSyncCode by viewModel.syncManager.deviceSyncCode.collectAsState()
    val autoSyncEnabled by viewModel.syncManager.autoSyncEnabled.collectAsState()
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            if (!showSyncSettings) {
                AppTopBar(
                    phaseTitle = phaseInfo.first,
                    phaseSubtitle = phaseInfo.second,
                    syncStatus = syncStatus,
                    isDarkMode = isDarkTheme,
                    onToggleDark = { viewModel.toggleDarkTheme(!isDarkTheme) },
                    onOpenSyncSettings = { showSyncSettings = true }
                )
            }
        },
        bottomBar = {
            if (!showSyncSettings) {
                AppBottomNav(
                    currentDestination = currentDestination,
                    onNavigate = { currentDestination = it }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = if (showSyncSettings) "SETTINGS" else currentDestination.name,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "screen_transition"
            ) { target ->
                when (target) {
                    "SETTINGS" -> {
                        SyncSettingsScreen(
                            syncStatus = syncStatus,
                            lastSyncTime = lastSyncTime,
                            deviceSyncCode = deviceSyncCode,
                            autoSyncEnabled = autoSyncEnabled,
                            isDarkMode = isDarkTheme,
                            onToggleDarkMode = { viewModel.toggleDarkTheme(it) },
                            onToggleAutoSync = { viewModel.syncManager.setAutoSync(it) },
                            onChangeSyncCode = { viewModel.syncManager.setSyncCode(it) },
                            onTriggerSync = { onDone -> viewModel.triggerCloudSync(onDone) },
                            onExportBackup = { onExp -> viewModel.exportCloudBackup(onExp) },
                            onImportBackup = { json, onImp -> viewModel.importCloudBackup(json, onImp) },
                            onTestNotification = { viewModel.testPushNotification() },
                            onBack = { showSyncSettings = false }
                        )
                    }
                    AppDestination.DASHBOARD.name -> {
                        DashboardScreen(
                            dayNumber = currentDay,
                            daysLeft = daysLeft,
                            percentDone = percentDone,
                            streak = streak,
                            checklist = todayChecklist,
                            errorLogs = errorLogs,
                            upcomingTasks = upcomingTasks,
                            onToggleChecklist = { key, cur -> viewModel.toggleChecklistItem(key, cur) },
                            onAddErrorLog = { topic -> viewModel.addErrorLog(topic) },
                            onDeleteErrorLog = { id -> viewModel.deleteErrorLog(id) },
                            onNavigateToRoutine = { currentDestination = AppDestination.ROUTINE },
                            onNavigateToTasks = { currentDestination = AppDestination.TASKS }
                        )
                    }
                    AppDestination.ROUTINE.name -> {
                        RoutineScreen(
                            dayNumber = currentDay,
                            previewDow = previewDow,
                            onSelectPreviewDow = { dow -> viewModel.setPreviewDow(dow) },
                            recurringActivities = allRecurringActivities,
                            calendarEvents = allEvents,
                            onAddRecurringActivity = { title, cat, start, end, days, loc, color, rem, notes, syncDevice, ctx ->
                                viewModel.addRecurringActivity(title, cat, start, end, days, loc, color, rem, notes, syncDevice, ctx)
                            },
                            onUpdateRecurringActivity = { viewModel.updateRecurringActivity(it) },
                            onToggleRecurringActivity = { viewModel.toggleRecurringActivityActive(it) },
                            onDeleteRecurringActivity = { viewModel.deleteRecurringActivity(it) },
                            onNavigateToCalendar = { currentDestination = AppDestination.CALENDAR }
                        )
                    }
                    AppDestination.TASKS.name -> {
                        TaskScreen(
                            tasks = allTasks,
                            onToggleTask = { task -> viewModel.toggleTaskCompleted(task) },
                            onDeleteTask = { id -> viewModel.deleteTask(id) },
                            onAddTask = { title, desc, subj, deadline, priority, reminderMins ->
                                viewModel.addTask(title, desc, subj, deadline, priority, reminderMins)
                            },
                            onTestNotification = { viewModel.testPushNotification() }
                        )
                    }
                    AppDestination.CALENDAR.name -> {
                        CalendarScreen(
                            events = allEvents,
                            recurringActivities = allRecurringActivities,
                            selectedDate = selectedCalendarDate,
                            onSelectDate = { date -> viewModel.setSelectedCalendarDate(date) },
                            onAddEvent = { title, desc, date, start, end, cat, color, loc, rem, notify, syncNative, ctx ->
                                viewModel.addCalendarEvent(title, desc, date, start, end, cat, color, loc, rem, notify, syncNative, ctx)
                            },
                            onUpdateEvent = { event, syncNative, ctx ->
                                viewModel.updateCalendarEvent(event, syncNative, ctx)
                            },
                            onDeleteEvent = { event ->
                                viewModel.deleteCalendarEvent(event)
                            },
                            onSyncEventToNative = { ctx, event ->
                                viewModel.syncEventToDeviceCalendar(ctx, event)
                            }
                        )
                    }
                    AppDestination.STUDY.name -> {
                        StudyScreen(
                            studyMaterials = studyMaterials,
                            notes = allNotes,
                            errorLogs = errorLogs,
                            onAddStudyMaterial = { title, subj, type, desc, content, tags ->
                                viewModel.addStudyMaterial(title, subj, type, desc, content, tags)
                            },
                            onTogglePinMaterial = { mat -> viewModel.togglePinMaterial(mat) },
                            onDeleteStudyMaterial = { id -> viewModel.deleteStudyMaterial(id) },
                            onAddNote = { title, content, subj, category ->
                                viewModel.addNote(title, content, subj, category)
                            },
                            onToggleFavoriteNote = { note -> viewModel.toggleFavoriteNote(note) },
                            onDeleteNote = { id -> viewModel.deleteNote(id) },
                            onAddErrorLog = { topic, subj, note -> viewModel.addErrorLog(topic, subj, note) },
                            onToggleResolveErrorLog = { log -> viewModel.toggleResolveErrorLog(log) },
                            onDeleteErrorLog = { id -> viewModel.deleteErrorLog(id) }
                        )
                    }
                }
            }
        }
    }
}
