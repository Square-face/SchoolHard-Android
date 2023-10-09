package com.example.schoolhard.ui.pages.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@Composable
fun Settings(modifier: Modifier = Modifier) {
    val categories = Categories()
    Column(
        modifier = modifier
            .padding(start = 40.dp, top = 75.dp, end = 40.dp),
        verticalArrangement = Arrangement.spacedBy(30.dp, Alignment.Top)
    ) {
        categories.Account()
        categories.Notifications()
        categories.Overrides()
        categories.Cache()
    }
}