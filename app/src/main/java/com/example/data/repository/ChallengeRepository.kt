package com.example.data.repository

import com.example.data.local.dao.ChallengeDao
import com.example.data.local.entity.*
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject

class ChallengeRepository(private val dao: ChallengeDao) {

    // Checklist
    fun getChecklistForDate(dateKey: String): Flow<ChecklistEntity?> = dao.getChecklistForDate(dateKey)
    fun getAllChecklists(): Flow<List<ChecklistEntity>> = dao.getAllChecklists()
    suspend fun saveChecklist(entity: ChecklistEntity) = dao.insertOrUpdateChecklist(entity)

    // Error Log
    val errorLogs: Flow<List<ErrorLogEntity>> = dao.getAllErrorLogs()
    suspend fun addErrorLog(topic: String, subject: String, note: String = "") {
        dao.insertErrorLog(ErrorLogEntity(topic = topic, subject = subject, note = note))
    }
    suspend fun deleteErrorLog(id: Long) = dao.deleteErrorLogById(id)
    suspend fun updateErrorLog(item: ErrorLogEntity) = dao.updateErrorLog(item)

    // Tasks
    val allTasks: Flow<List<TaskEntity>> = dao.getAllTasks()
    val pendingTasks: Flow<List<TaskEntity>> = dao.getPendingTasks()
    suspend fun getTaskById(id: Long) = dao.getTaskById(id)
    suspend fun addTask(task: TaskEntity): Long = dao.insertTask(task)
    suspend fun updateTask(task: TaskEntity) = dao.updateTask(task)
    suspend fun deleteTask(id: Long) = dao.deleteTaskById(id)

    // Study Materials
    val studyMaterials: Flow<List<StudyMaterialEntity>> = dao.getAllStudyMaterials()
    suspend fun addStudyMaterial(material: StudyMaterialEntity): Long = dao.insertStudyMaterial(material)
    suspend fun updateStudyMaterial(material: StudyMaterialEntity) = dao.updateStudyMaterial(material)
    suspend fun deleteStudyMaterial(id: Long) = dao.deleteStudyMaterialById(id)

    // Notes
    val allNotes: Flow<List<NoteEntity>> = dao.getAllNotes()
    suspend fun addNote(note: NoteEntity): Long = dao.insertNote(note)
    suspend fun updateNote(note: NoteEntity) = dao.updateNote(note)
    suspend fun deleteNote(id: Long) = dao.deleteNoteById(id)

    // Calendar Events
    val allEvents: Flow<List<CalendarEventEntity>> = dao.getAllEvents()
    fun getEventsForDate(date: String): Flow<List<CalendarEventEntity>> = dao.getEventsForDate(date)
    suspend fun addEvent(event: CalendarEventEntity): Long = dao.insertEvent(event)
    suspend fun updateEvent(event: CalendarEventEntity) = dao.updateEvent(event)
    suspend fun deleteEvent(id: Long) = dao.deleteEventById(id)

    // Recurring Activities
    val allRecurringActivities: Flow<List<RecurringActivityEntity>> = dao.getAllRecurringActivities()
    val activeRecurringActivities: Flow<List<RecurringActivityEntity>> = dao.getActiveRecurringActivities()
    suspend fun addRecurringActivity(activity: RecurringActivityEntity): Long = dao.insertRecurringActivity(activity)
    suspend fun updateRecurringActivity(activity: RecurringActivityEntity) = dao.updateRecurringActivity(activity)
    suspend fun deleteRecurringActivity(id: Long) = dao.deleteRecurringActivityById(id)

