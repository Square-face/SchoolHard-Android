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
import com.example.schoolhard.database.Database
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoField
import java.time.temporal.IsoFields
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.schoolhard.API.SchoolSoft.SchoolSoftAPI
import com.example.schoolhard.data.Logins

class UpdateSchedule(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        val store = applicationContext.getSharedPreferences("logins", Context.MODE_PRIVATE)
        val logins = Logins(store)

        val api = SchoolSoftAPI()
        api.loadLogin(logins)

        val database = Database(applicationContext, null)

        Log.d("ScheduleWorker", "Updating schedule")
        database.updateSchedule(api)

        return Result.success()
    }
}
fun updateSchemaContent(database: Database, date: LocalDate, lessons: MutableState<List<Lesson>>) {
    Log.d("UpdatingUISchema", "querying for date $date")

    // TODO: Implement getSchedule method that uses date and then implement here
    lessons.value = database.getSchedule(date.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR), DayOfWeek.of(date.get(ChronoField.DAY_OF_WEEK)))
    Log.d("UpdatingUISchedule", "Got ${lessons.value.size} lessons")
}

fun updateSchemaContent(database: Database, week: Int, dayOfWeek: DayOfWeek, lessons: MutableState<List<Lesson>>) {
    Log.d("UpdatingUISchedule", "Querying for ${dayOfWeek.name} on week $week")

    lessons.value = database.getSchedule(week, dayOfWeek)
    Log.d("UpdatingUISchedule", "Got ${lessons.value.size} lessons")
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
        }

        val workRequest = remember { OneTimeWorkRequestBuilder<UpdateSchedule>().build() }
        WorkManager.getInstance(LocalContext.current).enqueue(workRequest)

        DayInfo(update = { date: LocalDate -> updateSchemaContent(database, date, lessons) })
        Schema(lessons = lessons.value)
    }
}
