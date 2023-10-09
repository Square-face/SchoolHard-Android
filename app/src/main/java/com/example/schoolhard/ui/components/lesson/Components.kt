package com.example.schoolhard.ui.components.lesson

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.schoolhard.API.Lesson
import com.example.schoolhard.utils.getDelta
import com.example.schoolhard.utils.getDeltaString
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

/**
 * Lesson view components
 * */



/**
 * Progress view
 *
 * Holds different variations that show progression
 *
 * @param progress current progress as a float between 0 and 1
 * */
class Progress(val progress: Float) {

    /**
     * Thin progress view
     * */
    @Composable
    fun Thin(modifier: Modifier = Modifier) {
        val radius = if (progress < 0.9) 4 else ((1F-progress)*4).roundToInt()

        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(Color(0xFF707070))
        ){
            Box(
                modifier = modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress)
                    .background(Color(0xFF139013), RoundedCornerShape(radius.dp)) // gray
            )
        }
    }
}





/**
 * View options for meta data
 * 
 * name and location
 * */
class Meta(val lesson: Lesson) {



    /**
     * Raw Text display of lesson name
     * */
    @Composable
    fun Name(modifier: Modifier = Modifier, maxLines: Int = 1) {
        Text(
            text = lesson.name,
            fontSize = 14.sp,
            fontWeight = FontWeight(600),
            modifier = modifier,
            color = MaterialTheme.colorScheme.background,
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis
        )
    }



    /**
     * Raw text display of lesson location name
     * */
    @Composable
    fun Location(modifier: Modifier = Modifier) {
        Text(
            text = lesson.location.name,
            modifier = modifier,
            color = MaterialTheme.colorScheme.background,
            overflow = TextOverflow.Ellipsis,
            softWrap = false,
            maxLines = 1
        )
    }
}


/**
 * View options for time related fields of a lesson
 *
 * @param lesson Lesson to use for fields
 * */
class Time(val lesson: Lesson) {





    /**
     * Display lesson start time
     *
     * @param showDelta if the delta should be displayed
     * @param showDeltaIfPassed if the delta should be displayed after the relevant time has passed.
     * Only relevant if [showDelta] is true
     * */
    @Composable
    fun StartTime(modifier: Modifier = Modifier, showDelta: Boolean = true, showDeltaIfPassed: Boolean = false) {
        RawTimeWithDelta(modifier = modifier, time = lesson.startTime, showDelta = showDelta, showDeltaIfPassed = showDeltaIfPassed)
    }





    /**
     * Display lesson end time
     *
     * @param showDelta if the delta should be displayed
     * @param showDeltaIfPassed if the delta should be displayed after the relevant time has passed.
     * Only relevant if [showDelta] is true
     * */
    @Composable
    fun EndTime(modifier: Modifier = Modifier, showDelta: Boolean = true, showDeltaIfPassed: Boolean = false) {
        RawTimeWithDelta(modifier = modifier, time = lesson.endTime, showDelta = showDelta, showDeltaIfPassed = showDeltaIfPassed)
    }





    /**
     * Display lesson duration
     * */
    @Composable
    fun Duration(modifier: Modifier = Modifier){
        RawDelta(
            modifier = modifier,
            delta = lesson.duration,
            fontSize = 17.sp,
            fontWeight = 500,
        )
    }





    /**
     * Raw Time formatted as hh:mm with optional delta
     *
     * @param time Display time
     * @param showDelta if the delta from now to [time] should be displayed
     * @param showDeltaIfPassed if the delta should be displayed after the relevant time has passed.
     * Only relevant if [showDelta] is true
     * */
    @Composable
    private fun RawTimeWithDelta(modifier: Modifier = Modifier, time: LocalDateTime, showDelta: Boolean = true, showDeltaIfPassed: Boolean = false) {

        val delta = getDelta(LocalDateTime.now(), time)

        Row(
            modifier=modifier,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (checkDelta(delta, showDelta, showDeltaIfPassed)) {
                RawDelta(delta = delta, fontWeight = 500)
            }
            Spacer(modifier = Modifier.width(4.dp))
            RawTime(time = time)
        }
    }





    /**
     * Raw time formatted as hh:mm
     *
     * @param time Display time
     * */
    @Composable
    private fun RawTime(modifier: Modifier = Modifier, time: LocalDateTime) {
        val format = DateTimeFormatter.ofPattern("HH:mm")
        Text(
            text = time.format(format),
            modifier = modifier,
            color = MaterialTheme.colorScheme.background,
            overflow = TextOverflow.Ellipsis,
            softWrap = false,
            maxLines = 1
        )
    }



    /**
     * Raw time delta formatted to a string
     *
     * @param delta delta in milliseconds
     * */
    @Composable
    private fun RawDelta(
        modifier: Modifier = Modifier,
        delta: Long,
        fontSize: TextUnit = 12.sp,
        fontWeight: Int = 700,
    ) {
        Text(
            text = getDeltaString(delta),
            modifier = modifier,
            fontSize = fontSize,
            fontWeight = FontWeight(fontWeight),
            color = MaterialTheme.colorScheme.background,
            overflow = TextOverflow.Ellipsis,
            softWrap = false,
            maxLines = 1,
        )
    }


    /**
     * Get a easy to use boolean value from different delta configs
     *
     * @param delta Delta value
     * @param showDelta if the delta should be displayed in the first place
     * @param showDeltaIfPassed if the delta should be displayed after the relevant time has passed.
     * Only relevant if [showDelta] is true
     *
     * @return If the delta should be displayed or not
     * */
    private fun checkDelta(delta: Long, showDelta: Boolean, showDeltaIfPassed: Boolean): Boolean {
        if (!showDelta) return false
        if (delta >= 0) return true
        if (showDeltaIfPassed) return true
        return false
    }


}

