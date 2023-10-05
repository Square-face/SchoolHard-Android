package com.example.schoolhard.ui.schema

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.schoolhard.API.API
import com.example.schoolhard.API.Lesson
import com.example.schoolhard.API.Occasion
import com.example.schoolhard.database.Database
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoField
import java.time.temporal.IsoFields
import java.time.temporal.TemporalAccessor
import java.time.temporal.TemporalField
import kotlin.coroutines.coroutineContext

fun updateSchemaContent(database: Database, date: LocalDate, lessons: MutableState<List<Lesson>>) {
    Log.d("UpdatingUISchema", "querying for date $date")

    // TODO: Implement getSchedule method that uses date and then implement here
    lessons.value = database.getSchedule(date.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR), DayOfWeek.of(date.get(ChronoField.DAY_OF_WEEK)))
}

fun updateSchemaContent(database: Database, week: Int, dayOfWeek: DayOfWeek, lessons: MutableState<List<Lesson>>) {
    Log.d("UpdatingUISchema", "querying for ${dayOfWeek.name} on week $week")

    lessons.value = database.getSchedule(week, dayOfWeek)
}

@Composable
fun SchemaRoute(modifier: Modifier = Modifier, api: API, database: Database) {
    Column(
        verticalArrangement = Arrangement.spacedBy(15.dp, Alignment.Top),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .padding(start = 40.dp, top = 75.dp, end = 40.dp)
    ) {

        val lessons = remember { mutableStateOf(listOf<Lesson>()) }

        LaunchedEffect(key1 = true) {
            updateSchemaContent(database, LocalDate.now(), lessons)
            database.updateSchema(api) {
                updateSchemaContent(database, LocalDate.now(), lessons)
            }
        }

        DayInfo(update = { date: LocalDate -> updateSchemaContent(database, date, lessons) })
        Schema(lessons = lessons.value)
    }
}
