package com.example.schoolhard.utils

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.Date
import kotlin.math.floor

const val MillisInSec   = (1000)
const val MillisInMin   = (MillisInSec*60)
const val MillisInHour  = (MillisInMin*60)
const val MillisInDay   = (MillisInHour*24)

fun getDeltaToNow(time: LocalDateTime): Long{
    /**Get time delta in milliseconds from [time] to now
     * */
    val now = LocalDateTime.now()
    return getDelta(now, time)
}

fun getDelta(from: LocalDateTime, to: LocalDateTime): Long{
    return ChronoUnit.MILLIS.between(from, to)
}

fun getDeltaString(t: Long): String{
    var time = t

    val days = floor((time / MillisInDay).toDouble()).toInt()
    time -= (days * MillisInDay).toLong()

    val hours = floor((time / MillisInHour).toDouble()).toInt()
    time -= (hours * MillisInHour).toLong()

    val minutes = floor((time / MillisInMin).toDouble()).toInt()
    time -= (minutes * MillisInMin).toLong()

    val seconds = floor((time / MillisInSec).toDouble()).toInt()
    time -= (seconds * MillisInSec).toLong()

    if (days==0 && hours == 0 && minutes == 0){
        if (seconds == 0){return "now!"}
        return "$seconds sec"
    }
    if (days==0 && hours == 0 && minutes == 1){
        return "60 sec"
    }

    if (days > 1){
        return "$days days"
    }

    var result = ""

    if (days != 0){
        result += "$days days "
    }
    if (hours != 0){
        result += "$hours h "
    }
    if (minutes != 0){
        result += "$minutes min "
    }

    return result.dropLast(1)
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