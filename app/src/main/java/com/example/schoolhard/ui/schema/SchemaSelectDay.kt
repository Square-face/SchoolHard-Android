package com.example.schoolhard.ui.schema

import android.annotation.SuppressLint
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
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.schoolhard.R
import com.example.schoolhard.ui.theme.SchoolHardTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoField
import java.time.temporal.IsoFields

@Composable
fun DayInfo(modifier: Modifier = Modifier, update: (LocalDate) -> Unit){
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
fun DayOfWeekSelect(modifier: Modifier = Modifier, day: MutableState<LocalDate>, update: (LocalDate) -> Unit){
    val days: Array<String> = stringArrayResource(id = R.array.days)
    Row(
        modifier = modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        WeekDay(day = day, text = days[0], value = 1, update = update)
        WeekDay(day = day, text = days[1], value = 2, update = update)
        WeekDay(day = day, text = days[3], value = 3, update = update)
        WeekDay(day = day, text = days[4], value = 4, update = update)
        WeekDay(day = day, text = days[5], value = 5, update = update)
    }
}

@Composable
fun DateAndWeek(modifier: Modifier = Modifier, day: MutableState<LocalDate>, update: (LocalDate) -> Unit){
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
fun WeekDay(modifier: Modifier = Modifier, day: MutableState<LocalDate>, text: String, value: Int, update: (LocalDate) -> Unit){
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
            update(day.value)
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
fun Week(modifier: Modifier = Modifier, day: MutableState<LocalDate>, update: (LocalDate) -> Unit){
    Row(
        modifier = modifier
            .padding(0.dp)
            .defaultMinSize(1.dp, 1.dp)
            .height(15.dp)
        ,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Button(
            modifier = modifier
                .defaultMinSize(1.dp, 1.dp)
                .padding(0.dp)
            ,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent
            ),
            contentPadding = PaddingValues(0.dp, 0.dp, 0.dp, 0.dp),
            shape = RoundedCornerShape(0.5f),
            onClick = {
                day.value = day.value.with(IsoFields.WEEK_OF_WEEK_BASED_YEAR, day.value.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)-1L)
                update(day.value)
            }
        ) {
            Icon(
                modifier = Modifier
                    .padding(0.dp)
                    .height(14.dp)
                    .width(14.dp),
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = stringResource(
                    id = R.string.previous
                ),
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
        Text(
            text = stringResource(id = R.string.week, day.value.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)),
            style = TextStyle(
                fontSize = 12.sp,
                fontWeight = FontWeight(600),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )
        )
        Button(
            modifier = modifier
                .defaultMinSize(1.dp, 1.dp)
                .padding(0.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent
            ),
            contentPadding = PaddingValues(0.dp, 0.dp, 0.dp, 0.dp),
            shape = RoundedCornerShape(0.5f),
            onClick = {
                day.value = day.value.with(IsoFields.WEEK_OF_WEEK_BASED_YEAR, day.value.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)+1L)
                update(day.value)
            }
        ) {
            Icon(
                modifier = Modifier
                    .padding(0.dp)
                    .height(14.dp),
                imageVector = Icons.Filled.ArrowForward,
                contentDescription = stringResource(
                    id = R.string.next
                ),
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@SuppressLint("UnrememberedMutableState")
@Composable
@Preview
fun PreviewWeek(){
    SchoolHardTheme {
        Week(day = mutableStateOf(LocalDate.now()), update={})
    }
}


@SuppressLint("UnrememberedMutableState")
@Composable
@Preview
fun PreviewDays(){
    SchoolHardTheme {
        DayOfWeekSelect(day= mutableStateOf(LocalDate.now()), update={})
    }
}
