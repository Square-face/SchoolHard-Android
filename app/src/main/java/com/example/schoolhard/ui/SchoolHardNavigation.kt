package com.example.schoolhard.ui

import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController

/**
 * Destinations used in the [SchooHardApp].
 */
object SchoolHardDestinations {
    const val HOME_ROUTE = "home"
    const val Today_ROUTE = "today"
}

/**
 * Models the navigation actions in the app.
 */
class SchoolHardNavigationActions(navController: NavHostController) {
    val navigateToHome: () -> Unit = {
        navController.navigate(SchoolHardDestinations.HOME_ROUTE) {
            // Pop up to the start destination of the graph to
            // avoid building up a large stack of destinations
            // on the back stack as users select items
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            // Avoid multiple copies of the same destination when
            // reselecting the same item
            launchSingleTop = true
            // Restore state when reselecting a previously selected item
            restoreState = true
        }
    }
    val navigateToToday: () -> Unit = {
        navController.navigate(SchoolHardDestinations.Today_ROUTE) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }
}
