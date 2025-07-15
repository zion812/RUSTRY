// generated/phase1/app/src/main/java/com/rio/rustry/navigation/BottomNavConfig.kt
package com.rio.rustry.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.rio.rustry.data.model.UserType
import com.rio.rustry.auth.EnhancedUserType

/**
 * Sealed class hierarchy for type-safe navigation routes
 */
sealed class NavigationRoute(val route: String) {
    // General User Routes
    object Marketplace : NavigationRoute("marketplace")
    object Search : NavigationRoute("search")
    object Favorites : NavigationRoute("favorites")
    object Orders : NavigationRoute("orders")
    object Profile : NavigationRoute("profile")
    
    // Farmer Routes
    object MyFowls : NavigationRoute("my_fowls")
    object AddListing : NavigationRoute("add_listing")
    object Sales : NavigationRoute("sales")
    object Analytics : NavigationRoute("analytics")
    
    // High-Level Routes
    object Dashboard : NavigationRoute("dashboard")
    object Reports : NavigationRoute("reports")
    object UserManagement : NavigationRoute("user_management")
    object SystemSettings : NavigationRoute("system_settings")
    
    // Common Routes
    object Notifications : NavigationRoute("notifications")
    object Settings : NavigationRoute("settings")
    object Help : NavigationRoute("help")
}

/**
 * Data class representing a navigation tab
 */
data class NavigationTab(
    val route: NavigationRoute,
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector = icon
)

/**
 * Sealed class hierarchy for bottom navigation configurations per user role
 */
sealed class BottomNavConfig {
    abstract val tabs: List<NavigationTab>
    abstract val startDestination: NavigationRoute
    
    /**
     * Bottom navigation for General users (Buyers)
     */
    object General : BottomNavConfig() {
        override val tabs = listOf(
            NavigationTab(
                route = NavigationRoute.Marketplace,
                label = "Marketplace",
                icon = Icons.Default.Store,
                selectedIcon = Icons.Filled.Store
            ),
            NavigationTab(
                route = NavigationRoute.Search,
                label = "Search",
                icon = Icons.Default.Search,
                selectedIcon = Icons.Filled.Search
            ),
            NavigationTab(
                route = NavigationRoute.Favorites,
                label = "Favorites",
                icon = Icons.Default.FavoriteBorder,
                selectedIcon = Icons.Filled.Favorite
            ),
            NavigationTab(
                route = NavigationRoute.Orders,
                label = "Orders",
                icon = Icons.Default.ShoppingCart,
                selectedIcon = Icons.Filled.ShoppingCart
            ),
            NavigationTab(
                route = NavigationRoute.Profile,
                label = "Profile",
                icon = Icons.Default.Person,
                selectedIcon = Icons.Filled.Person
            )
        )
        override val startDestination = NavigationRoute.Marketplace
    }
    
    /**
     * Bottom navigation for Farmers (Sellers)
     */
    object Farmer : BottomNavConfig() {
        override val tabs = listOf(
            NavigationTab(
                route = NavigationRoute.MyFowls,
                label = "My Fowls",
                icon = Icons.Default.Pets,
                selectedIcon = Icons.Filled.Pets
            ),
            NavigationTab(
                route = NavigationRoute.AddListing,
                label = "Add Listing",
                icon = Icons.Default.Add,
                selectedIcon = Icons.Filled.Add
            ),
            NavigationTab(
                route = NavigationRoute.Sales,
                label = "Sales",
                icon = Icons.Default.TrendingUp,
                selectedIcon = Icons.Filled.TrendingUp
            ),
            NavigationTab(
                route = NavigationRoute.Analytics,
                label = "Analytics",
                icon = Icons.Default.Analytics,
                selectedIcon = Icons.Filled.Analytics
            ),
            NavigationTab(
                route = NavigationRoute.Profile,
                label = "Profile",
                icon = Icons.Default.Person,
                selectedIcon = Icons.Filled.Person
            )
        )
        override val startDestination = NavigationRoute.MyFowls
    }
    
    /**
     * Bottom navigation for High-Level users (Admins/Managers)
     */
    object HighLevel : BottomNavConfig() {
        override val tabs = listOf(
            NavigationTab(
                route = NavigationRoute.Dashboard,
                label = "Dashboard",
                icon = Icons.Default.Dashboard,
                selectedIcon = Icons.Filled.Dashboard
            ),
            NavigationTab(
                route = NavigationRoute.Reports,
                label = "Reports",
                icon = Icons.Default.Assessment,
                selectedIcon = Icons.Filled.Assessment
            ),
            NavigationTab(
                route = NavigationRoute.UserManagement,
                label = "Users",
                icon = Icons.Default.Group,
                selectedIcon = Icons.Filled.Group
            ),
            NavigationTab(
                route = NavigationRoute.SystemSettings,
                label = "Settings",
                icon = Icons.Default.Settings,
                selectedIcon = Icons.Filled.Settings
            ),
            NavigationTab(
                route = NavigationRoute.Profile,
                label = "Profile",
                icon = Icons.Default.Person,
                selectedIcon = Icons.Filled.Person
            )
        )
        override val startDestination = NavigationRoute.Dashboard
    }
    
    companion object {
        /**
         * Returns the appropriate bottom navigation configuration for the given user type
         */
        fun forUserType(userType: UserType): BottomNavConfig {
            return when (userType) {
                UserType.GENERAL -> General
                UserType.FARMER -> Farmer
            }
        }
        
        /**
         * Returns the appropriate bottom navigation configuration for the enhanced user type
         */
        fun forEnhancedUserType(userType: EnhancedUserType): BottomNavConfig {
            return when (userType) {
                EnhancedUserType.GENERAL -> General
                EnhancedUserType.FARMER -> Farmer
                EnhancedUserType.HIGH_LEVEL -> HighLevel
            }
        }
    }
}