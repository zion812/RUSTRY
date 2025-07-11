package com.rio.rustry.presentation.screen.fowl

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.rio.rustry.data.model.FowlBreed
import com.rio.rustry.presentation.viewmodel.AddFowlViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFowlScreen(
    onNavigateBack: () -> Unit,
    onFowlAdded: () -> Unit,
    addFowlViewModel: AddFowlViewModel = viewModel()
) {
    val uiState by addFowlViewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    // Form state
    var breed by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var isTraceable by remember { mutableStateOf(true) }
    var parentId by remember { mutableStateOf("") }
    var dateOfBirth by remember { mutableStateOf(Date()) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedProofUri by remember { mutableStateOf<Uri?>(null) }
    var showBreedDropdown by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    
    // Image picker launchers
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }
    
    val proofPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedProofUri = uri
    }
    
    // Date formatter
    val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    
    // Handle success
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onFowlAdded()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Add Fowl",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
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
            // Error display
            if (uiState.error != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = uiState.error!!,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            
            // Traceability Section
            Card {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Traceability Status",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Column(Modifier.selectableGroup()) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .selectable(
                                    selected = isTraceable,
                                    onClick = { isTraceable = true },
                                    role = Role.RadioButton
                                )
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isTraceable,
                                onClick = null
                            )
                            Text(
                                text = "✅ Traceable (with parent lineage)",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(start = 16.dp)
                            )
                        }
                        
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .selectable(
                                    selected = !isTraceable,
                                    onClick = { isTraceable = false },
                                    role = Role.RadioButton
                                )
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = !isTraceable,
                                onClick = null
                            )
                            Text(
                                text = "⚠️ Non-traceable",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(start = 16.dp)
                            )
                        }
                    }
                    
                    if (!isTraceable && uiState.nonTraceableCount >= 5) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "⚠️ You have reached the limit of 5 non-traceable fowls (${uiState.nonTraceableCount}/5)",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
            
            // Parent ID (only for traceable fowls)
            if (isTraceable) {
                OutlinedTextField(
                    value = parentId,
                    onValueChange = { parentId = it },
                    label = { Text("Parent Fowl ID (optional)") },
                    placeholder = { Text("Enter parent fowl ID for lineage") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(Icons.Default.Share, contentDescription = null)
                    }
                )
            }
            
            // Breed Selection
            ExposedDropdownMenuBox(
                expanded = showBreedDropdown,
                onExpandedChange = { showBreedDropdown = !showBreedDropdown }
            ) {
                OutlinedTextField(
                    value = breed,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Breed") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showBreedDropdown) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = showBreedDropdown,
                    onDismissRequest = { showBreedDropdown = false }
                ) {
                    FowlBreed.values().forEach { fowlBreed ->
                        DropdownMenuItem(
                            text = { Text(fowlBreed.displayName) },
                            onClick = {
                                breed = fowlBreed.displayName
                                showBreedDropdown = false
                            }
                        )
                    }
                }
            }
            
            // Date of Birth
            OutlinedTextField(
                value = dateFormatter.format(dateOfBirth),
                onValueChange = { },
                label = { Text("Date of Birth") },
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(Icons.Default.DateRange, contentDescription = null)
                },
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Select date")
                    }
                }
            )
            
            // Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                placeholder = { Text("Describe your fowl...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )
            
            // Location
            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Location") },
                placeholder = { Text("Farm location or city") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(Icons.Default.LocationOn, contentDescription = null)
                }
            )
            
            // Price
            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                label = { Text("Price (USD)") },
                placeholder = { Text("0.00") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Text("$", style = MaterialTheme.typography.bodyLarge)
                }
            )
            
            // Image Upload Section
            Card {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Photos",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Main fowl image
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Fowl Photo",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            if (selectedImageUri != null) {
                                AsyncImage(
                                    model = selectedImageUri,
                                    contentDescription = "Selected fowl image",
                                    modifier = Modifier
                                        .size(120.dp)
                                        .fillMaxWidth(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Card(
                                    modifier = Modifier
                                        .size(120.dp)
                                        .fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Add,
                                            contentDescription = null,
                                            modifier = Modifier.size(48.dp)
                                        )
                                    }
                                }
                            }
                            
                            Button(
                                onClick = { imagePickerLauncher.launch("image/*") },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Select Photo")
                            }
                        }
                        
                        // Proof image (for traceable fowls)
                        if (isTraceable) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Proof/Records",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                if (selectedProofUri != null) {
                                    AsyncImage(
                                        model = selectedProofUri,
                                        contentDescription = "Selected proof image",
                                        modifier = Modifier
                                            .size(120.dp)
                                            .fillMaxWidth(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Card(
                                        modifier = Modifier
                                            .size(120.dp)
                                            .fillMaxWidth(),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                                        )
                                    ) {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Default.Info,
                                                contentDescription = null,
                                                modifier = Modifier.size(48.dp)
                                            )
                                        }
                                    }
                                }
                                
                                Button(
                                    onClick = { proofPickerLauncher.launch("image/*") },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Select Proof")
                                }
                            }
                        }
                    }
                }
            }
            
            // Save Button
            Button(
                onClick = {
                    addFowlViewModel.addFowl(
                        breed = breed,
                        description = description,
                        location = location,
                        price = price.toDoubleOrNull() ?: 0.0,
                        isTraceable = isTraceable,
                        parentId = if (isTraceable && parentId.isNotBlank()) parentId else null,
                        dateOfBirth = dateOfBirth,
                        imageUri = selectedImageUri,
                        proofUri = if (isTraceable) selectedProofUri else null
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading && breed.isNotBlank() && location.isNotBlank() && 
                         selectedImageUri != null && (!isTraceable || uiState.nonTraceableCount < 5)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Add Fowl")
                }
            }
        }
    }
    
    // Date picker dialog would go here (simplified for now)
    // You can implement a proper date picker using a library like compose-material-dialogs
}