package com.example.schoolhard

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import com.example.schoolhard.API.Filter
import com.example.schoolhard.API.SchoolSoftAPI
import com.example.schoolhard.API.Student
import com.example.schoolhard.ui.SchoolHardApp
import com.example.schoolhard.ui.theme.SchoolHardTheme
import java.time.LocalDateTime

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            val widthSizeClass = calculateWindowSizeClass(this).widthSizeClass
            val api = SchoolSoftAPI()

            api.login(Student(
                "username",
                "password",
                "school")){}

            SchoolHardApp(widthSizeClass, api)
        }
    }
}