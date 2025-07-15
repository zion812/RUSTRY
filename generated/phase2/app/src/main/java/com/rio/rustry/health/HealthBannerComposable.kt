// generated/phase2/app/src/main/java/com/rio/rustry/health/HealthBannerComposable.kt

package com.rio.rustry.health

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun HealthBanner(
    viewModel: HealthBannerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.checkHealthAlerts()
    }

    if (uiState.hasAlerts) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (uiState.isUrgent) Icons.Default.Warning else Icons.Default.HealthAndSafety,
                    contentDescription = null,
                    tint = if (uiState.isUrgent) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (uiState.isUrgent) "Health Alert" else "Health Reminder",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (uiState.isUrgent) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = uiState.message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                
                TextButton(
                    onClick = { viewModel.dismissAlert() }
                ) {
                    Text("Dismiss")
                }
            }
        }
    }
}

@androidx.lifecycle.ViewModel
class HealthBannerViewModel @javax.inject.Inject constructor(
    private val healthService: EnhancedAIHealthService
) : androidx.lifecycle.ViewModel() {

    private val _uiState = kotlinx.coroutines.flow.MutableStateFlow(HealthBannerUiState())
    val uiState: kotlinx.coroutines.flow.StateFlow<HealthBannerUiState> = _uiState.asStateFlow()

    fun checkHealthAlerts() {
        // In a real implementation, this would check for pending health alerts
        // For demo purposes, show a sample alert
        _uiState.value = HealthBannerUiState(
            hasAlerts = true,
            isUrgent = false,
            message = "2 fowls are due for vaccination this week"
        )
    }

    fun dismissAlert() {
        _uiState.value = _uiState.value.copy(hasAlerts = false)
    }
}

data class HealthBannerUiState(
    val hasAlerts: Boolean = false,
    val isUrgent: Boolean = false,
    val message: String = ""
)