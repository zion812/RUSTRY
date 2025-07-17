// generated/phase3/app/src/main/java/com/rio/rustry/transfers/GenerateProofScreen.kt

package com.rio.rustry.transfers

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenerateProofScreen(
    transferId: String,
    onNavigateBack: () -> Unit,
    viewModel: TransferViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var nfcScanned by remember { mutableStateOf(false) }
    var photoTaken by remember { mutableStateOf(false) }
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(transferId) {
        viewModel.loadTransfer(transferId)
    }

    LaunchedEffect(transferId) {
        // Generate QR Code
        try {
            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(transferId, BarcodeFormat.QR_CODE, 512, 512)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
                }
            }
            qrBitmap = bitmap
        } catch (e: Exception) {
            // Handle QR generation error
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "Generate Proof",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (uiState) {
            is TransferUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is TransferUiState.TransferLoaded -> {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Transfer Info
                    Card {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Transfer Details",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Transfer ID: ${uiState.transfer.id}")
                            Text("Fowl: ${uiState.transfer.fowlId}")
                            Text("Status: ${uiState.transfer.status}")
                        }
                    }

                    // NFC Scan Step
                    Card {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Nfc,
                                    contentDescription = null,
                                    tint = if (nfcScanned) MaterialTheme.colorScheme.primary 
                                          else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "NFC Tag Scan",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            if (!nfcScanned) {
                                Button(
                                    onClick = {
                                        // Simulate NFC scan
                                        nfcScanned = true
                                        viewModel.scanNfcTag(transferId)
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Scan NFC Tag")
                                }
                            } else {
                                Text(
                                    text = "✓ NFC Tag Scanned",
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    // Photo Step
                    Card {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Camera,
                                    contentDescription = null,
                                    tint = if (photoTaken) MaterialTheme.colorScheme.primary 
                                          else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Photo Verification",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            if (!photoTaken) {
                                Button(
                                    onClick = {
                                        // Simulate photo capture
                                        photoTaken = true
                                        viewModel.capturePhoto(transferId)
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Take Photo")
                                }
                            } else {
                                Text(
                                    text = "✓ Photo Captured",
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    // QR Code
                    if (nfcScanned && photoTaken) {
                        Card {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.QrCode, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Transfer QR Code",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                qrBitmap?.let { bitmap ->
                                    Image(
                                        bitmap = bitmap.asImageBitmap(),
                                        contentDescription = "Transfer QR Code",
                                        modifier = Modifier.size(200.dp)
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Button(
                                    onClick = {
                                        viewModel.shareTransferProof(transferId)
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Share Transfer Proof")
                                }
                            }
                        }
                    }

                    // Generate Proof Button
                    if (nfcScanned && photoTaken) {
                        Button(
                            onClick = {
                                viewModel.generateTransferProof(transferId)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Generate Signed Proof")
                        }
                    }
                }
            }
            is TransferUiState.Error -> {
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