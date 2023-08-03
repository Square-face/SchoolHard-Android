package com.example.schoolhard.ui.components

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.schoolhard.utils.getDelta
import com.example.schoolhard.utils.getDeltaString
import com.example.schoolhard.utils.getDeltaToNow
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun Lesson(
    modifier: Modifier = Modifier,
    startTime: Date,
    endTime: Date,
){
    val bevel = RoundedCornerShape(8.dp)
    Column(modifier = modifier
        .fillMaxWidth()
        .height(80.dp)
        .background(MaterialTheme.colorScheme.secondary, shape = bevel)
        .clip(bevel)
    ) {
        ProgressBar(progress = 0.75F)
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 8.dp, bottom = 5.dp, end = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            LessonInfo()
            LessonTime(startTime=startTime, endTime=endTime)
        }
    }
}

@Composable
fun ProgressBar(modifier: Modifier = Modifier, progress: Float = 0F){
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
fun LessonInfo(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxHeight(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Lesson",
            style = TextStyle(
                fontSize = 20.sp,
                fontWeight = FontWeight(600),
                color = Color(0xFF000000),
            )
        )
        Text(
            text = "Room",
            style = TextStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight(500),
                color = Color(0xFF000000),
            )
        )
        Text(
            text = "Teacher Name",
            style = TextStyle(
                fontSize = 12.sp,
                fontWeight = FontWeight(400),
                color = Color(0xFF000000),
            )
        )
    }
}

@Composable
fun LessonTime(modifier: Modifier = Modifier, startTime: Date, endTime: Date){
    Column(
        modifier = modifier
            .fillMaxHeight(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.End
    ) {
        LessonClockTime(time=startTime)
        val delta = getDelta(startTime, endTime)
        Text(
            text = getDeltaString(delta),
            style = TextStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight(500),
                color = Color(0xFF000000),
            )
        )
        LessonClockTime(time=endTime)
    }
}

@Composable
fun LessonClockTime(
    modifier: Modifier = Modifier,
    time: Date,
    showDeltaIfPassed: Boolean = true,
){
    Row(modifier = modifier,
        verticalAlignment = Alignment.CenterVertically) {
        val clockFormat = SimpleDateFormat("hh:mm", Locale.ENGLISH)

        val delta = remember {
            mutableStateOf(getDeltaToNow(time))
        }

        LaunchedEffect(true) {
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
            text = clockFormat.format(time),
            style = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight(400),
                color = Color(0xFF000000),
            )
        )
    }
}