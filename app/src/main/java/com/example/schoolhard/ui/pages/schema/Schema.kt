package com.example.schoolhard.ui.pages.schema

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.schoolhard.data.Lesson

@Composable
fun Schema(modifier: Modifier = Modifier, lessons: List<Lesson>) {
    Log.d("UI - Schedule", "Rendering schedule")
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(40.dp, Alignment.Top),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
    ) {
        items(lessons) {
            it.Large()
        }
    }
}