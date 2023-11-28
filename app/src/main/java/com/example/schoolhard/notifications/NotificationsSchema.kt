package com.example.schoolhard.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.example.schoolhard.R
import com.example.schoolhard.data.Lesson
import com.example.schoolhard.utils.DeltaFormatter
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class NotificationsSchema {

    class PersistentNotification {
        private val channel = Channel.STATUS

        fun createLessonNotification(context: Context, lesson: Lesson): NotificationCompat.Builder {
            val format = DateTimeFormatter.ofPattern("HH:mm")

            return NotificationCompat.Builder(context, channel.channelId)
                .setChannelId(channel.channelId)
                .setContentTitle(DeltaFormatter.getDurationString(lesson.endTime, expanded = true))
                .setSubText("${lesson.startTime.format(format)} - ${lesson.endTime.format(format)}")
                .setContentText("${lesson.location.name} - ${lesson.name}")
                .setSmallIcon(R.drawable.schoolhard_logo)
                .setShowWhen(false)
                .setOnlyAlertOnce(true)
                .setOngoing(true)
        }

        fun createRecessNotification(context: Context, next: Lesson): NotificationCompat.Builder {

            return NotificationCompat.Builder(context, channel.channelId)
                .setChannelId(channel.channelId)
                .setContentTitle("Lesson in ${DeltaFormatter.getDurationString(next.startTime, expanded = true)}")
                .setContentText(next.name)
                .setSubText(next.location.name)
                .setWhen(next.startTime.toEpochSecond(ZoneOffset.of(ZoneId.systemDefault().rules.getOffset(Instant.now()).id))*1000)
                .setUsesChronometer(false)
                .setOnlyAlertOnce(true)
                .setOngoing(true)
                .setSmallIcon(R.drawable.schoolhard_logo)
        }
    }

    /**
     * Enum for every channel that might be used by the application.
     * */
    enum class Channel(val channelId: String, val channelName: CharSequence, val channelDescription: String, val importance: Int) {
        STATUS(
            "1",
            "Persistent",
            "Always visible notification showing schedule information and updates automatically",
            NotificationManager.IMPORTANCE_HIGH
        ),

        LESSON_START(
            "2",
            "Lesson start",
            "Notifications sent when a lesson starts",
            NotificationManager.IMPORTANCE_DEFAULT
        ),
        LESSON_END(
            "3",
            "Lesson end",
            "Notifications sent when a lesson ends",
            NotificationManager.IMPORTANCE_DEFAULT
        ),

        NEXT_LESSON(
            "4",
            "Next lesson",
            "Notifications sent when a lesson is about to start",
            NotificationManager.IMPORTANCE_DEFAULT
        ),
        ENDING_LESSON(
            "5",
            "Ending lesson",
            "Notifications sent when a lesson is about to end",
            NotificationManager.IMPORTANCE_DEFAULT
        ),

        RECESS_START(
            "6",
            "Recess start",
            "Notifications sent when a recess starts",
            NotificationManager.IMPORTANCE_DEFAULT
        ),
        RECESS_END(
            "7",
            "Recess end",
            "Notifications sent when a recess ends",
            NotificationManager.IMPORTANCE_DEFAULT
        ),

        DAY_OFF(
            "8",
            "Day off",
            "Notifications sent when there is no school on a day it usually is",
            NotificationManager.IMPORTANCE_DEFAULT
        );


        /**
         * Create a [NotificationChannel] object from the enum parameters
         * */
        private fun getChannel(): NotificationChannel {
            return NotificationChannel(channelId, channelName, importance).apply {
                description = description
            }
        }

        /**
         * Creates a notification channel
         *
         * @param manager [NotificationManager] to create the channel with
         * */
        fun createChannel(manager: NotificationManager) {
            manager.createNotificationChannel(getChannel())
        }

        companion object {
            /**
             * Shorthand for initializing all notification channels
             *
             * @param manager [NotificationManager] to create the channels with
             * */
            fun createAll(manager: NotificationManager) {
                values().forEach {
                    it.createChannel(manager)
                }
            }
        }
    }
}