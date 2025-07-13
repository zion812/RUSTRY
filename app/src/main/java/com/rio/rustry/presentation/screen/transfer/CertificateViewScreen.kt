package com.rio.rustry.presentation.screen.transfer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.NumberFormat
import java.util.Locale
import com.rio.rustry.data.model.*
import com.rio.rustry.presentation.viewmodel.CertificateViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CertificateViewScreen(
    certificateId: String,
    onNavigateBack: () -> Unit,
    onDownloadPDF: (String) -> Unit,
    onShareCertificate: (String) -> Unit,
    viewModel: CertificateViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    LaunchedEffect(certificateId) {
        viewModel.loadCertificate(certificateId)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Digital Certificate") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (uiState.certificate != null) {
                        IconButton(onClick = { onShareCertificate(certificateId) }) {
                            Icon(Icons.Default.Share, contentDescription = "Share")
                        }
                        IconButton(onClick = { onDownloadPDF(certificateId) }) {
                            Icon(Icons.Default.Download, contentDescription = "Download")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.certificate == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Error,
                        contentDescription = "Error",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = "Certificate not found",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = uiState.error ?: "The requested certificate could not be loaded",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            val certificate = uiState.certificate!!
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Certificate Header
                CertificateHeaderCard(certificate = certificate)
                
                // Validity Status
                CertificateValidityCard(certificate = certificate)
                
                // Fowl Information
                FowlDetailsCard(fowlDetails = certificate.fowlDetails ?: FowlCertificateDetails())
                
                // Ownership Information
                OwnershipDetailsCard(certificate = certificate)
                
                // Health Summary
                HealthSummaryCard(healthSummary = certificate.healthSummary ?: CertificateHealthSummary())
                
                // Transfer Details
                if (certificate.transferDetails.transferType != TransferType.SALE || certificate.transferDetails.transferPrice > 0) {
                    TransferDetailsCard(transferDetails = certificate.transferDetails)
                }
                
                // Lineage History
                if (certificate.lineageHistory.isNotEmpty()) {
                    LineageHistoryCard(lineageHistory = certificate.lineageHistory)
                }
                
                // Certificate Metadata
                CertificateMetadataCard(certificate = certificate)
                
                // Verification Section
                VerificationCard(certificate = certificate)
                
                // Action Buttons
                ActionButtonsCard(
                    certificate = certificate,
                    onDownloadPDF = { onDownloadPDF(certificateId) },
                    onShareCertificate = { onShareCertificate(certificateId) },
                    onVerifyOnline = { viewModel.verifyCertificateOnline(certificateId) }
                )
            }
        }
    }
}

