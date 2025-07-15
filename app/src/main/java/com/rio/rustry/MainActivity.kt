package com.rio.rustry

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.rio.rustry.presentation.theme.RoosterTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.perf.FirebasePerformance

// Removed @AndroidEntryPoint for Plan B simplified build
class MainActivity : ComponentActivity() {
    private lateinit var analytics: FirebaseAnalytics
    private lateinit var crashlytics: FirebaseCrashlytics
    private lateinit var performance: FirebasePerformance
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Firebase components for Plan B
        initializeFirebaseComponents()
        
        setContent {
            RoosterTheme {
                RustryApp()
            }
        }
    }
    
    private fun initializeFirebaseComponents() {
        // Initialize Firebase if not already initialized
        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this)
        }
        
        // Initialize Firebase Analytics
        analytics = FirebaseAnalytics.getInstance(this)
        analytics.logEvent("app_start", null)
        
        // Initialize Firebase Crashlytics
        crashlytics = FirebaseCrashlytics.getInstance()
        crashlytics.setCrashlyticsCollectionEnabled(true)
        
        // Initialize Firebase Performance
        performance = FirebasePerformance.getInstance()
        performance.isPerformanceCollectionEnabled = true
        
        // Log successful initialization
        crashlytics.log("Firebase components initialized successfully")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RustryApp() {
    var isAuthenticated by remember { mutableStateOf(false) }
    
    MainAppContent()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContent() {
    val navController = rememberNavController()
    var selectedTab by remember { mutableIntStateOf(0) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "RUSTRY",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            NavigationBar {
                val items = listOf(
                    "Marketplace" to Icons.Default.Store,
                    "My Fowls" to Icons.Default.Pets,
                    "Profile" to Icons.Default.Person
                )
                
                items.forEachIndexed { index, (title, icon) ->
                    NavigationBarItem(
                        icon = { Icon(icon, contentDescription = title) },
                        label = { Text(title) },
                        selected = selectedTab == index,
                        onClick = { 
                            selectedTab = index
                            when (index) {
                                0 -> navController.navigate("marketplace")
                                1 -> navController.navigate("my_fowls")
                                2 -> navController.navigate("profile")
                            }
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "marketplace",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("marketplace") {
                MarketplaceScreen()
            }
            composable("my_fowls") {
                MyFowlsScreen()
            }
            composable("profile") {
                ProfileScreen()
            }
        }
    }
}

@Composable
fun MarketplaceScreen() {
    val context = LocalContext.current
    val firebaseService = remember { FirebaseService(context) }
    var fowls by remember { mutableStateOf<List<FowlData>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }
    
    // Load marketplace fowls
    LaunchedEffect(Unit) {
        firebaseService.logScreenView("marketplace")
        
        val result = firebaseService.getMarketplaceFowls()
        result.fold(
            onSuccess = { 
                fowls = it
                isLoading = false
            },
            onFailure = { 
                errorMessage = "Failed to load fowls: ${it.message}"
                isLoading = false
                // Fallback to sample data
                fowls = getSampleFowls()
            }
        )
    }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Available Fowls",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }
            }
            
            if (errorMessage.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text(
                        "Demo Mode: Using sample data",
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        items(fowls) { fowl ->
            FowlCard(
                fowl = fowl,
                onBuyClick = {
                    firebaseService.logFowlPurchase(fowl.id, fowl.price)
                }
            )
        }
        
        if (fowls.isEmpty() && !isLoading) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Store,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "No fowls available",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FowlCard(
    fowl: FowlData,
    onBuyClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "${fowl.breed} - ₹${fowl.price.toInt()}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            if (fowl.description.isNotEmpty()) {
                Text(
                    fowl.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            if (fowl.age > 0) {
                Text(
                    "Age: ${fowl.age} months",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            
            if (fowl.location.isNotEmpty()) {
                Text(
                    "Location: ${fowl.location}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "₹${fowl.price.toInt()}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Button(
                    onClick = onBuyClick
                ) {
                    Text("Buy Now")
                }
            }
        }
    }
}

// Sample data for fallback
private fun getSampleFowls(): List<FowlData> {
    return listOf(
        FowlData(
            id = "sample1",
            breed = "Desi Hen",
            price = 800.0,
            description = "Healthy 6-month old hen",
            age = 6,
            location = "Hyderabad",
            isForSale = true
        ),
        FowlData(
            id = "sample2",
            breed = "Country Rooster",
            price = 1200.0,
            description = "Strong breeding rooster",
            age = 8,
            location = "Vijayawada",
            isForSale = true
        ),
        FowlData(
            id = "sample3",
            breed = "Kadaknath Hen",
            price = 1500.0,
            description = "Premium black chicken",
            age = 7,
            location = "Warangal",
            isForSale = true
        ),
        FowlData(
            id = "sample4",
            breed = "Aseel Rooster",
            price = 2000.0,
            description = "Fighting breed rooster",
            age = 12,
            location = "Guntur",
            isForSale = true
        )
    )
}

@Composable
fun MyFowlsScreen() {
    val firebaseService = remember { FirebaseService() }
    var myFowls by remember { mutableStateOf<List<FowlData>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    
    // Load user's fowls
    LaunchedEffect(Unit) {
        firebaseService.logScreenView("my_fowls")
        
        val result = firebaseService.getFowls()
        result.fold(
            onSuccess = { 
                myFowls = it
                isLoading = false
            },
            onFailure = { 
                errorMessage = "Failed to load fowls: ${it.message}"
                isLoading = false
                // Fallback to sample data
                myFowls = getSampleUserFowls()
            }
        )
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "My Fowls",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            }
        }
        
        if (errorMessage.isNotEmpty()) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    "Demo Mode: Using sample data",
                    modifier = Modifier.padding(12.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (myFowls.isEmpty() && !isLoading) {
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
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "No fowls added yet",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                    Button(
                        onClick = { showAddDialog = true },
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        Text("Add Your First Fowl")
                    }
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(myFowls) { fowl ->
                    MyFowlCard(fowl = fowl)
                }
            }
        }
        
        // Floating Action Button for adding fowls
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomEnd
        ) {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Fowl")
            }
        }
    }
    
    // Add Fowl Dialog
    if (showAddDialog) {
        AddFowlDialog(
            onDismiss = { showAddDialog = false },
            onAddFowl = { fowl ->
                // Add fowl to local list immediately for demo
                myFowls = myFowls + fowl
                showAddDialog = false
            }
        )
    }
}

@Composable
fun MyFowlCard(fowl: FowlData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                fowl.breed,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            if (fowl.description.isNotEmpty()) {
                Text(
                    fowl.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    if (fowl.age > 0) {
                        Text(
                            "Age: ${fowl.age} months",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (fowl.price > 0) {
                        Text(
                            "Price: ₹${fowl.price.toInt()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                
                if (fowl.isForSale) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Text(
                            "For Sale",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AddFowlDialog(
    onDismiss: () -> Unit,
    onAddFowl: (FowlData) -> Unit
) {
    var breed by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isForSale by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Fowl") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = breed,
                    onValueChange = { breed = it },
                    label = { Text("Breed") },
                    placeholder = { Text("e.g., Desi Hen") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = age,
                    onValueChange = { age = it },
                    label = { Text("Age (months)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Price (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isForSale,
                        onCheckedChange = { isForSale = it }
                    )
                    Text(
                        "List for sale",
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (breed.isNotBlank()) {
                        val fowl = FowlData(
                            breed = breed,
                            age = age.toIntOrNull() ?: 0,
                            price = price.toDoubleOrNull() ?: 0.0,
                            description = description,
                            isForSale = isForSale,
                            location = "Demo Location"
                        )
                        onAddFowl(fowl)
                    }
                },
                enabled = breed.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Sample data for fallback
private fun getSampleUserFowls(): List<FowlData> {
    return listOf(
        FowlData(
            id = "user1",
            breed = "My Desi Hen #1",
            age = 8,
            price = 900.0,
            description = "Healthy laying hen",
            isForSale = true,
            location = "My Farm"
        ),
        FowlData(
            id = "user2",
            breed = "My Rooster #1",
            age = 12,
            price = 1300.0,
            description = "Strong breeding rooster",
            isForSale = false,
            location = "My Farm"
        )
    )
}

@Composable
fun ProfileScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "Profile",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        // Profile Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Column(
                        modifier = Modifier.padding(start = 16.dp)
                    ) {
                        Text(
                            "Demo Farmer",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "+91 9876543210",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Menu Items
        val menuItems = listOf(
            "My Listings" to Icons.Default.List,
            "Transaction History" to Icons.Default.History,
            "Settings" to Icons.Default.Settings,
            "Help & Support" to Icons.Default.Help,
            "About" to Icons.Default.Info
        )
        
        menuItems.forEach { (title, icon) ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        icon,
                        contentDescription = title,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        title,
                        modifier = Modifier.padding(start = 16.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}