    // Cloud Sync JSON Serialization & Restoration
    suspend fun exportDataToJson(): String {
        val root = JSONObject()
        root.put("version", 2)
        root.put("exportedAt", System.currentTimeMillis())

        // Checklists
        val checklistsArray = JSONArray()
        dao.getAllChecklistsList().forEach {
            checklistsArray.put(JSONObject().apply {
                put("dateKey", it.dateKey)
                put("ssDone", it.ssDone)
                put("errorlogDone", it.errorlogDone)
                put("sleepDone", it.sleepDone)
                put("runDone", it.runDone)
                put("updatedAt", it.updatedAt)
            })
        }
        root.put("checklists", checklistsArray)

        // Error Logs
        val errorLogsArray = JSONArray()
        dao.getAllErrorLogsList().forEach {
            errorLogsArray.put(JSONObject().apply {
                put("id", it.id)
                put("topic", it.topic)
                put("subject", it.subject)
                put("note", it.note)
                put("isResolved", it.isResolved)
                put("createdAt", it.createdAt)
            })
        }
        root.put("errorLogs", errorLogsArray)

        // Tasks
        val tasksArray = JSONArray()
        dao.getAllTasksList().forEach {
            tasksArray.put(JSONObject().apply {
                put("id", it.id)
                put("title", it.title)
                put("description", it.description)
                put("subject", it.subject)
                put("deadlineEpochMs", it.deadlineEpochMs)
                put("priority", it.priority)
                put("isCompleted", it.isCompleted)
                put("reminderEnabled", it.reminderEnabled)
                put("reminderMinutesBefore", it.reminderMinutesBefore)
                put("updatedAt", it.updatedAt)
            })
        }
        root.put("tasks", tasksArray)

        // Study Materials
        val materialsArray = JSONArray()
        dao.getAllStudyMaterialsList().forEach {
            materialsArray.put(JSONObject().apply {
                put("id", it.id)
                put("title", it.title)
                put("subject", it.subject)
                put("type", it.type)
                put("description", it.description)
                put("linkOrContent", it.linkOrContent)
                put("isPinned", it.isPinned)
                put("tags", it.tags)
                put("updatedAt", it.updatedAt)
            })
        }
        root.put("studyMaterials", materialsArray)

        // Notes
        val notesArray = JSONArray()
        dao.getAllNotesList().forEach {
            notesArray.put(JSONObject().apply {
                put("id", it.id)
                put("title", it.title)
                put("content", it.content)
                put("subject", it.subject)
                put("category", it.category)
                put("isFavorite", it.isFavorite)
                put("createdAt", it.createdAt)
                put("updatedAt", it.updatedAt)
            })
        }
        root.put("notes", notesArray)

        // Events
        val eventsArray = JSONArray()
        dao.getAllEventsList().forEach {
            eventsArray.put(JSONObject().apply {
                put("id", it.id)
                put("title", it.title)
                put("description", it.description)
                put("eventDate", it.eventDate)
                put("startTime", it.startTime)
                put("endTime", it.endTime)
                put("category", it.category)
                put("colorHex", it.colorHex)
                put("location", it.location)
                put("remindersMinutes", it.remindersMinutes)
                put("notifyReminder", it.notifyReminder)
                put("syncedToDeviceCalendar", it.syncedToDeviceCalendar)
                put("updatedAt", it.updatedAt)
            })
        }
        root.put("events", eventsArray)

        // Recurring Activities
        val recurringArray = JSONArray()
        dao.getAllRecurringActivitiesList().forEach {
            recurringArray.put(JSONObject().apply {
                put("id", it.id)
                put("title", it.title)
                put("category", it.category)
                put("startTime", it.startTime)
                put("endTime", it.endTime)
                put("daysOfWeek", it.daysOfWeek)
                put("location", it.location)
                put("colorHex", it.colorHex)
                put("isActive", it.isActive)
                put("remindersMinutes", it.remindersMinutes)
                put("notes", it.notes)
                put("updatedAt", it.updatedAt)
            })
        }
        root.put("recurringActivities", recurringArray)

        return root.toString(2)
    }

