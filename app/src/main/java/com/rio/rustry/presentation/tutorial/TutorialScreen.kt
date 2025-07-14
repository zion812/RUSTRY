package com.rio.rustry.presentation.tutorial

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rio.rustry.presentation.viewmodel.TutorialViewModel
import kotlinx.coroutines.launch

data class TutorialStep(
    val title: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val actionText: String = "",
    val isInteractive: Boolean = false
)

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TutorialScreen(
    onTutorialComplete: () -> Unit,
    onSkipTutorial: () -> Unit,
    viewModel: TutorialViewModel = viewModel()
) {
    val context = LocalContext.current
    val currentStep by viewModel.currentStep.collectAsState()
    val isCompleted by viewModel.isCompleted.collectAsState()
    val voiceEnabled by viewModel.voiceEnabled.collectAsState()
    
    val tutorialSteps = remember {
        listOf(
            TutorialStep(
                title = "Welcome to RUSTRY",
                description = "Your complete poultry farm management solution. Let's get you started with a quick tour of the app.",
                icon = Icons.Default.Agriculture
            ),
            TutorialStep(
                title = "Farm Listing",
                description = "Add and manage your farm details, including location, contact information, and farm photos.",
                icon = Icons.Default.Home,
                actionText = "Try adding your farm",
                isInteractive = true
            ),
            TutorialStep(
                title = "Flock Management",
                description = "Track your poultry flocks, including breed information, health records, and breeding history.",
                icon = Icons.Default.Pets,
                actionText = "Add your first flock",
                isInteractive = true
            ),
            TutorialStep(
                title = "Health Records",
                description = "Keep detailed health records, vaccination schedules, and veterinary visits for each bird.",
                icon = Icons.Default.HealthAndSafety,
                actionText = "Record health data",
                isInteractive = true
            ),
            TutorialStep(
                title = "Sales Tracking",
                description = "Monitor your sales, track revenue, and manage customer relationships effectively.",
                icon = Icons.Default.AttachMoney,
                actionText = "Record a sale",
                isInteractive = true
            ),
            TutorialStep(
                title = "Inventory Management",
                description = "Keep track of feed, medicines, equipment, and other farm supplies with low-stock alerts.",
                icon = Icons.Default.Inventory,
                actionText = "Add inventory items",
                isInteractive = true
            ),
            TutorialStep(
                title = "Voice Commands",
                description = "Use voice commands for hands-free operation. Say 'Add flock' or 'Show sales' to get started.",
                icon = Icons.Default.Mic,
                actionText = "Enable voice commands",
                isInteractive = true
            ),
            TutorialStep(
                title = "Offline Support",
                description = "Work without internet connection. Your data will sync automatically when you're back online.",
                icon = Icons.Default.CloudOff
            ),
            TutorialStep(
                title = "You're All Set!",
                description = "You're ready to start managing your poultry farm with RUSTRY. Tap 'Get Started' to begin.",
                icon = Icons.Default.CheckCircle
            )
        )
    }
    
    val pagerState = rememberPagerState(pageCount = { tutorialSteps.size })
    val coroutineScope = rememberCoroutineScope()
    
    LaunchedEffect(isCompleted) {
        if (isCompleted) {
            onTutorialComplete()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Getting Started") },
                actions = {
                    TextButton(
                        onClick = onSkipTutorial
                    ) {
                        Text("Skip")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Progress indicator
            LinearProgressIndicator(
                progress = (pagerState.currentPage + 1).toFloat() / tutorialSteps.size,
                modifier = Modifier.fillMaxWidth()
            )
            
            // Tutorial content
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                TutorialStepContent(
                    step = tutorialSteps[page],
                    stepNumber = page + 1,
                    totalSteps = tutorialSteps.size,
                    onActionClick = {
                        if (tutorialSteps[page].isInteractive) {
                            viewModel.performStepAction(page)
                        }
                    },
                    voiceEnabled = voiceEnabled,
                    onVoiceToggle = { viewModel.toggleVoice() }
                )
            }
            
            // Navigation buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Previous button
                if (pagerState.currentPage > 0) {
                    OutlinedButton(
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                            }
                        }
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Previous")
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }
                
                // Page indicators
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(tutorialSteps.size) { index ->
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (index == pagerState.currentPage) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                    }
                                )
                        )
                    }
                }
                
                // Next/Finish button
                Button(
                    onClick = {
                        if (pagerState.currentPage < tutorialSteps.size - 1) {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        } else {
                            viewModel.completeTutorial()
                        }
                    }
                ) {
                    Text(
                        if (pagerState.currentPage < tutorialSteps.size - 1) "Next" else "Get Started"
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        if (pagerState.currentPage < tutorialSteps.size - 1) Icons.Default.ArrowForward else Icons.Default.Check,
                        contentDescription = null
                    )
                }
            }
        }
    }
}

@Composable
private fun TutorialStepContent(
    step: TutorialStep,
    stepNumber: Int,
    totalSteps: Int,
    onActionClick: () -> Unit,
    voiceEnabled: Boolean,
    onVoiceToggle: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Step counter
        Text(
            text = "Step $stepNumber of $totalSteps",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Icon
        Card(
            modifier = Modifier.size(120.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = step.icon,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Title
        Text(
            text = step.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Description
        Text(
            text = step.description,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.2
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Interactive action button
        if (step.isInteractive && step.actionText.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Try it now:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Button(
                        onClick = onActionClick,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(step.actionText)
                    }
                }
            }
        }
        
        // Voice command toggle for voice step
        if (step.title.contains("Voice Commands")) {
            Spacer(modifier = Modifier.height(16.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (voiceEnabled) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Voice Commands",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = if (voiceEnabled) "Enabled" else "Disabled",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Switch(
                        checked = voiceEnabled,
                        onCheckedChange = { onVoiceToggle() }
                    )
                }
            }
            
            if (voiceEnabled) {
                Spacer(modifier = Modifier.height(16.dp))
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Try saying:",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        listOf(
                            "\"Add flock\"",
                            "\"Show sales\"",
                            "\"Record health data\"",
                            "\"Check inventory\""
                        ).forEach { command ->
                            Text(
                                text = "• $command",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}