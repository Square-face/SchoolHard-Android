package com.example.schoolhard.ui.schema

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
import com.example.schoolhard.API.Filter
import com.example.schoolhard.API.Occasion
import java.time.LocalDateTime

fun updateSchemaContent(api: API, filter: Filter, lessons: MutableState<List<Occasion>>) {
    api.lessons(filter){
        lessons.value = it.lessons
    }
}

@Composable
fun SchemaRoute(modifier: Modifier = Modifier, api: API) {
    Column(
        verticalArrangement = Arrangement.spacedBy(15.dp, Alignment.Top),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .padding(start = 40.dp, top = 75.dp, end = 40.dp)
    ) {
        val lessons = remember { mutableStateOf<List<Occasion>>(listOf()) }

        LaunchedEffect(key1 = true) {
            updateSchemaContent(api, Filter(LocalDateTime.now(), LocalDateTime.MAX, 10), lessons)
        }

        DayInfo()
        Schema(lessons = lessons)
    }
}