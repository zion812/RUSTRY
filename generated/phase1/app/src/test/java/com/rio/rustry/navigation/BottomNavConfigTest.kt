// generated/phase1/app/src/test/java/com/rio/rustry/navigation/BottomNavConfigTest.kt
package com.rio.rustry.navigation

import com.google.common.truth.Truth.assertThat
import com.rio.rustry.auth.EnhancedUserType
import com.rio.rustry.data.model.UserType
import org.junit.Test

class BottomNavConfigTest {
    
    @Test
    fun `General config has correct tabs and start destination`() {
        // When
        val config = BottomNavConfig.General
        
        // Then
        assertThat(config.tabs).hasSize(5)
        assertThat(config.startDestination).isEqualTo(NavigationRoute.Marketplace)
        
        // Verify tab routes
        val routes = config.tabs.map { it.route }
        assertThat(routes).containsExactly(
            NavigationRoute.Marketplace,
            NavigationRoute.Search,
            NavigationRoute.Favorites,
            NavigationRoute.Orders,
            NavigationRoute.Profile
        )
        
        // Verify tab labels
        val labels = config.tabs.map { it.label }
        assertThat(labels).containsExactly(
            "Marketplace",
            "Search", 
            "Favorites",
            "Orders",
            "Profile"
        )
    }
    
    @Test
    fun `Farmer config has correct tabs and start destination`() {
        // When
        val config = BottomNavConfig.Farmer
        
        // Then
        assertThat(config.tabs).hasSize(5)
        assertThat(config.startDestination).isEqualTo(NavigationRoute.MyFowls)
        
        // Verify tab routes
        val routes = config.tabs.map { it.route }
        assertThat(routes).containsExactly(
            NavigationRoute.MyFowls,
            NavigationRoute.AddListing,
            NavigationRoute.Sales,
            NavigationRoute.Analytics,
            NavigationRoute.Profile
        )
        
        // Verify tab labels
        val labels = config.tabs.map { it.label }
        assertThat(labels).containsExactly(
            "My Fowls",
            "Add Listing",
            "Sales",
            "Analytics", 
            "Profile"
        )
    }
    
    @Test
    fun `HighLevel config has correct tabs and start destination`() {
        // When
        val config = BottomNavConfig.HighLevel
        
        // Then
        assertThat(config.tabs).hasSize(5)
        assertThat(config.startDestination).isEqualTo(NavigationRoute.Dashboard)
        
        // Verify tab routes
        val routes = config.tabs.map { it.route }
        assertThat(routes).containsExactly(
            NavigationRoute.Dashboard,
            NavigationRoute.Reports,
            NavigationRoute.UserManagement,
            NavigationRoute.SystemSettings,
            NavigationRoute.Profile
        )
        
        // Verify tab labels
        val labels = config.tabs.map { it.label }
        assertThat(labels).containsExactly(
            "Dashboard",
            "Reports",
            "Users",
            "Settings",
            "Profile"
        )
    }
    
    @Test
    fun `forUserType returns correct config for GENERAL`() {
        // When
        val config = BottomNavConfig.forUserType(UserType.GENERAL)
        
        // Then
        assertThat(config).isEqualTo(BottomNavConfig.General)
    }
    
    @Test
    fun `forUserType returns correct config for FARMER`() {
        // When
        val config = BottomNavConfig.forUserType(UserType.FARMER)
        
        // Then
        assertThat(config).isEqualTo(BottomNavConfig.Farmer)
    }
    
    @Test
    fun `forEnhancedUserType returns correct config for GENERAL`() {
        // When
        val config = BottomNavConfig.forEnhancedUserType(EnhancedUserType.GENERAL)
        
        // Then
        assertThat(config).isEqualTo(BottomNavConfig.General)
    }
    
    @Test
    fun `forEnhancedUserType returns correct config for FARMER`() {
        // When
        val config = BottomNavConfig.forEnhancedUserType(EnhancedUserType.FARMER)
        
        // Then
        assertThat(config).isEqualTo(BottomNavConfig.Farmer)
    }
    
    @Test
    fun `forEnhancedUserType returns correct config for HIGH_LEVEL`() {
        // When
        val config = BottomNavConfig.forEnhancedUserType(EnhancedUserType.HIGH_LEVEL)
        
        // Then
        assertThat(config).isEqualTo(BottomNavConfig.HighLevel)
    }
    
    @Test
    fun `NavigationRoute has correct route strings`() {
        // Then
        assertThat(NavigationRoute.Marketplace.route).isEqualTo("marketplace")
        assertThat(NavigationRoute.Search.route).isEqualTo("search")
        assertThat(NavigationRoute.Favorites.route).isEqualTo("favorites")
        assertThat(NavigationRoute.Orders.route).isEqualTo("orders")
        assertThat(NavigationRoute.Profile.route).isEqualTo("profile")
        
        assertThat(NavigationRoute.MyFowls.route).isEqualTo("my_fowls")
        assertThat(NavigationRoute.AddListing.route).isEqualTo("add_listing")
        assertThat(NavigationRoute.Sales.route).isEqualTo("sales")
        assertThat(NavigationRoute.Analytics.route).isEqualTo("analytics")
        
        assertThat(NavigationRoute.Dashboard.route).isEqualTo("dashboard")
        assertThat(NavigationRoute.Reports.route).isEqualTo("reports")
        assertThat(NavigationRoute.UserManagement.route).isEqualTo("user_management")
        assertThat(NavigationRoute.SystemSettings.route).isEqualTo("system_settings")
    }
    
    @Test
    fun `all configs have exactly 5 tabs`() {
        // Then
        assertThat(BottomNavConfig.General.tabs).hasSize(5)
        assertThat(BottomNavConfig.Farmer.tabs).hasSize(5)
        assertThat(BottomNavConfig.HighLevel.tabs).hasSize(5)
    }
    
    @Test
    fun `all configs include Profile tab`() {
        // Then
        val generalHasProfile = BottomNavConfig.General.tabs.any { it.route == NavigationRoute.Profile }
        val farmerHasProfile = BottomNavConfig.Farmer.tabs.any { it.route == NavigationRoute.Profile }
        val highLevelHasProfile = BottomNavConfig.HighLevel.tabs.any { it.route == NavigationRoute.Profile }
        
        assertThat(generalHasProfile).isTrue()
        assertThat(farmerHasProfile).isTrue()
        assertThat(highLevelHasProfile).isTrue()
    }
}