package com.example.schoolhard.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink

const val POST_ID = "postId"
const val SchoolHard_APP_URI = "https://developer.android.com/jetnews"
@Composable
fun SchoolHardNavGraph(
    isExpandedScreen: Boolean,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    openDrawer: () -> Unit = {},
    startDestination: String = SchoolHardDestinations.HOME_ROUTE,
) {
    SchoolHardLogo(modifier = Modifier.clickable { openDrawer() })
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(
            route = SchoolHardDestinations.HOME_ROUTE,
            deepLinks = listOf(
                navDeepLink {
                    uriPattern =
                        "$SchoolHard_APP_URI/${SchoolHardDestinations.HOME_ROUTE}?$POST_ID={$POST_ID}"
                }
            )
        ) { navBackStackEntry ->
            Text(modifier = Modifier.padding(30.dp), text="Home")
        }
        composable(SchoolHardDestinations.Today_ROUTE) {
            Text(modifier = Modifier.padding(30.dp), text="Today")
        }
    }
}
