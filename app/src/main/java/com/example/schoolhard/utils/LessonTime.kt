package com.example.schoolhard.utils

import java.util.Date
import kotlin.math.floor

const val MillisInSec   = (1000)
const val MillisInMin   = (MillisInSec*60)
const val MillisInHour  = (MillisInMin*60)
const val MillisInDay   = (MillisInHour*24)

fun getDeltaToNow(time: Date): Long{
    /**Get time delta in milliseconds from [time] to now
     * */
    val now = Date()
    return getDelta(now, time)
}

fun getDelta(from: Date, to: Date): Long{
    return to.time - from.time
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

fun getProgress(startTime: Date, now: Date, endTime: Date): Float {
    val lessonTime = endTime.time - startTime.time
    val passedTime = now.time - startTime.time

    if (passedTime < 0){
        return 0F
    }
    if (passedTime > lessonTime){
        return 1F
    }
    return passedTime.toFloat()/lessonTime.toFloat()
}