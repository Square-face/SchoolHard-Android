package com.example.schoolhard.ui.schema

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.schoolhard.API.Filter
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoField
import java.time.temporal.IsoFields

val startOfDay = LocalTime.of(0, 0)

@Composable
fun DayInfo(modifier: Modifier = Modifier, update: (Filter) -> Unit){
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(0.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val day = remember { mutableStateOf(LocalDate.now()) }

        DayOfWeekSelect(day = day, update = update)
        DateAndWeek(day = day, update = update)
    }
}

@Composable
fun DayOfWeekSelect(modifier: Modifier = Modifier, day: MutableState<LocalDate>, update: (Filter) -> Unit){
    Row(
        modifier = modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        WeekDay(day = day, text = "Monday", value = 1, update = update)
        WeekDay(day = day, text = "Tuesday", value = 2, update = update)
        WeekDay(day = day, text = "Wednesday", value = 3, update = update)
        WeekDay(day = day, text = "Thursday", value = 4, update = update)
        WeekDay(day = day, text = "Friday", value = 5, update = update)
    }
}

@Composable
fun DateAndWeek(modifier: Modifier = Modifier, day: MutableState<LocalDate>, update: (Filter) -> Unit){
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = day.value.format(DateTimeFormatter.ofPattern("yy/MM/dd")),
            style = TextStyle(
                fontSize = 12.sp,
                fontWeight = FontWeight(600),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )
        )
        Week(day = day, update = update)
    }
}

@Composable
fun WeekDay(modifier: Modifier = Modifier, day: MutableState<LocalDate>, text: String, value: Int, update: (Filter) -> Unit){
    Button(
        modifier = modifier
            .defaultMinSize(1.dp, 1.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent
        ),
        contentPadding = PaddingValues(0.dp, 3.dp, 0.dp, 7.dp),
        shape = RoundedCornerShape(0.5f),
        onClick = {
            day.value = day.value.with(ChronoField.DAY_OF_WEEK, value.toLong())
            val filter = Filter(
                day.value.atTime(startOfDay),
                day.value.plusDays(1L).atTime(startOfDay),
                10
            )
            update(filter)
        }) {
        Column(horizontalAlignment = Alignment.CenterHorizontally){

            if (day.value.get(ChronoField.DAY_OF_WEEK) == value) {
                Divider(modifier=Modifier.width(60.dp) ,thickness = 3.dp, color = MaterialTheme.colorScheme.primary)
            }

            Text(
                modifier = modifier.padding(0.dp),
                text = text,
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight(600),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    }
}

@Composable
fun Week(modifier: Modifier = Modifier, day: MutableState<LocalDate>, update: (Filter) -> Unit){
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Button(
            modifier = modifier
                .defaultMinSize(1.dp, 1.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent
            ),
            contentPadding = PaddingValues(0.dp, 0.dp, 0.dp, 0.dp),
            shape = RoundedCornerShape(0.5f),
            onClick = {
                day.value = day.value.with(IsoFields.WEEK_OF_WEEK_BASED_YEAR, day.value.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)-1L)
                val filter = Filter(
                    day.value.atTime(startOfDay),
                    day.value.plusDays(1L).atTime(startOfDay),
                    10
                )
                update(filter)
            }
        ) {
            Icon(modifier = Modifier.padding(0.dp).height(12.dp), imageVector = Icons.Filled.ArrowBack, contentDescription = "Previous", tint = MaterialTheme.colorScheme.onBackground)
        }
        Text(
            text = "Week ${day.value.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)}",
            style = TextStyle(
                fontSize = 12.sp,
                fontWeight = FontWeight(600),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )
        )
        Button(
            modifier = modifier
                .defaultMinSize(1.dp, 1.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent
            ),
            contentPadding = PaddingValues(0.dp, 0.dp, 0.dp, 0.dp),
            shape = RoundedCornerShape(0.5f),
            onClick = {
                day.value = day.value.with(IsoFields.WEEK_OF_WEEK_BASED_YEAR, day.value.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)+1L)
                val filter = Filter(
                    day.value.atTime(startOfDay),
                    day.value.plusDays(1L).atTime(startOfDay),
                    10
                )
                update(filter)
            }
        ) {
            Icon(modifier = Modifier.padding(0.dp).height(12.dp), imageVector = Icons.Filled.ArrowForward, contentDescription = "Next", tint = MaterialTheme.colorScheme.onBackground)
        }
    }
}