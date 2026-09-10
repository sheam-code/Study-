package com.example.model

import androidx.compose.ui.graphics.Color
import java.util.Calendar

enum class SlotCategory(
    val title: String,
    val hexColor: String,
    val color: Color
) {
    SLEEP("Sleep", "#AC91D6", Color(0xFFAC91D6)),
    PRAYER("Prayer", "#E6B869", Color(0xFFE6B869)),
    RUN("Run", "#8FBF9D", Color(0xFF8FBF9D)),
    DEEP("Deep focus", "#749ECF", Color(0xFF749ECF)),
    PRACTICE("Practice", "#6FB9AE", Color(0xFF6FB9AE)),
    REVISION("Revision", "#B49CC4", Color(0xFFB49CC4)),
    COLLEGE("College / batch", "#DD9257", Color(0xFFDD9257)),
    MEAL("Meal / break", "#C2B47F", Color(0xFFC2B47F)),
    FREE("Free / recovery", "#AB9D8D", Color(0xFFAB9D8D))
}

data class ScheduleSlot(
    val startHour: Int,
    val startMinute: Int,
    val label: String,
    val category: SlotCategory,
    val rotationKey: String? = null
) {
    val startMinutesOfDay: Int get() = startHour * 60 + startMinute

    fun formatTime(): String {
        val ampm = if (startHour >= 12) "pm" else "am"
        val h12 = when {
            startHour == 0 -> 12
            startHour > 12 -> startHour - 12
            else -> startHour
        }
        val minStr = if (startMinute == 0) "" else String.format(":%02d", startMinute)
        return "$h12$minStr$ampm"
    }
}

data class DaySchedule(
    val tag: String,
    val note: String,
    val slots: List<ScheduleSlot>
)

object ScheduleRepository {

    val QUOTES = listOf(
        "This chart is a guide, not a cage. Blocks can shift by 15–30 minutes without breaking anything.",
        "One off day in 90 does nothing; one bad night’s sleep repeated does.",
        "Judge yourself by the week’s total, not any single day.",
        "Friday is protected recovery time on purpose — it’s not optional.",
        "If you feel behind, shorten SS4 or SS5 before you ever touch sleep or Friday recovery."
    )

    val WeekdaySchedule = DaySchedule(
        tag = "College day",
        note = "Two deep-focus blocks before college, medium and practice blocks after.",
        slots = listOf(
            ScheduleSlot(5, 0, "Wake, Fajr", SlotCategory.PRAYER),
            ScheduleSlot(5, 15, "Run (flexible window)", SlotCategory.RUN),
            ScheduleSlot(6, 15, "Shower, breakfast", SlotCategory.MEAL),
            ScheduleSlot(6, 45, "SS1 — Deep Focus", SlotCategory.DEEP, "ss1"),
            ScheduleSlot(9, 15, "Break", SlotCategory.MEAL),
            ScheduleSlot(9, 30, "SS2 — Deep Focus", SlotCategory.DEEP, "ss2"),
            ScheduleSlot(11, 50, "Lunch, Dhuhr, travel", SlotCategory.MEAL),
            ScheduleSlot(12, 30, "College", SlotCategory.COLLEGE),
            ScheduleSlot(16, 20, "Travel back, Asr", SlotCategory.PRAYER),
            ScheduleSlot(16, 45, "Snack", SlotCategory.MEAL),
            ScheduleSlot(17, 0, "SS3 — Medium Block", SlotCategory.PRACTICE, "ss3"),
            ScheduleSlot(19, 0, "Break, Maghrib", SlotCategory.PRAYER),
            ScheduleSlot(19, 20, "SS4 — Practice Block", SlotCategory.PRACTICE, "ss4"),
            ScheduleSlot(21, 10, "Dinner, Isha", SlotCategory.PRAYER),
            ScheduleSlot(21, 30, "SS5 — Revision + error log", SlotCategory.REVISION, "ss5"),
            ScheduleSlot(21, 50, "Wind-down / journal", SlotCategory.FREE),
            ScheduleSlot(22, 0, "Sleep (~7h)", SlotCategory.SLEEP)
        )
    )

