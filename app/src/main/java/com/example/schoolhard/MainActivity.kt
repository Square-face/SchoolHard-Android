package com.example.schoolhard

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.core.view.WindowCompat
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.schoolhard.API.SchoolSoft.SchoolSoftAPI
import com.example.schoolhard.data.Logins
import com.example.schoolhard.database.Database
import com.example.schoolhard.ui.SchoolHardApp
import java.time.Duration
import java.util.UUID
import java.util.concurrent.TimeUnit

class RefreshWorker(appContext: Context, workerParams: WorkerParameters):
    Worker(appContext, workerParams) {
    override fun doWork(): Result {
        Log.d("Worker", "Starting db update worker")

        val store = applicationContext.getSharedPreferences("logins", ComponentActivity.MODE_PRIVATE)
        val logins = Logins(store)

        val api = SchoolSoftAPI()

        api.loadLogin(logins)
        val database = Database(applicationContext, null)
        database.updateSchedule(api) {}

        // Indicate whether the work finished successfully with the Result
        return Result.success()
    }
}

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {

            // initialize logins manager
            val store = getSharedPreferences("logins", MODE_PRIVATE)
            val logins = Logins(store)
            val login by remember{ mutableStateOf(logins.login) }

            if (login == null) {
                this.startActivity(Intent(this, Login::class.java))
            } else {
                val widthSizeClass = calculateWindowSizeClass(this).widthSizeClass
                val api = SchoolSoftAPI()
                api.loadLogin(logins)

                val database = Database(this, null)

                val uuid = UUID.fromString("789ff204-dbdf-4605-b282-91f3df96b988")

                val workContrains = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.UNMETERED)
                    .setRequiresStorageNotLow(true)
                    .setRequiresDeviceIdle(true)
                    .build()

                val refreshWorkRequest: WorkRequest =
                    PeriodicWorkRequestBuilder<RefreshWorker>(Duration.ofHours(24), Duration.ofHours(24))
                        .setConstraints(workContrains)
                        .setId(uuid)
                        .build()

                WorkManager.getInstance(application).enqueue(refreshWorkRequest)


                SchoolHardApp(widthSizeClass, api, database)
            }
        }
    }
}