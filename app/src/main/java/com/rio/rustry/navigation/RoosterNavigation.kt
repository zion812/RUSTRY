package com.rio.rustry.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.rio.rustry.presentation.screen.auth.LoginScreen
import com.rio.rustry.presentation.screen.auth.RegisterScreen
import com.rio.rustry.presentation.screen.fowl.AddFowlScreen
import com.rio.rustry.presentation.screen.fowl.FowlDetailScreen
import com.rio.rustry.presentation.screen.marketplace.MarketplaceScreen
import com.rio.rustry.presentation.screen.profile.ProfileScreen
import com.rio.rustry.presentation.viewmodel.AuthViewModel

@Composable
fun RoosterNavigation(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = viewModel()
) {
    val authState by authViewModel.uiState.collectAsState()
    
    val startDestination = if (authState.isAuthenticated) {
        Screen.Marketplace.route
    } else {
        Screen.Login.route
    }
    
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onNavigateToMarketplace = {
                    navController.navigate(Screen.Marketplace.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                authViewModel = authViewModel
            )
        }
        
        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onNavigateToMarketplace = {
                    navController.navigate(Screen.Marketplace.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                authViewModel = authViewModel
            )
        }
        
        composable(Screen.Marketplace.route) {
            MarketplaceScreen(
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Marketplace.route) { inclusive = true }
                    }
                },
                onNavigateToAddFowl = {
                    navController.navigate(Screen.AddFowl.route)
                },
                onNavigateToFowlDetail = { fowlId ->
                    navController.navigate(Screen.FowlDetail.createRoute(fowlId))
                }
            )
        }
        
        composable(Screen.Profile.route) {
            ProfileScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Marketplace.route) { inclusive = true }
                    }
                },
                authViewModel = authViewModel
            )
        }
        
        composable(Screen.AddFowl.route) {
            AddFowlScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onFowlAdded = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.FowlDetail.route) { backStackEntry ->
            val fowlId = backStackEntry.arguments?.getString("fowlId") ?: ""
            FowlDetailScreen(
                fowlId = fowlId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onContactSeller = { sellerId ->
                    // TODO: Navigate to messaging screen
                },
                onNavigateToParent = { parentId ->
                    navController.navigate(Screen.FowlDetail.createRoute(parentId))
                }
            )
        }
    }
}

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Marketplace : Screen("marketplace")
    object Profile : Screen("profile")
    object AddFowl : Screen("add_fowl")
    object FowlDetail : Screen("fowl_detail/{fowlId}") {
        fun createRoute(fowlId: String) = "fowl_detail/$fowlId"
    }
}