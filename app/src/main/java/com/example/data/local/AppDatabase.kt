package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.ChallengeDao
import com.example.data.local.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ChecklistEntity::class,
        ErrorLogEntity::class,
        TaskEntity::class,
        StudyMaterialEntity::class,
        NoteEntity::class,
        CalendarEventEntity::class,
        RecurringActivityEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun challengeDao(): ChallengeDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "challenge_90_db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database.challengeDao())
                    }
                }
            }
        }

        private suspend fun populateInitialData(dao: ChallengeDao) {
            // Initial Recurring Routine Activities
            dao.insertRecurringActivity(
                RecurringActivityEntity(
                    title = "Morning Exercise & Run",
                    category = "EXERCISE",
                    startTime = "05:45",
                    endTime = "06:30",
                    daysOfWeek = "SUN,MON,TUE,WED,THU,SAT",
                    location = "Local Park / Home Gym",
                    colorHex = "#8FBF9D",
                    remindersMinutes = "15",
                    notes = "Outdoor jog and dynamic warm-up to jumpstart cortisol"
                )
            )
            dao.insertRecurringActivity(
                RecurringActivityEntity(
                    title = "SS1 Deep Focus: Theory & Core Concepts",
                    category = "DEEP_STUDY",
                    startTime = "06:45",
                    endTime = "09:15",
                    daysOfWeek = "SUN,MON,TUE,WED,THU,SAT",
                    location = "Study Desk",
                    colorHex = "#749ECF",
                    remindersMinutes = "15",
                    notes = "High-energy mental peak; no phone, no interruptions"
                )
            )
            dao.insertRecurringActivity(
                RecurringActivityEntity(
                    title = "SS2 Core Concept Study Block",
                    category = "DEEP_STUDY",
                    startTime = "09:45",
                    endTime = "12:45",
                    daysOfWeek = "SUN,MON,TUE,WED,THU",
                    location = "Study Desk",
                    colorHex = "#749ECF",
                    remindersMinutes = "15",
                    notes = "Secondary syllabus rotation subject"
                )
            )
            dao.insertRecurringActivity(
                RecurringActivityEntity(
                    title = "College & Batch Lectures",
                    category = "COLLEGE",
                    startTime = "13:30",
                    endTime = "16:30",
                    daysOfWeek = "SUN,MON,TUE,WED",
                    location = "Campus Hall 201",
                    colorHex = "#DD9257",
                    remindersMinutes = "30",
                    notes = "Attendance and batch problem discussion"
                )
            )
            dao.insertRecurringActivity(
                RecurringActivityEntity(
                    title = "SS4 Practice & Problem Solving",
                    category = "PRACTICE",
                    startTime = "17:30",
                    endTime = "19:30",
                    daysOfWeek = "SUN,MON,TUE,WED,THU,SAT",
                    location = "Study Desk",
                    colorHex = "#6FB9AE",
                    remindersMinutes = "15",
                    notes = "Same-day problem sets from SS1/SS2 — test comprehension"
                )
            )
            dao.insertRecurringActivity(
                RecurringActivityEntity(
                    title = "SS5 Spaced Revision & Error Log",
                    category = "REVISION",
                    startTime = "20:30",
                    endTime = "22:30",
                    daysOfWeek = "SUN,MON,TUE,WED,THU,SAT",
                    location = "Study Desk",
                    colorHex = "#AC91D6",
                    remindersMinutes = "15",
                    notes = "Log every missed question into Error Log; quick recap"
                )
            )
            dao.insertRecurringActivity(
                RecurringActivityEntity(
                    title = "Friday Error-Log Deep Dive & Recovery",
                    category = "REVISION",
                    startTime = "09:00",
                    endTime = "12:00",
                    daysOfWeek = "FRI",
                    location = "Study Desk",
                    colorHex = "#E6B869",
                    remindersMinutes = "30",
                    notes = "Dedicated drill of weakest topics identified during the week"
                )
            )

            // Initial Error Log Weak Points
            dao.insertErrorLog(
                ErrorLogEntity(
                    topic = "Rotational dynamics — torque & angular momentum",
                    subject = "Physics",
                    note = "Review moment of inertia derivations and vector cross products"
                )
            )
            dao.insertErrorLog(
                ErrorLogEntity(
                    topic = "Organic chemistry — electrophilic substitution mechanisms",
                    subject = "Chemistry",
                    note = "Friedel-Crafts alkylation vs acylation edge cases"
                )
            )
            dao.insertErrorLog(
                ErrorLogEntity(
                    topic = "Genetics — dihybrid test cross and linkage ratios",
                    subject = "Biology",
                    note = "Crossing over percentage calculations"
                )
            )

            // Initial Study Materials
            dao.insertStudyMaterial(
                StudyMaterialEntity(
                    title = "Physics 1st Paper: Core Formula Sheet",
                    subject = "Physics",
                    type = "FORMULA_SHEET",
                    description = "Vectors, Newtonian mechanics, work & energy, periodic motion formulas",
                    linkOrContent = "Key formulas:\n• Torque: τ = r × F = Iα\n• Kinetic Energy of Rotation: K = 1/2 Iω²\n• Angular Momentum: L = Iω\n• SHM Period: T = 2π√(m/k)",
                    isPinned = true,
                    tags = "Formulas, Dynamics, Mechanics"
                )
            )
            dao.insertStudyMaterial(
                StudyMaterialEntity(
                    title = "Chemistry 2nd Paper: Organic Reaction Pathways",
                    subject = "Chemistry",
                    type = "CHAPTER_NOTES",
                    description = "Comprehensive summary of aldehyde, ketone, and carboxylic conversions",
                    linkOrContent = "Reaction Roadmap:\n1. Alcohol oxidation: 1° -> Aldehyde -> Carboxylic acid\n2. Grignard reagent: R-MgX + Carbonyl -> Secondary/Tertiary Alcohol\n3. Cannizzaro vs Aldol Condensation triggers",
                    isPinned = true,
                    tags = "Organic, Reactions, Synthesis"
                )
            )
            dao.insertStudyMaterial(
                StudyMaterialEntity(
                    title = "Biology 2nd Paper: Human Physiology & Blood Circulation",
                    subject = "Biology",
                    type = "CHAPTER_NOTES",
                    description = "Cardiac cycle timing, ECG wave interpretation, and blood pressure regulation",
                    linkOrContent = "Cardiac Cycle (0.8s):\n• Atrial systole: 0.1s\n• Ventricular systole: 0.3s\n• Joint diastole: 0.4s\n• P wave: Atrial depolarization\n• QRS complex: Ventricular depolarization\n• T wave: Ventricular repolarization",
                    isPinned = false,
                    tags = "Physiology, Cardiac, Circulation"
                )
            )
            dao.insertStudyMaterial(
                StudyMaterialEntity(
                    title = "Math 1st Paper: Calculus & Integration Shortcuts",
                    subject = "Math",
                    type = "FORMULA_SHEET",
                    description = "Integration by parts, substitution tricks, and definite integral properties",
                    linkOrContent = "Standard Forms:\n• ∫ e^(ax) sin(bx) dx = [e^(ax)/(a²+b²)] * [a sin(bx) - b cos(bx)] + C\n• Leibniz rule for differentiation under integral sign\n• Even/Odd function symmetries over [-a, a]",
                    isPinned = true,
                    tags = "Calculus, Integration, Math"
                )
            )

            // Initial Study Notes
            dao.insertNote(
                NoteEntity(
                    title = "90-Day Challenge Anchor Rules",
                    content = "1. This chart is a guide, not a cage. Blocks can shift by 15–30 min without breaking anything.\n2. One off day in 90 does nothing; one bad night's sleep repeated does.\n3. Judge yourself by the week's total, not any single day.\n4. Friday is protected recovery time on purpose — it's not optional.\n5. If feeling behind, shorten SS4 or SS5 before ever touching sleep.",
                    subject = "General",
                    category = "REVISION_KEY",
                    isFavorite = true
                )
            )
            dao.insertNote(
                NoteEntity(
                    title = "Calculus Differentiation Edge Cases",
                    content = "Remember: d/dx [ln|sec x + tan x|] = sec x. In parametric curves, d²y/dx² = [d/dt(dy/dx)] / (dx/dt), NOT d²y/dt² / d²x/dt²!",
                    subject = "Math",
                    category = "FORMULA",
                    isFavorite = false
                )
            )

            // Initial Tasks with upcoming deadlines
            val now = System.currentTimeMillis()
            val oneDayMs = 24 * 60 * 60 * 1000L
            dao.insertTask(
                TaskEntity(
                    title = "Complete SS4 Practice Problems: Physics + Biology",
                    description = "Finish 30 problem sets from Rotational Dynamics and Botany Cell Cycle",
                    subject = "Physics",
                    deadlineEpochMs = now + (4 * 3600 * 1000L),
                    priority = "HIGH",
                    reminderEnabled = true,
                    reminderMinutesBefore = 60
                )
            )
            dao.insertTask(
                TaskEntity(
                    title = "Math Batch Assignment: Differential Equations",
                    description = "Solve Chapter 9 exercise B problems 1-20 for Wednesday batch submission",
                    subject = "Math",
                    deadlineEpochMs = now + (28 * 3600 * 1000L),
                    priority = "HIGH",
                    reminderEnabled = true,
                    reminderMinutesBefore = 120
                )
            )
            dao.insertTask(
                TaskEntity(
                    title = "Update Weak Topics in Error Log",
                    description = "Log every question missed during today's practice block",
                    subject = "General",
                    deadlineEpochMs = now + (12 * 3600 * 1000L),
                    priority = "MEDIUM",
                    reminderEnabled = true,
                    reminderMinutesBefore = 30
                )
            )
            dao.insertTask(
                TaskEntity(
                    title = "Chemistry Batch Preparation: Ionic Equilibrium",
                    description = "Buffer solution derivations and Henderson-Hasselbalch equation applications",
                    subject = "Chemistry",
                    deadlineEpochMs = now + (2 * oneDayMs),
                    priority = "MEDIUM",
                    reminderEnabled = true,
                    reminderMinutesBefore = 180
                )
            )

            // Initial Calendar Events
            val todayDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date(now))
            val tomorrowDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date(now + oneDayMs))
            val nextWeekDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date(now + 7 * oneDayMs))

            dao.insertEvent(
                CalendarEventEntity(
                    title = "Physics Term Paper Exam",
                    description = "Chapters 1 to 5: Mechanics, Waves, and Thermodynamics",
                    eventDate = todayDate,
                    startTime = "10:00",
                    endTime = "12:30",
                    category = "EXAM",
                    location = "Hall 402, Science Complex",
                    remindersMinutes = "15,60,1440",
                    colorHex = "#EF5350"
                )
            )
            dao.insertEvent(
                CalendarEventEntity(
                    title = "Math Batch Evening Session",
                    description = "Differential Calculus and Trigonometry Integration",
                    eventDate = todayDate,
                    startTime = "17:00",
                    endTime = "18:30",
                    category = "BATCH",
                    location = "Batch Center, Room B-3",
                    remindersMinutes = "30,60",
                    colorHex = "#DD9257"
                )
            )
            dao.insertEvent(
                CalendarEventEntity(
                    title = "Phase 1 Milestone Review (Day 30)",
                    description = "Check progress across all 6 core subjects and error log resolution rate",
                    eventDate = tomorrowDate,
                    startTime = "20:00",
                    endTime = "21:30",
                    category = "MILESTONE",
                    location = "Home Study Desk",
                    remindersMinutes = "15,60",
                    colorHex = "#E6B869"
                )
            )
            dao.insertEvent(
                CalendarEventEntity(
                    title = "Full Syllabus Mock Test #1",
                    description = "Timed 3-hour mock test simulating official exam conditions",
                    eventDate = nextWeekDate,
                    startTime = "09:00",
                    endTime = "12:00",
                    category = "MOCK_TEST",
                    location = "Main Auditorium / Exam Hall",
                    remindersMinutes = "60,1440",
                    colorHex = "#AC91D6"
                )
            )
        }
    }
}
