package com.example.schoolhard

import android.app.NotificationManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.core.view.WindowCompat
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.schoolhard.API.SchoolSoft.SchoolSoftAPI
import com.example.schoolhard.stores.Logins
import com.example.schoolhard.database.Database
import com.example.schoolhard.notifications.PersistentWorker
import com.example.schoolhard.notifications.NotificationsSchema
import com.example.schoolhard.ui.SchoolHardApp
import com.microsoft.appcenter.AppCenter
import com.microsoft.appcenter.analytics.Analytics
import com.microsoft.appcenter.crashes.Crashes
import com.microsoft.appcenter.distribute.Distribute
import java.time.Duration

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {

        AppCenter.start(
            application, BuildConfig.APP_CENTER_SECRET,
            Analytics::class.java, Crashes::class.java, Distribute::class.java
        )

        NotificationsSchema.Channel.createAll(this.getSystemService(NotificationManager::class.java)!!)

        val persistantWorker = PeriodicWorkRequestBuilder<PersistentWorker>(Duration.ofSeconds(10)).build()
        WorkManager.getInstance(this).enqueue(persistantWorker)

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

                SchoolHardApp(widthSizeClass, api, database, logins)
            }
        }
    }
}