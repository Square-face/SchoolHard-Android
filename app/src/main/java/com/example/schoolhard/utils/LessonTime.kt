package com.example.schoolhard.utils

import java.time.Duration
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.absoluteValue






/**
 * Get time delta in milliseconds from now to given time.
 *
 * @param time time to get delta to
 * @return time delta in milliseconds
 * */
fun getDeltaToNow(time: LocalDateTime): Long{
    val now = LocalDateTime.now()
    return getDelta(now, time)
}




/**
 * Get time delta in milliseconds from one time to another.
 *
 * @param from start time
 * @param to end time
 *
 * @return time delta in milliseconds
 * */
fun getDelta(from: LocalDateTime, to: LocalDateTime): Long{
    return ChronoUnit.MILLIS.between(from, to)
}





/**
 * Convert time delta in milliseconds to string.
 *
 * Negative deltas will be converted to positive.
 * A maximum of two values are shown at once.
 * for example if the delta is 1 day 2 hours 3 minutes 4 seconds the result will be "1d 2h"
 *
 * @param delta time delta in milliseconds
 * @return time delta in string format
 * */
fun getDeltaString(delta: Long): String{
    val duration = Duration.ofMillis(delta.absoluteValue)

    val days = duration.toDays()
    val hours = (duration.toHours() % 24)
    val minutes = (duration.toMinutes() % 60)
    val seconds = (duration.seconds % 60)

    var output = ""


    if (days > 0) { output += "$days d " }

    if (hours > 0) { output += "$hours h " }

    if (minutes > 0 && days == 0L) { output += "$minutes m " }

    if (seconds > 0 && days == 0L && hours == 0L && minutes < 5) { output += "$seconds s " }


    return output.trim()
}





/**
 * Get lesson progress
 *
 * @param startTime start time of lesson
 * @param now current time
 * @param endTime end time of lesson
 * @return progress of lesson in range [0, 1]
 * */
fun getProgress(
    startTime: LocalDateTime,
    now: LocalDateTime,
    endTime: LocalDateTime
): Float {

    val lessonTime = getDelta(startTime, endTime)
    val passedTime = getDelta(startTime, now)

    if (passedTime < 0){ return 0F }
    if (passedTime > lessonTime){ return 1F }

    return passedTime.toFloat()/lessonTime.toFloat()
}
