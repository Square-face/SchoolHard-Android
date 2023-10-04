package com.example.schoolhard.database

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.example.schoolhard.API.Lesson
import com.example.schoolhard.API.Occasion
import com.example.schoolhard.API.Subject
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.UUID

class Utils {


    /**
     * Generate a query object with
     *
     * @param week Week number to use in the query
     * @param dayOfWeek Optional day number to use in the query
     *
     * @return Query object
     * */
    fun generateLessonQuery(week: Int, dayOfWeek: DayOfWeek?): Query {
        var query = "SELECT * FROM $LESSONS WHERE week = ?"
        var args = arrayOf(week.toString())

        // only add day of week if it is not null
        if (dayOfWeek != null) {
            query += " AND dayofweek = ?"

            args = arrayOf(
                week.toString(),
                dayOfWeek.value.toString()
            )
        }

        return Query(query, args)
    }



    /**
     * Parse a lesson from a cursor and the position it is currently in
     *
     * @param cursor the cursor to parse from
     * @param occasions optional list of already parsed occasions
     * @param subjects optional list of already parsed subjects
     * */
    fun parseLesson(
        db: Database,
        cursor: Cursor,
        occasions: MutableList<Occasion> = mutableListOf(),
        subjects: MutableList<Subject> = mutableListOf(),
    ): Lesson {

        val occasionUUID = cursor.getString(3)
        val subjectUUID = cursor.getString(3)

        val subject = findOrGet(db, subjects, subjectUUID)
        val occasion = findOrGet(db, subject, occasions, occasionUUID)

        return Lesson(
            occasion,
            cursor.getInt(6),
            LocalDate.MIN.plusDays(cursor.getInt(9).toLong()),
            UUID.fromString(cursor.getString(2))
        )

    }



    /**
     * Find a occasion fom [subjects] or if it doesn't exist, get it form the database.
     *
     * If not found, get from the database and add it to [subjects].
     *
     *
     * @param db Database to use if the subject isn't found
     * @param subjects List of previously cached subjects
     * @param uuid UUID string to search for
     *
     * @return Subject, generated or from the list
     * */
    private fun findOrGet(
        db: Database,
        subjects: MutableList<Subject>,
        uuid: String,
    ): Subject {

        return subjects.find { it.id.toString() == uuid } ?: run{
            val subject = db.getSubject(uuid)!!
            subjects.add(subject)

            return@run subject
        }
    }



    /**
     * Find a occasion fom [occasions] or if it doesn't exist, get it form the database.
     *
     * If not found, use [subject] as the parent and add it to [occasions].
     *
     * @param db Database to use if the occasion isn't found
     * @param subject Parent to use if the occasion isn't found
     * @param occasions List of previously cached occasions
     * @param uuid UUID string to search for
     *
     * @return Occasion generated or from the list
     * */
    private fun findOrGet(
        db: Database,
        subject: Subject,
        occasions: MutableList<Occasion>,
        uuid: String,
    ): Occasion {

        return occasions.find { it.id.toString() == uuid } ?: run {
            val occasion = db.getOccasion(uuid, subject)!!
            occasions.add(occasion)

            return@run occasion
        }
    }
}


data class Query(
    val query: String,
    val args: Array<String>,
)