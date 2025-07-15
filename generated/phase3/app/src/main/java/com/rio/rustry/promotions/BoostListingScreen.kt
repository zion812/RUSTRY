// generated/phase3/app/src/main/java/com/rio/rustry/promotions/BoostListingScreen.kt

package com.rio.rustry.promotions

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rio.rustry.data.model.Fowl
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoostListingScreen(
    viewModel: PromotionsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedFowl by remember { mutableStateOf<Fowl?>(null) }
    var selectedBoostDuration by remember { mutableStateOf(BoostDuration.ONE_DAY) }

    LaunchedEffect(Unit) {
        viewModel.loadUserListings()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Boost Your Listings",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Boost your listings to appear at the top of search results and get more visibility.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        when (uiState) {
            is PromotionsUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is PromotionsUiState.ListingsLoaded -> {
                if (uiState.listings.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.TrendingUp,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No listings to boost",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Boost Duration Selection
                        item {
                            Card {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text(
                                        text = "Boost Duration",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        BoostDuration.values().forEach { duration ->
                                            FilterChip(
                                                onClick = { selectedBoostDuration = duration },
                                                label = { 
                                                    Column(
                                                        horizontalAlignment = Alignment.CenterHorizontally
                                                    ) {
                                                        Text(duration.displayName)
                                                        Text(
                                                            text = "$${duration.price}",
                                                            style = MaterialTheme.typography.labelSmall
                                                        )
                                                    }
                                                },
                                                selected = selectedBoostDuration == duration
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Listings
                        items(uiState.listings) { fowl ->
                            BoostableListingCard(
                                fowl = fowl,
                                isSelected = selectedFowl?.id == fowl.id,
                                onSelect = { selectedFowl = fowl },
                                onBoost = { 
                                    selectedFowl?.let { 
                                        viewModel.boostListing(it.id, selectedBoostDuration)
                                    }
                                },
                                boostDuration = selectedBoostDuration
                            )
                        }
                    }
                }
            }
            is PromotionsUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.message,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            else -> {
                // Handle other states
            }
        }
    }
}

@Composable
fun BoostableListingCard(
    fowl: Fowl,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onBoost: () -> Unit,
    boostDuration: BoostDuration
) {
    Card(
        onClick = onSelect,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer 
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = fowl.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Breed: ${fowl.breed}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Price: $${fowl.price}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                // Current boost status
                fowl.boostExpiry?.let { expiry ->
                    if (expiry > System.currentTimeMillis()) {
                        BoostStatusChip(expiry = expiry)
                    }
                }
            }

            if (isSelected) {
                Spacer(modifier = Modifier.height(12.dp))
                
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "Boost Summary",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = "Duration: ${boostDuration.displayName}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "Cost: $${boostDuration.price}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "Benefits: Top placement in search results",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Button(
                    onClick = onBoost,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.TrendingUp, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Boost for $${boostDuration.price}")
                }
            }
        }
    }
}

@Composable
fun BoostStatusChip(expiry: Long) {
    val timeLeft = expiry - System.currentTimeMillis()
    val hoursLeft = (timeLeft / (1000 * 60 * 60)).toInt()
    
    Surface(
        color = Color(0xFF4CAF50).copy(alpha = 0.1f),
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = "Boosted (${hoursLeft}h left)",
            color = Color(0xFF4CAF50),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

enum class BoostDuration(val displayName: String, val hours: Int, val price: Double) {
    ONE_DAY("1 Day", 24, 5.0),
    THREE_DAYS("3 Days", 72, 12.0),
    ONE_WEEK("1 Week", 168, 25.0)
}