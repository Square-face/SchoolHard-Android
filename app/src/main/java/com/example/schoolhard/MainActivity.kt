package com.example.schoolhard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.core.view.WindowCompat
import com.example.schoolhard.API.SchoolSoftAPI
import com.example.schoolhard.API.Student
import com.example.schoolhard.database.Database
import com.example.schoolhard.ui.SchoolHardApp

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            val widthSizeClass = calculateWindowSizeClass(this).widthSizeClass
            val api = SchoolSoftAPI()
            val database = Database(this, null)

            api.login(Student(
                "22linmic",
                "XAX7UUhzA@rHXCttfXPB",
                "minervagymnasium")){}

            SchoolHardApp(widthSizeClass, api, database)
        }
    }
}