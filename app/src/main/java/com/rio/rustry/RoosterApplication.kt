package com.rio.rustry

import android.app.Application
import android.content.ComponentCallbacks2
import android.content.res.Configuration as AndroidConfiguration
import androidx.work.Configuration as WorkConfiguration
import androidx.work.WorkManager
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.perf.FirebasePerformance
import com.rio.rustry.utils.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import android.util.Log
import org.koin.dsl.module

class RoosterApplication : Application(), ComponentCallbacks2 {
    
    companion object {
        @Volatile
        private var INSTANCE: RoosterApplication? = null
        
        val instance: RoosterApplication
            get() = INSTANCE ?: throw IllegalStateException("Application not initialized")
    }
    
    // Manually instantiate since migrating from Hilt
    private lateinit var memoryManager: MemoryManager
    private lateinit var securityManager: com.rio.rustry.security.SecurityManager
    private lateinit var networkManager: NetworkManager
    private lateinit var databaseOptimizer: DatabaseOptimizer
    private lateinit var optimizedImageLoader: OptimizedImageLoader
    
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    override fun onCreate() {
        super.onCreate()
        INSTANCE = this
        
        // Initialize Koin
        startKoin {
            androidContext(this@RoosterApplication)
            modules(module {
                // Add module definitions here as needed
            })
        }
        
        // Initialize core components
        initializeFirebase()
        initializePerformanceMonitoring()
        initializeSecurity()
        initializeMemoryManagement()
        initializeNetworking()
        initializeDatabase()
        initializeWorkManager()
        
        // Start background optimizations
        startBackgroundOptimizations()
        
        Log.i("Application", "Rooster Platform initialized successfully")
    }
    
