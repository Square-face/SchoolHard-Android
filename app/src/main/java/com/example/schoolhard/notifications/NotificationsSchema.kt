package com.example.schoolhard.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.example.schoolhard.R
import com.example.schoolhard.data.Lesson
import com.example.schoolhard.utils.DeltaFormatter
import java.time.format.DateTimeFormatter

class NotificationsSchema {

    class PersistentNotification {
        private val channel = Channel.STATUS

        fun createLessonNotification(context: Context, lesson: Lesson): NotificationCompat.Builder {
            return NotificationCompat.Builder(context, channel.channelId)
                .setChannelId(channel.channelId)
                .setContentTitle(lesson.name)
                .setContentText("${lesson.location} ${lesson.progress*100}% \n${lesson.startTime} - ${lesson.endTime}")
                .setSmallIcon(R.drawable.schoolhard_logo)
        }

        fun createRecessNotification(context: Context, next: Lesson): NotificationCompat.Builder {
            val format = DateTimeFormatter.ofPattern("HH:mm")

            return NotificationCompat.Builder(context, channel.channelId)
                .setChannelId(channel.channelId)
                .setContentTitle(DeltaFormatter.getDurationString(next.startTime, expanded = true))
                .setContentText(next.name)
                .setSubText("${next.location.name} ${next.startTime.format(format)} - ${next.endTime.format(format)}")
                .setShowWhen(false)
                .setSmallIcon(R.drawable.schoolhard_logo)
        }
    }

    /**
     * Enum for every channel that might be used by the application.
     * */
    enum class Channel(val channelId: String, val channelName: CharSequence, val channelDescription: String) {
        STATUS(
            "1",
            "Persistent",
            "Always visible notification showing schedule information and updates automatically"
        ),

        LESSON_START(
            "2",
            "Lesson start",
            "Notifications sent when a lesson starts"
        ),
        LESSON_END(
            "3",
            "Lesson end",
            "Notifications sent when a lesson ends"
        ),

        NEXT_LESSON(
            "4",
            "Next lesson",
            "Notifications sent when a lesson is about to start"
        ),
        ENDING_LESSON(
            "5",
            "Ending lesson",
            "Notifications sent when a lesson is about to end"
        ),

        RECESS_START(
            "6",
            "Recess start",
            "Notifications sent when a recess starts"
        ),
        RECESS_END(
            "7",
            "Recess end",
            "Notifications sent when a recess ends"
        ),

        DAY_OFF(
            "8",
            "Day off",
            "Notifications sent when there is no school on a day it usually is"
        );


        /**
         * Create a [NotificationChannel] object from the enum parameters
         * */
        private fun getChannel(): NotificationChannel {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
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