    val BatchSchedule = DaySchedule(
        tag = "College + Math batch",
        note = "Same morning as a college day — evening trades the medium block for the batch, and sleep comes earlier.",
        slots = listOf(
            ScheduleSlot(5, 0, "Wake, Fajr", SlotCategory.PRAYER),
            ScheduleSlot(5, 15, "Run (flexible window)", SlotCategory.RUN),
            ScheduleSlot(6, 15, "Shower, breakfast", SlotCategory.MEAL),
            ScheduleSlot(6, 45, "SS1 — Deep Focus", SlotCategory.DEEP, "ss1"),
            ScheduleSlot(9, 15, "Break", SlotCategory.MEAL),
            ScheduleSlot(9, 30, "SS2 — Deep Focus", SlotCategory.DEEP, "ss2"),
            ScheduleSlot(11, 50, "Lunch, Dhuhr, travel", SlotCategory.MEAL),
            ScheduleSlot(12, 30, "College", SlotCategory.COLLEGE),
            ScheduleSlot(16, 20, "Travel back, Asr", SlotCategory.PRAYER),
            ScheduleSlot(16, 45, "Snack", SlotCategory.MEAL),
            ScheduleSlot(17, 0, "Math batch", SlotCategory.COLLEGE),
            ScheduleSlot(18, 30, "Break, Maghrib", SlotCategory.PRAYER),
            ScheduleSlot(18, 50, "SS4 — Practice Block", SlotCategory.PRACTICE, "ss4"),
            ScheduleSlot(20, 30, "Dinner, Isha", SlotCategory.PRAYER),
            ScheduleSlot(20, 50, "SS5 — Revision + error log", SlotCategory.REVISION, "ss5"),
            ScheduleSlot(21, 10, "Wind-down / journal", SlotCategory.FREE),
            ScheduleSlot(21, 30, "Sleep (~7.5h)", SlotCategory.SLEEP)
        )
    )

    val SaturdaySchedule = DaySchedule(
        tag = "Full intensity",
        note = "No college today — the one day with room for a full arc, plus real family/free time in the evening.",
        slots = listOf(
            ScheduleSlot(5, 0, "Wake, Fajr", SlotCategory.PRAYER),
            ScheduleSlot(5, 15, "Run", SlotCategory.RUN),
            ScheduleSlot(6, 15, "Breakfast", SlotCategory.MEAL),
            ScheduleSlot(6, 45, "SS1 — Deep Focus", SlotCategory.DEEP, "ss1"),
            ScheduleSlot(9, 15, "Break", SlotCategory.MEAL),
            ScheduleSlot(9, 30, "SS2 — Deep Focus", SlotCategory.DEEP, "ss2"),
            ScheduleSlot(12, 0, "Lunch, Dhuhr, rest", SlotCategory.MEAL),
            ScheduleSlot(13, 0, "SS3 — Medium Block", SlotCategory.PRACTICE, "ss3"),
            ScheduleSlot(15, 0, "Chemistry batch", SlotCategory.COLLEGE),
            ScheduleSlot(16, 0, "Break, Asr", SlotCategory.PRAYER),
            ScheduleSlot(16, 20, "SS4 — Practice Block", SlotCategory.PRACTICE, "ss4"),
            ScheduleSlot(18, 20, "Break, Maghrib", SlotCategory.PRAYER),
            ScheduleSlot(18, 40, "SS5 — Extended Revision", SlotCategory.REVISION, "ss5"),
            ScheduleSlot(20, 10, "Dinner, Isha", SlotCategory.PRAYER),
            ScheduleSlot(20, 40, "Wind-down, family / free time", SlotCategory.FREE),
            ScheduleSlot(21, 40, "Sleep (~7h20m)", SlotCategory.SLEEP)
        )
    )

