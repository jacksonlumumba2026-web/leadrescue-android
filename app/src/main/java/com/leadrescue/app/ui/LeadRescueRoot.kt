package com.leadrescue.app.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.leadrescue.app.LeadRescueApp
import com.leadrescue.app.data.LeadFilter
import com.leadrescue.app.reminders.Notifications
import com.leadrescue.app.ui.screens.DashboardScreen
import com.leadrescue.app.ui.screens.LeadDetailScreen
import com.leadrescue.app.ui.screens.LeadFormScreen
import com.leadrescue.app.ui.screens.LeadsScreen
import com.leadrescue.app.ui.screens.SettingsScreen

object Routes {
    const val DASHBOARD = "dashboard"
    const val LEADS = "leads?filter={filter}"
    const val SETTINGS = "settings"
    const val ADD = "lead/new"
    const val DETAIL = "lead/{leadId}"
    const val EDIT = "lead/{leadId}/edit"

    fun leads(filter: LeadFilter = LeadFilter.ALL) = "leads?filter=${filter.name}"
    fun detail(id: Long) = "lead/$id"
    fun edit(id: Long) = "lead/$id/edit"
}

private data class Tab(val route: String, val target: String, val label: String, val icon: ImageVector)

private val tabs = listOf(
    Tab(Routes.DASHBOARD, Routes.DASHBOARD, "Home", Icons.Filled.Home),
    Tab(Routes.LEADS, Routes.leads(), "Leads", Icons.AutoMirrored.Filled.List),
    Tab(Routes.SETTINGS, Routes.SETTINGS, "Settings", Icons.Filled.Settings),
)

/**
 * The whole app: bottom navigation + screens.
 * [openLeadId] is set when a reminder notification is tapped; [onLeadOpened] clears it.
 */
@Composable
fun LeadRescueRoot(openLeadId: Long?, onLeadOpened: () -> Unit) {
    val context = LocalContext.current
    val container = (context.applicationContext as LeadRescueApp).container
    val businessName by container.settings.businessName.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    // Ask for notification permission once, on first launch (Android 13+).
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !Notifications.canNotify(context) &&
            !container.settings.askedForNotifications
        ) {
            container.settings.askedForNotifications = true
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // Open the lead from a tapped reminder.
    LaunchedEffect(openLeadId) {
        if (openLeadId != null) {
            navController.navigate(Routes.detail(openLeadId)) { launchSingleTop = true }
            onLeadOpened()
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            if (tabs.any { it.route == currentRoute }) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainer, tonalElevation = 0.dp) {
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = currentRoute == tab.route,
                            onClick = { navController.navigateToTab(tab.target) },
                            icon = { Icon(tab.icon, contentDescription = null) },
                            label = { Text(tab.label) },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                            ),
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.DASHBOARD,
            modifier = Modifier.fillMaxSize().padding(padding),
            enterTransition = { fadeIn() + slideInHorizontally { it / 12 } },
            exitTransition = { fadeOut() },
            popEnterTransition = { fadeIn() },
            popExitTransition = { fadeOut() + slideOutHorizontally { it / 12 } },
        ) {
            composable(Routes.DASHBOARD) {
                DashboardScreen(
                    onAddLead = { navController.navigate(Routes.ADD) },
                    onOpenLead = { navController.navigate(Routes.detail(it)) },
                    onOpenLeads = { navController.navigateToTab(Routes.leads(it)) },
                )
            }
            composable(
                Routes.LEADS,
                arguments = listOf(navArgument("filter") { type = NavType.StringType; defaultValue = LeadFilter.ALL.name }),
            ) {
                LeadsScreen(
                    businessName = businessName,
                    onAddLead = { navController.navigate(Routes.ADD) },
                    onOpenLead = { navController.navigate(Routes.detail(it)) },
                )
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(businessName = businessName, onBusinessNameChange = container.settings::setBusinessName)
            }
            composable(Routes.ADD) {
                LeadFormScreen(
                    onBack = { navController.popBackStack() },
                    onSaved = { id ->
                        navController.navigate(Routes.detail(id)) { popUpTo(Routes.ADD) { inclusive = true } }
                    },
                )
            }
            composable(Routes.DETAIL, arguments = listOf(navArgument("leadId") { type = NavType.LongType })) {
                LeadDetailScreen(
                    onBack = { navController.popBackStack() },
                    onEdit = { navController.navigate(Routes.edit(it)) },
                )
            }
            composable(Routes.EDIT, arguments = listOf(navArgument("leadId") { type = NavType.LongType })) {
                LeadFormScreen(
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() },
                )
            }
        }
    }
}

/** Bottom-tab navigation: one copy of each tab, with Home always at the bottom of the back stack. */
private fun NavHostController.navigateToTab(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id)
        launchSingleTop = true
    }
}
