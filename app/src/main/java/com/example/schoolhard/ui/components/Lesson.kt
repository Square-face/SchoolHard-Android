package com.example.schoolhard.ui.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.schoolhard.API.Lesson
import com.example.schoolhard.utils.getDelta
import com.example.schoolhard.utils.getDeltaString
import com.example.schoolhard.utils.getDeltaToNow
import com.example.schoolhard.utils.getProgress
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun Lesson(
    modifier: Modifier = Modifier,
    lesson: Lesson,
){
    Log.d("UI - Lesson", "Drawing lesson ${lesson.occasion.subject.name}")
    val bevel = RoundedCornerShape(8.dp)

    Column(modifier = modifier
        .fillMaxWidth()
        .height(80.dp)
        .background(MaterialTheme.colorScheme.secondary, shape = bevel)
        .clip(bevel)
    ) {
        var progress by remember { mutableStateOf(getProgress(
            lesson.startTime,
            LocalDateTime.now(),
            lesson.endTime
        )) }

        LaunchedEffect(lesson) {
            while (true) {
                progress = getProgress(
                    lesson.startTime,
                    LocalDateTime.now(),
                    lesson.endTime
                )
                delay(1000)
            }
        }



        ProgressBar(progress = progress)
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 8.dp, bottom = 5.dp, end = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            LessonInfo(lesson=lesson)
            LessonTime(lesson=lesson)
        }
    }
}

@Composable
fun ProgressBar(
    modifier: Modifier = Modifier,
    progress: Float = 0F
){
    Box(modifier = modifier
        .fillMaxWidth()
        .height(8.dp)
        .background(Color(0xFF747474))

    ){
        Box(modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(progress)
            .background(Color(0xFF369B4C))
        )
    }
}

@Composable
fun LessonInfo(
    modifier: Modifier = Modifier,
    lesson: Lesson
) {
    Column(
        modifier = modifier
            .fillMaxHeight(),
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            modifier=Modifier.fillMaxWidth(0.65f),
            text = lesson.occasion.subject.name,
            style = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight(600),
                color = Color(0xFF000000),
            )
        )
        Text(
            text = lesson.occasion.location.name,
            style = TextStyle(
                fontSize = 12.sp,
                fontWeight = FontWeight(500),
                color = Color(0xFF000000),
            )
        )
    }
}

@Composable
fun LessonTime(
    modifier: Modifier = Modifier,
    lesson: Lesson
){
    Column(
        modifier = modifier
            .fillMaxHeight(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.End
    ) {

        LessonClockTime(time = lesson.startTime)

        val delta = getDelta(lesson.startTime, lesson.endTime)

        Text(
            text = getDeltaString(delta),
            style = TextStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight(500),
                color = Color(0xFF000000),
            )
        )
        LessonClockTime(time = lesson.endTime)
    }
}

@Composable
fun LessonClockTime(
    modifier: Modifier = Modifier,
    time: LocalDateTime,
    // showDeltaIfPassed: Boolean = true,
){
    Row(modifier = modifier,
        verticalAlignment = Alignment.CenterVertically) {

        val delta = remember {
            mutableStateOf(getDeltaToNow(time))
        }

        LaunchedEffect(time) {
            while (true) {
                delta.value = getDeltaToNow(time)
                delay(1000)
            }
        }

        if (delta.value >= 0){

            Text(
                text = getDeltaString(delta.value),
                style = TextStyle(
                    fontSize = 12.sp,
                    fontWeight = FontWeight(700),
                    color = Color(0xFF000000),
                )
            )
            Spacer(modifier = Modifier.width(7.5.dp))
        }

        Text(
            text = time.format(DateTimeFormatter.ofPattern("HH:mm")),
            style = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight(400),
                color = Color(0xFF000000),
            )
        )
    }
}