    // Rest of the file remains the same
    private fun initializeFirebase() {
        try {
            FirebaseApp.initializeApp(this)
            
            // Configure Crashlytics
            FirebaseCrashlytics.getInstance().apply {
                setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)
                setCustomKey("app_version", BuildConfig.VERSION_NAME)
                setCustomKey("build_type", BuildConfig.BUILD_TYPE)
            }
            
            // Configure Performance Monitoring
            FirebasePerformance.getInstance().apply {
                isPerformanceCollectionEnabled = BuildConfig.ENABLE_ANALYTICS
            }
            
            Log.d("Application", "Firebase initialized")
        } catch (e: Exception) {
            Log.e("Application", "Firebase initialization failed", e)
        }
    }
    
    private fun initializePerformanceMonitoring() {
        try {
            PerformanceMonitor.startTrace("app_startup")
            
            // Monitor app startup time
            applicationScope.launch {
                kotlinx.coroutines.delay(1000) // Wait for app to fully load
                PerformanceMonitor.stopTrace("app_startup")
                
                // Send initial performance report
                if (BuildConfig.ENABLE_ANALYTICS) {
                    PerformanceMonitor.sendPerformanceReport()
                }
            }
            
            Log.d("Application", "Performance monitoring initialized")
        } catch (e: Exception) {
            Log.e("Application", "Performance monitoring initialization failed", e)
        }
    }
    
    private fun initializeSecurity() {
        try {
            // Initialize security manager manually since Hilt is disabled
            securityManager = com.rio.rustry.security.SecurityManager(this)
            Log.d("Application", "Security initialized")
        } catch (e: Exception) {
            Log.e("Application", "Security initialization failed", e)
        }
    }
    
    private fun initializeMemoryManagement() {
        try {
            // Initialize memory manager manually since Hilt is disabled
            memoryManager = MemoryManager(this)
            memoryManager.addMemoryListener(object : MemoryManager.MemoryListener {
                override fun onMemoryEvent(event: MemoryManager.MemoryEvent) {
                    when (event) {
                        MemoryManager.MemoryEvent.CRITICAL -> {
                            Log.w("Memory", "Critical memory event - performing emergency cleanup")
                            optimizedImageLoader.clearCache()
                            databaseOptimizer.clearAllCache()
                        }
                        MemoryManager.MemoryEvent.WARNING -> {
                            Log.i("Memory", "Memory warning - performing light cleanup")
                            databaseOptimizer.clearExpiredCache()
                        }
                        MemoryManager.MemoryEvent.LOW_MEMORY -> {
                            Log.w("Memory", "System low memory - aggressive cleanup")
                            performAggressiveCleanup()
                        }
                        MemoryManager.MemoryEvent.NORMAL -> {
                            Log.d("Memory", "Memory state normalized")
                        }
                    }
                }
            })
            
            Log.d("Application", "Memory management initialized")
        } catch (e: Exception) {
            Log.e("Application", "Memory management initialization failed", e)
        }
    }
    
    private fun initializeNetworking() {
        try {
            // Initialize network manager manually since Hilt is disabled
            networkManager = NetworkManager(this)
            Log.d("Application", "Networking initialized")
        } catch (e: Exception) {
            Log.e("Application", "Networking initialization failed", e)
        }
    }
    
    private fun initializeDatabase() {
        try {
            // Initialize database optimizer manually since Hilt is disabled
            databaseOptimizer = DatabaseOptimizer()
            optimizedImageLoader = OptimizedImageLoader(this)
            
            // Start periodic cache cleanup
            applicationScope.launch {
                while (true) {
                    kotlinx.coroutines.delay(300000) // 5 minutes
                    databaseOptimizer.clearExpiredCache()
                }
            }
            
            Log.d("Application", "Database optimization initialized")
        } catch (e: Exception) {
            Log.e("Application", "Database initialization failed", e)
        }
    }
    
    private fun initializeWorkManager() {
        try {
            val workManagerConfig = WorkConfiguration.Builder()
                .setMinimumLoggingLevel(if (BuildConfig.DEBUG) android.util.Log.DEBUG else android.util.Log.ERROR)
                .build()
            
            WorkManager.initialize(this, workManagerConfig)
            
            Log.d("Application", "WorkManager initialized")
        } catch (e: Exception) {
            Log.e("Application", "WorkManager initialization failed", e)
        }
    }
    
    private fun startBackgroundOptimizations() {
        applicationScope.launch {
            // Periodic performance monitoring
            while (true) {
                kotlinx.coroutines.delay(600000) // 10 minutes
                
                try {
                    // Log memory usage
                    memoryManager.generateMemoryReport().let { report ->
                        Log.d("PeriodicReport", report)
                    }
                    
                    // Log performance metrics
                    PerformanceMonitor.generatePerformanceReport().let { report ->
                        Log.d("PeriodicReport", report)
                    }
                    
                    // Log database performance
                    databaseOptimizer.generatePerformanceReport().let { report ->
                        Log.d("PeriodicReport", report)
                    }
                    
                } catch (e: Exception) {
                    Log.e("Application", "Background optimization failed", e)
                }
            }
        }
    }
    
    private fun performAggressiveCleanup() {
        try {
            // Clear all caches
            optimizedImageLoader.clearCache()
            databaseOptimizer.clearAllCache()
            networkManager.clearCache()
            
            // Force garbage collection
            memoryManager.forceGarbageCollection()
            
            Log.i("Application", "Aggressive cleanup completed")
        } catch (e: Exception) {
            Log.e("Application", "Aggressive cleanup failed", e)
        }
    }
    
    override fun onConfigurationChanged(newConfig: AndroidConfiguration) {
        super.onConfigurationChanged(newConfig)
        Log.d("Application", "Configuration changed: ${newConfig.toString()}")
    }
    
    override fun onLowMemory() {
        super.onLowMemory()
        Log.w("Application", "System onLowMemory() called")
        performAggressiveCleanup()
    }
    
    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        
        val levelName = when (level) {
            ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN -> "UI_HIDDEN"
            ComponentCallbacks2.TRIM_MEMORY_RUNNING_MODERATE -> "RUNNING_MODERATE"
            ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW -> "RUNNING_LOW"
            ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL -> "RUNNING_CRITICAL"
            ComponentCallbacks2.TRIM_MEMORY_BACKGROUND -> "BACKGROUND"
            ComponentCallbacks2.TRIM_MEMORY_MODERATE -> "MODERATE"
            ComponentCallbacks2.TRIM_MEMORY_COMPLETE -> "COMPLETE"
            else -> "UNKNOWN($level)"
        }
        
        Log.i("Application", "Memory trim requested: $levelName")
        
        when (level) {
            ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN -> {
                // App UI is hidden, light cleanup
                databaseOptimizer.clearExpiredCache()
            }
            ComponentCallbacks2.TRIM_MEMORY_RUNNING_MODERATE,
            ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW -> {
                // App is running but system is under memory pressure
                optimizedImageLoader.clearCache()
                databaseOptimizer.clearExpiredCache()
            }
            ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL,
            ComponentCallbacks2.TRIM_MEMORY_COMPLETE -> {
                // Critical memory situation
                performAggressiveCleanup()
            }
            ComponentCallbacks2.TRIM_MEMORY_BACKGROUND,
            ComponentCallbacks2.TRIM_MEMORY_MODERATE -> {
                // App is in background
                memoryManager.optimizeForScenario(MemoryManager.MemoryScenario.BACKGROUND)
            }
        }
    }
    
    override fun onTerminate() {
        super.onTerminate()
        
        try {
            // Cleanup resources
            memoryManager.cleanup()
            networkManager.stopNetworkMonitoring()
            
            // Send final performance report
            if (BuildConfig.ENABLE_ANALYTICS) {
                PerformanceMonitor.sendPerformanceReport()
            }
            
            Log.i("Application", "Application terminated")
        } catch (e: Exception) {
            Log.e("Application", "Error during application termination", e)
        }
        
        INSTANCE = null
    }
}

val android.content.Context.roosterApplication: RoosterApplication
    get() = applicationContext as RoosterApplication
