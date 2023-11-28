package com.example.schoolhard.notifications

import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.schoolhard.data.Lesson
import com.example.schoolhard.API.SchoolSoft.SchoolSoftAPI
import com.example.schoolhard.stores.Logins
import com.example.schoolhard.database.Database
import com.example.schoolhard.utils.DeltaFormatter

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
            )
                .build()
        )
    }

    override fun doWork(): Result {

        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val store = applicationContext.getSharedPreferences("logins", Context.MODE_PRIVATE)
        val logins = Logins(store)

        val api = SchoolSoftAPI()
        api.loadLogin(logins)

        val database = Database(applicationContext, null)

        database.currentLesson()?.let {
            updateLessonNotification(notificationManager, it)
            val duration = DeltaFormatter.nextChange(it.endTime)
            val fixedDuration = (duration.isZero).let { if (it) duration.plusSeconds(1) else duration }
            Log.v("PersistentWorker", "active lesson, next change is in ${duration.seconds}s")

            val notificationWorker = OneTimeWorkRequestBuilder<PersistentWorker>()
                .setInitialDelay(fixedDuration)
                .build()

            WorkManager.getInstance(applicationContext).enqueueUniqueWork(
                "persistent", // Make sure there is only one notification worker running
                ExistingWorkPolicy.REPLACE, // Keep the old worker if there is one
                notificationWorker
            )

            return Result.success()
        }

        database.nextLesson()?.let {
            updateRecessNotification(notificationManager, it)
            val duration = DeltaFormatter.nextChange(it.startTime)
            val fixedDuration = (duration.isZero).let { if (it) duration.plusSeconds(1) else duration }
            Log.v("PersistentWorker", "In recess, next change is in ${duration.seconds}s")

            val notificationWorker = OneTimeWorkRequestBuilder<PersistentWorker>()
                .setInitialDelay(fixedDuration)
                .build()

            WorkManager.getInstance(applicationContext).enqueueUniqueWork(
                "persistent", // Make sure there is only one notification worker running
                ExistingWorkPolicy.REPLACE, // Keep the old worker if there is one
                notificationWorker
            )

            return Result.success()
        }

        return Result.failure()
    }
}