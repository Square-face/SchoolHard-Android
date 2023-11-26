package com.example.schoolhard.utils

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
                days > 1 &&
                        hours == 1L ->      "$days days and $hours hour"
                days > 1 ->                 "$days days and $hours hours"
                days > 0 &&
                        hours == 1L ->      "$days day and $hours hour"
                days > 0 ->                 "$days day and $hours hours"
                hours > 1 &&
                        minutes == 1L ->    "$hours hours and $minutes minute"
                hours > 1 ->                "$hours hours and $minutes minutes"
                hours > 0 &&
                        minutes == 1L ->    "$hours hour and $minutes minute"
                hours > 0 ->                "$hours hour and $minutes minutes"
                minutes >= 5 ->             "$minutes minutes"
                minutes > 1 &&
                        seconds == 1L ->    "$minutes minutes $seconds second"
                minutes > 1 ->              "$minutes minutes $seconds seconds"
                minutes > 0 &&
                        seconds == 1L ->    "$minutes minute $seconds second"
                minutes > 0 ->              "$minutes minute $seconds seconds"
                seconds > 1 ->              "$seconds seconds"
                else ->                     "$seconds second"
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

        return when {
            days > 0  ->    Duration.ofMinutes(minutes).plusSeconds(seconds)
            hours > 0 ->    Duration.ofSeconds(seconds)
            minutes >= 5 -> Duration.ofSeconds(seconds)
            else ->         Duration.ofSeconds(1)
        }
    }

}