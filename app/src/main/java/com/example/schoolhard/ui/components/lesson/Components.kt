package com.example.schoolhard.ui.components.lesson

import androidx.annotation.IntRange
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.schoolhard.API.Lesson
import com.example.schoolhard.utils.getDelta
import com.example.schoolhard.utils.getDeltaString
import kotlinx.coroutines.delay
import java.time.Duration
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
class Progress(private val progress: Float) {

    /**
     * Helper function for calculating a usable radius value.
     * the output is the same as [base] until progress goes above 0.95, then it starts decreasing to 0
     *
     * @param base Base radius
     * @return radius
     * */
    private fun calculateRadius(base: Int): Int {
        if (progress < 0.95) { return base }

        // stretch progress from 0.95 - 1 to 0 - 1
        val delta = progress - 0.95
        val stretched = delta / 0.05

        // calculate radius
        return base - (stretched * base).roundToInt()
    }


    /**
     * A thinner lesson view that only shows name and a string representation on the status.
     * */
    @Composable
    fun Thin(
        modifier: Modifier = Modifier,
        height: Int = 8,
        background: Color = Color(0xFF707070),
        fullyRounded: Boolean = false
    ) {

        val radius = if (fullyRounded) height/2 else calculateRadius(height/2)
        val rounding = if (fullyRounded) RoundedCornerShape(radius.dp) else RoundedCornerShape(0.dp)

        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(height.dp)
                .background(background, rounding)
                .clip(rounding)
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
     * @param deltaPos Where to position the delta. (0, 1, 2, 3) => (left, top, right, bottom).
     * */
    @Composable
    fun StartTime(
        modifier: Modifier = Modifier,
        showDelta: Boolean = true,
        showDeltaIfPassed: Boolean = false,
        @IntRange(from=0, to=3) deltaPos: Int = 0
    ) {
        RawTimeWithDelta(
            modifier = modifier,
            time = lesson.startTime,
            showDelta = showDelta,
            showDeltaIfPassed = showDeltaIfPassed,
            deltaPos=deltaPos
        )
    }





    /**
     * Display lesson end time
     *
     * @param showDelta if the delta should be displayed
     * @param showDeltaIfPassed if the delta should be displayed after the relevant time has passed.
     * Only relevant if [showDelta] is true
     * */
    @Composable
    fun EndTime(modifier: Modifier = Modifier,
                showDelta: Boolean = true,
                showDeltaIfPassed: Boolean = false,
                deltaPos: Int = 0) {
        RawTimeWithDelta(
            modifier = modifier,
            time = lesson.endTime,
            showDelta = showDelta,
            showDeltaIfPassed = showDeltaIfPassed,
            deltaPos = deltaPos
        )
    }





    /**
     * Display lesson duration
     * */
    @Composable
    fun Duration(modifier: Modifier = Modifier, weight: Int = 500){
        RawDelta(
            modifier = modifier,
            delta = lesson.duration,
            fontSize = 17.sp,
            fontWeight = weight,
        )
    }


    /**
     * A text view that displays how long until a lesson starts, ends or how long ago it occurred
     * */
    @Composable
    fun OneLineInfo(modifier: Modifier = Modifier) {
        val now = LocalDateTime.now()

        // lesson hasn't happened yet
        if (lesson.startTime.isAfter(now)) { TimeUntil(modifier = modifier); return }

        // lesson has ended
        if (lesson.endTime.isBefore(now) && lesson.endTime.plusMinutes(10).isAfter(now)) { TimeAgo(modifier = modifier); return }

        // lesson is happening right now
        if (lesson.startTime.isBefore(now) && lesson.endTime.isAfter(now)) { TimeLeft(modifier = modifier); return }

        Text(text = "")
    }





    /**
     * How long ago a lesson ended
     * */
    @Composable
    fun TimeAgo(modifier: Modifier = Modifier) {
        val delta = getDelta(LocalDateTime.now(), lesson.endTime)
        Text(
            text = "${getDeltaString(delta)} ago",
            modifier = modifier,
            fontSize = 14.sp,
            fontWeight = FontWeight(500),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }





    /**
     * How much time until a lesson starts in plaintext
     * */
    @Composable
    fun TimeUntil(modifier: Modifier = Modifier) {
        val delta = getDelta(LocalDateTime.now(), lesson.startTime)
        Text(
            text = "In ${getDeltaString(delta)}",
            modifier = modifier,
            fontSize = 14.sp,
            fontWeight = FontWeight(500),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }





    /**
     * How much time is left of a lesson in plaintext
     * */
    @Composable
    fun TimeLeft(modifier: Modifier = Modifier) {
        val delta = getDelta(LocalDateTime.now(), lesson.endTime)
        Text(
            text = "${getDeltaString(delta)} left",
            modifier = modifier,
            fontSize = 14.sp,
            fontWeight = FontWeight(500),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
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
    private fun RawTimeWithDelta(
        modifier: Modifier = Modifier,
        time: LocalDateTime,
        showDelta: Boolean = true,
        showDeltaIfPassed: Boolean = false,
        deltaPos: Int
    ) {

        val delta = getDelta(LocalDateTime.now(), time)

        if (!checkDelta(delta, showDelta, showDeltaIfPassed)) { return RawTime(time = time) }

        return when (deltaPos) {
            0 -> LeftDelta(modifier=modifier, delta = delta) { RawTime(time = time) }
            1 -> TopDelta(modifier=modifier, delta = delta) { RawTime(time = time) }
            2 -> RightDelta(modifier=modifier, delta = delta) { RawTime(time = time) }
            3 -> BottomDelta(modifier=modifier, delta = delta) { RawTime(time = time) }
            else -> {}
        }
    }

    @Composable
    fun LeftDelta(
        modifier: Modifier = Modifier,
        delta: Long,
        rawTime: @Composable() (RowScope.() -> Unit)
    ) {
        Row(
            modifier=modifier,
            verticalAlignment = Alignment.CenterVertically
        ) {

            RawDelta(delta = delta, fontWeight = 500)
            Spacer(modifier = Modifier.width(4.dp))
            rawTime()
        }
    }

    @Composable
    fun TopDelta(
        modifier: Modifier = Modifier,
        delta: Long,
        rawTime: @Composable() (RowScope.() -> Unit)
    ) {
        Row(
            modifier=modifier,
            verticalAlignment = Alignment.CenterVertically
        ) {

            RawDelta(delta = delta, fontWeight = 500)
            Spacer(modifier = Modifier.width(4.dp))
            rawTime()
        }
    }

    @Composable
    fun RightDelta(
        modifier: Modifier = Modifier,
        delta: Long,
        rawTime: @Composable() (RowScope.() -> Unit)
    ) {
        Row(
            modifier=modifier,
            verticalAlignment = Alignment.CenterVertically
        ) {

            rawTime()
            Spacer(modifier = Modifier.width(8.dp))
            RawDelta(delta = delta, fontWeight = 500)
        }
    }

    @Composable
    fun BottomDelta(
        modifier: Modifier = Modifier,
        delta: Long,
        rawTime: @Composable() (RowScope.() -> Unit)
    ) {
        Row(
            modifier=modifier,
            verticalAlignment = Alignment.CenterVertically
        ) {

            rawTime()
            Spacer(modifier = Modifier.width(4.dp))
            RawDelta(delta = delta, fontWeight = 500)
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
            fontSize = 17.sp,
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
        fontSize: TextUnit = 14.sp,
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



class Recess(private val start: LocalDateTime, end: LocalDateTime) {

    private val duration = Duration.between(start, end)

    // gray
    private val color = Color(0xFF707070)

    /**
     * Display recess duration as a thin line with a string representation of the duration
     * */
    @Composable
    fun Line(modifier: Modifier = Modifier) {

        // don't show if duration is 0
        if (duration.isZero) { return }


        // progress bars
        var progress by remember { mutableStateOf(getDelta(start, LocalDateTime.now()).toFloat() / duration.toMillis()) }
        val progress1 = Progress((progress*2).coerceIn(0f, 1f))
        val progress2 = Progress(((progress*2)-1f).coerceIn(0f, 1f))

        LaunchedEffect(key1 = true) {
            while (true) {
                progress = getDelta(start, LocalDateTime.now()).toFloat() / duration.toMillis()
                delay(1000)
            }
        }

        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // progress bar representing half the recess
            progress1.Thin(modifier = Modifier.weight(1f), height = 2, background = color, fullyRounded = true)

            Text(
                text = getDeltaString(duration.toMillis()),
                color = color
            )

            // progress bar representing half the recess
            progress2.Thin(modifier = Modifier.weight(1f), height = 2, background = color, fullyRounded = true)
        }

    }


    companion object {
        fun from(before: Lesson, after: Lesson): Recess {
            return Recess(before.endTime, after.startTime)
        }
    }

}

