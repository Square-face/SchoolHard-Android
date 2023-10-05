package com.example.schoolhard.database

import android.util.Log
import androidx.compose.foundation.layout.Column
import kotlin.reflect.KProperty

class Schema {
    class Lesson {
        companion object {
            const val table = "subjects"

            fun createQuery(): String {
                return "CREATE TABLE $table (" +
                        Columns.id.columnQuery() + "," +
                        Columns.uuid.columnQuery() + "," +
                        Columns.occasionUUID.columnQuery() + "," +
                        Columns.subjectUUID.columnQuery() + "," +
                        Columns.startTime.columnQuery() + "," +
                        Columns.endTime.columnQuery() + "," +
                        Columns.dayOfWeek.columnQuery() +
                        ")"
            }
        }

        class Columns{ companion object {
            val id              = Column("id", "INT", 1, true)
            val uuid            = Column("uuid", "TEXT", 2, true)
            val occasionUUID    = Column("occasionUUID", "TEXT", 3)
            val subjectUUID     = Column("subjectUUID", "TEXT", 4)
            val dayOfWeek       = Column("dayofweek", "INT", 5)
            val week            = Column("week", "INT", 6)
            val date            = Column("date", "INT", 7)
            val startTime       = Column("startTime", "INT", 8)
            val endTime         = Column("endTime", "INT", 9)
        } }
    }



    class Occasion {
        companion object {
            const val table = "occasions"

            fun createQuery(): String {
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
            val id = Column("id", "INT", 1, true)
            val uuid = Column("uuid", "TEXT", 2, true)
            val occasionId = Column("occasionId", "INT", 3)
            val subjectUUID = Column("subjectUUID", "TEXT", 4)
            val location = Column("location", "TEXT", 5)
            val startTime = Column("startTime", "INT", 6)
            val endTime = Column("endTime", "INT", 7)
            val dayOfWeek = Column("dayOfWeek", "INT", 8)
        } }
    }



    class Subject {
        companion object {
            const val table = "subjects"

            fun createQuery(): String {
                return "CREATE TABLE $table (" +
                        Columns.id.columnQuery() + "," +
                        Columns.uuid.columnQuery() + "," +
                        Columns.subjectId.columnQuery() + "," +
                        Columns.name.columnQuery() +
                        ")"
            }
        }

        class Columns { companion object {
            val id              = Column("id", "INT", 1, true, true, extra = " PRIMARY KEY AUTOINCREMENT")
            val uuid            = Column("uuid", "TEXT", 2, true)
            val subjectId       = Column("subjectId", "INT", 3, true)
            val name            = Column("name", "TEXT", 4)
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