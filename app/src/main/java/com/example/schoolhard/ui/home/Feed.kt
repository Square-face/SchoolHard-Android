package com.example.schoolhard.ui.home

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.schoolhard.API.Lesson
import com.example.schoolhard.ui.components.lesson.LessonView
import com.example.schoolhard.ui.components.lesson.Recess
import java.time.Duration
import java.time.LocalDateTime
import java.util.Timer
import java.util.TimerTask


/**
 * Display upcoming lessons in a feed that can be scrolled
 *
 * @param previous Previous lesson
 * @param current Current lesson
 * @param next Next lesson
 * */
@Composable
fun Feed(modifier: Modifier = Modifier, previous: Lesson?, current: Lesson?, next: Lesson?, triggerUpdate: () -> Unit = {}) {
    Log.d("Feed", "Previous: ${previous?.name}, Current: ${current?.name}, Next: ${next?.name}")


    // get delay for how long to wait before updating
    val triggerDate = current?.endTime ?: next?.startTime ?: return
    val triggerDelay = Duration.between(LocalDateTime.now(), triggerDate).toMillis()

    // Schedule update for when the current lesson ends
    Timer().schedule(object : TimerTask() {
        override fun run() {
            triggerUpdate()
        }
    }, triggerDelay+100)


    LazyColumn(modifier = modifier
        .fillMaxWidth()
        .padding(top = 40.dp, bottom = 0.dp, start = 20.dp, end = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp, Alignment.Top)
    ){

        val thiner = Modifier.fillMaxWidth(0.9f)

        // Set previous lesson to be the same size as next lesson if there is no current lesson
        previous?.let { item {

            LessonView(lesson = it).apply {
                when (current) {

                    null -> Medium(modifier = thiner)
                    else -> Thin(modifier = thiner)
                }
            }
        }}

        // Recess between previous and current
        previous?.let { if (current != null) item { Recess.from(it, current).Line(modifier = thiner) } }

        // Recess between previous and next
        previous?.let { if (current == null && next != null) item { Recess.from(it, next).Line(modifier = thiner) } }

        // next and current
        current?.let { item { LessonView(lesson = it).Large() } }

        // Recess between current and next
        current?.let { if (next != null) item { Recess.from(it, next).Line(modifier = thiner) } }

        next?.let { item { LessonView(lesson = it).Medium(modifier = thiner) } }
    }
}