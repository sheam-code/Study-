package com.example.notifications

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.local.entity.CalendarEventEntity
import com.example.data.local.entity.TaskEntity
import com.example.util.NativeCalendarHelper

object NotificationHelper {

    const val CHANNEL_DEADLINES_ID = "channel_deadlines"
    const val CHANNEL_DEADLINES_NAME = "Upcoming Deadlines & Tasks"

    const val CHANNEL_ROUTINE_ID = "channel_routine"
    const val CHANNEL_ROUTINE_NAME = "Daily Routine & Events"

    const val EXTRA_TASK_ID = "extra_task_id"
    const val EXTRA_TASK_TITLE = "extra_task_title"
    const val EXTRA_TASK_DESC = "extra_task_desc"

    fun initChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val deadlineChannel = NotificationChannel(
                CHANNEL_DEADLINES_ID,
                CHANNEL_DEADLINES_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts for study task deadlines and scheduled milestones"
                enableVibration(true)
            }

            val routineChannel = NotificationChannel(
                CHANNEL_ROUTINE_ID,
                CHANNEL_ROUTINE_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders for scheduled calendar events and daily routine blocks"
                enableVibration(true)
            }

            notificationManager.createNotificationChannel(deadlineChannel)
            notificationManager.createNotificationChannel(routineChannel)
        }
    }

    fun scheduleTaskReminder(context: Context, task: TaskEntity) {
        if (!task.reminderEnabled || task.isCompleted) return

        val reminderTime = task.deadlineEpochMs - (task.reminderMinutesBefore * 60 * 1000L)
        val now = System.currentTimeMillis()

        if (reminderTime <= now) return // already in past

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, DeadlineReminderReceiver::class.java).apply {
            putExtra(EXTRA_TASK_ID, task.id)
            putExtra(EXTRA_TASK_TITLE, task.title)
            putExtra(EXTRA_TASK_DESC, task.description.ifEmpty { "Due in ${task.reminderMinutesBefore} minutes (${task.subject})" })
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            task.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent)
            }
        } catch (e: SecurityException) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent)
        }
    }

    fun cancelTaskReminder(context: Context, taskId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, DeadlineReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    /**
     * Schedules MULTIPLE reminders for an event based on event.remindersMinutes (e.g. "15,60,1440").
     */
    fun scheduleEventReminders(context: Context, event: CalendarEventEntity) {
        if (!event.notifyReminder) return

        val startEpoch = NativeCalendarHelper.parseDateTimeToEpoch(event.eventDate, event.startTime)
        val now = System.currentTimeMillis()
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val minutesList = event.remindersMinutes.split(",")
            .mapNotNull { it.trim().toIntOrNull() }

        minutesList.forEach { offsetMinutes ->
            val reminderTime = startEpoch - (offsetMinutes * 60 * 1000L)
            if (reminderTime > now) {
                val requestCode = (event.id * 1000 + offsetMinutes).toInt()
                val offsetLabel = when {
                    offsetMinutes == 0 -> "Now starting"
                    offsetMinutes < 60 -> "In $offsetMinutes min"
                    offsetMinutes < 1440 -> "In ${offsetMinutes / 60} hour(s)"
                    else -> "In ${offsetMinutes / 1440} day(s)"
                }

                val locationDesc = if (event.location.isNotBlank()) "📍 ${event.location}" else ""
                val desc = "$offsetLabel at ${event.startTime}. $locationDesc"

                val intent = Intent(context, DeadlineReminderReceiver::class.java).apply {
                    putExtra(EXTRA_TASK_ID, requestCode.toLong())
                    putExtra(EXTRA_TASK_TITLE, "Event: ${event.title}")
                    putExtra(EXTRA_TASK_DESC, desc)
                }

                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent)
                    } else {
                        alarmManager.setExact(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent)
                    }
                } catch (e: SecurityException) {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent)
                }
            }
        }
    }

    fun cancelEventReminders(context: Context, eventId: Long, remindersMinutesStr: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val minutesList = remindersMinutesStr.split(",").mapNotNull { it.trim().toIntOrNull() }
        minutesList.forEach { offsetMinutes ->
            val requestCode = (eventId * 1000 + offsetMinutes).toInt()
            val intent = Intent(context, DeadlineReminderReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
        }
    }

    fun showImmediateNotification(context: Context, title: String, message: String, notificationId: Int = 999) {
        initChannels(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_DEADLINES_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, builder.build())
    }
}
