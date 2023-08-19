package com.example.schoolhard.ui.schema

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

@Composable
fun DayInfo(modifier: Modifier = Modifier){
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Monday",
            style = TextStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight(600),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )
        )
        DateAndWeek()
    }
}

@Composable
fun DateAndWeek(modifier: Modifier = Modifier){
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "23/7/31",
            style = TextStyle(
                fontSize = 12.sp,
                fontWeight = FontWeight(600),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )
        )
        Text(
            text = "Week 33",
            style = TextStyle(
                fontSize = 12.sp,
                fontWeight = FontWeight(600),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )
        )
    }
}