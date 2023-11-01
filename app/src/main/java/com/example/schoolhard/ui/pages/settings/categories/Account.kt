package com.example.schoolhard.ui.pages.settings.categories

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.schoolhard.API.API
import com.example.schoolhard.data.Logins
import com.example.schoolhard.database.Database
import com.example.schoolhard.ui.components.UserInput
import com.example.schoolhard.ui.pages.settings.Category

class Account: Category {
    @Composable
    override fun CategoryButton(update: (Category) -> Unit) {
        RawButton(title = "Account", description = "Manage account settings", update = update)
    }

    @Composable
    override fun RawPage(modifier: Modifier, logins: Logins, database: Database, api: API) {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(30.dp, Alignment.Top)
        ) {
            //UserInput.Toggle(state = true, title = "Auto Login", description = "Automatically login when the app starts") {}
            UserInput.Button(title = "Logout", description = "Logout of the current session") {

            }
        }
    }
}