package com.example.schoolhard.ui.pages.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.schoolhard.API.API
import com.example.schoolhard.data.Logins
import com.example.schoolhard.database.Database
import com.example.schoolhard.ui.pages.settings.categories.Account
import com.example.schoolhard.ui.pages.settings.categories.Cache
import com.example.schoolhard.ui.pages.settings.categories.Notifications
import com.example.schoolhard.ui.pages.settings.categories.Overrides

interface Category {


    @Composable
    fun CategoryButton(update: (Category) -> Unit)

    @Composable
    fun RawPage(modifier: Modifier, logins: Logins, database: Database, api: API)

    @Composable
    fun Page(modifier: Modifier = Modifier, logins: Logins, database: Database, api: API) {
        Box(
            modifier = modifier
                .padding(start=25.dp, top = 20.dp, end=25.dp, bottom=0.dp)
        ) {
            RawPage(
                modifier=Modifier,
                logins = logins,
                database = database,
                api = api
            )
        }
    }

    @Composable
    fun RawButton(modifier: Modifier = Modifier, title: String, description: String, update: (Category) -> Unit) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .clickable { update(this) }
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

/**
 * Enum class for managing all the settings pages that currently exist.
 *
 * every entry represents a category class instance
 * */
enum class Categories(val category: Category) {
    ACCOUNT(Account()),
    NOTIFICATIONS(Notifications()),
    OVERRIDES(Overrides()),
    CACHE(Cache());

    companion object {
        fun fromString(string: String?): Category? {
            if (string == null) return null
            return valueOf(string.uppercase()).category
        }
    }
}