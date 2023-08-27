package com.example.schoolhard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.core.view.WindowCompat
import com.example.schoolhard.API.SchoolSoftAPI
import com.example.schoolhard.database.Database
import com.example.schoolhard.ui.LoginPage
import com.example.schoolhard.ui.SchoolHardApp

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {

            val logins = getSharedPreferences("logins", MODE_PRIVATE)
            val index = remember { mutableStateOf(logins.getInt("index", -1)) }

            if (index.value == -1) {
                LoginPage(logins =logins, index =index)
            } else {
                val widthSizeClass = calculateWindowSizeClass(this).widthSizeClass
                val api = SchoolSoftAPI()
                val appKey = logins.getString(index.toString()+"appKey", "nothing")
                val url = logins.getString(index.toString()+"url", "nothing")
                api.setSchoolUrl(url!!)
                api.loginWithAppKey(appKey!!)
                val database = Database(this, null)
                SchoolHardApp(widthSizeClass, api, database)
            }
        }
    }
}