package com.example.schoolhard.ui.schema

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.schoolhard.ui.components.Lesson
import java.util.Date

@Composable
fun Schema(modifier: Modifier = Modifier) {
    Column(
        verticalArrangement = Arrangement.spacedBy(40.dp, Alignment.Top),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxSize()
    ) {
        val now = Date().time

        val t1 = Date(now - 60*60*1000*2)
        val t2 = Date(now - 60*60*1000*1)
        Lesson(startTime=t1, endTime=t2)

        val t3 = Date(now - 60*1000*45)
        val t4 = Date(now + 60*1000*1)
        Lesson(startTime=t3, endTime=t4)


        val t5 = Date(now + 60*1000*15)
        val t6 = Date(now + 60*1000*25 + 60*60*1000*1)
        Lesson(startTime=t5, endTime=t6)

    }
}