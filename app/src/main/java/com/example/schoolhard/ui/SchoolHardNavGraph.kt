package com.example.schoolhard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.schoolhard.API.API
import com.example.schoolhard.R
import com.example.schoolhard.data.Logins
import com.example.schoolhard.database.Database
import com.example.schoolhard.ui.home.Home
import com.example.schoolhard.ui.pages.settings.Settings
import com.example.schoolhard.ui.pages.schema.SchemaRoute

@Composable
fun SchoolHardNavGraph(
    api: API,
    database: Database,
    logins: Logins,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    openDrawer: () -> Unit = {},
    startDestination: String = SchoolHardDestinations.HOME_ROUTE,
) {
    Column(modifier


    ) {
        Row(
            Modifier
                .background(MaterialTheme.colorScheme.primary)
                .fillMaxWidth()
                .padding(7.dp)
                .clickable { openDrawer() },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Menu, null, tint = Color.White, modifier = Modifier
                .scale(1.5F)
                .padding(15.dp, 10.dp, 0.dp, 10.dp))
            Spacer(modifier = Modifier.width(30.dp))
            navController.currentDestination?.let { navDestination ->
                Text(
                    text = navDestination.route!!.replaceFirstChar { it.uppercase() },
                    style = TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight(600),
                        color = Color(0xFFFFFFFF),
                    )
                )
            } ?:run {
                Text(
                    text = stringResource(id = R.string.app_name),
                    style = TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight(600),
                        color = Color(0xFFFFFFFF),
                    )
                )
            }
        }
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = modifier
        ) {
            composable(route = SchoolHardDestinations.HOME_ROUTE,) {
                Home(api = api, database = database)
            }

            composable(SchoolHardDestinations.SCHEDULE_ROUTE) {
                SchemaRoute(api = api, database = database)
            }

            composable(SchoolHardDestinations.SETTINGS_ROUTE) {
                Settings(
                    navController = navController,
                    path = null,
                    logins = logins,
                    database = database,
                    api = api,
                )
            }

            val pageArg = navArgument("page") {
                type = NavType.StringType
            }

            composable(
                SchoolHardDestinations.SETTINGS_ROUTE+"/{page}",
                listOf(pageArg)
            ) { backStackEntry ->
                Settings(
                    navController = navController,
                    path = backStackEntry.arguments?.getString("page"),
                    logins = logins,
                    database = database,
                    api = api,
                )
            }
        }
    }
}
