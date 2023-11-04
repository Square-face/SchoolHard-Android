package com.example.schoolhard.database

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.schoolhard.API.API
import com.example.schoolhard.API.Lesson
import com.example.schoolhard.API.Location
import com.example.schoolhard.API.Subject
import com.example.schoolhard.API.Occasion
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import kotlin.Exception

private val timeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
private val dateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("uuuu/MM/dd")
private val _dateTimeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("uuuu/MM/dd HH:mm:ss")

private val OLD_TABLES = listOf("schema")

class Database(context: Context, factory: SQLiteDatabase.CursorFactory?):
    SQLiteOpenHelper(context, "schema", factory, 23) {

    private val utils = Utils(this)

    /**
     * Creating all database tables
     *
     * @param db writable database version
     * */
    override fun onCreate(db: SQLiteDatabase) {
        Log.v("Database", "creating tables ${Schema.Subject.table}, ${Schema.Occasion.table}, ${Schema.Lesson.table}")

        val queries = listOf(Schema.Subject.createQuery(), Schema.Occasion.createQuery(), Schema.Lesson.createQuery())

        queries.forEach { query ->
            Log.v("Database - createTables", "executing query: $query")
            db.execSQL(query)
        }

        Log.v("Database", "done creating tables")
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
     * Drop all tables
     *
     * @param db writable database object
     * */
    private fun dropAllTables(db: SQLiteDatabase){
        Log.w("Database", "Dropping tables ${Schema.Subject.table}, ${Schema.Occasion.table}, ${Schema.Lesson.table}, $OLD_TABLES")

        db.execSQL("DROP TABLE IF EXISTS ${Schema.Subject.table}")
        db.execSQL("DROP TABLE IF EXISTS ${Schema.Occasion.table}")
        db.execSQL("DROP TABLE IF EXISTS ${Schema.Lesson.table}")

        OLD_TABLES.forEach { db.execSQL("DROP TABLE IF EXISTS $it") }

        Log.v("Database", "Dropped tables")
    }





    /**
     * Clear all schedule related tables of any entries
     *
     * */
    fun clearSchedule() {
        val db = this.writableDatabase

        db.execSQL("DELETE FROM ${Schema.Subject.table}")
        db.execSQL("DELETE FROM ${Schema.Occasion.table}")
        db.execSQL("DELETE FROM ${Schema.Lesson.table}")
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
    fun updateSchedule(api: API, finishedCallback: (Boolean) -> Unit = {}) {
        Log.i("Database - UpdateSchedule", "Updating schema")





        api.lessons({ finishedCallback(false) }) {response ->

            val db = this.writableDatabase

            if (db.isDbLockedByCurrentThread) {
                Log.w("Database - UpdateSchedule", "Database looked")
                finishedCallback(false)
                return@lessons
            }

            db.beginTransaction()

            dropAllTables(db)
            onCreate(db)

            Log.d("Database - UpdateSchedule", "Caching ${response.size} lessons")

            response.forEach {lesson ->
                createLessonIfNotExist(db, lesson)
                createOccasionIfNotExist(db, lesson.occasion)
                createSubjectIfNotExist(db, lesson.occasion.subject)
            }

            db.setTransactionSuccessful()
            db.endTransaction()
            db.close()

            Log.d("Database - UpdateSchedule", "Finished caching schema to database")
            finishedCallback(true)
        }
    }





    /**
     * Get parts of the schedule based on a filter with week and optional weekday
     *
     * @param week The requested week
     * @param dayOfWeek The day of the week, can be null in witch case the entire week will be returned
     *
     * @return A filtered schedule
     * */
    fun getSchedule(week: Int, dayOfWeek: DayOfWeek? = null): List<Lesson> {
        Log.d("Database - getSchedule", "Getting schedule with query (week: $week, dayOfWeek: $dayOfWeek)")
        val db = this.readableDatabase

        val query = Utils.Query.lessonQuery(week, dayOfWeek)

        // execute sql query
        val cursor = db.rawQuery(query.query, query.args)
        Log.v("Database - getSchedule", "query returned with ${cursor.count} rows")

        // cache
        val subjects = mutableListOf<Subject>()
        val occasions = mutableListOf<Occasion>()
        val lessons = mutableListOf<Lesson>()


        cursor.moveToFirst()

        // parse results
        while (!cursor.isAfterLast) {
            lessons.add(utils.parseLesson(cursor, occasions, subjects))

            cursor.moveToNext()
        }
        cursor.close()

        return lessons
    }



    /**
     * Get parts of the schedule based on a filter using dates
     *
     * @param after The requested start date
     * @param before The requested end date
     *
     * @return A filtered schedule
     * */
    fun getSchedule(after: LocalDateTime?=null, before: LocalDateTime?=null): List<Lesson> {
        Log.d("Database - getSchedule", "Getting schedule with query (after: $after, before: $before)")
        val db = this.readableDatabase

        val query = Utils.Query.lessonQuery(after, before)

        // execute sql query
        val cursor = db.rawQuery(query.query + " ORDER BY ${Schema.Lesson.Columns.date} ASC, ${Schema.Lesson.Columns.startTime} ASC", query.args)
        Log.v("Database - getSchedule", "query returned with ${cursor.count} rows")

        // cache
        val subjects = mutableListOf<Subject>()
        val occasions = mutableListOf<Occasion>()
        val lessons = mutableListOf<Lesson>()

        cursor.moveToFirst()

        // parse results
        while (!cursor.isAfterLast) {
            lessons.add(utils.parseLesson(cursor, occasions, subjects))

            cursor.moveToNext()
        }
        cursor.close()

        return lessons
    }





    /**
     * Get parts of the schedule based on a filter using dates from lesson isntances
     *
     * @param after The requested start date
     * @param before The requested end date
     *
     * @return A filtered schedule
     * */
    fun getSchedule(after: Lesson?=null, before: Lesson?=null): List<Lesson> {
        return getSchedule(after?.endTime, before?.startTime)
    }





    /**
     * Get the previous lesson or null.
     *
     * @return The previous lesson or null if no lesson is found
     * */
    fun previousLesson(): Lesson? {
        val db = this.readableDatabase

        // make and execute request
        val query = Utils.Query.lessonQuery(before = LocalDateTime.now())
        val cursor = db.rawQuery(query.query + " ORDER BY ${Schema.Lesson.Columns.date} DESC, ${Schema.Lesson.Columns.endTime} DESC", query.args)

        // null check
        if (cursor.count == 0) { return null }

        // parse
        cursor.moveToFirst()
        val lesson = utils.parseLesson(cursor, mutableListOf(), mutableListOf())
        cursor.close()

        return lesson
    }





    /**
     * Get the lesson that is currently ongoing or null.
     *
     * @return The current lesson or null if no lesson is currently ongoing
     * */
    fun currentLesson(): Lesson? {
        val db = this.readableDatabase

        // make and execute request
        val query = Utils.Query.lessonQuery(at = LocalDateTime.now())
        val cursor = db.rawQuery(query.query, query.args)


        // null check
        if (cursor.count == 0) { return null }

        // parse
        cursor.moveToFirst()
        val lesson = utils.parseLesson(cursor, mutableListOf(), mutableListOf())
        cursor.close()

        return lesson
    }





    /**
     * Get the first lesson with a start time after now.
     *
     * @return Next lesson or null if no lesson is found
     * */
    fun nextLesson(): Lesson? {
        val db = this.readableDatabase

        // make and execute request
        val query = Utils.Query.lessonQuery(after = LocalDateTime.now())
        val cursor = db.rawQuery(query.query + " ORDER BY ${Schema.Lesson.Columns.date} ASC, ${Schema.Lesson.Columns.startTime} ASC", query.args)

        // null check
        if (cursor.count == 0) { return null }

        // parse
        cursor.moveToFirst()
        val lesson = utils.parseLesson(cursor, mutableListOf(), mutableListOf())
        cursor.close()

        return lesson
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

        val cursor = db.rawQuery(
            "SELECT * FROM ${Schema.Subject.table} WHERE ${Schema.Subject.Columns.subjectId} = ?",
            arrayOf(subject.subjectId.toString()))
        val count = cursor.count
        cursor.close()

        if (count > 0) {
            return false
        }

        Log.v("Database - createSubjectIfNotExist", "Subject ${subject.id} doesn't exist")

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

        val cursor = db.rawQuery(
            "SELECT * FROM ${Schema.Occasion.table} WHERE ${Schema.Occasion.Columns.uuid} = ?",
            arrayOf(occasion.id.toString())
        )

        val count = cursor.count
        cursor.close()

        if (count > 0) {
            return true
        }


        Log.v("Database - createOccasionIfNotExist", "Occasion ${occasion.id} doesn't exist")

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

        val cursor = db.rawQuery(
            "SELECT * FROM ${Schema.Lesson.table} WHERE ${Schema.Lesson.Columns.uuid} = ?",
            arrayOf(lesson.id.toString())
        )
        val count = cursor.count
        cursor.close()

        if (count > 0) {
            Log.d("Database - createLessonIfNotExist", "Lesson ${lesson.id} already exists")
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
        val query = "SELECT * FROM ${Schema.Lesson.table} WHERE ${Schema.Lesson.Columns.occasionUUID} = ?"
        val cursor = this.readableDatabase.rawQuery(query, arrayOf(occasion.id.toString()))
        cursor.moveToFirst()

        while (!cursor.isAfterLast) {

            // TODO: Move parse to separate function
            val lesson = Lesson(
                occasion,
                cursor.getInt(Schema.Lesson.Columns.week.index),
                LocalDate.parse(cursor.getString(Schema.Lesson.Columns.date.index), dateFormat),
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
    @SuppressLint("Range")
    fun getOccasions(subject: Subject): List<Occasion> {
        Log.v("Database - FetchOccasions", "Fetching occasions with ${subject.id} as parent")
        val results = mutableListOf<Occasion>()

        val query = "SELECT * FROM ${Schema.Occasion.table} WHERE ${Schema.Occasion.Columns.subjectUUID} = ?"
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
        val query = "SELECT * FROM ${Schema.Subject.table}"
        val cursor = this.readableDatabase.rawQuery(query, null)
        cursor.moveToFirst()

        while (!cursor.isAfterLast) {

            // TODO: Move parse to separate function
            val subject = Subject(
                cursor.getInt(Schema.Subject.Columns.subjectId.index),
                cursor.getString(Schema.Subject.Columns.name.index),
                UUID.fromString(cursor.getString(Schema.Subject.Columns.uuid.index))
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
    fun getLesson(_uuid: String, _parent: Occasion): Lesson? {
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

        val db = this.readableDatabase

        // make and execute request
        val query = Utils.Query.occasionQuery(uuid)
        val cursor = db.rawQuery(query.query, query.args)

        // null check
        if (cursor.count != 1) { return null }

        // parse
        cursor.moveToFirst()
        val occasion = utils.parseOccasion(cursor, mutableListOf(parent))
        cursor.close()

        return occasion
    }



    /**
     * Get a single subject from the database
     *
     * @param uuid The uuid to match with the subject
     *
     * @return parsed subject object or null if no subject is found
     * */
    fun getSubject(uuid: String): Subject? {

        val db = this.readableDatabase

        // make and execute request
        val query = Utils.Query.subjectQuery(uuid)
        val cursor = db.rawQuery(query.query, query.args)

        // null check
        if (cursor.count != 1) {
            Log.v("Database - getSubject", "Subject $uuid not found (${cursor.count}, ${query.query}, ${query.args})")
            return null
        }

        // parse
        cursor.moveToFirst()
        val subject = utils.parseSubject(cursor)
        cursor.close()

        return subject
    }






    /**
     * Save a subject to the database
     *
     * @param db writable database object
     * @param subject Subject to save
     * */
    fun storeSubject(db: SQLiteDatabase, subject: Subject) {

        val values = ContentValues()

        values.put("uuid", subject.id.toString())
        values.put("subjectId", subject.subjectId)
        values.put("name", subject.name)

        db.insert(Schema.Subject.table, null, values)
    }


    /**
     * Save a occasion to the database
     *
     * @param db writable database object
     * @param occasion Occasion to save
     * */
    fun storeOccasion(db: SQLiteDatabase, occasion: Occasion) {
        val values = ContentValues()

        // uuid's
        values.put("uuid", occasion.id.toString())
        values.put("occasionId", occasion.occasionId)
        values.put("subjectUUID", occasion.subject.id.toString())

        values.put("location", occasion.location.name)

        values.put("dayOfWeek", occasion.dayOfWeek.value)
        values.put("startTime", occasion.startTime.toSecondOfDay())
        values.put("endTime", occasion.endTime.toSecondOfDay())

        db.insert(Schema.Occasion.table, null, values)
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

        // represent date as days since 1970-01-01
        values.put("date", lesson.date.toEpochDay())

        // represent time as seconds from start of day
        values.put("startTime", lesson.occasion.startTime.toSecondOfDay())
        values.put("endTime", lesson.occasion.endTime.toSecondOfDay())

        db.insert(Schema.Lesson.table, null, values)
    }


    /**
     * Database related exceptions
     * */
    class Exceptions {

        class SubjectNotFound(uuid: String): Exception("Subject with uuid $uuid was not found in the database")
        class OccasionNotFound(uuid: String): Exception("Occasion with uuid $uuid was not found in the database")
        class LessonNotFound(uuid: String): Exception("Lesson with uuid $uuid was not found in the database")
    }

}