    suspend fun importDataFromJson(jsonStr: String): Boolean {
        return try {
            val root = JSONObject(jsonStr)

            if (root.has("checklists")) {
                val array = root.getJSONArray("checklists")
                val list = mutableListOf<ChecklistEntity>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        ChecklistEntity(
                            dateKey = obj.getString("dateKey"),
                            ssDone = obj.optBoolean("ssDone", false),
                            errorlogDone = obj.optBoolean("errorlogDone", false),
                            sleepDone = obj.optBoolean("sleepDone", false),
                            runDone = obj.optBoolean("runDone", false),
                            updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())
                        )
                    )
                }
                dao.insertAllChecklists(list)
            }

            if (root.has("errorLogs")) {
                val array = root.getJSONArray("errorLogs")
                val list = mutableListOf<ErrorLogEntity>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        ErrorLogEntity(
                            id = obj.optLong("id", 0),
                            topic = obj.getString("topic"),
                            subject = obj.optString("subject", "General"),
                            note = obj.optString("note", ""),
                            isResolved = obj.optBoolean("isResolved", false),
                            createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                        )
                    )
                }
                dao.insertAllErrorLogs(list)
            }

            if (root.has("tasks")) {
                val array = root.getJSONArray("tasks")
                val list = mutableListOf<TaskEntity>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        TaskEntity(
                            id = obj.optLong("id", 0),
                            title = obj.getString("title"),
                            description = obj.optString("description", ""),
                            subject = obj.optString("subject", "General"),
                            deadlineEpochMs = obj.getLong("deadlineEpochMs"),
                            priority = obj.optString("priority", "HIGH"),
                            isCompleted = obj.optBoolean("isCompleted", false),
                            reminderEnabled = obj.optBoolean("reminderEnabled", true),
                            reminderMinutesBefore = obj.optInt("reminderMinutesBefore", 60),
                            updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())
                        )
                    )
                }
                dao.insertAllTasks(list)
            }

            if (root.has("studyMaterials")) {
                val array = root.getJSONArray("studyMaterials")
                val list = mutableListOf<StudyMaterialEntity>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        StudyMaterialEntity(
                            id = obj.optLong("id", 0),
                            title = obj.getString("title"),
                            subject = obj.getString("subject"),
                            type = obj.optString("type", "CHAPTER_NOTES"),
                            description = obj.optString("description", ""),
                            linkOrContent = obj.optString("linkOrContent", ""),
                            isPinned = obj.optBoolean("isPinned", false),
                            tags = obj.optString("tags", ""),
                            updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())
                        )
                    )
                }
                dao.insertAllStudyMaterials(list)
            }

            if (root.has("notes")) {
                val array = root.getJSONArray("notes")
                val list = mutableListOf<NoteEntity>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        NoteEntity(
                            id = obj.optLong("id", 0),
                            title = obj.getString("title"),
                            content = obj.getString("content"),
                            subject = obj.optString("subject", "General"),
                            category = obj.optString("category", "QUICK_NOTE"),
                            isFavorite = obj.optBoolean("isFavorite", false),
                            createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                            updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())
                        )
                    )
                }
                dao.insertAllNotes(list)
            }

            if (root.has("events")) {
                val array = root.getJSONArray("events")
                val list = mutableListOf<CalendarEventEntity>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        CalendarEventEntity(
                            id = obj.optLong("id", 0),
                            title = obj.getString("title"),
                            description = obj.optString("description", ""),
                            eventDate = obj.getString("eventDate"),
                            startTime = obj.optString("startTime", "09:00"),
                            endTime = obj.optString("endTime", "10:00"),
                            category = obj.optString("category", "DEADLINE"),
                            colorHex = obj.optString("colorHex", "#E6B869"),
                            location = obj.optString("location", ""),
                            remindersMinutes = obj.optString("remindersMinutes", "15,60"),
                            notifyReminder = obj.optBoolean("notifyReminder", true),
                            syncedToDeviceCalendar = obj.optBoolean("syncedToDeviceCalendar", false),
                            updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())
                        )
                    )
                }
                dao.insertAllEvents(list)
            }

            if (root.has("recurringActivities")) {
                val array = root.getJSONArray("recurringActivities")
                val list = mutableListOf<RecurringActivityEntity>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        RecurringActivityEntity(
                            id = obj.optLong("id", 0),
                            title = obj.getString("title"),
                            category = obj.optString("category", "DEEP_STUDY"),
                            startTime = obj.optString("startTime", "07:00"),
                            endTime = obj.optString("endTime", "08:00"),
                            daysOfWeek = obj.optString("daysOfWeek", "MON,TUE,WED,THU,FRI"),
                            location = obj.optString("location", ""),
                            colorHex = obj.optString("colorHex", "#E6B869"),
                            isActive = obj.optBoolean("isActive", true),
                            remindersMinutes = obj.optString("remindersMinutes", "15"),
                            notes = obj.optString("notes", ""),
                            updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())
                        )
                    )
                }
                dao.insertAllRecurringActivities(list)
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
