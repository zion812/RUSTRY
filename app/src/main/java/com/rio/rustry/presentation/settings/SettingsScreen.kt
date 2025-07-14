package com.rio.rustry.presentation.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rio.rustry.presentation.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToProfile: () -> Unit = {},
    onNavigateToPrivacy: () -> Unit = {},
    onNavigateToHelp: () -> Unit = {},
    viewModel: SettingsViewModel = viewModel()
) {
    val context = LocalContext.current
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
    val voiceCommandsEnabled by viewModel.voiceCommandsEnabled.collectAsState()
    val offlineModeEnabled by viewModel.offlineModeEnabled.collectAsState()
    
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Account Section
            item {
                SettingsSectionHeader("Account")
            }
            
            item {
                SettingsItem(
                    title = "Profile",
                    subtitle = "Manage your profile information",
                    icon = Icons.Default.Person,
                    onClick = onNavigateToProfile
                )
            }
            
            item {
                SettingsItem(
                    title = "Farm Information",
                    subtitle = "Update farm details and location",
                    icon = Icons.Default.Agriculture,
                    onClick = { /* Navigate to farm settings */ }
                )
            }
            
            // Preferences Section
            item {
                SettingsSectionHeader("Preferences")
            }
            
            item {
                SettingsToggleItem(
                    title = "Dark Mode",
                    subtitle = "Use dark theme",
                    icon = Icons.Default.DarkMode,
                    checked = isDarkMode,
                    onCheckedChange = { viewModel.setDarkMode(it) }
                )
            }
            
            item {
                SettingsItem(
                    title = "Language",
                    subtitle = selectedLanguage,
                    icon = Icons.Default.Language,
                    onClick = { showLanguageDialog = true }
                )
            }
            
            item {
                SettingsToggleItem(
                    title = "Voice Commands",
                    subtitle = "Enable hands-free operation",
                    icon = Icons.Default.Mic,
                    checked = voiceCommandsEnabled,
                    onCheckedChange = { viewModel.setVoiceCommandsEnabled(it) }
                )
            }
            
            // Notifications Section
            item {
                SettingsSectionHeader("Notifications")
            }
            
            item {
                SettingsToggleItem(
                    title = "Push Notifications",
                    subtitle = "Receive important updates",
                    icon = Icons.Default.Notifications,
                    checked = notificationsEnabled,
                    onCheckedChange = { viewModel.setNotificationsEnabled(it) }
                )
            }
            
            item {
                SettingsItem(
                    title = "Notification Settings",
                    subtitle = "Customize notification preferences",
                    icon = Icons.Default.NotificationsActive,
                    onClick = { /* Navigate to notification settings */ }
                )
            }
            
            // Data & Storage Section
            item {
                SettingsSectionHeader("Data & Storage")
            }
            
            item {
                SettingsToggleItem(
                    title = "Offline Mode",
                    subtitle = "Work without internet connection",
                    icon = Icons.Default.CloudOff,
                    checked = offlineModeEnabled,
                    onCheckedChange = { viewModel.setOfflineModeEnabled(it) }
                )
            }
            
            item {
                SettingsItem(
                    title = "Data Sync",
                    subtitle = "Manage data synchronization",
                    icon = Icons.Default.Sync,
                    onClick = { viewModel.syncData() }
                )
            }
            
            item {
                SettingsItem(
                    title = "Storage Usage",
                    subtitle = "View app storage usage",
                    icon = Icons.Default.Storage,
                    onClick = { /* Navigate to storage settings */ }
                )
            }
            
            // Security Section
            item {
                SettingsSectionHeader("Security & Privacy")
            }
            
            item {
                SettingsItem(
                    title = "Privacy Policy",
                    subtitle = "Read our privacy policy",
                    icon = Icons.Default.PrivacyTip,
                    onClick = onNavigateToPrivacy
                )
            }
            
            item {
                SettingsItem(
                    title = "Terms of Service",
                    subtitle = "View terms and conditions",
                    icon = Icons.Default.Description,
                    onClick = { /* Navigate to terms */ }
                )
            }
            
            item {
                SettingsItem(
                    title = "Data Export",
                    subtitle = "Export your farm data",
                    icon = Icons.Default.Download,
                    onClick = { viewModel.exportData() }
                )
            }
            
            // Support Section
            item {
                SettingsSectionHeader("Support")
            }
            
            item {
                SettingsItem(
                    title = "Help & FAQ",
                    subtitle = "Get help and find answers",
                    icon = Icons.Default.Help,
                    onClick = onNavigateToHelp
                )
            }
            
            item {
                SettingsItem(
                    title = "Contact Support",
                    subtitle = "Get in touch with our team",
                    icon = Icons.Default.ContactSupport,
                    onClick = { /* Open contact support */ }
                )
            }
            
            item {
                SettingsItem(
                    title = "Send Feedback",
                    subtitle = "Help us improve RUSTRY",
                    icon = Icons.Default.Feedback,
                    onClick = { /* Open feedback form */ }
                )
            }
            
            // About Section
            item {
                SettingsSectionHeader("About")
            }
            
            item {
                SettingsItem(
                    title = "App Version",
                    subtitle = "1.0.0-rc1",
                    icon = Icons.Default.Info,
                    onClick = { /* Show version info */ }
                )
            }
            
            item {
                SettingsItem(
                    title = "Check for Updates",
                    subtitle = "Update to the latest version",
                    icon = Icons.Default.Update,
                    onClick = { viewModel.checkForUpdates() }
                )
            }
            
            // Logout Section
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    SettingsItem(
                        title = "Sign Out",
                        subtitle = "Sign out of your account",
                        icon = Icons.Default.Logout,
                        onClick = { showLogoutDialog = true },
                        titleColor = MaterialTheme.colorScheme.onErrorContainer,
                        subtitleColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
    
    // Language Selection Dialog
    if (showLanguageDialog) {
        LanguageSelectionDialog(
            currentLanguage = selectedLanguage,
            onLanguageSelected = { language ->
                viewModel.setLanguage(language)
                showLanguageDialog = false
            },
            onDismiss = { showLanguageDialog = false }
        )
    }
    
    // Logout Confirmation Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Sign Out") },
            text = { Text("Are you sure you want to sign out? Any unsaved changes will be lost.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.signOut()
                        showLogoutDialog = false
                    }
                ) {
                    Text("Sign Out")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    titleColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
    subtitleColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = titleColor
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = subtitleColor
                )
            }
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SettingsToggleItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LanguageSelectionDialog(
    currentLanguage: String,
    onLanguageSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val languages = listOf(
        "English" to "en",
        "తెలుగు (Telugu)" to "te",
        "தமிழ் (Tamil)" to "ta",
        "ಕನ್ನಡ (Kannada)" to "kn",
        "हिंदी (Hindi)" to "hi"
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Language") },
        text = {
            LazyColumn {
                items(languages) { (languageName, languageCode) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentLanguage == languageName,
                            onClick = { onLanguageSelected(languageName) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = languageName,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}