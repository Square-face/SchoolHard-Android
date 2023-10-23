package com.example.schoolhard.ui.home

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.schoolhard.API.API
import com.example.schoolhard.API.Lesson
import com.example.schoolhard.database.Database

/**
 * Display upcoming lessons in a feed that can be scrolled
 *
 * @param api API to use for fetching data
 * @param database Database to use for fetching data
 * */
@Composable
fun Home(modifier: Modifier = Modifier, api: API, database: Database) {

    var previous: Lesson? by remember { mutableStateOf(database.previousLesson()) }
    var current: Lesson? by remember { mutableStateOf(database.currentLesson()) }
    var next: Lesson? by remember { mutableStateOf(database.nextLesson()) }


    // Update the shown lessons
    fun update() {
        Log.d("Home", "Updating")

        previous = database.previousLesson()
        current = database.currentLesson()
        next = database.nextLesson()
    }


    Feed(modifier=modifier, previous = previous, current = current, next = next, triggerUpdate = ::update)
}