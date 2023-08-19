package com.example.schoolhard.ui.schema

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.schoolhard.API.API
import com.example.schoolhard.API.Filter
import com.example.schoolhard.API.Occasion
import com.example.schoolhard.ui.components.Lesson
import java.time.LocalDateTime
import java.util.Date
import java.util.concurrent.CountDownLatch

@Composable
fun Schema(modifier: Modifier = Modifier, lessons: MutableState<List<Occasion>>) {
    Column(
        verticalArrangement = Arrangement.spacedBy(40.dp, Alignment.Top),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxSize().verticalScroll(ScrollState(0), enabled = true)
    ) {

        lessons.value.forEach { occasion ->
            Lesson(startTime = occasion.date.atTime(occasion.startTime), endTime = occasion.date.atTime(occasion.endTime), title = occasion.lesson.fullName, room = occasion.place, teacher = occasion.teacher)
        }
    }
}