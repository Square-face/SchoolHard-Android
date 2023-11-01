package com.example.schoolhard.ui.pages.settings.categories

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.schoolhard.ui.pages.settings.Category

class Overrides: Category {
    @Composable
    override fun CategoryButton(update: (Category) -> Unit) {
        RawButton(title = "Overrides", description = "Manage override settings", update = update)
    }

    @Composable
    override fun RawPage(modifier: Modifier, reset: () -> Unit, update: (Category) -> Unit) {
        Text(text = "Overrides")
    }

}