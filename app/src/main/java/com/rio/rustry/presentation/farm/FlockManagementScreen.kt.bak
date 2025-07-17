package com.rio.rustry.presentation.farm

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.rio.rustry.R
import com.rio.rustry.domain.model.Flock
import com.rio.rustry.presentation.viewmodel.FlockViewModel
import com.rio.rustry.utils.ValidationUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FlockManagementScreen(
    navController: NavController,
    farmId: String,
    viewModel: FlockViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // State
    val flocks by viewModel.flocks.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingFlock by remember { mutableStateOf<Flock?>(null) }
    
    // Form state
    var breed by remember { mutableStateOf("") }
    var ageMonths by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var maleCount by remember { mutableStateOf("") }
    var femaleCount by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var healthStatus by remember { mutableStateOf("HEALTHY") }
    var productionType by remember { mutableStateOf("MEAT") }
    var housingType by remember { mutableStateOf("COOP") }
    var feedType by remember { mutableStateOf("COMMERCIAL") }
    
    // Validation state
    var breedError by remember { mutableStateOf<String?>(null) }
    var quantityError by remember { mutableStateOf<String?>(null) }
    var ageError by remember { mutableStateOf<String?>(null) }
    var showValidationErrors by remember { mutableStateOf(false) }
    
    // Dropdown options
    val breedOptions = listOf(
        "Aseel", "Kadaknath", "Brahma", "Leghorn", "Rhode Island Red",
        "Cochin", "Bantam", "Silkie", "Orpington", "Sussex", "Local Desi"
    )
    val healthStatusOptions = listOf("HEALTHY", "SICK", "QUARANTINE", "RECOVERING")
    val productionTypeOptions = listOf("MEAT", "EGGS", "BREEDING", "SHOW", "DUAL_PURPOSE")
    val housingTypeOptions = listOf("COOP", "FREE_RANGE", "CAGE", "BARN", "SEMI_INTENSIVE")
    val feedTypeOptions = listOf("COMMERCIAL", "ORGANIC", "MIXED", "HOMEMADE", "NATURAL")
    
    var breedExpanded by remember { mutableStateOf(false) }
    var healthExpanded by remember { mutableStateOf(false) }
    var productionExpanded by remember { mutableStateOf(false) }
    var housingExpanded by remember { mutableStateOf(false) }
    var feedExpanded by remember { mutableStateOf(false) }
    
    // Camera permission
    val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)
    
    // Photo launchers
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        photoUri = uri
    }
    
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success) {
            // Photo captured successfully
        }
    }
    
    // Load flocks for this farm
    LaunchedEffect(farmId) {
        viewModel.loadFlocksByFarm(farmId)
    }
    
    // Validation functions
    fun validateForm(): Boolean {
        showValidationErrors = true
        
        breedError = ValidationUtils.validateFlockBreed(breed)
        quantityError = ValidationUtils.validateFlockQuantity(quantity)
        ageError = ValidationUtils.validateFlockAge(ageMonths)
        
        return breedError == null && quantityError == null && ageError == null
    }
    
    fun resetForm() {
        breed = ""
        ageMonths = ""
        quantity = ""
        maleCount = ""
        femaleCount = ""
        notes = ""
        photoUri = null
        healthStatus = "HEALTHY"
        productionType = "MEAT"
        housingType = "COOP"
        feedType = "COMMERCIAL"
        showValidationErrors = false
        breedError = null
        quantityError = null
        ageError = null
        editingFlock = null
    }
    
    fun saveFlock() {
        if (!validateForm()) return
        
        scope.launch {
            try {
                val flock = Flock(
                    id = editingFlock?.id ?: "",
                    farmId = farmId,
                    breed = breed.trim(),
                    ageMonths = ageMonths.toIntOrNull() ?: 0,
                    quantity = quantity.toInt(),
                    maleCount = maleCount.toIntOrNull() ?: 0,
                    femaleCount = femaleCount.toIntOrNull() ?: 0,
                    notes = notes.trim(),
                    photoUrls = if (photoUri != null) listOf(photoUri.toString()) else emptyList(),
                    healthStatus = healthStatus,
                    productionType = productionType,
                    housingType = housingType,
                    feedType = feedType,
                    createdAt = editingFlock?.createdAt ?: System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                
                if (editingFlock != null) {
                    viewModel.updateFlock(flock)
                } else {
                    viewModel.addFlock(flock)
                }
                
                resetForm()
                showAddDialog = false
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    fun editFlock(flock: Flock) {
        editingFlock = flock
        breed = flock.breed
        ageMonths = flock.ageMonths.toString()
        quantity = flock.quantity.toString()
        maleCount = flock.maleCount.toString()
        femaleCount = flock.femaleCount.toString()
        notes = flock.notes
        healthStatus = flock.healthStatus
        productionType = flock.productionType
        housingType = flock.housingType
        feedType = flock.feedType
        showAddDialog = true
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.flock_management)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshFlocks(farmId) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    resetForm()
                    showAddDialog = true 
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Flock")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Statistics Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.flock_statistics),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatisticItem(
                            label = stringResource(R.string.total_flocks),
                            value = flocks.size.toString()
                        )
                        StatisticItem(
                            label = stringResource(R.string.total_birds),
                            value = flocks.sumOf { it.quantity }.toString()
                        )
                        StatisticItem(
                            label = stringResource(R.string.healthy_flocks),
                            value = flocks.count { it.healthStatus == "HEALTHY" }.toString()
                        )
                    }
                }
            }
            
            // Flocks List
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (flocks.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Pets,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.no_flocks_found),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.tap_add_to_create_flock),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(flocks) { flock ->
                        FlockCard(
                            flock = flock,
                            onEdit = { editFlock(flock) },
                            onDelete = { viewModel.deleteFlock(flock.id) },
                            onViewDetails = { 
                                // Navigate to flock details
                            }
                        )
                    }
                }
            }
        }
        
        // Add/Edit Flock Dialog
        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { 
                    resetForm()
                    showAddDialog = false 
                },
                title = { 
                    Text(
                        if (editingFlock != null) 
                            stringResource(R.string.edit_flock) 
                        else 
                            stringResource(R.string.add_new_flock)
                    ) 
                },
                text = {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            // Breed Dropdown
                            ExposedDropdownMenuBox(
                                expanded = breedExpanded,
                                onExpandedChange = { breedExpanded = !breedExpanded }
                            ) {
                                OutlinedTextField(
                                    value = breed,
                                    onValueChange = { },
                                    readOnly = true,
                                    label = { Text(stringResource(R.string.breed)) },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = breedExpanded) },
                                    isError = breedError != null && showValidationErrors,
                                    supportingText = {
                                        if (breedError != null && showValidationErrors) {
                                            Text(breedError!!, color = MaterialTheme.colorScheme.error)
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor()
                                )
                                
                                ExposedDropdownMenu(
                                    expanded = breedExpanded,
                                    onDismissRequest = { breedExpanded = false }
                                ) {
                                    breedOptions.forEach { option ->
                                        DropdownMenuItem(
                                            text = { Text(option) },
                                            onClick = {
                                                breed = option
                                                breedExpanded = false
                                                if (showValidationErrors) {
                                                    breedError = ValidationUtils.validateFlockBreed(option)
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        
                        item {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Quantity
                                OutlinedTextField(
                                    value = quantity,
                                    onValueChange = { 
                                        quantity = it
                                        if (showValidationErrors) {
                                            quantityError = ValidationUtils.validateFlockQuantity(it)
                                        }
                                    },
                                    label = { Text(stringResource(R.string.total_quantity)) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    isError = quantityError != null && showValidationErrors,
                                    supportingText = {
                                        if (quantityError != null && showValidationErrors) {
                                            Text(quantityError!!, color = MaterialTheme.colorScheme.error)
                                        }
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                                
                                // Age
                                OutlinedTextField(
                                    value = ageMonths,
                                    onValueChange = { 
                                        ageMonths = it
                                        if (showValidationErrors) {
                                            ageError = ValidationUtils.validateFlockAge(it)
                                        }
                                    },
                                    label = { Text(stringResource(R.string.age_months)) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    isError = ageError != null && showValidationErrors,
                                    supportingText = {
                                        if (ageError != null && showValidationErrors) {
                                            Text(ageError!!, color = MaterialTheme.colorScheme.error)
                                        }
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        
                        item {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Male Count
                                OutlinedTextField(
                                    value = maleCount,
                                    onValueChange = { maleCount = it },
                                    label = { Text(stringResource(R.string.male_count)) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )
                                
                                // Female Count
                                OutlinedTextField(
                                    value = femaleCount,
                                    onValueChange = { femaleCount = it },
                                    label = { Text(stringResource(R.string.female_count)) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        
                        item {
                            // Health Status Dropdown
                            ExposedDropdownMenuBox(
                                expanded = healthExpanded,
                                onExpandedChange = { healthExpanded = !healthExpanded }
                            ) {
                                OutlinedTextField(
                                    value = healthStatus,
                                    onValueChange = { },
                                    readOnly = true,
                                    label = { Text(stringResource(R.string.health_status)) },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = healthExpanded) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor()
                                )
                                
                                ExposedDropdownMenu(
                                    expanded = healthExpanded,
                                    onDismissRequest = { healthExpanded = false }
                                ) {
                                    healthStatusOptions.forEach { option ->
                                        DropdownMenuItem(
                                            text = { Text(option) },
                                            onClick = {
                                                healthStatus = option
                                                healthExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        
                        item {
                            // Production Type Dropdown
                            ExposedDropdownMenuBox(
                                expanded = productionExpanded,
                                onExpandedChange = { productionExpanded = !productionExpanded }
                            ) {
                                OutlinedTextField(
                                    value = productionType,
                                    onValueChange = { },
                                    readOnly = true,
                                    label = { Text(stringResource(R.string.production_type)) },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = productionExpanded) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor()
                                )
                                
                                ExposedDropdownMenu(
                                    expanded = productionExpanded,
                                    onDismissRequest = { productionExpanded = false }
                                ) {
                                    productionTypeOptions.forEach { option ->
                                        DropdownMenuItem(
                                            text = { Text(option) },
                                            onClick = {
                                                productionType = option
                                                productionExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        
                        item {
                            // Notes
                            OutlinedTextField(
                                value = notes,
                                onValueChange = { notes = it },
                                label = { Text(stringResource(R.string.notes_optional)) },
                                maxLines = 3,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        
                        item {
                            // Photo Upload Section
                            Card {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.flock_photo_proof),
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                    
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = {
                                                if (cameraPermissionState.status.isGranted) {
                                                    // Launch camera
                                                } else {
                                                    cameraPermissionState.launchPermissionRequest()
                                                }
                                            },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Default.CameraAlt, contentDescription = null)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(stringResource(R.string.camera))
                                        }
                                        
                                        OutlinedButton(
                                            onClick = { galleryLauncher.launch("image/*") },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(stringResource(R.string.gallery))
                                        }
                                    }
                                    
                                    if (photoUri != null) {
                                        Text(
                                            text = stringResource(R.string.photo_selected),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { saveFlock() },
                        enabled = !uiState.isLoading
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            if (editingFlock != null) 
                                stringResource(R.string.update) 
                            else 
                                stringResource(R.string.save)
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { 
                            resetForm()
                            showAddDialog = false 
                        }
                    ) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
        }
    }
    
    // Show error messages
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // Show snackbar or toast
            viewModel.clearError()
        }
    }
    
    // Show success messages
    uiState.successMessage?.let { message ->
        LaunchedEffect(message) {
            // Show snackbar or toast
            viewModel.clearSuccessMessage()
        }
    }
}

@Composable
private fun StatisticItem(
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FlockCard(
    flock: Flock,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onViewDetails: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onViewDetails
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = flock.breed,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${flock.quantity} birds • ${flock.ageMonths} months",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.HealthAndSafety,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = when (flock.healthStatus) {
                                "HEALTHY" -> MaterialTheme.colorScheme.primary
                                "SICK" -> MaterialTheme.colorScheme.error
                                "QUARANTINE" -> MaterialTheme.colorScheme.tertiary
                                else -> MaterialTheme.colorScheme.outline
                            }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = flock.healthStatus,
                            style = MaterialTheme.typography.bodySmall,
                            color = when (flock.healthStatus) {
                                "HEALTHY" -> MaterialTheme.colorScheme.primary
                                "SICK" -> MaterialTheme.colorScheme.error
                                "QUARANTINE" -> MaterialTheme.colorScheme.tertiary
                                else -> MaterialTheme.colorScheme.outline
                            }
                        )
                    }
                }
                
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            
            if (flock.notes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = flock.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}