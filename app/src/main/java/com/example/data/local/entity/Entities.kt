package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_checklist")
data class ChecklistEntity(
    @PrimaryKey val dateKey: String, // YYYY-MM-DD
    val ssDone: Boolean = false,
    val errorlogDone: Boolean = false,
    val sleepDone: Boolean = false,
    val runDone: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "error_log")
data class ErrorLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val topic: String,
    val subject: String = "General",
    val note: String = "",
    val isResolved: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val subject: String = "General",
    val deadlineEpochMs: Long,
    val priority: String = "HIGH", // HIGH, MEDIUM, LOW
    val isCompleted: Boolean = false,
    val reminderEnabled: Boolean = true,
    val reminderMinutesBefore: Int = 60, // e.g. 60 min before deadline
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "study_materials")
data class StudyMaterialEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val subject: String,
    val type: String = "CHAPTER_NOTES", // CHAPTER_NOTES, FORMULA_SHEET, QUESTION_BANK, VIDEO, DOC
    val description: String = "",
    val linkOrContent: String = "",
    val isPinned: Boolean = false,
    val tags: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "study_notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val subject: String = "General",
    val category: String = "QUICK_NOTE", // QUICK_NOTE, FORMULA, REVISION_KEY, MOCK_ANALYSIS
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "calendar_events")
data class CalendarEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val eventDate: String, // YYYY-MM-DD
    val startTime: String = "09:00", // HH:mm
    val endTime: String = "10:00", // HH:mm
    val category: String = "DEADLINE", // EXAM, DEADLINE, MOCK_TEST, COLLEGE, BATCH, PRAYER, MILESTONE, PERSONAL
    val colorHex: String = "#E6B869",
    val location: String = "", // e.g., "Hall 402, Science Complex", "Home Desk"
    val remindersMinutes: String = "15,60", // Comma-separated minutes before event (e.g., "15,60,1440")
    val notifyReminder: Boolean = true,
    val syncedToDeviceCalendar: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "recurring_activities")
data class RecurringActivityEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: String = "DEEP_STUDY", // EXERCISE, DEEP_STUDY, PRACTICE, REVISION, COLLEGE, BATCH, MEAL, SLEEP, CUSTOM
    val startTime: String = "07:00", // HH:mm
    val endTime: String = "08:00", // HH:mm
    val daysOfWeek: String = "MON,TUE,WED,THU,FRI", // e.g., "SUN,MON,TUE,WED,THU,FRI,SAT"
    val location: String = "",
    val colorHex: String = "#E6B869",
    val isActive: Boolean = true,
    val remindersMinutes: String = "15",
    val notes: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)
