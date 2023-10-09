package com.example.schoolhard.ui.pages.schema

import android.util.Log
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.schoolhard.API.Lesson
import com.example.schoolhard.ui.components.Lesson

@Composable
fun Schema(modifier: Modifier = Modifier, lessons: List<Lesson>) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(40.dp, Alignment.Top),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
    ) {
        items(lessons) {
            Lesson(lesson = it)
        }
    }
}