package com.rio.rustry.presentation.farm

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.rio.rustry.R
import com.rio.rustry.presentation.viewmodel.FarmViewModel
import com.rio.rustry.utils.FeatureFlagManager
import java.io.File
import android.speech.tts.TextToSpeech
import java.util.Locale
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FarmListingScreen(
    navController: NavController,
    viewModel: FarmViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val addFarmState = viewModel.addFarmState.collectAsState()
    val uiState = viewModel.uiState.collectAsState()
    val snackbarState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.value.error) {
        if (uiState.value.error != null) {
            snackbarState.showSnackbar(uiState.value.error!!)
            viewModel.clearError()
        }
    }

    LaunchedEffect(uiState.value.successMessage) {
        if (uiState.value.successMessage != null) {
            navController.popBackStack()
            viewModel.clearSuccessMessage()
        }
    }

    // TTS
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    LaunchedEffect(Unit) {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale("te", "IN")
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            tts?.stop()
            tts?.shutdown()
        }
    }

    // Permissions
    val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)

    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        viewModel.updatePhotoUri(uri?.toString() ?: "")
    }

    // Camera launcher
    val tempFile = remember { File.createTempFile("farm_photo", ".jpg", context.cacheDir) }
    val tempUri = remember { FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", tempFile) }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success) {
            viewModel.updatePhotoUri(tempUri.toString())
        }
    }

    // Feature flag manager
    val featureFlagManager = remember { FeatureFlagManager() }

    // Location dropdown options
    val locationOptions = listOf(
        "Andhra Pradesh - Visakhapatnam",
        "Andhra Pradesh - Vijayawada",
        "Andhra Pradesh - Guntur",
        "Andhra Pradesh - Nellore",
        "Andhra Pradesh - Kurnool",
        "Telangana - Hyderabad",
        "Telangana - Warangal",
        "Telangana - Nizamabad",
        "Telangana - Karimnagar",
        "Telangana - Khammam"
    )
    var expanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.add_new_farm)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                IconButton(
                    onClick = {
                        tts?.speak(
                            "Click to add a new farm",
                            TextToSpeech.QUEUE_FLUSH,
                            null,
                            null
                        )
                    }
                ) {
                    Icon(Icons.Default.VolumeUp, contentDescription = "Play voice tutorial")
                }
            }

            item {
                OutlinedTextField(
                    value = addFarmState.value.farmName,
                    onValueChange = { viewModel.updateFarmName(it) },
                    label = { Text(stringResource(R.string.farm_name)) },
                    isError = addFarmState.value.errors["name"] != null,
                    supportingText = {
                        addFarmState.value.errors["name"]?.let {
                            Text(it, color = MaterialTheme.colorScheme.error)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = addFarmState.value.location,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text(stringResource(R.string.location)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        isError = addFarmState.value.errors["location"] != null,
                        supportingText = {
                            addFarmState.value.errors["location"]?.let {
                                Text(it, color = MaterialTheme.colorScheme.error)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        locationOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    viewModel.updateLocation(option)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = addFarmState.value.size,
                    onValueChange = { viewModel.updateSize(it) },
                    label = { Text(stringResource(R.string.farm_size_acres)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = addFarmState.value.errors["size"] != null,
                    supportingText = {
                        addFarmState.value.errors["size"]?.let {
                            Text(it, color = MaterialTheme.colorScheme.error)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                OutlinedTextField(
                    value = addFarmState.value.ownershipDetails,
                    onValueChange = { viewModel.updateOwnershipDetails(it) },
                    label = { Text(stringResource(R.string.ownership_details)) },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (featureFlagManager.isFeatureEnabled("enable_photo_upload", defaultValue = true)) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.farm_photo_optional),
                                style = MaterialTheme.typography.titleMedium
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        if (cameraPermissionState.status.isGranted) {
                                            cameraLauncher.launch(tempUri)
                                        } else {
                                            cameraPermissionState.launchPermissionRequest()
                                        }
                                    },
                                    modifier = Modifier.weight(1f).height(56.dp)
                                ) {
                                    Icon(Icons.Default.CameraAlt, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(stringResource(R.string.take_photo))
                                }

                                OutlinedButton(
                                    onClick = { galleryLauncher.launch("image/*") },
                                    modifier = Modifier.weight(1f).height(56.dp)
                                ) {
                                    Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(stringResource(R.string.upload_photo))
                                }
                            }

                            if (addFarmState.value.photoUri.isNotEmpty()) {
                                AsyncImage(
                                    model = addFarmState.value.photoUri,
                                    contentDescription = "Selected photo",
                                    modifier = Modifier.size(200.dp).align(Alignment.CenterHorizontally)
                                )
                            }
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = { viewModel.addFarm() },
                    enabled = !addFarmState.value.isLoading,
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    if (addFarmState.value.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(stringResource(R.string.save_farm))
                }
            }
        }
    }
}
