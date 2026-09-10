package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
import android.widget.Toast
import com.example.data.local.entity.CalendarEventEntity
import com.example.data.local.entity.RecurringActivityEntity
import java.text.SimpleDateFormat
import java.util.*

object NativeCalendarHelper {

    private val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    fun parseDateTimeToEpoch(dateStr: String, timeStr: String): Long {
        return try {
            val date = dateTimeFormat.parse("$dateStr $timeStr")
            date?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }

    fun parseDateToEpoch(dateStr: String): Long {
        return try {
            val date = dateFormat.parse(dateStr)
            date?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }

    /**
     * Integrates with Android's native device calendar (Google Calendar, Samsung Calendar, etc.)
     * via Intent.ACTION_INSERT so the user can save the event directly with zero friction.
     */
    fun insertEventToDeviceCalendar(context: Context, event: CalendarEventEntity): Boolean {
        val startMillis = parseDateTimeToEpoch(event.eventDate, event.startTime)
        val endMillis = parseDateTimeToEpoch(event.eventDate, event.endTime).let {
            if (it <= startMillis) startMillis + 3600000L else it
        }

        val intent = Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.Events.TITLE, event.title)
            putExtra(CalendarContract.Events.DESCRIPTION, "${event.description}\nCategory: ${event.category}\nManaged via 90 Days Challenge")
            putExtra(CalendarContract.Events.EVENT_LOCATION, event.location)
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startMillis)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endMillis)
            putExtra(CalendarContract.Events.ALL_DAY, false)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        return try {
            context.startActivity(intent)
            Toast.makeText(context, "Opening device calendar for '${event.title}'", Toast.LENGTH_SHORT).show()
            true
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open native calendar app", Toast.LENGTH_SHORT).show()
            false
        }
    }

    /**
     * Exports a recurring routine activity as a recurring entry to the native calendar.
     */
    fun insertRoutineToDeviceCalendar(context: Context, routine: RecurringActivityEntity): Boolean {
        val todayStr = dateFormat.format(Date())
        val startMillis = parseDateTimeToEpoch(todayStr, routine.startTime)
        val endMillis = parseDateTimeToEpoch(todayStr, routine.endTime).let {
            if (it <= startMillis) startMillis + 3600000L else it
        }

        // Convert days of week e.g. "MON,WED,FRI" to RRULE BYDAY "MO,WE,FR"
        val rruleDays = routine.daysOfWeek.split(",")
            .mapNotNull {
                when (it.trim().uppercase()) {
                    "SUN" -> "SU"
                    "MON" -> "MO"
                    "TUE" -> "TU"
                    "WED" -> "WE"
                    "THU" -> "TH"
                    "FRI" -> "FR"
                    "SAT" -> "SA"
                    else -> null
                }
            }.joinToString(",")

        val rrule = if (rruleDays.isNotEmpty()) "FREQ=WEEKLY;BYDAY=$rruleDays" else "FREQ=WEEKLY"

        val intent = Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.Events.TITLE, routine.title)
            putExtra(CalendarContract.Events.DESCRIPTION, "${routine.notes}\nRecurring Routine [${routine.category}]\nManaged via 90 Days Challenge")
            putExtra(CalendarContract.Events.EVENT_LOCATION, routine.location)
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startMillis)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endMillis)
            putExtra(CalendarContract.Events.RRULE, rrule)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        return try {
            context.startActivity(intent)
            Toast.makeText(context, "Adding recurring routine '${routine.title}' to device calendar", Toast.LENGTH_SHORT).show()
            true
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open native calendar app", Toast.LENGTH_SHORT).show()
            false
        }
    }

    /**
     * Opens the native calendar app focused on a specific date.
     */
    fun openDeviceCalendarAtDate(context: Context, dateStr: String) {
        val epochMs = parseDateToEpoch(dateStr)
        val uri = Uri.parse("content://com.android.calendar/time/$epochMs")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "No device calendar app found", Toast.LENGTH_SHORT).show()
        }
    }
}
