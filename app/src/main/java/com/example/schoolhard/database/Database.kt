package com.example.schoolhard.database

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.schoolhard.API.API
import com.example.schoolhard.API.Filter
import com.example.schoolhard.API.Subject
import com.example.schoolhard.API.Occasion
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoField

const val SCHEMA = "schema"
const val SUBJECTS = "subjects"

class Database(context: Context, factory: SQLiteDatabase.CursorFactory?):
    SQLiteOpenHelper(context, "schema", factory, 10) {
    override fun onCreate(db: SQLiteDatabase) {
        Log.v("Database", "creating tables ($SCHEMA, $SUBJECTS)")

        val occasionQuery = ("CREATE TABLE $SCHEMA ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "userId INT NOT NULL,"
                + "orgId INT NOT NULL,"
                + "subjectId INT NOT NULL,"
                + "week INT NOT NULL,"
                + "dayOfWeek INT NOT NULL,"
                + "date INT NOT NULL,"
                + "startTime INT NOT NULL,"
                + "endTime INT NOT NULL,"
                + "place TEXT NOT NULL"
                + ")")

        val subjectQuery = ("CREATE TABLE $SUBJECTS ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "subjectId INT NOT NULL,"
                + "name TEXT NOT NULL,"
                + "nameOverride TEXT"
                + ")")

        db.execSQL(occasionQuery)
        db.execSQL(subjectQuery)
        Log.v("Database", "done creating tables")
    }

    private fun dropAllTables(db: SQLiteDatabase){
        Log.w("Database", "Dropping tables ($SCHEMA, $SUBJECTS)")
        db.execSQL("DROP TABLE IF EXISTS $SCHEMA")
        db.execSQL("DROP TABLE IF EXISTS $SUBJECTS")
        Log.v("Database", "Dropped tables")
    }

    override fun onUpgrade(db: SQLiteDatabase, p1: Int, p2: Int) {
        Log.i("Database - Upgrade", "Database version bump from v$p1 to v$p2")
        dropAllTables(db)
        onCreate(db)
    }

    private fun createSubjectIfNotExist(db: SQLiteDatabase, subject: Subject) {
        Log.d("Database - createSubjectIfNotExist", "${subject.id} - ${subject.name}")

        val cursor = db.rawQuery("SELECT * FROM $SUBJECTS WHERE subjectId = ${subject.id}", null)
        if (cursor.count == 1) {
            Log.d("Database - createSubjectIfNotExist", "Subject ${subject.id} already exists")
            cursor.close()
            return}
        cursor.close()

        Log.v("Database - createSubjectIfNotExist", "Creating subject ${subject.id}")

        val values = ContentValues()
        values.put("subjectId", subject.id)
        values.put("name", subject.fullName)

        db.insert("subjects", null, values)
    }

    private fun updateSchema(api: API, finishedCallback: ()->Unit = {}) {
        Log.i("Database - UpdateSchema", "Updating schema")
        val db = this.writableDatabase
        if (db.isDbLockedByCurrentThread) {
            Log.w("Database - UpdateSchema", "Database looked, exiting")
            return
        }

        api.lessons(Filter(LocalDateTime.MIN, LocalDateTime.MAX, 100000)){response ->
            response.lessons.forEach {occasion ->
                Log.d("Database - UpdateSchema", "Caching lessonId: ${occasion.subject.id} date: ${occasion.date} startTime: ${occasion.startTime}")
                createSubjectIfNotExist(db, occasion.subject)

                val values = ContentValues()
                values.put("userId", occasion.userid)
                values.put("orgId", occasion.orgId)
                values.put("subjectId", occasion.subject.id)
                values.put("place", occasion.place)
                values.put("week", occasion.week)
                values.put("dayOfWeek", occasion.weekDay.value)
                values.put("date", occasion.date.getLong(ChronoField.EPOCH_DAY))
                values.put("startTime", occasion.startTime.getLong(ChronoField.MINUTE_OF_DAY))
                values.put("endTime", occasion.endTime.getLong(ChronoField.MINUTE_OF_DAY))

                db.insert(SCHEMA, null, values)
            }
            db.close()
            Log.v("Database - UpdateSchema", "Finished caching schema to database")
            finishedCallback()
        }
    }

    fun updateSchemaIfEmpty(api: API, finishedCallback: (result: Boolean) -> Unit): Boolean {
        val cursor = this.readableDatabase.rawQuery("SELECT * FROM $SCHEMA WHERE userId=${api.userId}", null)
        val count = cursor.count
        cursor.close()

        if (count == 0) {
            Log.w("Database - SmartReload", "Schema database is empty")
            this.updateSchema(api) { finishedCallback(true) }
            return true
        }
        Log.i("Database - SmartRelaod", "Schema database is not empty ($count rows)")
        finishedCallback(false)
        return false
    }

    @SuppressLint("Range")
    fun getSchema(
        minWeek: Int? = null, maxWeek: Int? = null,
        minDayOfWeek: DayOfWeek? = null, maxDayOfWeek: DayOfWeek? = null,
        fromTime: LocalTime? = null, fromDate: LocalDate? = null,
        toTime: LocalTime? = null, toDate: LocalDate? = null,
        maxCount: Int? = null
    ): List<Occasion> {
        Log.d("Database - getSchema", "getting schema")
        val db = this.readableDatabase

        var query = "SELECT * FROM $SCHEMA WHERE "
        if (minWeek != null) {query += "week >= $minWeek AND "}
        if (maxWeek != null) {query += "week <= $maxWeek AND "}
        if (minDayOfWeek != null) {query += "dayOfWeek >= ${minDayOfWeek.value} AND "}
        if (maxDayOfWeek != null) {query += "dayOfWeek <= ${maxDayOfWeek.value} AND "}
        if (fromDate != null) {query += "date >= ${fromDate.getLong(ChronoField.EPOCH_DAY)} AND "}
        if (toDate != null) {query += "date <= ${toDate.getLong(ChronoField.EPOCH_DAY)} AND "}
        if (fromTime != null) {query += "startTime >= ${fromTime.getLong(ChronoField.MINUTE_OF_DAY)} AND "}
        if (toTime != null) {query += "endTime <= ${toTime.getLong(ChronoField.MINUTE_OF_DAY)}; "}

        if (query.endsWith("WHERE ")) { query = "SELECT * FROM $SCHEMA" }
        if (query.endsWith(" AND ")) { query = query.dropLast(5)+";" }
        Log.d("Database - getSchema", "Schema query: $query")

        val cursor = db.rawQuery(query, null)
        Log.d("Database - getSchema", "Query returned with ${cursor.count} rows")

        cursor.moveToFirst()
        val results = mutableListOf<Occasion>()

        while (!cursor.isAfterLast) {
            results.add(
                Occasion(
                    cursor.getInt(cursor.getColumnIndex("userId")),
                    cursor.getInt(cursor.getColumnIndex("orgId")),
                    getSubject(cursor.getInt(cursor.getColumnIndex("subjectId"))),
                    cursor.getInt(cursor.getColumnIndex("week")),
                    DayOfWeek.of(cursor.getInt(cursor.getColumnIndex("dayOfWeek"))),
                    cursor.getString(cursor.getColumnIndex("place")),
                    "<placeholder>",
                    LocalDate.now().with(ChronoField.EPOCH_DAY, cursor.getInt(cursor.getColumnIndex("date")).toLong()),
                    LocalTime.now().with(ChronoField.MINUTE_OF_DAY, cursor.getInt(cursor.getColumnIndex("startTime")).toLong()),
                    LocalTime.now().with(ChronoField.MINUTE_OF_DAY, cursor.getInt(cursor.getColumnIndex("endTime")).toLong()),
                )
            )
            cursor.moveToNext()
        }
        cursor.close()

        return if (maxCount == null) results else results.take(maxCount)
    }

    @SuppressLint("Range")
    fun getSubject(id: Int): Subject {
        Log.d("Database - getSubject", "Getting subject with id $id")
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $SUBJECTS WHERE subjectId = $id", null)
        cursor.moveToFirst()
        val subject = Subject(
            cursor.getString(cursor.getColumnIndex("name")),
            cursor.getString(cursor.getColumnIndex("name")),
            cursor.getInt(cursor.getColumnIndex("subjectId")),
            "TODO"
        )
        cursor.close()
        Log.d("Database - getSubject", "subject id ${subject.id} is ${subject.name}")
        return subject
    }

}