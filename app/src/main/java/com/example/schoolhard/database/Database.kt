package com.example.schoolhard.database

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.schoolhard.API.API
import com.example.schoolhard.API.Filter
import com.example.schoolhard.API.Lesson
import com.example.schoolhard.API.Occasion
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoField

class Database(context: Context, factory: SQLiteDatabase.CursorFactory?):
    SQLiteOpenHelper(context, "schema", factory, 2) {
    override fun onCreate(db: SQLiteDatabase) {
        Log.i("Database", "creating tables")

        val occasionQuery = ("CREATE TABLE occasions ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "subjectId INT NOT NULL,"
                + "week INT NOT NULL,"
                + "dayOfWeek INT NOT NULL,"
                + "date INT NOT NULL,"
                + "startTime INT NOT NULL,"
                + "endTime INT NOT NULL,"
                + "place TEXT NOT NULL"
                + ")")

        val subjectQuery = ("CREATE TABLE subjects ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "subjectId INT NOT NULL,"
                + "name TEXT NOT NULL,"
                + "nameOverride TEXT"
                + ")")

        db.execSQL(occasionQuery)
        db.execSQL(subjectQuery)
    }

    private fun dropAllTables(db: SQLiteDatabase){
        Log.w("Database", "Dropping all tables")
        db.execSQL("DROP TABLE IF EXISTS occasions")
        db.execSQL("DROP TABLE IF EXISTS subjects")
    }

    override fun onUpgrade(db: SQLiteDatabase, p1: Int, p2: Int) {
        dropAllTables(db)
        onCreate(db)
    }

    private fun createSubjectIfNotExist(db: SQLiteDatabase, subject: Lesson) {
        val cursor = db.rawQuery("SELECT * FROM subjects WHERE subjectId = ${subject.id}", null)
        if (cursor.count == 1) {cursor.close(); return}

        val values = ContentValues()
        values.put("subjectId", subject.id)
        values.put("name", subject.fullName)

        db.insert("subjects", null, values)
    }

    private fun updateSchema(api: API) {
        Log.i("Database - UpdateSchema", "Updating...")
        api.lessons(Filter(LocalDateTime.MIN, LocalDateTime.MAX, 100000)){response ->

            val db = this.writableDatabase
            this.dropAllTables(db)
            this.onCreate(db)

            response.lessons.forEach {occasion ->
                createSubjectIfNotExist(db, occasion.lesson)

                val values = ContentValues()
                values.put("subjectId", occasion.lesson.id)
                values.put("place", occasion.place)
                values.put("week", occasion.week)
                values.put("dayOfWeek", occasion.weekDay.value)
                values.put("date", occasion.date.getLong(ChronoField.EPOCH_DAY))
                values.put("startTime", occasion.startTime.getLong(ChronoField.MINUTE_OF_DAY))
                values.put("endTime", occasion.endTime.getLong(ChronoField.MINUTE_OF_DAY))

                db.insert("occasions", null, values)
            }
            db.close()
            Log.i("Database - UpdateSchema", "occasions table populated")
        }
    }

    fun updateSchemaIfEmpty(api: API): Boolean {
        val cursor = this.readableDatabase.rawQuery("SELECT * FROM occasions", null)
        val count = cursor.count
        cursor.close()

        if (count == 0) {
            Log.w("Database - SmartReload", "Database is empty")
            this.updateSchema(api)
            return true
        }
        Log.i("Database - SmartRelaod", "Database was not empty ($count)")
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
        val db = this.readableDatabase

        var query = "SELECT * FROM occasions WHERE "
        if (minWeek != null) {query += "week >= $minWeek AND "}
        if (maxWeek != null) {query += "week <= $maxWeek AND "}
        if (minDayOfWeek != null) {query += "dayOfWeek >= ${minDayOfWeek.value} AND "}
        if (maxDayOfWeek != null) {query += "dayOfWeek <= ${maxDayOfWeek.value} AND "}
        if (fromDate != null) {query += "date >= ${fromDate.getLong(ChronoField.EPOCH_DAY)} AND "}
        if (toDate != null) {query += "date <= ${toDate.getLong(ChronoField.EPOCH_DAY)} AND "}
        if (fromTime != null) {query += "startTime >= ${fromTime.getLong(ChronoField.MINUTE_OF_DAY)} AND "}
        if (toTime != null) {query += "endTime <= ${toTime.getLong(ChronoField.MINUTE_OF_DAY)}; "}

        if (query.endsWith("WHERE ")) { query = "SELECT * FROM occasions" }
        if (query.endsWith(" AND ")) { query = query.dropLast(5)+";" }
        Log.d("Database - getSchema", "Final query: $query")

        val cursor = db.rawQuery(query, null)
        Log.v("Database - getSchema", "Query returned with ${cursor.count} rows")

        cursor.moveToFirst()
        val results = mutableListOf<Occasion>()

        while (!cursor.isAfterLast) {
            results.add(
                Occasion(
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
    fun getSubject(id: Int): Lesson {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM subjects WHERE subjectId = $id", null)
        cursor.moveToFirst()
        val lesson = Lesson(
            cursor.getString(cursor.getColumnIndex("name")),
            cursor.getString(cursor.getColumnIndex("name")),
            cursor.getInt(cursor.getColumnIndex("subjectId")),
            "TODO"
        )
        cursor.close()
        return lesson
    }

}