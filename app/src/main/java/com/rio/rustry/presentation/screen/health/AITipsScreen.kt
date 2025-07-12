package com.rio.rustry.presentation.screen.health

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rio.rustry.data.model.*
import com.rio.rustry.presentation.viewmodel.AITipsViewModel
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AITipsScreen(
    fowlId: String,
    onNavigateBack: () -> Unit,
    viewModel: AITipsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedCategory by remember { mutableStateOf<TipCategory?>(null) }
    var selectedPriority by remember { mutableStateOf<TipPriority?>(null) }
    
    LaunchedEffect(fowlId) {
        viewModel.loadAITips(fowlId)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Health Recommendations") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshTips() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Filter Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Filter Recommendations",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    // Category Filter
                    Text(
                        text = "Category",
                        style = MaterialTheme.typography.labelLarge
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = selectedCategory == null,
                                onClick = { 
                                    selectedCategory = null
                                    viewModel.filterByCategory(null)
                                },
                                label = { Text("All") }
                            )
                        }
                        items(TipCategory.values()) { category ->
                            FilterChip(
                                selected = selectedCategory == category,
                                onClick = { 
                                    selectedCategory = category
                                    viewModel.filterByCategory(category)
                                },
                                label = { Text(category.displayName) }
                            )
                        }
                    }
                    
                    // Priority Filter
                    Text(
                        text = "Priority",
                        style = MaterialTheme.typography.labelLarge
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedPriority == null,
                            onClick = { 
                                selectedPriority = null
                                viewModel.filterByPriority(null)
                            },
                            label = { Text("All") }
                        )
                        TipPriority.values().forEach { priority ->
                            FilterChip(
                                selected = selectedPriority == priority,
                                onClick = { 
                                    selectedPriority = priority
                                    viewModel.filterByPriority(priority)
                                },
                                label = { Text(priority.displayName) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(android.graphics.Color.parseColor(priority.color)).copy(alpha = 0.2f)
                                )
                            )
                        }
                    }
                }
            }
            
            // Tips List
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.filteredTips.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Psychology,
                            contentDescription = "No tips",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "No recommendations found",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Try adjusting your filters",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.filteredTips) { tip ->
                        AITipDetailCard(
                            tip = tip,
                            onMarkAsRead = { viewModel.markTipAsRead(tip.id) }
                        )
                    }
                }
            }
        }
    }
    
    // Error handling
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // Show error snackbar
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AITipDetailCard(
    tip: AIHealthTip,
    onMarkAsRead: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (tip.priority) {
                TipPriority.URGENT -> Color(0xFF9C27B0).copy(alpha = 0.05f)
                TipPriority.HIGH -> Color(0xFFF44336).copy(alpha = 0.05f)
                TipPriority.MEDIUM -> Color(0xFFFF9800).copy(alpha = 0.05f)
                TipPriority.LOW -> Color(0xFF4CAF50).copy(alpha = 0.05f)
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        when (tip.category) {
                            TipCategory.VACCINATION -> Icons.Default.Vaccines
                            TipCategory.NUTRITION -> Icons.Default.Restaurant
                            TipCategory.HOUSING -> Icons.Default.Home
                            TipCategory.BREEDING -> Icons.Default.Favorite
                            TipCategory.DISEASE_PREVENTION -> Icons.Default.HealthAndSafety
                            TipCategory.GENERAL_CARE -> Icons.Default.Pets
                            TipCategory.SEASONAL_CARE -> Icons.Default.WbSunny
                            TipCategory.EMERGENCY -> Icons.Default.Emergency
                            TipCategory.MEDICATION -> Icons.Default.Medication
                            TipCategory.HYGIENE -> Icons.Default.CleanHands
                        },
                        contentDescription = tip.category.displayName,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = tip.category.displayName,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(android.graphics.Color.parseColor(tip.priority.color))
                ) {
                    Text(
                        text = tip.priority.displayName,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            // Title and Description
            Text(
                text = tip.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = tip.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // Details Grid
            if (tip.fowlAge > 0 || tip.ageRange.isNotEmpty() || tip.frequency.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (tip.fowlAge > 0) {
                            DetailRow(
                                label = "Fowl Age",
                                value = "${tip.fowlAge} weeks"
                            )
                        }
                        if (tip.ageRange.isNotEmpty()) {
                            DetailRow(
                                label = "Age Range",
                                value = tip.ageRange
                            )
                        }
                        if (tip.frequency.isNotEmpty()) {
                            DetailRow(
                                label = "Frequency",
                                value = tip.frequency
                            )
                        }
                        if (tip.estimatedCost > 0) {
                            DetailRow(
                                label = "Estimated Cost",
                                value = NumberFormat.getCurrencyInstance(Locale("en", "IN")).format(tip.estimatedCost)
                            )
                        }
                    }
                }
            }
            
            // Symptoms (if any)
            if (tip.symptoms.isNotEmpty()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Watch for these symptoms:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(tip.symptoms) { symptom ->
                            AssistChip(
                                onClick = { },
                                label = { Text(symptom) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Warning,
                                        contentDescription = "Symptom",
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                )
                            )
                        }
                    }
                }
            }
            
            // Prevention measures (if any)
            if (tip.prevention.isNotEmpty()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Prevention measures:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    tip.prevention.forEach { measure ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Prevention",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = measure,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            
            // Action indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (tip.actionRequired) {
                        AssistChip(
                            onClick = { },
                            label = { Text("Action Required") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Assignment,
                                    contentDescription = "Action Required",
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                    
                    if (tip.vetConsultationRequired) {
                        AssistChip(
                            onClick = { },
                            label = { Text("Vet Consultation") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.MedicalServices,
                                    contentDescription = "Vet Required",
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        )
                    }
                }
                
                // Urgency indicator
                if (tip.urgencyLevel > 3) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        repeat(tip.urgencyLevel) {
                            Icon(
                                Icons.Default.PriorityHigh,
                                contentDescription = "Urgency",
                                tint = when {
                                    tip.urgencyLevel >= 5 -> Color(0xFF9C27B0)
                                    tip.urgencyLevel >= 4 -> Color(0xFFF44336)
                                    else -> Color(0xFFFF9800)
                                },
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }
            
            // Due date (if any)
            tip.dueDate?.let { dueDate ->
                HorizontalDivider()
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = "Due Date",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Due: ${java.text.SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(java.util.Date(dueDate))}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
    }
}