package com.rio.rustry.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.rio.rustry.domain.model.*
import com.rio.rustry.data.repository.MarketFilters
import com.rio.rustry.data.repository.SortOption

@Composable
fun SimpleMarketScreen(
    navController: NavHostController,
    userType: UserType
) {
    var searchQuery by remember { mutableStateOf("") }
    var showFilters by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<ProductCategory?>(null) }
    var selectedBreed by remember { mutableStateOf("") }
    var selectedAgeGroup by remember { mutableStateOf<AgeGroup?>(null) }
    var minPrice by remember { mutableStateOf("") }
    var maxPrice by remember { mutableStateOf("") }
    var nearbyOnly by remember { mutableStateOf(false) }
    var verifiedOnly by remember { mutableStateOf(false) }
    var sortOption by remember { mutableStateOf(SortOption.RECENT) }
    
    val products = getSampleSimpleProducts()
    val filteredProducts = remember(searchQuery, selectedCategory, selectedBreed, selectedAgeGroup, minPrice, maxPrice, nearbyOnly, verifiedOnly) {
        products.filter { product ->
            val matchesSearch = searchQuery.isEmpty() || 
                product.title.contains(searchQuery, ignoreCase = true) ||
                product.breed.contains(searchQuery, ignoreCase = true) ||
                product.location.contains(searchQuery, ignoreCase = true)
            
            val matchesBreed = selectedBreed.isEmpty() || product.breed.equals(selectedBreed, ignoreCase = true)
            val matchesVerified = !verifiedOnly || product.isVerified
            val matchesPrice = (minPrice.isEmpty() || product.price >= minPrice.toDoubleOrNull() ?: 0.0) &&
                             (maxPrice.isEmpty() || product.price <= maxPrice.toDoubleOrNull() ?: Double.MAX_VALUE)
            
            matchesSearch && matchesBreed && matchesVerified && matchesPrice
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header with Filter Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${userType.name} Market",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            IconButton(onClick = { showFilters = !showFilters }) {
                Icon(
                    Icons.Default.FilterList,
                    contentDescription = "Filters",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search products, breeds, locations...") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Search")
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Quick Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    onClick = { nearbyOnly = !nearbyOnly },
                    label = { Text("Nearby") },
                    selected = nearbyOnly,
                    leadingIcon = if (nearbyOnly) {
                        { Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    } else null
                )
            }
            item {
                FilterChip(
                    onClick = { verifiedOnly = !verifiedOnly },
                    label = { Text("Verified") },
                    selected = verifiedOnly,
                    leadingIcon = if (verifiedOnly) {
                        { Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    } else null
                )
            }
            item {
                FilterChip(
                    onClick = { selectedBreed = if (selectedBreed == "Desi") "" else "Desi" },
                    label = { Text("Desi") },
                    selected = selectedBreed == "Desi"
                )
            }
            item {
                FilterChip(
                    onClick = { selectedBreed = if (selectedBreed == "Kadaknath") "" else "Kadaknath" },
                    label = { Text("Kadaknath") },
                    selected = selectedBreed == "Kadaknath"
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Advanced Filters (Expandable)
        if (showFilters) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Advanced Filters",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Price Range
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = minPrice,
                            onValueChange = { minPrice = it },
                            label = { Text("Min Price") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = maxPrice,
                            onValueChange = { maxPrice = it },
                            label = { Text("Max Price") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Clear Filters Button
                    TextButton(
                        onClick = {
                            selectedCategory = null
                            selectedBreed = ""
                            selectedAgeGroup = null
                            minPrice = ""
                            maxPrice = ""
                            nearbyOnly = false
                            verifiedOnly = false
                        }
                    ) {
                        Text("Clear All Filters")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Results Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Found ${filteredProducts.size} products",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            // Sort Options
            var showSortMenu by remember { mutableStateOf(false) }
            Box {
                TextButton(onClick = { showSortMenu = true }) {
                    Text("Sort: ${sortOption.name}")
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
                DropdownMenu(
                    expanded = showSortMenu,
                    onDismissRequest = { showSortMenu = false }
                ) {
                    SortOption.values().forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.name) },
                            onClick = {
                                sortOption = option
                                showSortMenu = false
                            }
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Products List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredProducts) { product ->
                EnhancedProductCard(
                    product = product,
                    userType = userType,
                    onProductClick = {
                        // Navigate to product details
                        navController.navigate("product_details/${product.id}")
                    },
                    onActionClick = {
                        when (userType) {
                            UserType.GENERAL -> {
                                // Navigate to order placement
                                navController.navigate("order_placement/${product.id}")
                            }
                            UserType.FARMER -> {
                                // Navigate to product details
                                navController.navigate("product_details/${product.id}")
                            }
                            UserType.HIGH_LEVEL -> {
                                // Navigate to analytics
                                navController.navigate("product_analytics/${product.id}")
                            }
                        }
                    }
                )
            }
            
            if (filteredProducts.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.SearchOff,
                                contentDescription = "No results",
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No products found",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Try adjusting your filters",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SimpleProductCard(
    product: SimpleProduct,
    userType: UserType,
    onProductClick: () -> Unit,
    onActionClick: () -> Unit
) {
    Card(
        onClick = onProductClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Product Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${product.breed} • ${product.age} months",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Verification Badge
                if (product.isVerified) {
                    Icon(
                        Icons.Default.Verified,
                        contentDescription = "Verified",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Product Image Placeholder
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Image,
                        contentDescription = "Product Image",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Product Details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "₹${product.price.toInt()}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${product.weight}kg",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = product.location,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "by ${product.farmerName}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Action Button based on user type
            Button(
                onClick = onActionClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                when (userType) {
                    UserType.GENERAL -> {
                        Icon(
                            Icons.Default.ShoppingCart,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add to Cart")
                    }
                    UserType.FARMER -> {
                        Icon(
                            Icons.Default.Visibility,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("View Details")
                    }
                    UserType.HIGH_LEVEL -> {
                        Icon(
                            Icons.Default.Analytics,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Analyze")
                    }
                }
            }
        }
    }
}

@Composable
fun EnhancedProductCard(
    product: SimpleProduct,
    userType: UserType,
    onProductClick: () -> Unit,
    onActionClick: () -> Unit
) {
    Card(
        onClick = onProductClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Product Header with Enhanced Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${product.breed} • ${product.age} months",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    // Additional info row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = "Location",
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = product.location,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        if (product.isVerified) {
                            Icon(
                                Icons.Default.Verified,
                                contentDescription = "Verified",
                                modifier = Modifier.size(14.dp),
                                tint = Color(0xFF4CAF50)
                            )
                        }
                    }
                }
                
                // Price Badge
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "₹${product.price.toInt()}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Product Image Placeholder with Enhanced Design
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Image,
                            contentDescription = "Product Image",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Photo",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Product Details Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "${product.weight}kg",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Weight",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = product.farmerName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Seller",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Primary Action Button
                Button(
                    onClick = onActionClick,
                    modifier = Modifier.weight(1f)
                ) {
                    when (userType) {
                        UserType.GENERAL -> {
                            Icon(
                                Icons.Default.ShoppingCart,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Buy Now")
                        }
                        UserType.FARMER -> {
                            Icon(
                                Icons.Default.Visibility,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("View")
                        }
                        UserType.HIGH_LEVEL -> {
                            Icon(
                                Icons.Default.Analytics,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Analyze")
                        }
                    }
                }
                
                // Secondary Action Button
                OutlinedButton(
                    onClick = { /* Handle secondary action like chat */ }
                ) {
                    Icon(
                        Icons.Default.Chat,
                        contentDescription = "Chat",
                        modifier = Modifier.size(18.dp)
                    )
                }
                
                // Favorite Button
                IconButton(
                    onClick = { /* Handle favorite toggle */ }
                ) {
                    Icon(
                        Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

// Simple data class for products
data class SimpleProduct(
    val id: String,
    val title: String,
    val breed: String,
    val age: Int,
    val price: Double,
    val weight: Double,
    val location: String,
    val farmerName: String,
    val isVerified: Boolean
)

// Sample data
private fun getSampleSimpleProducts(): List<SimpleProduct> {
    return listOf(
        SimpleProduct(
            id = "1",
            title = "Healthy Desi Hen",
            breed = "Desi",
            age = 6,
            price = 800.0,
            weight = 1.5,
            location = "Hyderabad",
            farmerName = "Ravi Kumar",
            isVerified = true
        ),
        SimpleProduct(
            id = "2",
            title = "Premium Kadaknath Rooster",
            breed = "Kadaknath",
            age = 12,
            price = 2500.0,
            weight = 2.5,
            location = "Warangal",
            farmerName = "Sunita Farms",
            isVerified = true
        ),
        SimpleProduct(
            id = "3",
            title = "Rhode Island Red Chicks",
            breed = "Rhode Island Red",
            age = 2,
            price = 150.0,
            weight = 0.2,
            location = "Vijayawada",
            farmerName = "Green Valley Poultry",
            isVerified = false
        ),
        SimpleProduct(
            id = "4",
            title = "Country Chicken",
            breed = "Country",
            age = 8,
            price = 1200.0,
            weight = 2.0,
            location = "Guntur",
            farmerName = "Village Farm",
            isVerified = true
        )
    )
}