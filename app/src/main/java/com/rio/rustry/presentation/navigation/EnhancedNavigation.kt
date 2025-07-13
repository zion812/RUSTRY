package com.rio.rustry.presentation.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.*
import androidx.navigation.compose.*

/**
 * Enhanced Navigation System for Rooster Platform
 * 
 * Features:
 * - Smooth animations and transitions
 * - Performance-optimized navigation
 * - Type-safe navigation arguments
 * - Deep linking support
 * - Navigation state management
 */

// Navigation Routes
sealed class RoosterRoute(val route: String) {
    object Auth : RoosterRoute("auth")
    object Home : RoosterRoute("home")
    object Marketplace : RoosterRoute("marketplace")
    object MyFowls : RoosterRoute("my_fowls")
    object Health : RoosterRoute("health")
    object Profile : RoosterRoute("profile")
    object Messages : RoosterRoute("messages")
    object Transfers : RoosterRoute("transfers")
    
    // Detailed routes with parameters
    object FowlDetails : RoosterRoute("fowl_details/{fowlId}") {
        fun createRoute(fowlId: String) = "fowl_details/$fowlId"
    }
    
    object HealthDetails : RoosterRoute("health_details/{fowlId}") {
        fun createRoute(fowlId: String) = "health_details/$fowlId"
    }
    
    object TransferDetails : RoosterRoute("transfer_details/{transferId}") {
        fun createRoute(transferId: String) = "transfer_details/$transferId"
    }
    
    object CertificateView : RoosterRoute("certificate/{certificateId}") {
        fun createRoute(certificateId: String) = "certificate/$certificateId"
    }
    
    object PaymentFlow : RoosterRoute("payment/{fowlId}") {
        fun createRoute(fowlId: String) = "payment/$fowlId"
    }
}

// Bottom Navigation Items
data class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector = icon,
    val label: String,
    val badgeCount: Int = 0
)

val bottomNavItems = listOf(
    BottomNavItem(
        route = RoosterRoute.Home.route,
        icon = Icons.Default.Home,
        label = "Home"
    ),
    BottomNavItem(
        route = RoosterRoute.Marketplace.route,
        icon = Icons.Default.Store,
        label = "Marketplace"
    ),
    BottomNavItem(
        route = RoosterRoute.MyFowls.route,
        icon = Icons.Default.Pets,
        label = "My Fowls"
    ),
    BottomNavItem(
        route = RoosterRoute.Health.route,
        icon = Icons.Default.HealthAndSafety,
        label = "Health"
    ),
    BottomNavItem(
        route = RoosterRoute.Profile.route,
        icon = Icons.Default.Person,
        label = "Profile"
    )
)

// Enhanced Bottom Navigation Bar
@Composable
fun RoosterBottomNavigation(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        bottomNavItems.forEach { item ->
            val isSelected = currentRoute == item.route
            
            NavigationBarItem(
                icon = {
                    BadgedBox(
                        badge = {
                            if (item.badgeCount > 0) {
                                Badge {
                                    Text(
                                        text = if (item.badgeCount > 99) "99+" else item.badgeCount.toString(),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (isSelected) item.selectedIcon else item.icon,
                            contentDescription = item.label
                        )
                    }
                },
                label = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                selected = isSelected,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            // Pop up to the start destination to avoid building up a large stack
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            // Avoid multiple copies of the same destination
                            launchSingleTop = true
                            // Restore state when reselecting a previously selected item
                            restoreState = true
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

// Navigation Extensions
fun NavController.navigateToFowlDetails(fowlId: String) {
    navigate(RoosterRoute.FowlDetails.createRoute(fowlId))
}

fun NavController.navigateToHealthDetails(fowlId: String) {
    navigate(RoosterRoute.HealthDetails.createRoute(fowlId))
}

fun NavController.navigateToTransferDetails(transferId: String) {
    navigate(RoosterRoute.TransferDetails.createRoute(transferId))
}

fun NavController.navigateToCertificate(certificateId: String) {
    navigate(RoosterRoute.CertificateView.createRoute(certificateId))
}

fun NavController.navigateToPayment(fowlId: String) {
    navigate(RoosterRoute.PaymentFlow.createRoute(fowlId))
}

fun NavController.navigateToAuth() {
    navigate(RoosterRoute.Auth.route) {
        popUpTo(0) { inclusive = true }
    }
}

fun NavController.navigateToHome() {
    navigate(RoosterRoute.Home.route) {
        popUpTo(RoosterRoute.Auth.route) { inclusive = true }
    }
}

// Navigation State Management
@Composable
fun rememberRoosterNavController(): NavHostController {
    return rememberNavController()
}

// Deep Link Handling
object DeepLinkHandler {
    const val SCHEME = "rooster"
    const val HOST = "app"
    
    fun createFowlDetailsDeepLink(fowlId: String): String {
        return "$SCHEME://$HOST/fowl/$fowlId"
    }
    
    fun createCertificateDeepLink(certificateId: String): String {
        return "$SCHEME://$HOST/certificate/$certificateId"
    }
    
    fun createTransferDeepLink(transferId: String): String {
        return "$SCHEME://$HOST/transfer/$transferId"
    }
}

// Navigation Analytics
object NavigationAnalytics {
    fun trackScreenView(screenName: String, parameters: Map<String, Any> = emptyMap()) {
        // Implementation for analytics tracking
        println("Screen View: $screenName with parameters: $parameters")
    }
    
    fun trackNavigation(from: String, to: String, method: String = "tap") {
        // Implementation for navigation tracking
        println("Navigation: $from -> $to via $method")
    }
}