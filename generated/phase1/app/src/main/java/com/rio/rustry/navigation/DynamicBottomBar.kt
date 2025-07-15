// generated/phase1/app/src/main/java/com/rio/rustry/navigation/DynamicBottomBar.kt
package com.rio.rustry.navigation

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.rio.rustry.auth.EnhancedUserType

/**
 * Dynamic bottom navigation bar that adapts based on user role
 */
@Composable
fun DynamicBottomBar(
    navController: NavController,
    userType: EnhancedUserType,
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier
) {
    val config = BottomNavConfig.forEnhancedUserType(userType)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    NavigationBar(modifier = modifier) {
        config.tabs.forEach { tab ->
            val isSelected = currentRoute == tab.route.route
            
            NavigationBarItem(
                icon = { 
                    Icon(
                        imageVector = if (isSelected) tab.selectedIcon else tab.icon,
                        contentDescription = tab.label
                    )
                },
                label = { Text(tab.label) },
                selected = isSelected,
                onClick = {
                    if (currentRoute != tab.route.route) {
                        navController.navigate(tab.route.route) {
                            // Pop up to the start destination to avoid building up a large stack
                            popUpTo(config.startDestination.route) {
                                saveState = true
                            }
                            // Avoid multiple copies of the same destination
                            launchSingleTop = true
                            // Restore state when reselecting a previously selected item
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}

/**
 * Bottom navigation bar for General users (Buyers)
 */
@Composable
fun GeneralBottomBar(
    navController: NavController,
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier
) {
    DynamicBottomBar(
        navController = navController,
        userType = EnhancedUserType.GENERAL,
        modifier = modifier
    )
}

/**
 * Bottom navigation bar for Farmers (Sellers)
 */
@Composable
fun FarmerBottomBar(
    navController: NavController,
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier
) {
    DynamicBottomBar(
        navController = navController,
        userType = EnhancedUserType.FARMER,
        modifier = modifier
    )
}

/**
 * Bottom navigation bar for High-Level users (Admins/Managers)
 */
@Composable
fun HighLevelBottomBar(
    navController: NavController,
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier
) {
    DynamicBottomBar(
        navController = navController,
        userType = EnhancedUserType.HIGH_LEVEL,
        modifier = modifier
    )
}

/**
 * Helper function to get the appropriate bottom bar composable for a user type
 */
@Composable
fun BottomBarForUserType(
    navController: NavController,
    userType: EnhancedUserType,
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier
) {
    when (userType) {
        EnhancedUserType.GENERAL -> GeneralBottomBar(navController, modifier)
        EnhancedUserType.FARMER -> FarmerBottomBar(navController, modifier)
        EnhancedUserType.HIGH_LEVEL -> HighLevelBottomBar(navController, modifier)
    }
}