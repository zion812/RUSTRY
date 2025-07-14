package com.rio.rustry.presentation.marketplace

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import com.rio.rustry.data.model.Fowl
import com.rio.rustry.data.model.FowlBreed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedMarketplaceScreen(
    onFowlClick: (String) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var showFilters by remember { mutableStateOf(false) }
    var selectedBreed by remember { mutableStateOf<FowlBreed?>(null) }
    var selectedPriceRange by remember { mutableStateOf<PriceRange?>(null) }
    var isVerifiedOnly by remember { mutableStateOf(false) }
    var isNearbyOnly by remember { mutableStateOf(false) }
    var favoriteFowlIds by remember { mutableStateOf(setOf<String>()) }

    // Sample data for demonstration
    val sampleFowls = remember {
        listOf(
            Fowl(
                id = "1",
                breed = "Rhode Island Red",
                ownerName = "Farmer John",
                price = 1200.0,
                description = "Healthy laying hen, excellent egg production",
                location = "Hyderabad",
                isTraceable = true,
                isForSale = true
            ),
            Fowl(
                id = "2", 
                breed = "Leghorn",
                ownerName = "Ravi Kumar",
                price = 800.0,
                description = "Young rooster, good for breeding",
                location = "Vijayawada",
                isTraceable = false,
                isForSale = true
            ),
            Fowl(
                id = "3",
                breed = "Brahma",
                ownerName = "Sita Devi",
                price = 1500.0,
                description = "Large breed, dual purpose chicken",
                location = "Warangal",
                isTraceable = true,
                isForSale = true
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Search Bar
        SearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            onFilterClick = { showFilters = !showFilters },
            modifier = Modifier.padding(16.dp)
        )

        // Filter Section
        if (showFilters) {
            FilterSection(
                selectedBreed = selectedBreed,
                selectedPriceRange = selectedPriceRange,
                isVerifiedOnly = isVerifiedOnly,
                isNearbyOnly = isNearbyOnly,
                onBreedSelected = { selectedBreed = it },
                onPriceRangeSelected = { selectedPriceRange = it },
                onVerifiedToggle = { isVerifiedOnly = !isVerifiedOnly },
                onNearbyToggle = { isNearbyOnly = !isNearbyOnly },
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // Fowl Listings
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Available Fowls (${sampleFowls.size})",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            items(sampleFowls) { fowl ->
                EnhancedFowlCard(
                    fowl = fowl,
                    onClick = { onFowlClick(fowl.id) },
                    onFavoriteClick = { 
                        favoriteFowlIds = if (favoriteFowlIds.contains(fowl.id)) {
                            favoriteFowlIds - fowl.id
                        } else {
                            favoriteFowlIds + fowl.id
                        }
                    },
                    isFavorite = favoriteFowlIds.contains(fowl.id)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onFilterClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text("Search fowls, breeds, location...") },
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = "Search"
                )
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = "Clear"
                        )
                    }
                }
            },
            modifier = Modifier.weight(1f),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        IconButton(
            onClick = onFilterClick,
            modifier = Modifier
                .background(
                    MaterialTheme.colorScheme.primary,
                    RoundedCornerShape(12.dp)
                )
                .size(56.dp)
        ) {
            Icon(
                Icons.Default.FilterList,
                contentDescription = "Filters",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
fun FilterSection(
    selectedBreed: FowlBreed?,
    selectedPriceRange: PriceRange?,
    isVerifiedOnly: Boolean,
    isNearbyOnly: Boolean,
    onBreedSelected: (FowlBreed?) -> Unit,
    onPriceRangeSelected: (PriceRange?) -> Unit,
    onVerifiedToggle: () -> Unit,
    onNearbyToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Filters",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // Quick Filters
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = isVerifiedOnly,
                    onClick = onVerifiedToggle,
                    label = { Text("Verified Only") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Verified,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )

                FilterChip(
                    selected = isNearbyOnly,
                    onClick = onNearbyToggle,
                    label = { Text("Nearby") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            }

            // Breed Filter
            Text(
                "Breed",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium
            )
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedBreed == null,
                        onClick = { onBreedSelected(null) },
                        label = { Text("All") }
                    )
                }
                
                items(FowlBreed.values()) { breed ->
                    FilterChip(
                        selected = selectedBreed == breed,
                        onClick = { onBreedSelected(breed) },
                        label = { Text(breed.displayName) }
                    )
                }
            }

            // Price Range Filter
            Text(
                "Price Range",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium
            )
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedPriceRange == null,
                        onClick = { onPriceRangeSelected(null) },
                        label = { Text("All") }
                    )
                }
                
                items(PriceRange.ranges) { range ->
                    FilterChip(
                        selected = selectedPriceRange == range,
                        onClick = { onPriceRangeSelected(range) },
                        label = { Text(range.label) }
                    )
                }
            }
        }
    }
}

@Composable
fun EnhancedFowlCard(
    fowl: Fowl,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    isFavorite: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column {
            // Image Section with placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Pets,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Traceability Badge
                if (fowl.isTraceable) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp)
                            .background(
                                Color.Green.copy(alpha = 0.9f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.Verified,
                                contentDescription = "Traceable",
                                modifier = Modifier.size(16.dp),
                                tint = Color.White
                            )
                            Text(
                                "Traceable",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp)
                            .background(
                                androidx.compose.ui.graphics.Color(0xFFFF9800).copy(alpha = 0.9f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = "Not Traceable",
                                modifier = Modifier.size(16.dp),
                                tint = Color.White
                            )
                            Text(
                                "Not Traceable",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Favorite Button
                IconButton(
                    onClick = onFavoriteClick,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(
                            Color.White.copy(alpha = 0.9f),
                            CircleShape
                        )
                ) {
                    Icon(
                        if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) Color.Red else Color.Gray
                    )
                }
            }

            // Content Section
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Title and Price
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = fowl.breed,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        
                        Text(
                            text = "by ${fowl.ownerName}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Text(
                        text = "₹${fowl.price.toInt()}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Description
                if (fowl.description.isNotEmpty()) {
                    Text(
                        text = fowl.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Location and Age
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (fowl.location.isNotEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = "Location",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = fowl.location,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = "Age",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = calculateAge(fowl.dateOfBirth),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { /* TODO: Contact seller */ },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Default.Message,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Contact")
                    }

                    Button(
                        onClick = { /* TODO: Buy now */ },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Default.ShoppingCart,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Buy Now")
                    }
                }
            }
        }
    }
}

// Helper function to calculate age
private fun calculateAge(dateOfBirth: java.util.Date): String {
    val now = System.currentTimeMillis()
    val ageInMillis = now - dateOfBirth.time
    val ageInDays = ageInMillis / (1000 * 60 * 60 * 24)
    
    return when {
        ageInDays < 30 -> "${ageInDays} days"
        ageInDays < 365 -> "${ageInDays / 30} months"
        else -> "${ageInDays / 365} years"
    }
}