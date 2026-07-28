package com.example.examping.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.examping.ui.components.AlarmDialogHost
import com.example.examping.ui.screens.CalendarScreen
import com.example.examping.ui.screens.DashboardScreen
import com.example.examping.ui.screens.HistoryScreen
import com.example.examping.ui.screens.SettingsScreen
import com.example.examping.ui.screens.UploadScreen
import com.example.examping.ui.theme.DarkCard
import com.example.examping.ui.viewmodel.ExamViewModel

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Beranda", Icons.Default.Home)
    object Calendar : Screen("calendar", "Kalender", Icons.Default.CalendarMonth)
    object Upload : Screen("upload", "Upload", Icons.Default.AddCircle)
    object History : Screen("history", "Riwayat", Icons.Default.History)
    object Settings : Screen("settings", "Pengaturan", Icons.Default.Settings)
}

val navItems = listOf(
    Screen.Dashboard,
    Screen.Calendar,
    Screen.Upload,
    Screen.History,
    Screen.Settings
)

@Composable
fun AppNavGraph(viewModel: ExamViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val activeAlarm by viewModel.activeAlarm.collectAsState()

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = DarkCard
            ) {
                navItems.forEach { screen ->
                    val selected = currentRoute == screen.route
                    NavigationBarItem(
                        icon = { Icon(imageVector = screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = selected,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        ),
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToUpload = { navController.navigate(Screen.Upload.route) }
                )
            }
            composable(Screen.Calendar.route) {
                CalendarScreen(viewModel = viewModel)
            }
            composable(Screen.Upload.route) {
                UploadScreen(
                    viewModel = viewModel,
                    onNavigateHome = { navController.navigate(Screen.Dashboard.route) }
                )
            }
            composable(Screen.History.route) {
                HistoryScreen(viewModel = viewModel)
            }
            composable(Screen.Settings.route) {
                SettingsScreen(viewModel = viewModel)
            }
        }

        // Overlay Alarm Ring Host
        AlarmDialogHost(
            activeAlarm = activeAlarm,
            onDismiss = { viewModel.dismissAlarm() }
        )
    }
}
