// generated/phase1/app/src/main/java/com/rio/rustry/ui/theme/ConsentScreen.kt
package com.rio.rustry.ui.theme

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rio.rustry.auth.GdprConsentManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsentScreen(
    onConsentGiven: () -> Unit,
    viewModel: ConsentViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val uriHandler = LocalUriHandler.current
    
    // Show loading or error states
    LaunchedEffect(uiState.consentRecorded) {
        if (uiState.consentRecorded) {
            onConsentGiven()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Privacy & Data Protection",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Icon
            Icon(
                Icons.Default.Security,
                contentDescription = null,
                modifier = Modifier
                    .size(64.dp)
                    .align(Alignment.CenterHorizontally),
                tint = MaterialTheme.colorScheme.primary
            )
            
            // Title
            Text(
                "Your Privacy Matters",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            
            // GDPR Compliance Notice
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "GDPR Compliance",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    
                    Text(
                        "We collect and process your personal data in accordance with the General Data Protection Regulation (GDPR). Your consent is required to continue using RUSTRY.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            // Data Collection Information
            Card {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "What data we collect:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    val dataPoints = listOf(
                        "Email address (for authentication)",
                        "User role (General, Farmer, or High-Level)",
                        "Profile information you provide",
                        "App usage analytics (anonymized)"
                    )
                    
                    dataPoints.forEach { point ->
                        Text(
                            "• $point",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
            
            // Your Rights
            Card {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Your rights:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    val rights = listOf(
                        "Access your personal data",
                        "Correct inaccurate data",
                        "Request data deletion",
                        "Withdraw consent at any time"
                    )
                    
                    rights.forEach { right ->
                        Text(
                            "• $right",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
            
            // Privacy Policy Link
            OutlinedButton(
                onClick = { 
                    uriHandler.openUri(GdprConsentManager.PRIVACY_POLICY_URL)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Read Full Privacy Policy")
            }
            
            // Error Message
            if (uiState.errorMessage.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        uiState.errorMessage,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Action Buttons
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Accept Button
                Button(
                    onClick = { viewModel.recordConsent() },
                    enabled = !uiState.isLoading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text("I Accept - Continue to RUSTRY")
                }
                
                // Data Deletion Button
                OutlinedButton(
                    onClick = { viewModel.requestDataDeletion() },
                    enabled = !uiState.isLoading,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Request Data Deletion")
                }
            }
            
            // Footer Note
            Text(
                "By accepting, you agree to our data processing practices as outlined in our Privacy Policy. You can withdraw consent or request data deletion at any time from your profile settings.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}