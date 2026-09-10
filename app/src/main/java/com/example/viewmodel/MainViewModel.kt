package com.example.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entity.*
import com.example.data.repository.ChallengeRepository
import com.example.data.sync.CloudSyncManager
import com.example.model.*
import com.example.notifications.NotificationHelper
import com.example.util.NativeCalendarHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application, viewModelScope)
    val repository = ChallengeRepository(database.challengeDao())
    val syncManager = CloudSyncManager(application, repository)

    // Current Date Keys
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val todayKey: String = dateFormat.format(Date())

    // Dark Mode Theme preference
    private val themePrefs = application.getSharedPreferences("sheam_theme_prefs", Context.MODE_PRIVATE)
    private val _isDarkTheme = MutableStateFlow(themePrefs.getBoolean("is_dark_theme", true))
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    fun toggleDarkTheme(isDark: Boolean) {
        _isDarkTheme.value = isDark
        themePrefs.edit().putBoolean("is_dark_theme", isDark).apply()
    }

    // Day & Challenge Info
    private val _currentDayNumber = MutableStateFlow(ScheduleRepository.calculateDayNumber())
    val currentDayNumber: StateFlow<Int> = _currentDayNumber.asStateFlow()

    val daysLeft: StateFlow<Int> = currentDayNumber.map { Math.max(0, 90 - it) }.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), 88
    )
    val percentDone: StateFlow<Int> = currentDayNumber.map {
        Math.min(100, Math.max(0, (it * 100) / 90))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 2)

    val currentPhaseInfo: StateFlow<Pair<String, String>> = currentDayNumber.map {
        ScheduleRepository.getPhaseName(it)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Phase 1" to "Foundation Sprint")

    // Routine Preview Selection (null = show real today)
    private val _previewDow = MutableStateFlow<Int?>(null)
    val previewDow: StateFlow<Int?> = _previewDow.asStateFlow()

    fun setPreviewDow(dow: Int?) {
        _previewDow.value = dow
    }

    // Today's Checklist
    val todayChecklist: StateFlow<ChecklistEntity> = repository.getChecklistForDate(todayKey)
        .map { it ?: ChecklistEntity(dateKey = todayKey) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ChecklistEntity(dateKey = todayKey))

    // Streaks
    val allChecklists: StateFlow<List<ChecklistEntity>> = repository.getAllChecklists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentStreak: StateFlow<Int> = allChecklists.map { list ->
        calculateStreak(list)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1)

    private fun calculateStreak(checklists: List<ChecklistEntity>): Int {
        if (checklists.isEmpty()) return 1
        val map = checklists.associateBy { it.dateKey }
        var streak = 0
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -1) // check from yesterday backwards

        while (true) {
            val key = dateFormat.format(cal.time)
            val item = map[key]
            if (item != null && item.ssDone && item.errorlogDone && item.sleepDone && item.runDone) {
                streak++
                cal.add(Calendar.DAY_OF_YEAR, -1)
            } else {
                break
            }
        }
        val todayItem = map[todayKey]
        if (todayItem != null && todayItem.ssDone && todayItem.errorlogDone && todayItem.sleepDone && todayItem.runDone) {
            streak++
        }
        return Math.max(1, streak)
    }

    fun toggleChecklistItem(key: String, currentVal: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val current = todayChecklist.value
            val updated = when (key) {
                "ss" -> current.copy(ssDone = !currentVal, updatedAt = System.currentTimeMillis())
                "errorlog" -> current.copy(errorlogDone = !currentVal, updatedAt = System.currentTimeMillis())
                "sleep" -> current.copy(sleepDone = !currentVal, updatedAt = System.currentTimeMillis())
                "run" -> current.copy(runDone = !currentVal, updatedAt = System.currentTimeMillis())
                else -> current
            }
            repository.saveChecklist(updated)
            if (syncManager.autoSyncEnabled.value) {
                syncManager.performCloudSync()
            }
        }
    }

    // Error Log
    val errorLogs: StateFlow<List<ErrorLogEntity>> = repository.errorLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addErrorLog(topic: String, subject: String = "General", note: String = "") {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addErrorLog(topic.trim(), subject, note.trim())
            val current = todayChecklist.value
            if (!current.errorlogDone) {
                repository.saveChecklist(current.copy(errorlogDone = true, updatedAt = System.currentTimeMillis()))
            }
        }
    }

    fun deleteErrorLog(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteErrorLog(id)
        }
    }

    fun toggleResolveErrorLog(item: ErrorLogEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateErrorLog(item.copy(isResolved = !item.isResolved))
        }
    }

    // Tasks Dashboard
    val allTasks: StateFlow<List<TaskEntity>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val upcomingTasks: StateFlow<List<TaskEntity>> = allTasks.map { list ->
        list.filter { !it.isCompleted }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addTask(
        title: String,
        description: String,
        subject: String,
        deadlineEpochMs: Long,
        priority: String,
        reminderMinutesBefore: Int = 60
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val task = TaskEntity(
                title = title.trim(),
                description = description.trim(),
                subject = subject,
                deadlineEpochMs = deadlineEpochMs,
                priority = priority,
                reminderEnabled = true,
                reminderMinutesBefore = reminderMinutesBefore
            )
            val newId = repository.addTask(task)
            val createdTask = task.copy(id = newId)
            NotificationHelper.scheduleTaskReminder(getApplication(), createdTask)

            if (syncManager.autoSyncEnabled.value) {
                syncManager.performCloudSync()
            }
        }
    }

    fun toggleTaskCompleted(task: TaskEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = task.copy(isCompleted = !task.isCompleted, updatedAt = System.currentTimeMillis())
            repository.updateTask(updated)
            if (updated.isCompleted) {
                NotificationHelper.cancelTaskReminder(getApplication(), task.id)
            } else if (updated.reminderEnabled) {
                NotificationHelper.scheduleTaskReminder(getApplication(), updated)
            }
        }
    }

    fun deleteTask(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteTask(id)
            NotificationHelper.cancelTaskReminder(getApplication(), id)
        }
    }

    // Study Materials
    val studyMaterials: StateFlow<List<StudyMaterialEntity>> = repository.studyMaterials
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addStudyMaterial(
        title: String,
        subject: String,
        type: String,
        description: String,
        linkOrContent: String,
        tags: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addStudyMaterial(
                StudyMaterialEntity(
                    title = title.trim(),
                    subject = subject,
                    type = type,
                    description = description.trim(),
                    linkOrContent = linkOrContent.trim(),
                    tags = tags.trim()
                )
            )
        }
    }

    fun togglePinMaterial(material: StudyMaterialEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateStudyMaterial(material.copy(isPinned = !material.isPinned, updatedAt = System.currentTimeMillis()))
        }
    }

    fun deleteStudyMaterial(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteStudyMaterial(id)
        }
    }

    // Notes
    val allNotes: StateFlow<List<NoteEntity>> = repository.allNotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addNote(
        title: String,
        content: String,
        subject: String,
        category: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addNote(
                NoteEntity(
                    title = title.trim(),
                    content = content.trim(),
                    subject = subject,
                    category = category
                )
            )
        }
    }

    fun toggleFavoriteNote(note: NoteEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateNote(note.copy(isFavorite = !note.isFavorite, updatedAt = System.currentTimeMillis()))
        }
    }

    fun deleteNote(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteNote(id)
        }
    }

    // Calendar Events
    val allEvents: StateFlow<List<CalendarEventEntity>> = repository.allEvents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedCalendarDate = MutableStateFlow(todayKey)
    val selectedCalendarDate: StateFlow<String> = _selectedCalendarDate.asStateFlow()

    fun setSelectedCalendarDate(date: String) {
        _selectedCalendarDate.value = date
    }

    fun addCalendarEvent(
        title: String,
        description: String,
        eventDate: String,
        startTime: String,
        endTime: String,
        category: String,
        colorHex: String,
        location: String = "",
        remindersMinutes: String = "15,60",
        notifyReminder: Boolean = true,
        addToNativeCalendar: Boolean = false,
        context: Context? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val event = CalendarEventEntity(
                title = title.trim(),
                description = description.trim(),
                eventDate = eventDate,
                startTime = startTime,
                endTime = endTime,
                category = category,
                colorHex = colorHex,
                location = location.trim(),
                remindersMinutes = remindersMinutes,
                notifyReminder = notifyReminder,
                syncedToDeviceCalendar = addToNativeCalendar
            )
            val newId = repository.addEvent(event)
            val savedEvent = event.copy(id = newId)

            if (notifyReminder) {
                NotificationHelper.scheduleEventReminders(getApplication(), savedEvent)
            }

            if (addToNativeCalendar && context != null) {
                launch(Dispatchers.Main) {
                    NativeCalendarHelper.insertEventToDeviceCalendar(context, savedEvent)
                }
            }

            if (syncManager.autoSyncEnabled.value) {
                syncManager.performCloudSync()
            }
        }
    }

    fun updateCalendarEvent(
        event: CalendarEventEntity,
        addToNativeCalendar: Boolean = false,
        context: Context? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateEvent(event)
            NotificationHelper.cancelEventReminders(getApplication(), event.id, event.remindersMinutes)
            if (event.notifyReminder) {
                NotificationHelper.scheduleEventReminders(getApplication(), event)
            }

            if (addToNativeCalendar && context != null) {
                launch(Dispatchers.Main) {
                    NativeCalendarHelper.insertEventToDeviceCalendar(context, event)
                }
            }

            if (syncManager.autoSyncEnabled.value) {
                syncManager.performCloudSync()
            }
        }
    }

    fun deleteCalendarEvent(event: CalendarEventEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteEvent(event.id)
            NotificationHelper.cancelEventReminders(getApplication(), event.id, event.remindersMinutes)
        }
    }

    fun syncEventToDeviceCalendar(context: Context, event: CalendarEventEntity) {
        NativeCalendarHelper.insertEventToDeviceCalendar(context, event)
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateEvent(event.copy(syncedToDeviceCalendar = true))
        }
    }

    // Recurring Routine Activities
    val allRecurringActivities: StateFlow<List<RecurringActivityEntity>> = repository.allRecurringActivities
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeRecurringActivities: StateFlow<List<RecurringActivityEntity>> = repository.activeRecurringActivities
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addRecurringActivity(
        title: String,
        category: String,
        startTime: String,
        endTime: String,
        daysOfWeek: String,
        location: String = "",
        colorHex: String = "#E6B869",
        remindersMinutes: String = "15",
        notes: String = "",
        addToNativeCalendar: Boolean = false,
        context: Context? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val activity = RecurringActivityEntity(
                title = title.trim(),
                category = category,
                startTime = startTime,
                endTime = endTime,
                daysOfWeek = daysOfWeek,
                location = location.trim(),
                colorHex = colorHex,
                isActive = true,
                remindersMinutes = remindersMinutes,
                notes = notes.trim()
            )
            val newId = repository.addRecurringActivity(activity)
            val savedActivity = activity.copy(id = newId)

            if (addToNativeCalendar && context != null) {
                launch(Dispatchers.Main) {
                    NativeCalendarHelper.insertRoutineToDeviceCalendar(context, savedActivity)
                }
            }

            if (syncManager.autoSyncEnabled.value) {
                syncManager.performCloudSync()
            }
        }
    }

    fun updateRecurringActivity(activity: RecurringActivityEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateRecurringActivity(activity)
            if (syncManager.autoSyncEnabled.value) {
                syncManager.performCloudSync()
            }
        }
    }

    fun toggleRecurringActivityActive(activity: RecurringActivityEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateRecurringActivity(activity.copy(isActive = !activity.isActive))
        }
    }

    fun deleteRecurringActivity(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteRecurringActivity(id)
        }
    }

    // Cloud Sync Trigger
    fun triggerCloudSync(onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val result = syncManager.performCloudSync()
            if (result.isSuccess) {
                onComplete(true, result.getOrDefault("Synchronized successfully"))
            } else {
                onComplete(false, result.exceptionOrNull()?.message ?: "Sync failed")
            }
        }
    }

    fun exportCloudBackup(onExported: (String) -> Unit) {
        viewModelScope.launch {
            val json = syncManager.exportCloudBackup()
            onExported(json)
        }
    }

    fun importCloudBackup(json: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val ok = syncManager.restoreFromCloudBackup(json)
            onComplete(ok)
        }
    }

    fun testPushNotification() {
        NotificationHelper.showImmediateNotification(
            getApplication(),
            "⏰ 90 Days Challenge Focus Alert",
            "Next scheduled block: SS4 Practice Block (Physics + Biology). Let's nail the problems!"
        )
    }
}
