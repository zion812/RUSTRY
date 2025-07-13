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

/**
 * Optimized Application class for the Rooster Platform
 * 
 * Features:
 * - Performance monitoring and optimization
 * - Memory management and leak prevention
 * - Security initialization
 * - Firebase integration
 * - Background task management
 * - Crash reporting and analytics
 */
class RoosterApplication : Application(), ComponentCallbacks2 {
    
    companion object {
        @Volatile
        private var INSTANCE: RoosterApplication? = null
        
        val instance: RoosterApplication
            get() = INSTANCE ?: throw IllegalStateException("Application not initialized")
    }
    
    // Manual dependency injection - simplified for now
    lateinit var memoryManager: MemoryManager
    lateinit var securityManager: SecurityManager
    lateinit var networkManager: NetworkManager
    lateinit var databaseOptimizer: DatabaseOptimizer
    lateinit var optimizedImageLoader: OptimizedImageLoader
    
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    override fun onCreate() {
        super.onCreate()
        INSTANCE = this
        
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
        
        Logger.i("Application") { "Rooster Platform initialized successfully" }
    }
    
    /**
     * Initialize Firebase services
     */
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
            
            Logger.d("Application") { "Firebase initialized" }
        } catch (e: Exception) {
            Logger.e("Application", "Firebase initialization failed", e)
        }
    }
    
    /**
     * Initialize performance monitoring
     */
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
            
            Logger.d("Application") { "Performance monitoring initialized" }
        } catch (e: Exception) {
            Logger.e("Application", "Performance monitoring initialization failed", e)
        }
    }
    
    /**
     * Initialize security components
     */
    private fun initializeSecurity() {
        try {
            // Security manager is initialized via Hilt
            // Additional security setup can be done here
            
            Logger.d("Application") { "Security initialized" }
        } catch (e: Exception) {
            Logger.e("Application", "Security initialization failed", e)
        }
    }
    
    /**
     * Initialize memory management
     */
    private fun initializeMemoryManagement() {
        try {
            // Initialize dependencies manually
            memoryManager = MemoryManager(this)
            securityManager = SecurityManager(this)
            networkManager = NetworkManager(this)
            databaseOptimizer = DatabaseOptimizer()
            optimizedImageLoader = OptimizedImageLoader(this)
            // Register memory listener for critical events
            memoryManager.addMemoryListener(object : MemoryManager.MemoryListener {
                override fun onMemoryEvent(event: MemoryManager.MemoryEvent) {
                    when (event) {
                        MemoryManager.MemoryEvent.CRITICAL -> {
                            Logger.w("Memory", { "Critical memory event - performing emergency cleanup" })
                            optimizedImageLoader.clearCache()
                            databaseOptimizer.clearAllCache()
                        }
                        MemoryManager.MemoryEvent.WARNING -> {
                            Logger.i("Memory") { "Memory warning - performing light cleanup" }
                            databaseOptimizer.clearExpiredCache()
                        }
                        MemoryManager.MemoryEvent.LOW_MEMORY -> {
                            Logger.w("Memory", { "System low memory - aggressive cleanup" })
                            performAggressiveCleanup()
                        }
                        MemoryManager.MemoryEvent.NORMAL -> {
                            Logger.d("Memory") { "Memory state normalized" }
                        }
                    }
                }
            })
            
            Logger.d("Application") { "Memory management initialized" }
        } catch (e: Exception) {
            Logger.e("Application", "Memory management initialization failed", e)
        }
    }
    
    /**
     * Initialize networking components
     */
    private fun initializeNetworking() {
        try {
            // Network monitoring is automatically started in NetworkManager
            Logger.d("Application") { "Networking initialized" }
        } catch (e: Exception) {
            Logger.e("Application", "Networking initialization failed", e)
        }
    }
    
    /**
     * Initialize database optimizations
     */
    private fun initializeDatabase() {
        try {
            // Start periodic cache cleanup
            applicationScope.launch {
                while (true) {
                    kotlinx.coroutines.delay(300000) // 5 minutes
                    databaseOptimizer.clearExpiredCache()
                }
            }
            
            Logger.d("Application") { "Database optimization initialized" }
        } catch (e: Exception) {
            Logger.e("Application", "Database initialization failed", e)
        }
    }
    
    /**
     * Initialize WorkManager
     */
    private fun initializeWorkManager() {
        try {
            val workManagerConfig = WorkConfiguration.Builder()
                .setMinimumLoggingLevel(if (BuildConfig.DEBUG) android.util.Log.DEBUG else android.util.Log.ERROR)
                .build()
            
            WorkManager.initialize(this, workManagerConfig)
            
            Logger.d("Application") { "WorkManager initialized" }
        } catch (e: Exception) {
            Logger.e("Application", "WorkManager initialization failed", e)
        }
    }
    
    /**
     * Start background optimization tasks
     */
    private fun startBackgroundOptimizations() {
        applicationScope.launch {
            // Periodic performance monitoring
            while (true) {
                kotlinx.coroutines.delay(600000) // 10 minutes
                
                try {
                    // Log memory usage
                    memoryManager.generateMemoryReport().let { report ->
                        Logger.d("PeriodicReport") { report }
                    }
                    
                    // Log performance metrics
                    PerformanceMonitor.generatePerformanceReport().let { report ->
                        Logger.d("PeriodicReport") { report }
                    }
                    
                    // Log database performance
                    databaseOptimizer.generatePerformanceReport().let { report ->
                        Logger.d("PeriodicReport") { report }
                    }
                    
                } catch (e: Exception) {
                    Logger.e("Application", "Background optimization failed", e)
                }
            }
        }
    }
    
    /**
     * Perform aggressive cleanup during memory pressure
     */
    private fun performAggressiveCleanup() {
        try {
            // Clear all caches
            optimizedImageLoader.clearCache()
            databaseOptimizer.clearAllCache()
            networkManager.clearCache()
            
            // Force garbage collection
            memoryManager.forceGarbageCollection()
            
            Logger.i("Application") { "Aggressive cleanup completed" }
        } catch (e: Exception) {
            Logger.e("Application", "Aggressive cleanup failed", e)
        }
    }
    
    override fun onConfigurationChanged(newConfig: AndroidConfiguration) {
        super.onConfigurationChanged(newConfig)
        Logger.d("Application") { "Configuration changed: ${newConfig.toString()}" }
    }
    
    override fun onLowMemory() {
        super.onLowMemory()
        Logger.w("Application", { "System onLowMemory() called" })
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
        
        Logger.i("Application") { "Memory trim requested: $levelName" }
        
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
            
            Logger.i("Application") { "Application terminated" }
        } catch (e: Exception) {
            Logger.e("Application", "Error during application termination", e)
        }
        
        INSTANCE = null
    }
}

/**
 * Extension function to get application instance from context
 */
val android.content.Context.roosterApplication: RoosterApplication
    get() = applicationContext as RoosterApplication