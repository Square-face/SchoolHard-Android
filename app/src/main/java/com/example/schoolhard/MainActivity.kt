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
import androidx.compose.runtime.setValue
import androidx.core.view.WindowCompat
import com.example.schoolhard.API.SchoolSoft.SchoolSoftAPI
import com.example.schoolhard.data.Logins
import com.example.schoolhard.database.Database
import com.example.schoolhard.ui.SchoolHardApp

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
                SchoolHardApp(widthSizeClass, api, database)
            }
        }
    }
}