@Composable
private fun CertificateHeaderCard(certificate: DigitalCertificate) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                Icons.Default.Assignment,
                contentDescription = "Certificate",
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Text(
                text = "DIGITAL OWNERSHIP CERTIFICATE",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = "Certificate No: ${certificate.certificateNumber}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Text(
                text = "Issued by ${certificate.issuedBy}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun CertificateValidityCard(certificate: DigitalCertificate) {
    val isValid = certificate.isValid && certificate.revokedAt == null
    val isExpired = certificate.validUntil != null && certificate.validUntil < System.currentTimeMillis()
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                !isValid -> Color(0xFFF44336).copy(alpha = 0.1f)
                isExpired -> Color(0xFFFF9800).copy(alpha = 0.1f)
                else -> Color(0xFF4CAF50).copy(alpha = 0.1f)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                when {
                    !isValid -> Icons.Default.Error
                    isExpired -> Icons.Default.Warning
                    else -> Icons.Default.Verified
                },
                contentDescription = "Validity",
                tint = when {
                    !isValid -> Color(0xFFF44336)
                    isExpired -> Color(0xFFFF9800)
                    else -> Color(0xFF4CAF50)
                },
                modifier = Modifier.size(24.dp)
            )
            
            Column {
                Text(
                    text = when {
                        !isValid -> "Certificate Revoked"
                        isExpired -> "Certificate Expired"
                        else -> "Valid Certificate"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        !isValid -> Color(0xFFF44336)
                        isExpired -> Color(0xFFFF9800)
                        else -> Color(0xFF4CAF50)
                    }
                )
                
                Text(
                    text = when {
                        !isValid -> certificate.revokedReason.ifEmpty { "This certificate has been revoked" }
                        isExpired -> "This certificate has expired"
                        else -> "This certificate is valid and authentic"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                if (certificate.validUntil != null) {
                    Text(
                        text = "Valid until: ${SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(certificate.validUntil))}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun FowlDetailsCard(fowlDetails: FowlCertificateDetails) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Fowl Information",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            CertificateDetailRow(
                label = "Breed:",
                value = fowlDetails.breed
            )
            
            CertificateDetailRow(
                label = "Date of Birth:",
                value = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(fowlDetails.dateOfBirth))
            )
            
            CertificateDetailRow(
                label = "Traceability:",
                value = if (fowlDetails.isTraceable) "Traceable" else "Non-traceable"
            )
            
            if (fowlDetails.gender.isNotEmpty()) {
                CertificateDetailRow(
                    label = "Gender:",
                    value = fowlDetails.gender
                )
            }
            
            if (fowlDetails.weight > 0) {
                CertificateDetailRow(
                    label = "Weight:",
                    value = "${fowlDetails.weight} kg"
                )
            }
            
            if (fowlDetails.registrationNumber.isNotEmpty()) {
                CertificateDetailRow(
                    label = "Registration No:",
                    value = fowlDetails.registrationNumber
                )
            }
            
            if (fowlDetails.parentIds.isNotEmpty()) {
                HorizontalDivider()
                Text(
                    text = "Parent IDs:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                fowlDetails.parentIds.forEach { parentId ->
                    Text(
                        text = "• $parentId",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun OwnershipDetailsCard(certificate: DigitalCertificate) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Ownership Information",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            CertificateDetailRow(
                label = "Current Owner:",
                value = certificate.currentOwnerName
            )
            
            if (certificate.previousOwnerName.isNotEmpty()) {
                CertificateDetailRow(
                    label = "Previous Owner:",
                    value = certificate.previousOwnerName
                )
            }
            
            CertificateDetailRow(
                label = "Ownership Date:",
                value = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(certificate.transferDetails.transferDate))
            )
        }
    }
}

@Composable
private fun HealthSummaryCard(healthSummary: CertificateHealthSummary) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Health Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Health Score:")
                Text(
                    text = "${healthSummary.healthScore}/100",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        healthSummary.healthScore >= 80 -> Color(0xFF4CAF50)
                        healthSummary.healthScore >= 60 -> Color(0xFFFF9800)
                        else -> Color(0xFFF44336)
                    }
                )
            }
            
            CertificateDetailRow(
                label = "Vaccination Status:",
                value = healthSummary.vaccinationStatus.displayName
            )
            
            if (healthSummary.lastVaccinationDate != null) {
                CertificateDetailRow(
                    label = "Last Vaccination:",
                    value = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(healthSummary.lastVaccinationDate))
                )
            }
            
            CertificateDetailRow(
                label = "Health Records:",
                value = "${healthSummary.healthRecordsCount} records"
            )
            
            CertificateDetailRow(
                label = "Vet Certificates:",
                value = "${healthSummary.vetCertificates} certificates"
            )
            
            if (healthSummary.currentMedications.isNotEmpty()) {
                HorizontalDivider()
                Text(
                    text = "Current Medications:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                healthSummary.currentMedications.forEach { medication ->
                    Text(
                        text = "• $medication",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun TransferDetailsCard(transferDetails: TransferCertificateDetails) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Transfer Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            CertificateDetailRow(
                label = "Transfer Type:",
                value = transferDetails.transferType.displayName
            )
            
            if (transferDetails.transferPrice > 0) {
                CertificateDetailRow(
                    label = "Transfer Price:",
                    value = NumberFormat.getCurrencyInstance(Locale("en", "IN")).format(transferDetails.transferPrice)
                )
            }
            
            CertificateDetailRow(
                label = "Transfer Date:",
                value = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(transferDetails.transferDate))
            )
            
            CertificateDetailRow(
                label = "Verification Method:",
                value = transferDetails.verificationMethod.displayName
            )
            
            if (transferDetails.witnessName.isNotEmpty()) {
                CertificateDetailRow(
                    label = "Witness:",
                    value = transferDetails.witnessName
                )
            }
            
            if (transferDetails.transferNotes.isNotEmpty()) {
                HorizontalDivider()
                Text(
                    text = "Transfer Notes:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = transferDetails.transferNotes,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun LineageHistoryCard(lineageHistory: List<LineageEntry>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Ownership History",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            lineageHistory.forEach { entry ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Generation ${entry.generation}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Owner: ${entry.ownerName}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Period: ${SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(Date().apply { time = entry.ownershipStartDate })} - ${
                                entry.ownershipEndDate?.let { 
                                    SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(Date().apply { time = it })
                                } ?: "Present"
                            }",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (entry.transferReason.isNotEmpty()) {
                            Text(
                                text = "Reason: ${entry.transferReason}",
                                style = MaterialTheme.typography.bodySmall,
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
private fun CertificateMetadataCard(certificate: DigitalCertificate) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Certificate Information",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            CertificateDetailRow(
                label = "Certificate ID:",
                value = certificate.id
            )
            
            CertificateDetailRow(
                label = "Issue Date:",
                value = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(certificate.issueDate))
            )
            
            CertificateDetailRow(
                label = "Version:",
                value = certificate.certificateVersion
            )
            
            if (certificate.blockchainHash.isNotEmpty()) {
                CertificateDetailRow(
                    label = "Blockchain Hash:",
                    value = certificate.blockchainHash.take(16) + "..."
                )
            }
        }
    }
}

@Composable
private fun VerificationCard(certificate: DigitalCertificate) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.QrCode,
                    contentDescription = "QR Code",
                    tint = MaterialTheme.colorScheme.tertiary
                )
                Text(
                    text = "Verification",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
            
            Text(
                text = "Scan the QR code or visit the verification URL to verify this certificate online.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
            
            // QR Code placeholder (would be actual QR code in real implementation)
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .align(Alignment.CenterHorizontally),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.QrCode,
                            contentDescription = "QR Code",
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
            
            Text(
                text = certificate.verificationUrl,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ActionButtonsCard(
    certificate: DigitalCertificate,
    onDownloadPDF: () -> Unit,
    onShareCertificate: () -> Unit,
    onVerifyOnline: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onDownloadPDF,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.Download,
                        contentDescription = "Download",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Download")
                }
                
                OutlinedButton(
                    onClick = onShareCertificate,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.Share,
                        contentDescription = "Share",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Share")
                }
            }
            
            Button(
                onClick = onVerifyOnline,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    Icons.Default.Verified,
                    contentDescription = "Verify",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Verify Online")
            }
        }
    }
}

@Composable
private fun CertificateDetailRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End
        )
    }
}