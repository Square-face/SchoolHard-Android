package com.example.schoolhard.utils

import android.util.Log
import java.time.Duration
import java.time.LocalDateTime

object DeltaFormatter {
    fun getDurationString(time: LocalDateTime, now: LocalDateTime = LocalDateTime.now(), expanded: Boolean = false): String {
        val duration = Duration.between(now, time)

        val days = duration.toDays()
        val hours = duration.toHours() % 24
        val minutes = duration.toMinutes() % 60
        val seconds = duration.seconds % 60

        if (expanded) {
            return when {
                days > 0 ->     "${days}days ${hours}hours"
                hours > 0 ->    "${hours}hours ${minutes}minutes"
                minutes >= 5 -> "${minutes}minutes"
                else ->         "${seconds}seconds"
            }
        }

        return when {
            days > 0 ->     "${days}d ${hours}h"
            hours > 0 ->    "${hours}h ${minutes}m"
            minutes >= 5 -> "${minutes}m"
            else ->         "${seconds}s"
        }
    }

    fun nextChange(time: LocalDateTime, now: LocalDateTime = LocalDateTime.now()): Duration {
        val duration = Duration.between(now, time)

        val days = duration.toDays()
        val hours = duration.toHours() % 24
        val minutes = duration.toMinutes() % 60
        val seconds = duration.seconds % 60

        Log.v("DeltaFormatter", "days: $days, hours: $hours, minutes: $minutes, seconds: $seconds")

        return when {
            days > 0  ->    Duration.ofMinutes(minutes).plusSeconds(seconds)
            hours > 0 ->    Duration.ofSeconds(seconds)
            minutes >= 5 -> Duration.ofSeconds(seconds)
            else ->         Duration.ofSeconds(1)
        }
    }

}