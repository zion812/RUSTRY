package com.rio.rustry

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleAuthScreen(
    onAuthSuccess: () -> Unit
) {
    var phoneNumber by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var isOtpSent by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var verificationId by remember { mutableStateOf("") }
    
    val auth = FirebaseAuth.getInstance()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo/Title
        Icon(
            Icons.Default.Pets,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Text(
            "RUSTRY",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
        )
        
        Text(
            "Poultry Marketplace",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        if (!isOtpSent) {
            // Phone Number Input
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                label = { Text("Phone Number") },
                placeholder = { Text("+91 9876543210") },
                leadingIcon = {
                    Icon(Icons.Default.Phone, contentDescription = null)
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = {
                    if (phoneNumber.isNotBlank()) {
                        isLoading = true
                        errorMessage = ""
                        // For demo purposes, simulate OTP sent
                        isOtpSent = true
                        isLoading = false
                        verificationId = "demo_verification_id"
                    } else {
                        errorMessage = "Please enter a valid phone number"
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Send OTP")
                }
            }
        } else {
            // OTP Input
            Text(
                "Enter OTP sent to $phoneNumber",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            OutlinedTextField(
                value = otp,
                onValueChange = { otp = it },
                label = { Text("OTP") },
                placeholder = { Text("123456") },
                leadingIcon = {
                    Icon(Icons.Default.Lock, contentDescription = null)
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        isOtpSent = false
                        otp = ""
                        errorMessage = ""
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Back")
                }
                
                Button(
                    onClick = {
                        if (otp.isNotBlank()) {
                            isLoading = true
                            errorMessage = ""
                            // For demo purposes, accept any 6-digit OTP
                            if (otp.length == 6) {
                                onAuthSuccess()
                            } else {
                                errorMessage = "Please enter a valid 6-digit OTP"
                                isLoading = false
                            }
                        } else {
                            errorMessage = "Please enter the OTP"
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Verify")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            TextButton(
                onClick = {
                    // Resend OTP logic
                    errorMessage = "OTP resent successfully"
                }
            ) {
                Text("Resend OTP")
            }
        }
        
        // Error Message
        if (errorMessage.isNotBlank()) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (errorMessage.contains("success")) 
                        MaterialTheme.colorScheme.primaryContainer 
                    else 
                        MaterialTheme.colorScheme.errorContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    errorMessage,
                    modifier = Modifier.padding(16.dp),
                    color = if (errorMessage.contains("success")) 
                        MaterialTheme.colorScheme.onPrimaryContainer 
                    else 
                        MaterialTheme.colorScheme.onErrorContainer,
                    textAlign = TextAlign.Center
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Demo Note
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    "Demo Mode",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "Enter any phone number and use any 6-digit OTP to login",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}