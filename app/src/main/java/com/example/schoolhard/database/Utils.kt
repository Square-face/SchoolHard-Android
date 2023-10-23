package com.example.schoolhard.database

import android.database.Cursor
import com.example.schoolhard.API.Lesson
import com.example.schoolhard.API.Location
import com.example.schoolhard.API.Occasion
import com.example.schoolhard.API.Subject
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.UUID


/**
 * Helper class with utility functions used by [Database]
 * */
class Utils(private val database: Database) {





    /**
     * Parses a lesson from a cursor and the position it is currently in.
     *
     * @param cursor Cursor to parse from.
     * @param occasions Optional list of already parsed occasions.
     * @param subjects Optional list of already parsed subjects.
     * @return The parsed Lesson object.
     */
    fun parseLesson(
        cursor: Cursor,
        occasions: MutableList<Occasion> = mutableListOf(),
        subjects: MutableList<Subject> = mutableListOf(),
    ): Lesson {
        val columns = Schema.Lesson.Columns // Define a variable for convenience

        // Extract occasion and subject UUIDs from the cursor
        val occasionUUID = cursor.getString(columns.occasionUUID.index)
        val subjectUUID = cursor.getString(columns.subjectUUID.index)

        // Find or retrieve the subject and occasion based on their UUIDs
        val subject = findOrGet(subjects, subjectUUID)
        val occasion = findOrGet(subject, occasions, occasionUUID)

        // Calculate the date using days since 1970-01-01
        val date = calculateDateFromCursor(cursor, columns.date.index)

        // Create and return the Lesson object
        return Lesson(
            occasion,
            cursor.getInt(columns.week.index),
            date,
            UUID.fromString(cursor.getString(columns.uuid.index))
        )
    }

    /**
     * Calculates the date from a cursor based on the number of days since 1970-01-01.
     *
     * @param cursor The cursor containing the date information.
     * @param columnIndex The index of the column containing the number of days.
     * @return The calculated LocalDate.
     */
    private fun calculateDateFromCursor(cursor: Cursor, columnIndex: Int): LocalDate {
        val daysSince1970 = cursor.getInt(columnIndex).toLong()
        return LocalDate.ofEpochDay(daysSince1970)
    }





    /**
     * Parses an Occasion from a database cursor, subject list, and other data.
     *
     * @param cursor The cursor containing the occasion data.
     * @param subjects The list of subjects used to associate with the occasion.
     * @return The parsed Occasion object.
     */
    fun parseOccasion(
        cursor: Cursor,
        subjects: MutableList<Subject>
    ): Occasion {
        // column quick access
        val columns = Schema.Occasion.Columns

        // Extract the subject UUID from the cursor
        val subjectUUID     = cursor.getString(columns.subjectUUID.index)

        // Find or retrieve the subject based on the subject UUID
        val subject         = findSubjectByUUID(subjects, subjectUUID)

        // Extract various data from the cursor to create an Occasion
        val occasionUUID    = UUID.fromString(cursor.getString(columns.uuid.index))
        val occasionId      = cursor.getInt(columns.occasionId.index)
        val location        = Location(cursor.getString(columns.location.index))
        val startTime       = extractLocalTime(cursor, columns.startTime.index)
        val endTime         = extractLocalTime(cursor, columns.endTime.index)
        val dayOfWeek       = extractDayOfWeek(cursor, columns.dayOfWeek.index)

        // Create and return the Occasion object
        return Occasion(occasionUUID, occasionId, subject, location, startTime, endTime, dayOfWeek)
    }


    /**
     * Finds a subject in the provided list of subjects based on its UUID. If the subject
     * with the specified UUID is not found in the list, it is retrieved from the database
     * and added to the list.
     *
     * @param subjects The list of subjects to search in or update.
     * @param subjectUUID The UUID of the subject to find or retrieve.
     * @return The subject with the specified UUID, either found in the list or retrieved from the database.
     */
    private fun findSubjectByUUID(subjects: MutableList<Subject>, subjectUUID: String): Subject {
        return findOrGet(subjects, subjectUUID)
    }

