package com.rio.rustry.presentation.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.rio.rustry.domain.model.UserType
import com.rio.rustry.presentation.screens.SimpleMarketScreen

/**
 * Working navigation structure for different user types
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkingNavigation(
    userType: UserType = UserType.GENERAL,
    onUserTypeChange: (UserType) -> Unit = {}
) {
    val navController = rememberNavController()
    var selectedTab by remember { mutableIntStateOf(0) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "RUSTRY - ${userType.name}",
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                actions = {
                    if (userType == UserType.HIGH_LEVEL) {
                        IconButton(onClick = { /* Handle notifications */ }) {
                            Icon(
                                Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            NavigationBar {
                val navigationItems = getWorkingNavigationItems(userType)
                
                navigationItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = selectedTab == index,
                        onClick = { 
                            selectedTab = index
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.startDestinationId) {
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
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = getWorkingStartDestination(userType),
            modifier = Modifier.padding(paddingValues)
        ) {
            // General User Routes
            if (userType == UserType.GENERAL) {
                composable("market") {
                    SimpleMarketScreen(navController = navController, userType = userType)
                }
                composable("explore") {
                    WorkingPlaceholderScreen("General Explore - Social Feed")
                }
                composable("create") {
                    WorkingPlaceholderScreen("General Create - Social Posts")
                }
                composable("cart") {
                    WorkingPlaceholderScreen("General Cart - Shopping Cart")
                }
                composable("profile") {
                    WorkingPlaceholderScreen("General Profile - User Settings")
                }
            }
            
            // Farmer User Routes
            if (userType == UserType.FARMER) {
                composable("home") {
                    WorkingPlaceholderScreen("Farmer Home - Dashboard")
                }
                composable("market") {
                    SimpleMarketScreen(navController = navController, userType = userType)
                }
                composable("create") {
                    WorkingPlaceholderScreen("Farmer Create - List Products")
                }
                composable("community") {
                    WorkingPlaceholderScreen("Farmer Community - Connect with Farmers")
                }
                composable("profile") {
                    WorkingPlaceholderScreen("Farmer Profile - Business Profile")
                }
            }
            
            // High Level User Routes
            if (userType == UserType.HIGH_LEVEL) {
                composable("home") {
                    WorkingPlaceholderScreen("Breeder Home - Farm Overview")
                }
                composable("explore") {
                    WorkingPlaceholderScreen("Breeder Explore - Advanced Social")
                }
                composable("create") {
                    WorkingPlaceholderScreen("Breeder Create - Broadcasting & Listing")
                }
                composable("dashboard") {
                    WorkingPlaceholderScreen("Breeder Dashboard - Analytics & Monitoring")
                }
                composable("transfers") {
                    WorkingPlaceholderScreen("Breeder Transfers - Ownership Management")
                }
            }
        }
    }
}

/**
 * Get navigation items based on user type
 */
private fun getWorkingNavigationItems(userType: UserType): List<WorkingNavigationItem> {
    return when (userType) {
        UserType.GENERAL -> listOf(
            WorkingNavigationItem("Market", Icons.Default.Store, "market"),
            WorkingNavigationItem("Explore", Icons.Default.Explore, "explore"),
            WorkingNavigationItem("Create", Icons.Default.Add, "create"),
            WorkingNavigationItem("Cart", Icons.Default.ShoppingCart, "cart"),
            WorkingNavigationItem("Profile", Icons.Default.Person, "profile")
        )
        
        UserType.FARMER -> listOf(
            WorkingNavigationItem("Home", Icons.Default.Home, "home"),
            WorkingNavigationItem("Market", Icons.Default.Store, "market"),
            WorkingNavigationItem("Create", Icons.Default.Add, "create"),
            WorkingNavigationItem("Community", Icons.Default.Group, "community"),
            WorkingNavigationItem("Profile", Icons.Default.Person, "profile")
        )
        
        UserType.HIGH_LEVEL -> listOf(
            WorkingNavigationItem("Home", Icons.Default.Home, "home"),
            WorkingNavigationItem("Explore", Icons.Default.Explore, "explore"),
            WorkingNavigationItem("Create", Icons.Default.Add, "create"),
            WorkingNavigationItem("Dashboard", Icons.Default.Dashboard, "dashboard"),
            WorkingNavigationItem("Transfers", Icons.Default.SwapHoriz, "transfers")
        )
    }
}

/**
 * Get start destination based on user type
 */
private fun getWorkingStartDestination(userType: UserType): String {
    return when (userType) {
        UserType.GENERAL -> "market"
        UserType.FARMER -> "home"
        UserType.HIGH_LEVEL -> "home"
    }
}

/**
 * Navigation item data class
 */
data class WorkingNavigationItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

@Composable
fun WorkingPlaceholderScreen(title: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Construction,
                contentDescription = "Under Construction",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Coming Soon!",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}