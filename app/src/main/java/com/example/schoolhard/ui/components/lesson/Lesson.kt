package com.example.schoolhard.ui.components.lesson

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.schoolhard.API.Lesson
import kotlinx.coroutines.delay

/**
 * Holding class for all lesson view models
 *
 * @param lesson Lesson to create view for
 * */
class LessonView(val lesson: Lesson) {



    /**
     * Medium sized lesson view model
     * */
    @Composable
    fun Medium(modifier: Modifier = Modifier) {

        var progress by remember { mutableStateOf(Progress(lesson.progress)) }
        var time by remember { mutableStateOf(Time(lesson)) }
        val meta = Meta(lesson)

        LaunchedEffect(Unit) {
            while(true) {
                delay(1000)
                progress = Progress(lesson.progress)
                time = Time(lesson)
            }
        }

        Column(
            modifier = modifier
                .fillMaxWidth()
                .height(80.dp)
                .background(MaterialTheme.colorScheme.secondary, RoundedCornerShape(8.dp))
                .clip(RoundedCornerShape(8.dp))
        ) {

            progress.Thin()

            // content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(7.dp, 2.dp),
            ) {
                meta.Name()

                Row(
                    modifier = Modifier
                        .fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        time.Duration()
                        meta.Location()
                    }

                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        time.StartTime()
                        time.EndTime()
                    }
                }
            }
        }
    }
}