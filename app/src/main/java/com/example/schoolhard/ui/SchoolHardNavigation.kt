package com.example.schoolhard.ui

import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController

/**
 * Navigation Destinations.
 */
object SchoolHardDestinations {
    const val HOME_ROUTE = "home"
    const val SCHEDULE_ROUTE = "schedule"
    const val SETTINGS_ROUTE = "settings"
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

    val navigateToSchedule: () -> Unit = {
        navController.navigate(SchoolHardDestinations.SCHEDULE_ROUTE) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    val navigateToSettings: () -> Unit = {
        navController.navigate(SchoolHardDestinations.SETTINGS_ROUTE) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }
}
