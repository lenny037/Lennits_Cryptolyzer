package com.lennit.cryptolyzer.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.lennit.cryptolyzer.screens.*

sealed class Screen(val route: String) {
    object Dashboard      : Screen("dashboard")
    object Agents         : Screen("agents")
    object Vault          : Screen("vault")
    object Strategies     : Screen("strategies")
    object Notifications  : Screen("notifications")
    object Safe           : Screen("safe")
    object Settings       : Screen("settings")
}

val bottomNavScreens = listOf(
    Screen.Dashboard, Screen.Agents, Screen.Vault,
    Screen.Strategies, Screen.Notifications,
)

@Composable
fun LennitNavHost(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Dashboard.route) {
        composable(Screen.Dashboard.route)     { DashboardScreen(hiltViewModel()) }
        composable(Screen.Agents.route)        { AgentsScreen(hiltViewModel()) }
        composable(Screen.Vault.route)         { VaultScreen(hiltViewModel()) }
        composable(Screen.Strategies.route)    { StrategiesScreen(hiltViewModel()) }
        composable(Screen.Notifications.route) { NotificationsScreen(hiltViewModel()) }
        composable(Screen.Safe.route)          { SafeScreen(hiltViewModel()) }
        composable(Screen.Settings.route)      { SettingsScreen(hiltViewModel()) }
    }
}
