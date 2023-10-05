package com.example.schoolhard.database

import android.database.Cursor
import com.example.schoolhard.API.Lesson
import com.example.schoolhard.API.Occasion
import com.example.schoolhard.API.Subject
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.UUID



/**
 * Helper class with utility functions used by [Database]
 * */
class Utils {





    /**
     * Parse a lesson from a cursor and the position it is currently in
     *
     * @param db Database to use to get occasions or subjects if they don't exist
     * in [occasions] or [subjects]
     * @param cursor Cursor to parse from
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
     * Parse an occasion from a cursor and the position it is currently in
     *
     * @param db Database to use to get occasions or subjects if they don't exist
     * in [subjects]
     * @param cursor Cursor to parse from
     * @param subjects optional list of already parsed subjects
     * */
    fun parseOccasion(
        db: Database,
        cursor: Cursor,
        subjects: MutableList<Subject>
    ): Occasion {
        TODO("Not Implemented")
    }


    /**
     * Parse a subject from a cursor and the position it is currently in
     *
     * @param cursor Cursor to parse from
     * */
    fun parseSubject(cursor: Cursor): Subject {
        TODO("Not Implemented")
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

    /**
     * SQL query representation.
     *
     * Represent a sql query with the query itself and the args
     *
     * @property query The SQL query in text
     * @property args The args to replace all ? with
     *
     * @author Linus Michelsson
     * */
    class Query(
        val query: String,
        val args: Array<String>,
    ) {
        companion object {

            /**
             * Generate a query object with sql string and args
             *
             * @param week Week number to use in the query
             * @param dayOfWeek Optional day number to use in the query
             *
             * @return Query object
             * */
            fun lessonQuery(week: Int, dayOfWeek: DayOfWeek?): Query {
                var query = "SELECT * FROM ${Database.LESSONS} WHERE week = ?"
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
             * Generate a query object with sql string and args
             *
             * @param uuid Subject uuid
             *
             * @return Query object
             * */
            fun subjectQuery(uuid: String): Query {
                val query = "SELECT * FROM ${Database.SUBJECTS} WHERE uuid = ?"
                val args = arrayOf(uuid)

                return Query(query, args)
            }
        }
    }
}