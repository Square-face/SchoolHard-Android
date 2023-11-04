package com.example.schoolhard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.schoolhard.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDrawer(
    currentRoute: String,
    navigateToToday: () -> Unit,
    navigateToSchedule: () -> Unit,
    navigateToSettings: () -> Unit,
    closeDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModalDrawerSheet(
        modifier
            .background(MaterialTheme.colorScheme.primaryContainer)
            .fillMaxHeight()
    ) {
        Row {
            SchoolHardLogo(
                modifier = Modifier.padding(horizontal = 28.dp, vertical = 24.dp)
            )
        }

        NavigationDrawerItem(
            label = { Text(stringResource(id = R.string.home_title)) },
            icon = { Icon(Icons.Filled.Home, null) },
            selected = currentRoute == SchoolHardDestinations.HOME_ROUTE,
            onClick = { navigateToToday(); closeDrawer() },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )

        NavigationDrawerItem(
            label = { Text(stringResource(id = R.string.schema_title)) },
            icon = { Icon(Icons.Filled.DateRange, null) },
            selected = currentRoute == SchoolHardDestinations.SCHEDULE_ROUTE,
            onClick = { navigateToSchedule(); closeDrawer() },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )

        Spacer(modifier = Modifier.weight(1f))

        NavigationDrawerItem(
            label = { Text("Settings") },
            icon = { Icon(Icons.Filled.Settings, null) },
            selected = currentRoute == SchoolHardDestinations.SETTINGS_ROUTE,
            onClick = { navigateToSettings(); closeDrawer() },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )
        Spacer(modifier = Modifier.height(15.dp))
    }
}

@Composable
fun SchoolHardLogo(modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painterResource(R.drawable.foreground_schoolhard_logo),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.width(35.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(text = stringResource(id = R.string.app_name))
    }
}