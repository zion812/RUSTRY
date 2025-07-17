package com.rio.rustry

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rio.rustry.presentation.theme.RoosterTheme
import com.rio.rustry.presentation.navigation.WorkingNavigation
import com.rio.rustry.domain.model.UserType
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.perf.FirebasePerformance

class MainActivity : ComponentActivity() {
    private lateinit var analytics: FirebaseAnalytics
    private lateinit var crashlytics: FirebaseCrashlytics
    private lateinit var performance: FirebasePerformance
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Firebase components
        initializeFirebaseComponents()
        
        setContent {
            RoosterTheme {
                RustryApp()
            }
        }
    }
    
    private fun initializeFirebaseComponents() {
        try {
            // Initialize Firebase if not already initialized
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this)
            }
            
            // Initialize Firebase Analytics
            analytics = FirebaseAnalytics.getInstance(this)
            analytics.logEvent("app_start", null)
            
            // Initialize Firebase Crashlytics
            crashlytics = FirebaseCrashlytics.getInstance()
            crashlytics.setCrashlyticsCollectionEnabled(true)
            
            // Initialize Firebase Performance
            performance = FirebasePerformance.getInstance()
            performance.isPerformanceCollectionEnabled = true
            
            // Log successful initialization
            crashlytics.log("Firebase components initialized successfully")
        } catch (e: Exception) {
            // Handle Firebase initialization errors gracefully
            println("Firebase initialization failed: ${e.message}")
        }
    }
}

@Composable
fun RustryApp() {
    var currentUserType by remember { mutableStateOf(UserType.GENERAL) }
    
    Column {
        // User Type Selector (for demo)
        UserTypeSelector(
            currentUserType = currentUserType,
            onUserTypeChange = { currentUserType = it }
        )
        
        // Main Navigation
        WorkingNavigation(
            userType = currentUserType,
            onUserTypeChange = { currentUserType = it }
        )
    }
}

@Composable
fun UserTypeSelector(
    currentUserType: UserType,
    onUserTypeChange: (UserType) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            UserType.values().forEach { userType ->
                FilterChip(
                    selected = currentUserType == userType,
                    onClick = { onUserTypeChange(userType) },
                    label = { 
                        Text(
                            text = when (userType) {
                                UserType.GENERAL -> "General User"
                                UserType.FARMER -> "Farmer"
                                UserType.HIGH_LEVEL -> "Breeder"
                            },
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                )
            }
        }
    }
}