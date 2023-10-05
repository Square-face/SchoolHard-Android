package com.example.schoolhard.database

import android.util.Log
import androidx.compose.foundation.layout.Column
import kotlin.reflect.KProperty

class Schema {
    class Lesson {
        companion object {
            const val table = "lessons"

            fun createQuery(): String {
                Log.d("DBSchema - TableQuery", "Generating table query for $table")
                return "CREATE TABLE $table (" +
                        Columns.id.columnQuery() + "," +
                        Columns.uuid.columnQuery() + "," +
                        Columns.occasionUUID.columnQuery() + "," +
                        Columns.subjectUUID.columnQuery() + "," +
                        Columns.date.columnQuery() + "," +
                        Columns.week.columnQuery() + "," +
                        Columns.dayOfWeek.columnQuery() + "," +
                        Columns.startTime.columnQuery() + "," +
                        Columns.endTime.columnQuery() + ")"
            }
        }

        class Columns{ companion object {
            val id              = Column("id", "INTEGER", 0, nullAllowed = true, extra = "PRIMARY KEY AUTOINCREMENT")
            val uuid            = Column("uuid", "TEXT", 1, true)
            val occasionUUID    = Column("occasionUUID", "TEXT", 2)
            val subjectUUID     = Column("subjectUUID", "TEXT", 3)
            val date            = Column("date", "INT", 4)
            val week            = Column("week", "INT", 5)
            val dayOfWeek       = Column("dayofweek", "INT", 6)
            val startTime       = Column("startTime", "INT", 7)
            val endTime         = Column("endTime", "INT", 8)
        } }
    }



    class Occasion {
        companion object {
            const val table = "occasions"

            fun createQuery(): String {
                Log.d("DBSchema - TableQuery", "Generating table query for ${Lesson.table}")
                return "CREATE TABLE $table (" +
                        Columns.id.columnQuery() + "," +
                        Columns.uuid.columnQuery() + "," +
                        Columns.occasionId.columnQuery() + "," +
                        Columns.subjectUUID.columnQuery() + "," +
                        Columns.location.columnQuery() + "," +
                        Columns.startTime.columnQuery() + "," +
                        Columns.endTime.columnQuery() + "," +
                        Columns.dayOfWeek.columnQuery() +
                        ")"
            }
        }

        class Columns { companion object {
            val id = Column("id", "INTEGER", 0, nullAllowed = true, extra = "PRIMARY KEY AUTOINCREMENT")
            val uuid = Column("uuid", "TEXT", 1, true)
            val occasionId = Column("occasionId", "INT", 2)
            val subjectUUID = Column("subjectUUID", "TEXT", 3)
            val location = Column("location", "TEXT", 4)
            val startTime = Column("startTime", "INT", 5)
            val endTime = Column("endTime", "INT", 6)
            val dayOfWeek = Column("dayOfWeek", "INT", 7)
        } }
    }



    class Subject {
        companion object {
            const val table = "subjects"

            fun createQuery(): String {
                Log.d("DBSchema - TableQuery", "Generating table query for ${Lesson.table}")
                return "CREATE TABLE $table (" +
                        Columns.id.columnQuery() + "," +
                        Columns.uuid.columnQuery() + "," +
                        Columns.subjectId.columnQuery() + "," +
                        Columns.name.columnQuery() +
                        ")"
            }
        }

        class Columns { companion object {
            val id              = Column("id", "INTEGER", 0, nullAllowed = true, extra = "PRIMARY KEY AUTOINCREMENT")
            val uuid            = Column("uuid", "TEXT", 1, true)
            val subjectId       = Column("subjectId", "INT", 2, true)
            val name            = Column("name", "TEXT", 3)
        } }
    }
}




data class Column(
    val name: String,
    val type: String,
    val index: Int,
    val unique: Boolean = false,
    val nullAllowed: Boolean = false,
    val extra: String = "",
) {
    fun columnQuery(): String {
        var query = "$name ${type.uppercase()}"

        if (unique) {
            query += " UNIQUE"
        }

        if (!nullAllowed) {
            query += " NOT NULL"
        }

        if (extra != "") {
            query += " $extra"
        }

        Log.v("DBColumn-CreateQuery", "Query: $query")

        return query
    }

    override fun toString(): String {
        return name
    }
}