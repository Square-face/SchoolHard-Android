package com.example.schoolhard.data

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.schoolhard.ui.components.lesson.Meta
import com.example.schoolhard.ui.components.lesson.Progress
import com.example.schoolhard.ui.components.lesson.Time
import com.example.schoolhard.utils.DeltaFormatter
import com.example.schoolhard.utils.getProgress
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

/**
 * Lesson representation
 *
 * Stores information about a specific occurrence of a subject. Unlike [Occasion] witch
 * represents when in a week a lesson might be scheduled for. This class represents exactly one
 * schedule item.
 *
 * @param occasion Parent occasion
 * @param week Week this lesson is scheduled for
 * @param date Date this lesson is scheduled for
 * @param uuid UUID to use, if null a new uuid will be generated
 *
 * @property id Unique identifier, if [uuid] is not null it will be used, otherwise a new is generated
 * @property subject Parent subject
 * @property name subject name
 * @property location Where the lesson is going to be taking place
 * @property dayOfWeek What day of the week the lesson happens on
 * @property startTime [LocalDateTime] object representing when the lesson starts
 * @property endTime [LocalDateTime] object representing when the lesson ends
 *
 * @property progress Current progress as a float between 0 and 1. Regenerated every time requested
 *
 * @property StartTimeString [startTime] as a string
 * @property EndTimeString [endTime] as a string
 *
 * @property StartTimeChangeDuration Time until [startTime] changes
 * @property EndTimeChangeDuration Time until [endTime] changes
 * */
data class Lesson (
    val occasion: Occasion,
    val week: Int,
    val date: LocalDate,
    val uuid: UUID? = null,
) {



    // generate a new uuid if [uuid] is null
    val id: UUID = uuid.also { uuid }?: run { UUID.randomUUID() }

    val subject = occasion.subject
    val name = subject.name
    val location = occasion.location

    val dayOfWeek = occasion.dayOfWeek
    val startTime = occasion.startTime.atDate(date)
    val endTime = occasion.endTime.atDate(date)
    val duration = Duration.between(startTime, endTime).toMillis()

    // generated on request
    val progress: Float get() { return getProgress(startTime, LocalDateTime.now(), endTime) }

    val StartTimeString: String get() {
        return DeltaFormatter.getDurationString(startTime)
    }

    val EndTimeString: String get() {
        return DeltaFormatter.getDurationString(endTime)
    }

    val StartTimeChangeDuration: Duration get() {
        return DeltaFormatter.nextChange(startTime)
    }

    val EndTimeChangeDuration: Duration get() {
        return DeltaFormatter.nextChange(endTime)
    }


    /**
     * Large lesson view model
     * */
    @Composable
    fun Large(modifier: Modifier = Modifier) {

        var progress by remember { mutableStateOf(Progress(progress)) }
        var time by remember { mutableStateOf(Time(this)) }
        val meta = Meta(this)
        val lesson = this

        // Update progress and time every second
        LaunchedEffect(this) {
            while(true) {
                progress = Progress(lesson.progress)
                time = Time(lesson)
                delay(1000)
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



    /**
     * Medium sized lesson view model
     *
     * ===== Progress =====
     *     Name | Location
     * Duration | OneLineInfo
     * */
    @Composable
    fun Medium(modifier: Modifier = Modifier) {

        var progress by remember { mutableStateOf(Progress(progress)) }
        var time by remember { mutableStateOf(Time(this)) }
        val meta = Meta(this)
        val lesson = this

        // Update progress and time every second
        LaunchedEffect(lesson) {
            while(true) {
                progress = Progress(lesson.progress)
                time = Time(lesson)
                delay(1000)
            }
        }

        Column(
            modifier = modifier
                .fillMaxWidth()
                .height(55.dp)
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

                Row(
                    modifier = Modifier
                        .fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        meta.Name()
                        time.Duration(weight = 400)
                    }

                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        meta.Location()
                        time.OneLineInfo()
                    }
                }
            }
        }
    }





    /**
     * Thin lesson view model
     * */
    @Composable
    fun Thin(modifier: Modifier = Modifier) {

        var time by remember { mutableStateOf(Time(this)) }
        val lesson = this

        // Update progress and time every second
        LaunchedEffect(this) {
            while(true) {
                time = Time(lesson)
                delay(1000)
            }
        }

        Box(
            modifier = modifier
                .height(30.dp)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.secondary, RoundedCornerShape(15.dp))
                .clip(RoundedCornerShape(15.dp)),
            contentAlignment = Alignment.CenterStart
        ){
            if (lesson.progress == 1f) {
                // If lesson is over, reduce the alpha layer
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(lesson.progress)
                        .background(Color(0x7013B013))
                )
            } else {

                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(lesson.progress)
                        .background(Color(0xFF13B013))
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Layout(
                    content = {
                        Text(
                            text = lesson.name,
                            fontSize = 15.sp,
                            fontWeight = FontWeight(400),
                            modifier = Modifier,
                            color = MaterialTheme.colorScheme.background,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        time.OneLineInfo()
                    }
                ) { measurables, constraints ->
                    // Measure the second element
                    val placeable2 = measurables[1].measure(constraints)

                    // Measure the first element with the remaining width
                    val remainingWidth = constraints.maxWidth - placeable2.width
                    val placeable1 = measurables[0].measure(constraints.copy(maxWidth = remainingWidth))

                    // Define the layout
                    layout(constraints.maxWidth, maxOf(placeable1.height, placeable2.height)) {
                        // Place the first element at the start
                        placeable1.placeRelative(0, 0)

                        // Place the second element at the end
                        placeable2.placeRelative(constraints.maxWidth - placeable2.width, 0)
                    }
                }
            }
        }
    }
}