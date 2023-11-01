package com.example.schoolhard.ui.pages.settings

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.schoolhard.ui.SchoolHardDestinations


@OptIn(ExperimentalStdlibApi::class)
@Composable
fun Settings(modifier: Modifier = Modifier, navController: NavHostController, path: String?) {

    val page = Categories.fromString(path)

    if (page == null) {
        Index(modifier, navController)
        return
    }

    page.Page(modifier)
}

@Composable
fun Index(modifier: Modifier = Modifier, navController: NavHostController) {
    Column(
        modifier = modifier
            .padding(start = 40.dp, top = 75.dp, end = 40.dp),
        verticalArrangement = Arrangement.spacedBy(30.dp, Alignment.Top)
    ) {

        for (page in Categories.values()) {
            page.category.CategoryButton() {
                Log.d("Navigation", "Navigating to settings/${page.name}")
                navController.navigate(SchoolHardDestinations.SETTINGS_ROUTE+"/${page.name}")
            }
        }
    }
}