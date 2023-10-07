package com.example.schoolhard

import com.example.schoolhard.database.Schema
import com.example.schoolhard.database.Utils
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.DayOfWeek
import java.util.UUID

class DatabaseUtilsUnitTest {
    class LessonQuery {
        @Test
        fun weekAndDayOfWeek() {

            var expected: Utils.Query
            var actual: Utils.Query



            expected = Utils.Query(
                "SELECT * FROM ${Schema.Lesson.table} WHERE ${Schema.Lesson.Columns.week} = ?" ,
                arrayOf("10"))
            actual = Utils.Query.lessonQuery(10)

            assertEquals("week 10 query", expected.query, actual.query)
            assertArrayEquals("week 10 args", expected.args, actual.args)



            expected = Utils.Query(
                "SELECT * FROM ${Schema.Lesson.table} WHERE ${Schema.Lesson.Columns.week} = ?",
                arrayOf("52"))
            actual = Utils.Query.lessonQuery(52)

            assertEquals("week 52 query", expected.query, actual.query)
            assertArrayEquals("week 52 args", expected.args, actual.args)



            expected = Utils.Query(
                "SELECT * FROM ${Schema.Lesson.table} WHERE ${Schema.Lesson.Columns.week} = ? AND ${Schema.Lesson.Columns.dayOfWeek} = ?",
                arrayOf("14", DayOfWeek.MONDAY.value.toString()))
            actual = Utils.Query.lessonQuery(14, DayOfWeek.MONDAY)

            assertEquals("monday week 14 query", expected.query, actual.query)
            assertArrayEquals("monday week 14 args", expected.args, actual.args)



            expected = Utils.Query(
                "SELECT * FROM ${Schema.Lesson.table} WHERE ${Schema.Lesson.Columns.week} = ? AND ${Schema.Lesson.Columns.dayOfWeek} = ?",
                arrayOf("1", DayOfWeek.FRIDAY.value.toString()))
            actual = Utils.Query.lessonQuery(1, DayOfWeek.FRIDAY)

            assertEquals("friday week 1 query", expected.query, actual.query)
            assertArrayEquals("friday week 1 args", expected.args, actual.args)
        }
    }




    class SubjectQuery {
        @Test
        fun uuid() {

            var expected: Utils.Query
            var actual: Utils.Query



            val uuid = UUID.randomUUID()

            expected = Utils.Query(
                "SELECT * FROM ${Schema.Subject.table} WHERE uuid = ?",
                arrayOf(uuid.toString())
            )
            actual = Utils.Query.subjectQuery(uuid.toString())

            assertEquals("uuid query", expected.query, actual.query)
            assertArrayEquals("uuid args", expected.args, actual.args)
        }
    }
}