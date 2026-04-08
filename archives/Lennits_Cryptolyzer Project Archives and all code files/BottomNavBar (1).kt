package com.lennit.cryptolyzer.ui.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.AccountBalanceWallet

@Composable
fun BottomNavBar(navController: NavController, currentRoute: String?) {
    val items = listOf(
        Triple(Destinations.Dashboard, Icons.Filled.Dashboard, "Dashboard"),
        Triple(Destinations.Agents, Icons.Filled.List, "Agents"),
        Triple(Destinations.Vault, Icons.Filled.AccountBalanceWallet, "Vault"),
        Triple(Destinations.Strategies, Icons.Filled.Lock, "Strategies"),
        Triple(Destinations.Notifications, Icons.Filled.Notifications, "Alerts"),
        Triple(Destinations.Settings, Icons.Filled.Settings, "Settings"),
    )

    NavigationBar {
        items.forEach { (dest, icon, label) ->
            NavigationBarItem(
                selected = currentRoute == dest.route,
                onClick = {
                    navController.navigate(dest.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(icon, contentDescription = label) },
                label = { Text(label) }
            )
        }
    }
}
