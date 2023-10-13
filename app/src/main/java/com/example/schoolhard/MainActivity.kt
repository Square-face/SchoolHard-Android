package com.example.schoolhard

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
import com.example.schoolhard.API.SchoolSoft.SchoolSoftAPI
import com.example.schoolhard.data.Logins
import com.example.schoolhard.database.Database
import com.example.schoolhard.ui.SchoolHardApp
import com.microsoft.appcenter.AppCenter
import com.microsoft.appcenter.analytics.Analytics
import com.microsoft.appcenter.crashes.Crashes
import com.microsoft.appcenter.distribute.Distribute

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {

        AppCenter.start(
            application, BuildConfig.APP_CENTER_SECRET,
            Analytics::class.java, Crashes::class.java, Distribute::class.java
        )

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

                SchoolHardApp(widthSizeClass, api, database)
            }
        }
    }
}