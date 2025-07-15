// generated/phase2/app/src/main/java/com/rio/rustry/marketplace/ProductDetailScreen.kt

package com.rio.rustry.marketplace

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.rio.rustry.data.model.Fowl

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    fowlId: String,
    onAddToCart: () -> Unit,
    onBuyNow: () -> Unit,
    onShareToCommunity: () -> Unit,
    viewModel: ProductDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(fowlId) {
        viewModel.loadFowlDetail(fowlId)
    }

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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = uiState.error,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { viewModel.loadFowlDetail(fowlId) }) {
                    Text("Retry")
                }
            }
        }
        uiState.fowl != null -> {
            ProductDetailContent(
                fowl = uiState.fowl,
                lineage = uiState.lineage,
                onAddToCart = onAddToCart,
                onBuyNow = onBuyNow,
                onShare = { shareProduct(context, uiState.fowl) },
                onShareToCommunity = onShareToCommunity
            )
        }
    }
}

@Composable
fun ProductDetailContent(
    fowl: Fowl,
    lineage: List<Fowl>,
    onAddToCart: () -> Unit,
    onBuyNow: () -> Unit,
    onShare: () -> Unit,
    onShareToCommunity: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Image Gallery
            ImageGallery(images = fowl.images)
        }

        item {
            // Product Info
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = fowl.breed.displayName,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = fowl.ageGroup.displayName,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        IconButton(onClick = onShare) {
                            Icon(Icons.Default.Share, contentDescription = "Share")
                        }
                    }

                    Text(
                        text = "$${fowl.price}",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    if (fowl.isTraceable) {
                        AssistChip(
                            onClick = { },
                            label = { Text("Traceable") },
                            leadingIcon = {
                                Icon(Icons.Default.Add, contentDescription = null)
                            }
                        )
                    }
                }
            }
        }

        if (lineage.isNotEmpty()) {
            item {
                // Lineage Tree
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Lineage",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LineageTree(lineage = lineage)
                    }
                }
            }
        }

        item {
            // Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onAddToCart,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add to Cart")
                }
                
                Button(
                    onClick = onBuyNow,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Buy Now")
                }
            }
        }

        item {
            // Share to Community
            OutlinedButton(
                onClick = onShareToCommunity,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text("Share to Community")
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun ImageGallery(images: List<String>) {
    if (images.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("No images available")
        }
        return
    }

    val pagerState = rememberPagerState(pageCount = { images.size })

    Column {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        ) { page ->
            AsyncImage(
                model = images[page],
                contentDescription = "Product image ${page + 1}",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        // Image indicators
        if (images.size > 1) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Center,
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                items(images.size) { index ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .padding(horizontal = 2.dp)
                    ) {
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(1.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LineageTree(lineage: List<Fowl>) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        lineage.forEachIndexed { index, fowl ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Indentation based on generation
                Spacer(modifier = Modifier.width((index * 16).dp))
                
                Text(
                    text = "├─ ${fowl.breed.displayName} (${fowl.ageGroup.displayName})",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

private fun shareProduct(context: android.content.Context, fowl: Fowl) {
    val shareText = "Check out this ${fowl.breed.displayName} (${fowl.ageGroup.displayName}) for $${fowl.price} on RUSTRY!"
    
    val shareIntent = Intent().apply {
        action = Intent.ACTION_SEND
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, shareText)
    }
    
    val chooser = Intent.createChooser(shareIntent, "Share via")
    context.startActivity(chooser)
}