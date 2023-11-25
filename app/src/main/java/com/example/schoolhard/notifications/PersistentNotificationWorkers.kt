package com.example.schoolhard.notifications

import android.app.NotificationManager
import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.schoolhard.API.Lesson
import com.example.schoolhard.API.SchoolSoft.SchoolSoftAPI
import com.example.schoolhard.data.Logins
import com.example.schoolhard.database.Database

/**
 * Notification worker for handling the persistent notification during a lesson
 *
 * Updates the notification
 * */
class PersistentWorker(context: Context, params: WorkerParameters): Worker(context, params) {

    fun updateLessonNotification(manager: NotificationManager, lesson: Lesson) {
        manager.notify(
            1,
            NotificationsSchema.PersistentNotification().createLessonNotification(
                applicationContext,
                lesson
            ).build()
        )
    }

    fun updateRecessNotification(manager: NotificationManager, next: Lesson) {
        manager.notify(
            1,
            NotificationsSchema.PersistentNotification().createRecessNotification(
                applicationContext,
                next
            ).build()
        )
    }

    override fun doWork(): Result {

        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val store = applicationContext.getSharedPreferences("logins", Context.MODE_PRIVATE)
        val logins = Logins(store)

        val api = SchoolSoftAPI()
        api.loadLogin(logins)

        val database = Database(applicationContext, null)

        database.currentLesson()?.let { updateLessonNotification(notificationManager, it); return Result.success() }
        database.nextLesson()?.let { updateRecessNotification(notificationManager, it); return Result.success() }

        return Result.success()
    }
}