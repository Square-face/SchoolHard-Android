package com.example.schoolhard.utils

import java.time.Duration
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.absoluteValue

fun getDeltaToNow(time: LocalDateTime): Long{
    /**Get time delta in milliseconds from [time] to now
     * */
    val now = LocalDateTime.now()
    return getDelta(now, time)
}

fun getDelta(from: LocalDateTime, to: LocalDateTime): Long{
    return ChronoUnit.MILLIS.between(from, to)
}



/**
 * convert time delta in milliseconds to string
 * negative deltas will be converted to positive
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

    if (days > 0) {
        output += "$days d "
    }
    if (hours > 0) {
        output += "$hours h "
    }
    if (minutes > 0 && days == 0L) {
        output += "$minutes m "
    }
    if (seconds > 0 && days == 0L && hours == 0L && minutes < 5) {
        output += "$seconds s "
    }

    return output.trim()
}




fun getProgress(startTime: LocalDateTime, now: LocalDateTime, endTime: LocalDateTime): Float {
    val lessonTime = getDelta(startTime, endTime)
    val passedTime = getDelta(startTime, now)

    if (passedTime < 0){
        return 0F
    }
    if (passedTime > lessonTime){
        return 1F
    }
    return passedTime.toFloat()/lessonTime.toFloat()
}