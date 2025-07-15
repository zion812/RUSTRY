// generated/phase2/app/src/main/java/com/rio/rustry/listing/AddListingScreen.kt

package com.rio.rustry.listing

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.rio.rustry.data.model.AgeGroup
import com.rio.rustry.data.model.Breed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddListingScreen(
    fowlId: String? = null,
    onNavigateBack: () -> Unit,
    viewModel: AddListingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        viewModel.addImages(uris)
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            // Handle camera result
        }
    }

    LaunchedEffect(fowlId) {
        fowlId?.let { viewModel.loadFowl(it) }
    }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onNavigateBack()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = if (fowlId == null) "Add New Listing" else "Edit Listing",
            style = MaterialTheme.typography.headlineMedium
        )

        // Breed Selection
        var breedExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = breedExpanded,
            onExpandedChange = { breedExpanded = it }
        ) {
            OutlinedTextField(
                value = uiState.breed?.displayName ?: "",
                onValueChange = { },
                readOnly = true,
                label = { Text("Breed") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = breedExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                isError = uiState.breedError != null,
                supportingText = uiState.breedError?.let { { Text(it) } }
            )
            ExposedDropdownMenu(
                expanded = breedExpanded,
                onDismissRequest = { breedExpanded = false }
            ) {
                Breed.values().forEach { breed ->
                    DropdownMenuItem(
                        text = { Text(breed.displayName) },
                        onClick = {
                            viewModel.updateBreed(breed)
                            breedExpanded = false
                        }
                    )
                }
            }
        }

        // Age Group Selection
        var ageGroupExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = ageGroupExpanded,
            onExpandedChange = { ageGroupExpanded = it }
        ) {
            OutlinedTextField(
                value = uiState.ageGroup?.displayName ?: "",
                onValueChange = { },
                readOnly = true,
                label = { Text("Age Group") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = ageGroupExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                isError = uiState.ageGroupError != null,
                supportingText = uiState.ageGroupError?.let { { Text(it) } }
            )
            ExposedDropdownMenu(
                expanded = ageGroupExpanded,
                onDismissRequest = { ageGroupExpanded = false }
            ) {
                AgeGroup.values().forEach { ageGroup ->
                    DropdownMenuItem(
                        text = { Text(ageGroup.displayName) },
                        onClick = {
                            viewModel.updateAgeGroup(ageGroup)
                            ageGroupExpanded = false
                        }
                    )
                }
            }
        }

        // Price
        OutlinedTextField(
            value = uiState.price,
            onValueChange = viewModel::updatePrice,
            label = { Text("Price ($)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
            isError = uiState.priceError != null,
            supportingText = uiState.priceError?.let { { Text(it) } }
        )

        // Images
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Images",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Row {
                        IconButton(onClick = { imagePickerLauncher.launch("image/*") }) {
                            Icon(Icons.Default.Add, contentDescription = "Add from gallery")
                        }
                        IconButton(onClick = { /* TODO: Implement camera */ }) {
                            Icon(Icons.Default.Camera, contentDescription = "Take photo")
                        }
                    }
                }

                if (uiState.images.isNotEmpty()) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.images) { uri ->
                            Box {
                                AsyncImage(
                                    model = uri,
                                    contentDescription = null,
                                    modifier = Modifier.size(80.dp),
                                    contentScale = ContentScale.Crop
                                )
                                IconButton(
                                    onClick = { viewModel.removeImage(uri) },
                                    modifier = Modifier.align(Alignment.TopEnd)
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Remove",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Text(
                        text = "No images added",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                uiState.imagesError?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        // Traceability Toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Enable Traceability",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Track lineage and breeding history",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = uiState.isTraceable,
                onCheckedChange = viewModel::updateTraceability
            )
        }

        // Submit Button
        Button(
            onClick = viewModel::saveListing,
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(if (fowlId == null) "Create Listing" else "Update Listing")
        }

        // Error Message
        uiState.error?.let { error ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = error,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}