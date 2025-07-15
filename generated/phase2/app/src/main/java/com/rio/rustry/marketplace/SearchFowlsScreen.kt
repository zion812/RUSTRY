// generated/phase2/app/src/main/java/com/rio/rustry/marketplace/SearchFowlsScreen.kt

package com.rio.rustry.marketplace

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rio.rustry.data.model.Fowl
import com.rio.rustry.data.model.AgeGroup
import com.rio.rustry.data.model.Breed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchFowlsScreen(
    onFowlClick: (String) -> Unit,
    viewModel: SearchFowlsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showFilters by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Search Bar
        SearchBar(
            query = uiState.query,
            onQueryChange = viewModel::updateQuery,
            onSearch = viewModel::search,
            active = false,
            onActiveChange = { },
            placeholder = { Text("Search fowls...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                IconButton(onClick = { showFilters = true }) {
                    Icon(Icons.Default.FilterList, contentDescription = "Filters")
                }
            }
        ) {}

        Spacer(modifier = Modifier.height(16.dp))

        // Sort Options
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SortOption.values().forEach { option ->
                FilterChip(
                    onClick = { viewModel.updateSort(option) },
                    label = { Text(option.displayName) },
                    selected = uiState.sortOption == option
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Results
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Text(
                        text = uiState.error,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
            uiState.fowls.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No fowls found")
                }
            }
            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.fowls) { fowl ->
                        FowlSearchItem(
                            fowl = fowl,
                            onClick = { onFowlClick(fowl.id) }
                        )
                    }
                }
            }
        }
    }

    // Filter Dialog
    if (showFilters) {
        FilterDialog(
            filters = uiState.filters,
            onFiltersChange = viewModel::updateFilters,
            onDismiss = { showFilters = false }
        )
    }
}

@Composable
fun FowlSearchItem(
    fowl: Fowl,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Image placeholder
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("IMG")
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = fowl.breed.displayName,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = fowl.ageGroup.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$${fowl.price}",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (fowl.isTraceable) {
                AssistChip(
                    onClick = { },
                    label = { Text("Traceable") }
                )
            }
        }
    }
}

@Composable
fun FilterDialog(
    filters: SearchFilters,
    onFiltersChange: (SearchFilters) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter Results") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Breed Filter
                Text("Breed", style = MaterialTheme.typography.titleSmall)
                LazyColumn(
                    modifier = Modifier.height(120.dp)
                ) {
                    items(Breed.values()) { breed ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = breed in filters.breeds,
                                onCheckedChange = { checked ->
                                    val newBreeds = if (checked) {
                                        filters.breeds + breed
                                    } else {
                                        filters.breeds - breed
                                    }
                                    onFiltersChange(filters.copy(breeds = newBreeds))
                                }
                            )
                            Text(breed.displayName)
                        }
                    }
                }

                // Age Group Filter
                Text("Age Group", style = MaterialTheme.typography.titleSmall)
                AgeGroup.values().forEach { ageGroup ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = ageGroup in filters.ageGroups,
                            onCheckedChange = { checked ->
                                val newAgeGroups = if (checked) {
                                    filters.ageGroups + ageGroup
                                } else {
                                    filters.ageGroups - ageGroup
                                }
                                onFiltersChange(filters.copy(ageGroups = newAgeGroups))
                            }
                        )
                        Text(ageGroup.displayName)
                    }
                }

                // Price Range
                Text("Price Range", style = MaterialTheme.typography.titleSmall)
                RangeSlider(
                    value = filters.priceRange,
                    onValueChange = { range ->
                        onFiltersChange(filters.copy(priceRange = range))
                    },
                    valueRange = 0f..1000f,
                    steps = 19
                )
                Text("$${filters.priceRange.start.toInt()} - $${filters.priceRange.endInclusive.toInt()}")
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

enum class SortOption(val displayName: String) {
    NEWEST("Newest"),
    PRICE_LOW_HIGH("Price: Low to High"),
    PRICE_HIGH_LOW("Price: High to Low"),
    DISTANCE("Distance")
}

data class SearchFilters(
    val breeds: Set<Breed> = emptySet(),
    val ageGroups: Set<AgeGroup> = emptySet(),
    val priceRange: ClosedFloatingPointRange<Float> = 0f..1000f,
    val locationRadius: Float = 50f,
    val traceableOnly: Boolean = false
)