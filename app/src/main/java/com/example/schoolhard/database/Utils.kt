package com.example.schoolhard.database

import android.database.Cursor
import com.example.schoolhard.API.Lesson
import com.example.schoolhard.API.Location
import com.example.schoolhard.API.Occasion
import com.example.schoolhard.API.Subject
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
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

        val occasionUUID = cursor.getString(Schema.Lesson.Columns.occasionUUID.index)
        val subjectUUID = cursor.getString(Schema.Lesson.Columns.subjectUUID.index)

        val subject = findOrGet(db, subjects, subjectUUID)
        val occasion = findOrGet(db, subject, occasions, occasionUUID)

        return Lesson(
            occasion,
            cursor.getInt(Schema.Lesson.Columns.week.index),
            LocalDate.ofEpochDay(cursor.getInt(Schema.Lesson.Columns.date.index).toLong()),
            UUID.fromString(cursor.getString(Schema.Lesson.Columns.uuid.index))
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

        val subjectUUID = cursor.getString(Schema.Occasion.Columns.subjectUUID.index)
        val subject = findOrGet(db, subjects, subjectUUID)

        return Occasion(
            UUID.fromString(cursor.getString(Schema.Occasion.Columns.uuid.index)),
            cursor.getInt(Schema.Occasion.Columns.occasionId.index),
            subject,
            Location(cursor.getString(Schema.Occasion.Columns.location.index)),
            LocalTime.MIN.plusSeconds(cursor.getInt(Schema.Occasion.Columns.startTime.index).toLong()),
            LocalTime.MIN.plusSeconds(cursor.getInt(Schema.Occasion.Columns.endTime.index).toLong()),
            DayOfWeek.of(cursor.getInt(Schema.Occasion.Columns.dayOfWeek.index))
        )
    }


    /**
     * Parse a subject from a cursor and the position it is currently in
     *
     * @param cursor Cursor to parse from
     *
     * @return Parsed subject
     * */
    fun parseSubject(cursor: Cursor): Subject {
        return Subject(
            cursor.getInt(Schema.Subject.Columns.subjectId.index),
            cursor.getString(Schema.Subject.Columns.name.index),
            UUID.fromString(cursor.getString(Schema.Subject.Columns.uuid.index))
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
            fun lessonQuery(week: Int, dayOfWeek: DayOfWeek? = null): Query {
                var query = "SELECT * FROM ${Schema.Lesson.table} WHERE ${Schema.Lesson.Columns.week} = ?"
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
                val query = "SELECT * FROM ${Schema.Subject.table} WHERE ${Schema.Subject.Columns.uuid} = ?"
                val args = arrayOf(uuid)

                return Query(query, args)
            }



            /**
             * Generate a query object with sql string and args
             *
             * @param uuid Occasion uuid
             *
             * @return Query object
             * */
            fun occasionQuery(uuid: String): Query {
                val query = "SELECT * FROM ${Schema.Occasion.table} WHERE ${Schema.Occasion.Columns.uuid} = ?"
                val args = arrayOf(uuid)

                return Query(query, args)
            }
        }
    }
}