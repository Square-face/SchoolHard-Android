package com.example.schoolhard.ui.pages.settings.categories

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.schoolhard.ui.pages.settings.Category

class Account: Category {
    @Composable
    override fun CategoryButton(update: (Category) -> Unit) {
        RawButton(title = "Account", description = "Manage account settings", update = update)
    }

    @Composable
    override fun RawPage(modifier: Modifier, reset: () -> Unit, update: (Category) -> Unit) {
        Column(modifier = modifier) {

        }
    }
}