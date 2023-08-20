package com.example.schoolhard

import android.content.Context
import android.content.ContextParams
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
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
            val index = logins.getInt("index", -1)

            if (index == -1) {
                LoginPage(logins=logins)
            } else {
                val widthSizeClass = calculateWindowSizeClass(this).widthSizeClass
                val api = SchoolSoftAPI()
                val appKey = logins.getString(index.toString()+"appKey", "nothing")
                val school = logins.getString(index.toString()+"school", "nothing")
                api.loginWithAppKey(appKey!!, school!!)
                val database = Database(this, null)
                SchoolHardApp(widthSizeClass, api, database)
            }
        }
    }
}