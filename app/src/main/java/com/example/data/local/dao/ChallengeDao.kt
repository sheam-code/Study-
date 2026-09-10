package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ChallengeDao {

    // Checklist
    @Query("SELECT * FROM daily_checklist WHERE dateKey = :dateKey LIMIT 1")
    fun getChecklistForDate(dateKey: String): Flow<ChecklistEntity?>

    @Query("SELECT * FROM daily_checklist")
    fun getAllChecklists(): Flow<List<ChecklistEntity>>

    @Query("SELECT * FROM daily_checklist")
    suspend fun getAllChecklistsList(): List<ChecklistEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateChecklist(entity: ChecklistEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllChecklists(entities: List<ChecklistEntity>)

    // Error Log
    @Query("SELECT * FROM error_log ORDER BY createdAt DESC")
    fun getAllErrorLogs(): Flow<List<ErrorLogEntity>>

    @Query("SELECT * FROM error_log WHERE isResolved = 0 ORDER BY createdAt DESC")
    fun getActiveErrorLogs(): Flow<List<ErrorLogEntity>>

    @Query("SELECT * FROM error_log")
    suspend fun getAllErrorLogsList(): List<ErrorLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertErrorLog(item: ErrorLogEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllErrorLogs(items: List<ErrorLogEntity>)

    @Update
    suspend fun updateErrorLog(item: ErrorLogEntity)

    @Delete
    suspend fun deleteErrorLog(item: ErrorLogEntity)

    @Query("DELETE FROM error_log WHERE id = :id")
    suspend fun deleteErrorLogById(id: Long)

    // Tasks
    @Query("SELECT * FROM tasks ORDER BY isCompleted ASC, deadlineEpochMs ASC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE isCompleted = 0 ORDER BY deadlineEpochMs ASC")
    fun getPendingTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    suspend fun getTaskById(id: Long): TaskEntity?

    @Query("SELECT * FROM tasks")
    suspend fun getAllTasksList(): List<TaskEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTasks(tasks: List<TaskEntity>)

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Long)

    // Study Materials
    @Query("SELECT * FROM study_materials ORDER BY isPinned DESC, updatedAt DESC")
    fun getAllStudyMaterials(): Flow<List<StudyMaterialEntity>>

    @Query("SELECT * FROM study_materials WHERE subject = :subject ORDER BY isPinned DESC, updatedAt DESC")
    fun getStudyMaterialsBySubject(subject: String): Flow<List<StudyMaterialEntity>>

    @Query("SELECT * FROM study_materials")
    suspend fun getAllStudyMaterialsList(): List<StudyMaterialEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudyMaterial(material: StudyMaterialEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllStudyMaterials(materials: List<StudyMaterialEntity>)

    @Update
    suspend fun updateStudyMaterial(material: StudyMaterialEntity)

    @Delete
    suspend fun deleteStudyMaterial(material: StudyMaterialEntity)

    @Query("DELETE FROM study_materials WHERE id = :id")
    suspend fun deleteStudyMaterialById(id: Long)

    // Study Notes
    @Query("SELECT * FROM study_notes ORDER BY isFavorite DESC, updatedAt DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM study_notes WHERE subject = :subject ORDER BY updatedAt DESC")
    fun getNotesBySubject(subject: String): Flow<List<NoteEntity>>

    @Query("SELECT * FROM study_notes")
    suspend fun getAllNotesList(): List<NoteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllNotes(notes: List<NoteEntity>)

    @Update
    suspend fun updateNote(note: NoteEntity)

    @Delete
    suspend fun deleteNote(note: NoteEntity)

    @Query("DELETE FROM study_notes WHERE id = :id")
    suspend fun deleteNoteById(id: Long)

    // Calendar Events
    @Query("SELECT * FROM calendar_events ORDER BY eventDate ASC, startTime ASC")
    fun getAllEvents(): Flow<List<CalendarEventEntity>>

    @Query("SELECT * FROM calendar_events WHERE eventDate = :date ORDER BY startTime ASC")
    fun getEventsForDate(date: String): Flow<List<CalendarEventEntity>>

    @Query("SELECT * FROM calendar_events WHERE id = :id LIMIT 1")
    suspend fun getEventById(id: Long): CalendarEventEntity?

    @Query("SELECT * FROM calendar_events")
    suspend fun getAllEventsList(): List<CalendarEventEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: CalendarEventEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllEvents(events: List<CalendarEventEntity>)

    @Update
    suspend fun updateEvent(event: CalendarEventEntity)

    @Delete
    suspend fun deleteEvent(event: CalendarEventEntity)

    @Query("DELETE FROM calendar_events WHERE id = :id")
    suspend fun deleteEventById(id: Long)

    // Recurring Routine Activities
    @Query("SELECT * FROM recurring_activities ORDER BY startTime ASC")
    fun getAllRecurringActivities(): Flow<List<RecurringActivityEntity>>

    @Query("SELECT * FROM recurring_activities WHERE isActive = 1 ORDER BY startTime ASC")
    fun getActiveRecurringActivities(): Flow<List<RecurringActivityEntity>>

    @Query("SELECT * FROM recurring_activities")
    suspend fun getAllRecurringActivitiesList(): List<RecurringActivityEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecurringActivity(activity: RecurringActivityEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllRecurringActivities(activities: List<RecurringActivityEntity>)

    @Update
    suspend fun updateRecurringActivity(activity: RecurringActivityEntity)

    @Delete
    suspend fun deleteRecurringActivity(activity: RecurringActivityEntity)

    @Query("DELETE FROM recurring_activities WHERE id = :id")
    suspend fun deleteRecurringActivityById(id: Long)
}
