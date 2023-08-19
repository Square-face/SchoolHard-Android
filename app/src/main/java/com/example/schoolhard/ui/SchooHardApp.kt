package com.example.schoolhard.ui

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.schoolhard.ui.theme.SchoolHardTheme
import kotlinx.coroutines.launch
import androidx.navigation.compose.rememberNavController
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.schoolhard.API.API
import com.example.schoolhard.API.Filter
import com.example.schoolhard.API.SchoolSoftAPI
import com.example.schoolhard.API.Student
import com.example.schoolhard.API.User
import com.example.schoolhard.API.userType
import com.example.schoolhard.ui.components.AppNavRail
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoField
import java.time.temporal.IsoFields

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchoolHardApp(
    widthSizeClass: WindowWidthSizeClass,
    api: API,
) {
    SchoolHardTheme {
        val navController = rememberNavController()
        val navigationActions = remember(navController) {
            SchoolHardNavigationActions(navController)
        }

        val coroutineScope = rememberCoroutineScope()

        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute =
            navBackStackEntry?.destination?.route ?: SchoolHardDestinations.HOME_ROUTE

        val isExpandedScreen = widthSizeClass == WindowWidthSizeClass.Expanded
        val sizeAwareDrawerState = rememberSizeAwareDrawerState(isExpandedScreen)

        ModalNavigationDrawer(
            drawerContent = {
                AppDrawer(
                    currentRoute = currentRoute,
                    navigateToHome = navigationActions.navigateToHome,
                    navigateToToday = navigationActions.navigateToToday,
                    closeDrawer = { coroutineScope.launch { sizeAwareDrawerState.close() } }
                )
            },
            drawerState = sizeAwareDrawerState,
            // Only enable opening the drawer via gestures if the screen is not expanded
            gesturesEnabled = !isExpandedScreen,
            modifier = Modifier.background(MaterialTheme.colorScheme.background)
        ) {
            Row(Modifier.systemBarsPadding().fillMaxSize()) {
                if (isExpandedScreen) {
                    AppNavRail(
                        currentRoute = currentRoute,
                        navigateToHome = navigationActions.navigateToHome,
                        navigateToInterests = navigationActions.navigateToToday,
                    )
                }
                SchoolHardNavGraph(
                    api = api,
                    isExpandedScreen = isExpandedScreen,
                    navController = navController,
                    openDrawer = { coroutineScope.launch { sizeAwareDrawerState.open() } },
                )
            }
        }
    }
}

/**
 * Determine the drawer state to pass to the modal drawer.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun rememberSizeAwareDrawerState(isExpandedScreen: Boolean): DrawerState {
    val drawerState = rememberDrawerState(DrawerValue.Open)

    return if (!isExpandedScreen) {
        // If we want to allow showing the drawer, we use a real, remembered drawer
        // state defined above
        drawerState
    } else {
        // If we don't want to allow the drawer to be shown, we provide a drawer state
        // that is locked closed. This is intentionally not remembered, because we
        // don't want to keep track of any changes and always keep it closed
        DrawerState(DrawerValue.Closed)
    }
}

/**
 * Determine the content padding to apply to the different screens of the app
 */
@Composable
fun rememberContentPaddingForScreen(
    additionalTop: Dp = 0.dp,
    excludeTop: Boolean = false
) =
    WindowInsets.systemBars
        .only(if (excludeTop) WindowInsetsSides.Bottom else WindowInsetsSides.Vertical)
        .add(WindowInsets(top = additionalTop))
        .asPaddingValues()
