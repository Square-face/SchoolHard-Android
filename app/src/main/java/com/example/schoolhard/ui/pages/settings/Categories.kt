package com.example.schoolhard.ui.pages.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

class Categories {

    @Composable
    fun Account(modifier: Modifier = Modifier) {
        Raw(modifier = modifier, title = "Account", description = "Account related settings")
    }

    @Composable
    fun Cache(modifier: Modifier = Modifier) {
        Raw(modifier = modifier, title = "Cache", description = "Configure how the cache operates")
    }

    @Composable
    fun Notifications(modifier: Modifier = Modifier) {
        Raw(modifier = modifier, title = "Notifications", description = "Push notifications")
    }

    @Composable
    fun Overrides(modifier: Modifier = Modifier) {
        Raw(modifier = modifier, title = "Overrides", description = "Custom names and parameters")
    }

    @Composable
    private fun Raw(modifier: Modifier = Modifier, title: String, description: String) {
        Column(
            modifier = modifier
        ) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.secondary,
                fontSize = 25.sp,
                fontWeight = FontWeight(500)
            )

            Text(
                text = description,
                color = MaterialTheme.colorScheme.secondary,
                fontSize = 13.sp,
                fontWeight = FontWeight(400)
            )
        }
    }
}