    /**
     * Extracts a [LocalTime] value from the specified [Cursor] at the given column index.
     *
     * @param cursor The cursor to extract the value from.
     * @param columnIndex The index of the column containing the LocalTime value.
     * @return The extracted LocalTime.
     */
    private fun extractLocalTime(cursor: Cursor, columnIndex: Int): LocalTime {
        return LocalTime.ofSecondOfDay(cursor.getInt(columnIndex).toLong())
    }




    /**
     * Extracts a [DayOfWeek] value from the specified [Cursor] at the given column index.
     *
     * @param cursor The cursor to extract the value from.
     * @param columnIndex The index of the column containing the DayOfWeek value.
     * @return The extracted DayOfWeek.
     */
    private fun extractDayOfWeek(cursor: Cursor, columnIndex: Int): DayOfWeek {
        return DayOfWeek.of(cursor.getInt(columnIndex))
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
     * @param subjects List of previously cached subjects
     * @param uuid UUID string to search for
     *
     * @return Subject, generated or from the list
     * */
    private fun findOrGet(
        subjects: MutableList<Subject>,
        uuid: String,
    ): Subject {

        return subjects.find { it.id.toString() == uuid } ?: run{
            val subject = database.getSubject(uuid)!!
            subjects.add(subject)

            return@run subject
        }
    }



    /**
     * Find a occasion fom [occasions] or if it doesn't exist, get it form the database.
     *
     * If not found, use [subject] as the parent and add it to [occasions].
     *
     * @param subject Parent to use if the occasion isn't found
     * @param occasions List of previously cached occasions
     * @param uuid UUID string to search for
     *
     * @return Occasion generated or from the list
     * */
    private fun findOrGet(
        subject: Subject,
        occasions: MutableList<Occasion>,
        uuid: String,
    ): Occasion {

        return occasions.find { it.id.toString() == uuid } ?: run {
            val occasion = database.getOccasion(uuid, subject)!!
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
             * @param after Optional start date
             * @param before Optional end date
             *
             * @return Query object
             * */
            fun lessonQuery(after: LocalDateTime?=null, before: LocalDateTime?=null): Query {
                var query = "SELECT * FROM ${Schema.Lesson.table}"
                val args = mutableListOf<String>()

                // only add WHERE if any args are not null
                if (after != null || before != null) {
                    query += " WHERE"
                }

                if (after != null) {
                    // unknowns: date, startTime, date again

                    query += " (${Schema.Lesson.Columns.date} = ? AND ${Schema.Lesson.Columns.startTime} >= ?) OR ${Schema.Lesson.Columns.date} > ?"
                    args.add(after.toLocalDate().toEpochDay().toString())
                    args.add(after.toLocalTime().toSecondOfDay().toString())
                    args.add(after.toLocalDate().toEpochDay().toString())
                }

                if (before != null) {
                    // insert AND if there exists any previous checks
                    if (after != null) { query += " AND" }

                    // unknowns: date, endTime
                    query += " ${Schema.Lesson.Columns.date} <= ? AND ${Schema.Lesson.Columns.endTime} <= ?"
                    args.add(before.toLocalDate().toEpochDay().toString())
                    args.add(before.toLocalTime().toSecondOfDay().toString())
                }

                return Query(query, args.toTypedArray())
            }




            /**
             * Get a query object for retrieving a lesson at a specific date
             *
             * @param at DateTime to get lesson at
             * */
            fun lessonQuery(at: LocalDateTime): Query {

                // unknowns: date, startTime, endTime
                val query = "SELECT * FROM ${Schema.Lesson.table} WHERE ${Schema.Lesson.Columns.date} = ? AND ${Schema.Lesson.Columns.startTime} <= ? AND ${Schema.Lesson.Columns.endTime} >= ?"

                val args = arrayOf(
                    at.toLocalDate().toEpochDay().toString(),
                    at.toLocalTime().toSecondOfDay().toString(),
                    at.toLocalTime().toSecondOfDay().toString()
                )

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