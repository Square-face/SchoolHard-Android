package com.example.schoolhard.database

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.schoolhard.API.API
import com.example.schoolhard.API.Lesson
import com.example.schoolhard.API.Location
import com.example.schoolhard.API.Subject
import com.example.schoolhard.API.Occasion
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoField
import java.util.UUID

val timeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
val dateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("uuuu/MM/dd")
val dateTimeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("uuuu/MM/dd HH:mm:ss")

val OLD_TABLES = listOf("schema")

const val SUBJECTS = "subjects"
const val OCCASIONS = "occasions"
const val LESSONS = "lessons"

class Database(context: Context, factory: SQLiteDatabase.CursorFactory?):
    SQLiteOpenHelper(context, "schema", factory, 11) {

    private val utils = Utils()

    /**
     * Creating all database tables
     *
     * @param db writable database version
     * */
    override fun onCreate(db: SQLiteDatabase) {
        Log.v("Database", "creating tables $SUBJECTS, $OCCASIONS, $LESSONS")


        val subjectQuery = ("CREATE TABLE $SUBJECTS ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "uuid TEXT UNIQUE NOT NULL,"
                + "subjectId INT NOT NULL,"
                + "name TEXT NOT NULL"
                + ")")

        val occasionQuery = ("CREATE TABLE $OCCASIONS ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "uuid TEXT UNIQUE NOT NULL, "
                + "occasionId INT NOT NULL,"
                + "subjectUUID TEXT NOT NULL,"
                + "location TEXT NOT NULL,"
                + "startTime TEXT NOT NULL,"
                + "endTime TEXT NOT NULL,"
                + "dayOfWeek INT NOT NULL"
                + ")")

        val lessonQuery = ("CREATE TABLE $LESSONS ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "uuid TEXT UNIQUE NOT NULL,"
                + "occasionUUID TEXT NOT NULL,"
                + "subjectUUID TEXT NOT NULL,"
                + "dayofweek INT NOT NULL,"
                + "week INT NOT NULL,"
                + "date INT NOT NULL,"
                + "startTime INT NOT NULL,"
                + "endTime INT NOT NULL,"
                + ")")

        db.execSQL(occasionQuery)
        db.execSQL(subjectQuery)
        db.execSQL(lessonQuery)

        Log.v("Database", "done creating tables")
    }



    /**
     * Drop all tables
     *
     * @param db writable database object
     * */
    private fun dropAllTables(db: SQLiteDatabase){
        Log.w("Database", "Dropping tables $SUBJECTS, $OCCASIONS, $LESSONS, $OLD_TABLES")

        db.execSQL("DROP TABLE IF EXISTS $SUBJECTS")
        db.execSQL("DROP TABLE IF EXISTS $OCCASIONS")
        db.execSQL("DROP TABLE IF EXISTS $LESSONS")

        OLD_TABLES.forEach { db.execSQL("DROP TABLE IF EXISTS $it") }

        Log.v("Database", "Dropped tables")
    }



    /**
     * On version bump
     * Drops and recreates all tables
     *
     * @param db writable database
     * @param p1 Previous version
     * @param p2 New version
     * */
    override fun onUpgrade(db: SQLiteDatabase, p1: Int, p2: Int) {
        Log.i("Database - Upgrade", "Database version bump from v$p1 to v$p2")

        dropAllTables(db)
        onCreate(db)
    }



    /**
     * Update the entire schema
     *
     * Attempts to get the current schema from [api] and write it to the database
     * If the request fails nothing is done
     *
     * @param api API to use to get the schema
     * @param finishedCallback Callback to run after updating is finished,
     * takes one boolean argument, if updating succeeded or failed
     * */
    fun updateSchema(api: API, finishedCallback: (Boolean) -> Unit = {}) {
        Log.i("Database - UpdateSchema", "Updating schema")
        val db = this.writableDatabase


        if (db.isDbLockedByCurrentThread) {
            Log.w("Database - UpdateSchema", "Database looked")
            finishedCallback(false)
            return
        }


        api.lessons({ finishedCallback(false) }) {response ->

            db.beginTransaction()

            dropAllTables(db)
            onCreate(db)

            response.forEach {lesson ->
                Log.v("Database - UpdateSchema", "Caching lesson: ${lesson.occasion.subject.name} id: ${lesson.id}")


                createLessonIfNotExist(db, lesson)
                createOccasionIfNotExist(db, lesson.occasion)
                createSubjectIfNotExist(db, lesson.occasion.subject)
            }

            db.setTransactionSuccessful()
            db.endTransaction()
            db.close()
            Log.d("Database - UpdateSchema", "Finished caching schema to database")
            finishedCallback(true)
        }
    }





    /**
     * Get parts of the schedule based on a filter
     *
     * @param week The requested week
     * @param dayOfWeek The day of the week, can be null in witch case the entire week will be returned
     *
     * @return A filtered schedule
     * */
    fun getSchedule(week: Int, dayOfWeek: DayOfWeek? = null): List<Lesson> {
        Log.d("Database - getSchedule", "Getting schedule with query (week: $week, dayOfWeek: $dayOfWeek)")
        val db = this.readableDatabase

        // the query changes if day of week is null
        val args = if (dayOfWeek != null) {
            query += " AND dayofweek = ?"
            arrayOf(week.toString(), dayOfWeek.value.toString())
        } else {
            arrayOf(week.toString())
        }

        // execute sql query
        val cursor = db.rawQuery(query, args)
        Log.v("Database - getSchedule", "query returned with ${cursor.count} rows")

        // cache
        val subjects = mutableListOf<Subject>()
        val occasions = mutableListOf<Occasion>()
        val lessons = mutableListOf<Lesson>()


        cursor.moveToFirst()

        // parse results
        while (!cursor.isAfterLast) {
            lessons.add(utils.parseLesson(this, cursor, occasions, subjects))

            cursor.moveToNext()
        }
        cursor.close()

        return lessons
    }





    /**
     * Check if a subject exists in the database. If it doesn't create it.
     *
     * @param db A writable database
     * @param subject The subject to check and create
     *
     * @return If a new subject was created
     * */
    private fun createSubjectIfNotExist(db: SQLiteDatabase, subject: Subject): Boolean {
        Log.v("Database - createSubjectIfNotExist", "Subject ${subject.name} - ${subject.id}")

        val cursor = db.rawQuery(
            "SELECT * FROM $SUBJECTS WHERE uuid = ?",
            arrayOf(subject.id.toString()))
        val count = cursor.count
        cursor.close()

        if (count > 0) {
            Log.d("Database - createSubjectIfNotExist", "Subject ${subject.id} already exists")
            return false
        }

        storeSubject(db, subject)
        return true
    }




    /**
     * Check if [occasion] exists in the database, if it doesn't save it
     *
     * @param db writable database object
     * @param occasion Lesson to check and store
     *
     * @return True means the lesson already existed, false means it had to be saved
     * */
    private fun createOccasionIfNotExist(db: SQLiteDatabase, occasion: Occasion):Boolean {
        Log.v("Database - createOccasionIfNotExist", "Occasion ${occasion.subject.name} at ${occasion.startTime.format(timeFormat)} on ${occasion.dayOfWeek.name}, ${occasion.id}")

        val cursor = db.rawQuery(
            "SELECT * FROM $OCCASIONS WHERE uuid = ?",
            arrayOf(occasion.id.toString())
        )
        val count = cursor.count
        cursor.close()

        if (count > 0) {
            Log.d("Database - createOccasionIfNotExist", "Occasion ${occasion.id} already exists")
            return true
        }

        storeOccasion(db, occasion)
        return false
    }



    /**
     * Check if [lesson] exists in the database, if it doesn't save it
     *
     * @param db writable database object
     * @param lesson Lesson to check and store
     *
     * @return True means the lesson already existed, false means it had to be saved
     * */
    private fun createLessonIfNotExist(db: SQLiteDatabase, lesson: Lesson): Boolean {
        Log.v("Database - createLessonIfNotExist", "Lesson ${lesson.occasion.subject.name} at ${lesson.date.atTime(lesson.occasion.startTime).format(dateTimeFormat)}, ${lesson.id}")

        val cursor = db.rawQuery(
            "SELECT * FROM $LESSONS WHERE uuid = ?",
            arrayOf(lesson.id.toString())
        )
        val count = cursor.count
        cursor.close()

        if (count > 0) {
            Log.d("Database - createOccasionIfNotExist", "Occasion ${lesson.id} already exists")
            return true
        }

        storeLesson(db, lesson)
        return false
    }






    /**
     * Get the lessons associated with a occasion
     *
     * @param occasion parent
     *
     * @return list of lessons with [occasion] as parent
     * */
    fun getLessons(occasion: Occasion): List<Lesson> {
        Log.v("Database - FetchLessons", "Fetching lessons with ${occasion.id} as parent")
        val results = mutableListOf<Lesson>()

        // read database
        val query = "SELECT * FROM $LESSONS WHERE occasionUUID = ?"
        val cursor = this.readableDatabase.rawQuery(query, arrayOf(occasion.id.toString()))
        cursor.moveToFirst()

        while (!cursor.isAfterLast) {

            // TODO: Move parse to separate function
            val lesson = Lesson(
                occasion,
                cursor.getInt(1),
                LocalDate.parse(cursor.getString(3), dateFormat),
            )
            results.add(lesson)

            cursor.moveToNext()
        }
        cursor.close()

        return results
    }



    /**
     * Get the occasions associated with a subject
     *
     * @param subject parent
     *
     * @return list of occasions with [subject] as parent
     * */
    fun getOccasions(subject: Subject): List<Occasion> {
        Log.v("Database - FetchOccasions", "Fetching occasions with ${subject.id} as parent")
        val results = mutableListOf<Occasion>()

        val query = "SELECT * FROM $OCCASIONS WHERE subjectUUID = ?"
        val cursor = this.readableDatabase.rawQuery(query, arrayOf(subject.id.toString()))
        cursor.moveToFirst()

        while (!cursor.isAfterLast) {

            // TODO: Move parse to separate function
            val occasion = Occasion(
                UUID.fromString(cursor.getString(cursor.getColumnIndex("uuid"))),
                cursor.getInt(2),
                subject,
                Location(cursor.getString(4)),
                LocalTime.parse(cursor.getString(5), timeFormat),
                LocalTime.parse(cursor.getString(6), timeFormat),
                DayOfWeek.of(cursor.getInt(7)+1)
            )

            results.add(occasion)

            cursor.moveToNext()
        }
        cursor.close()

        return results
    }



    /**
     * Get all subjects
     *
     * @return list of subjects
     * */
    fun getSubjects(): List<Subject> {
        Log.v("Database - FetchSubjects", "Fetching all subjects")
        val results = mutableListOf<Subject>()

        // read database
        val query = "SELECT * FROM $SUBJECTS"
        val cursor = this.readableDatabase.rawQuery(query, null)
        cursor.moveToFirst()

        while (!cursor.isAfterLast) {

            // TODO: Move parse to separate function
            val subject = Subject(
                cursor.getInt(2),
                cursor.getString(3),
                UUID.fromString(cursor.getString(1))
            )

            results.add(subject)

            cursor.moveToNext()
        }

        cursor.close()

        return results
    }









    /**
     * Get a single Lesson from the database
     *
     * @param uuid The uuid to match with the lesson
     * @param parent Parent occasion
     *
     * @return parsed lesson object or null if no lesson is found
     * */
    fun getLesson(uuid: String, parent: Occasion): Lesson? {
        TODO("Not Implemented")
    }





    /**
     * Get a single Occasion from the database
     *
     * @param uuid The uuid to match with the occasion
     * @param parent Parent subject
     *
     * @return parsed occasion object or null if no occasion is found
     * */
    fun getOccasion(uuid: String, parent: Subject): Occasion? {
        TODO("Not Implemented")
    }

    /**
     * Get a single subject from the database
     *
     * @param uuid The uuid to match with the subject
     *
     * @return parsed subject object or null if no subject is found
     * */
    fun getSubject(uuid: String): Subject? {
        TODO("Not Implemented")
    }






    /**
     * Save a subject to the database
     *
     * @param db writable database object
     * @param subject Subject to save
     * */
    fun storeSubject(db: SQLiteDatabase, subject: Subject) {
        Log.d("Database - Subject", "Storing subject (${subject.id})")

        val values = ContentValues()

        values.put("uuid", subject.id.toString())
        values.put("subjectId", subject.subjectId)
        values.put("name", subject.name)

        db.insert(SUBJECTS, null, values)
    }


    /**
     * Save a occasion to the database
     *
     * @param db writable database object
     * @param occasion Occasion to save
     * */
    fun storeOccasion(db: SQLiteDatabase, occasion: Occasion) {
        val values = ContentValues()

        values.put("uuid", occasion.id.toString())
        values.put("occasionId", occasion.occasionId)
        values.put("subjectUUID", occasion.subject.id.toString())
        values.put("location", occasion.location.name)
        values.put("dayOfWeek", occasion.dayOfWeek.ordinal)
        values.put("startTime", occasion.startTime.format(timeFormat))
        values.put("endTime", occasion.endTime.format(timeFormat))

        db.insert(OCCASIONS, null, values)
    }



    /**
     * Save a lesson to the database
     *
     * @param db writable database object
     * @param lesson Lesson to save
     * */
    fun storeLesson(db: SQLiteDatabase, lesson: Lesson) {
        val values = ContentValues()

        // uuid's
        values.put("uuid", lesson.id.toString())
        values.put("occasionUUID", lesson.occasion.id.toString())
        values.put("subjectUUID", lesson.subject.id.toString())

        values.put("dayofweek", lesson.dayOfWeek.value)
        values.put("week", lesson.week)

        // days from [LocalDate.MIN] witch is always the same
        values.put("date", Duration.between(lesson.date, LocalDate.MIN).toDays())

        // represent time as minutes from start of day
        values.put("startTime", Duration.between(lesson.startTime, lesson.date.atTime(0, 0)).toMinutes())
        values.put("endTime", Duration.between(lesson.endTime, lesson.date.atTime(0, 0)).toMinutes())

        db.insert(LESSONS, null, values)
    }
}