    val FridaySchedule = DaySchedule(
        tag = "Recovery day",
        note = "No new material today, on purpose. Fully error-log driven — this is the day that makes the other six sustainable.",
        slots = listOf(
            ScheduleSlot(5, 0, "Wake, Fajr", SlotCategory.PRAYER),
            ScheduleSlot(5, 15, "Run (optional, lighter)", SlotCategory.RUN),
            ScheduleSlot(6, 15, "Breakfast, rest", SlotCategory.MEAL),
            ScheduleSlot(7, 0, "Full revision — error log driven, all subjects", SlotCategory.REVISION),
            ScheduleSlot(9, 30, "Free / family / extra football", SlotCategory.FREE),
            ScheduleSlot(11, 30, "Jummah prep, Jummah, lunch", SlotCategory.PRAYER),
            ScheduleSlot(13, 30, "Light revision or free time", SlotCategory.REVISION),
            ScheduleSlot(15, 0, "Chemistry batch (within revision)", SlotCategory.COLLEGE),
            ScheduleSlot(16, 0, "Fully free — release valve, no guilt", SlotCategory.FREE),
            ScheduleSlot(22, 0, "Earlier bedtime — bank sleep for the week", SlotCategory.SLEEP)
        )
    )

    fun getScheduleForDow(dow: Int): DaySchedule {
        return when (dow) {
            Calendar.FRIDAY -> FridaySchedule
            Calendar.SATURDAY -> SaturdaySchedule
            Calendar.MONDAY, Calendar.WEDNESDAY -> BatchSchedule
            else -> WeekdaySchedule
        }
    }

    private val ROTATION = mapOf(
        Calendar.SUNDAY to mapOf(
            "ss1" to "Physics (1st)",
            "ss2" to "Biology (2nd)",
            "ss3" to "Chemistry (alt.)",
            "ss4" to "Problems — today's Physics + Biology",
            "ss5" to "English (1st pass)"
        ),
        Calendar.MONDAY to mapOf(
            "ss1" to "Physics (2nd)",
            "ss2" to "Biology (1st)",
            "ss4" to "Problems + 1 weak-topic drill",
            "ss5" to "English (2nd pass)"
        ),
        Calendar.TUESDAY to mapOf(
            "ss1" to "Physics (1st)",
            "ss2" to "Biology (2nd)",
            "ss3" to "Math (alt.)",
            "ss4" to "Problems — today's topics",
            "ss5" to "Bangla (1st pass)"
        ),
        Calendar.WEDNESDAY to mapOf(
            "ss1" to "Physics (2nd)",
            "ss2" to "Biology (1st)",
            "ss4" to "Problems + 1 weak-topic drill",
            "ss5" to "Bangla (2nd pass)"
        ),
        Calendar.THURSDAY to mapOf(
            "ss1" to "Physics (1st)",
            "ss2" to "Biology (2nd)",
            "ss3" to "Chemistry (alt.)",
            "ss4" to "Problems — today's topics",
            "ss5" to "ICT"
        ),
        Calendar.SATURDAY to mapOf(
            "ss1" to "Physics (2nd)",
            "ss2" to "Biology (1st)",
            "ss3" to "Math (alt.)",
            "ss4" to "Week's weak topics — mixed",
            "ss5" to "Extended English/Bangla/ICT"
        )
    )

    fun getPhase(day: Int): Int {
        return when {
            day <= 0 -> 0
            day <= 70 -> 1
            day <= 85 -> 2
            day <= 90 -> 3
            else -> 4
        }
    }

    fun getPhaseName(day: Int): Pair<String, String> {
        return when (getPhase(day)) {
            0 -> "Day zero" to "Rest and get ready"
            1 -> "Phase 1" to "Foundation Sprint"
            2 -> "Phase 2" to "Weak-Point Assault"
            3 -> "Phase 3" to "Full Syllabus Lockdown"
            else -> "Complete" to "90 Days Accomplished"
        }
    }

    fun getRotationLabel(day: Int, dow: Int, key: String): String? {
        val phase = getPhase(day)
        if (phase == 1) {
            return ROTATION[dow]?.get(key)
        }
        if (phase == 2) {
            return if (key == "ss5") "Error log + weak review" else "Error-log focus"
        }
        if (phase == 3) {
            return "Timed mock / lockdown review"
        }
        return null
    }

    fun calculateDayNumber(referenceDate: Calendar = Calendar.getInstance()): Int {
        val dayZero = Calendar.getInstance().apply {
            set(2026, Calendar.SEPTEMBER, 8, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val today = (referenceDate.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val diffMs = today.timeInMillis - dayZero.timeInMillis
        val days = (diffMs / (86400000L)).toInt()
        // If before day zero or newly started, ensure sensible value (e.g. Day 2)
        return if (days < 1) 2 else days
    }
}
