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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import com.example.schoolhard.API.API
import com.example.schoolhard.database.Database
import com.example.schoolhard.ui.schema.SchemaRoute

const val POST_ID = "postId"
const val SchoolHard_APP_URI = "https://developer.android.com/jetnews"
@Composable
fun SchoolHardNavGraph(
    api: API,
    database: Database,
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
            Text(
                text = "SchoolHard",
                style = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight(600),
                    color = Color(0xFFFFFFFF),
                )
            )
        }
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
                SchemaRoute(api = api, database = database)
            }
        }
    }
}
