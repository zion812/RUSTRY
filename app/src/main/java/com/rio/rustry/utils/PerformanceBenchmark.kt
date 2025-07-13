package com.rio.rustry.utils

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Debug
import androidx.compose.runtime.*
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import com.rio.rustry.BuildConfig
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Comprehensive performance monitoring and benchmarking utility
 * 
 * Features:
 * - App startup time measurement
 * - Screen transition performance
 * - Memory usage tracking
 * - Network performance monitoring
 * - User interaction latency
 * - Firebase Performance integration
 */
class PerformanceBenchmark(
    private val context: Context
) : DefaultLifecycleObserver {
    
    companion object {
        private const val STARTUP_TRACE = "app_startup"
        private const val SCREEN_LOAD_TRACE = "screen_load"
        private const val NETWORK_REQUEST_TRACE = "network_request"
        private const val USER_INTERACTION_TRACE = "user_interaction"
        
        // Performance targets
        private const val TARGET_STARTUP_TIME = 1900L // 1.9 seconds
        private const val TARGET_SCREEN_LOAD_TIME = 500L // 500ms
        private const val TARGET_MEMORY_USAGE = 0.8 // 80% of available
        private const val TARGET_FRAME_TIME = 16.67 // 60 FPS
    }
    
    private val firebasePerformance = FirebasePerformance.getInstance()
    private val firebaseAnalytics = FirebaseAnalytics.getInstance(context)
    private val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    
    private val activeTraces = ConcurrentHashMap<String, Trace>()
    private val performanceMetrics = ConcurrentHashMap<String, PerformanceMetric>()
    
    private val _performanceState = MutableStateFlow(PerformanceState())
    val performanceState: StateFlow<PerformanceState> = _performanceState.asStateFlow()
    
    private var appStartTime: Long = 0
    private var isMonitoring = false
    
    init {
        if (BuildConfig.ENABLE_ANALYTICS) {
            ProcessLifecycleOwner.get().lifecycle.addObserver(this)
            startPerformanceMonitoring()
        }
    }
    
    /**
     * Start app startup measurement
     */
    fun startAppStartup() {
        appStartTime = System.currentTimeMillis()
        startTrace(STARTUP_TRACE)
        Logger.d("Performance") { "App startup measurement started" }
    }
    
    /**
     * Complete app startup measurement
     */
    fun completeAppStartup() {
        val startupTime = System.currentTimeMillis() - appStartTime
        stopTrace(STARTUP_TRACE)
        
        // Log startup performance
        val isTargetMet = startupTime <= TARGET_STARTUP_TIME
        Logger.i("Performance") { 
            "App startup completed: ${startupTime}ms (Target: ${TARGET_STARTUP_TIME}ms) - ${if (isTargetMet) "✅" else "❌"}"
        }
        
        // Record metric
        recordMetric("startup_time", startupTime.toDouble(), "ms")
        
        // Firebase Analytics
        firebaseAnalytics.logEvent("app_startup_complete") {
            param("startup_time_ms", startupTime)
            param("target_met", if (isTargetMet) 1L else 0L)
        }
        
        // Update state
        updatePerformanceState { 
            it.copy(
                startupTime = startupTime,
                isStartupTargetMet = isTargetMet
            )
        }
    }
    
    /**
     * Measure screen load performance
     */
    fun measureScreenLoad(screenName: String, loadAction: suspend () -> Unit) {
        CoroutineScope(Dispatchers.Main).launch {
            val traceKey = "${SCREEN_LOAD_TRACE}_$screenName"
            val startTime = System.currentTimeMillis()
            
            startTrace(traceKey)
            
            try {
                loadAction()
                
                val loadTime = System.currentTimeMillis() - startTime
                stopTrace(traceKey)
                
                val isTargetMet = loadTime <= TARGET_SCREEN_LOAD_TIME
                Logger.d("Performance") { 
                    "Screen '$screenName' loaded: ${loadTime}ms (Target: ${TARGET_SCREEN_LOAD_TIME}ms) - ${if (isTargetMet) "✅" else "❌"}"
                }
                
                // Record metric
                recordMetric("screen_load_$screenName", loadTime.toDouble(), "ms")
                
                // Firebase Analytics
                firebaseAnalytics.logEvent("screen_load_complete") {
                    param("screen_name", screenName)
                    param("load_time_ms", loadTime)
                    param("target_met", if (isTargetMet) 1L else 0L)
                }
                
            } catch (e: Exception) {
                stopTrace(traceKey)
                Logger.e("Performance", "Screen load failed for $screenName", e)
                
                firebaseAnalytics.logEvent("screen_load_error") {
                    param("screen_name", screenName)
                    param("error_message", e.message ?: "Unknown error")
                }
            }
        }
    }
    
    /**
     * Measure network request performance
     */
    fun measureNetworkRequest(requestName: String, request: suspend () -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val traceKey = "${NETWORK_REQUEST_TRACE}_$requestName"
            val startTime = System.currentTimeMillis()
            
            startTrace(traceKey)
            
            try {
                request()
                
                val requestTime = System.currentTimeMillis() - startTime
                stopTrace(traceKey)
                
                Logger.d("Performance") { "Network request '$requestName' completed: ${requestTime}ms" }
                
                // Record metric
                recordMetric("network_request_$requestName", requestTime.toDouble(), "ms")
                
                // Firebase Analytics
                firebaseAnalytics.logEvent("network_request_complete") {
                    param("request_name", requestName)
                    param("request_time_ms", requestTime)
                }
                
            } catch (e: Exception) {
                stopTrace(traceKey)
                Logger.e("Performance", "Network request failed for $requestName", e)
                
                firebaseAnalytics.logEvent("network_request_error") {
                    param("request_name", requestName)
                    param("error_message", e.message ?: "Unknown error")
                }
            }
        }
    }
    
    /**
     * Measure user interaction latency
     */
    fun measureUserInteraction(interactionName: String, action: () -> Unit) {
        val traceKey = "${USER_INTERACTION_TRACE}_$interactionName"
        val startTime = System.currentTimeMillis()
        
        startTrace(traceKey)
        
        try {
            action()
            
            val interactionTime = System.currentTimeMillis() - startTime
            stopTrace(traceKey)
            
            Logger.d("Performance") { "User interaction '$interactionName' completed: ${interactionTime}ms" }
            
            // Record metric
            recordMetric("user_interaction_$interactionName", interactionTime.toDouble(), "ms")
            
            // Firebase Analytics
            firebaseAnalytics.logEvent("user_interaction_complete") {
                param("interaction_name", interactionName)
                param("interaction_time_ms", interactionTime)
            }
            
        } catch (e: Exception) {
            stopTrace(traceKey)
            Logger.e("Performance", "User interaction failed for $interactionName", e)
        }
    }
    
    /**
     * Get current memory usage
     */
    fun getCurrentMemoryUsage(): MemoryUsage {
        val runtime = Runtime.getRuntime()
        val memInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memInfo)
        
        val javaHeapUsed = runtime.totalMemory() - runtime.freeMemory()
        val javaHeapMax = runtime.maxMemory()
        val nativeHeapUsed = Debug.getNativeHeapAllocatedSize()
        val totalUsed = javaHeapUsed + nativeHeapUsed
        val totalMax = javaHeapMax + Debug.getNativeHeapSize()
        
        val usagePercentage = if (totalMax > 0) totalUsed.toDouble() / totalMax.toDouble() else 0.0
        
        return MemoryUsage(
            javaHeapUsed = javaHeapUsed,
            javaHeapMax = javaHeapMax,
            nativeHeapUsed = nativeHeapUsed,
            totalUsed = totalUsed,
            totalMax = totalMax,
            usagePercentage = usagePercentage,
            isLowMemory = memInfo.lowMemory,
            availableMemory = memInfo.availMem,
            totalMemory = memInfo.totalMem
        )
    }
    
    /**
     * Generate performance report
     */
    fun generatePerformanceReport(): PerformanceReport {
        val memoryUsage = getCurrentMemoryUsage()
        val currentState = _performanceState.value
        
        val issues = mutableListOf<PerformanceIssue>()
        
        // Check startup time
        if (currentState.startupTime > TARGET_STARTUP_TIME) {
            issues.add(
                PerformanceIssue(
                    type = PerformanceIssueType.SLOW_STARTUP,
                    severity = PerformanceIssueSeverity.HIGH,
                    description = "App startup time (${currentState.startupTime}ms) exceeds target (${TARGET_STARTUP_TIME}ms)",
                    recommendation = "Optimize app initialization, reduce startup dependencies"
                )
            )
        }
        
        // Check memory usage
        if (memoryUsage.usagePercentage > TARGET_MEMORY_USAGE) {
            issues.add(
                PerformanceIssue(
                    type = PerformanceIssueType.HIGH_MEMORY_USAGE,
                    severity = PerformanceIssueSeverity.MEDIUM,
                    description = "Memory usage (${(memoryUsage.usagePercentage * 100).toInt()}%) exceeds target (${(TARGET_MEMORY_USAGE * 100).toInt()}%)",
                    recommendation = "Optimize memory usage, implement better caching strategies"
                )
            )
        }
        
        return PerformanceReport(
            timestamp = System.currentTimeMillis(),
            startupTime = currentState.startupTime,
            memoryUsage = memoryUsage,
            metrics = performanceMetrics.values.toList(),
            issues = issues,
            overallScore = calculateOverallScore(currentState, memoryUsage, issues)
        )
    }
    
    /**
     * Start continuous performance monitoring
     */
    private fun startPerformanceMonitoring() {
        if (isMonitoring) return
        
        isMonitoring = true
        CoroutineScope(Dispatchers.Default).launch {
            while (isMonitoring) {
                val memoryUsage = getCurrentMemoryUsage()
                
                updatePerformanceState { state ->
                    state.copy(
                        currentMemoryUsage = memoryUsage.usagePercentage,
                        isMemoryTargetMet = memoryUsage.usagePercentage <= TARGET_MEMORY_USAGE
                    )
                }
                
                // Log memory warning if needed
                if (memoryUsage.usagePercentage > TARGET_MEMORY_USAGE) {
                    Logger.w("Performance") { 
                        "High memory usage detected: ${(memoryUsage.usagePercentage * 100).toInt()}%"
                    }
                }
                
                delay(30000) // Check every 30 seconds
            }
        }
    }
    
    private fun startTrace(traceName: String) {
        if (!BuildConfig.ENABLE_ANALYTICS) return
        
        try {
            val trace = firebasePerformance.newTrace(traceName)
            trace.start()
            activeTraces[traceName] = trace
            Logger.d("Performance") { "Started trace: $traceName" }
        } catch (e: Exception) {
            Logger.e("Performance", "Failed to start trace: $traceName", e)
        }
    }
    
    private fun stopTrace(traceName: String) {
        if (!BuildConfig.ENABLE_ANALYTICS) return
        
        try {
            activeTraces.remove(traceName)?.let { trace ->
                trace.stop()
                Logger.d("Performance") { "Stopped trace: $traceName" }
            }
        } catch (e: Exception) {
            Logger.e("Performance", "Failed to stop trace: $traceName", e)
        }
    }
    
    fun recordMetric(name: String, value: Double, unit: String) {
        performanceMetrics[name] = PerformanceMetric(
            name = name,
            value = value,
            unit = unit,
            timestamp = System.currentTimeMillis()
        )
    }
    
    private fun updatePerformanceState(update: (PerformanceState) -> PerformanceState) {
        _performanceState.value = update(_performanceState.value)
    }
    
    private fun calculateOverallScore(
        state: PerformanceState,
        memoryUsage: MemoryUsage,
        issues: List<PerformanceIssue>
    ): Int {
        var score = 100
        
        // Deduct points for startup time
        if (state.startupTime > TARGET_STARTUP_TIME) {
            score -= 20
        }
        
        // Deduct points for memory usage
        if (memoryUsage.usagePercentage > TARGET_MEMORY_USAGE) {
            score -= 15
        }
        
        // Deduct points for issues
        issues.forEach { issue ->
            score -= when (issue.severity) {
                PerformanceIssueSeverity.HIGH -> 25
                PerformanceIssueSeverity.MEDIUM -> 15
                PerformanceIssueSeverity.LOW -> 5
            }
        }
        
        return maxOf(0, score)
    }
    
    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        if (!isMonitoring) {
            startPerformanceMonitoring()
        }
    }
    
    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        isMonitoring = false
    }
    
    data class PerformanceState(
        val startupTime: Long = 0,
        val isStartupTargetMet: Boolean = false,
        val currentMemoryUsage: Double = 0.0,
        val isMemoryTargetMet: Boolean = true
    )
    
    data class MemoryUsage(
        val javaHeapUsed: Long,
        val javaHeapMax: Long,
        val nativeHeapUsed: Long,
        val totalUsed: Long,
        val totalMax: Long,
        val usagePercentage: Double,
        val isLowMemory: Boolean,
        val availableMemory: Long,
        val totalMemory: Long
    )
    
    data class PerformanceMetric(
        val name: String,
        val value: Double,
        val unit: String,
        val timestamp: Long
    )
    
    data class PerformanceReport(
        val timestamp: Long,
        val startupTime: Long,
        val memoryUsage: MemoryUsage,
        val metrics: List<PerformanceMetric>,
        val issues: List<PerformanceIssue>,
        val overallScore: Int
    )
    
    data class PerformanceIssue(
        val type: PerformanceIssueType,
        val severity: PerformanceIssueSeverity,
        val description: String,
        val recommendation: String
    )
    
    enum class PerformanceIssueType {
        SLOW_STARTUP, HIGH_MEMORY_USAGE, SLOW_SCREEN_LOAD, NETWORK_LATENCY, UI_JANK
    }
    
    enum class PerformanceIssueSeverity {
        LOW, MEDIUM, HIGH
    }
}

/**
 * Composable for performance monitoring in UI
 */
@Composable
fun PerformanceMonitor(
    benchmark: PerformanceBenchmark,
    content: @Composable () -> Unit
) {
    val performanceState by benchmark.performanceState.collectAsState()
    
    // Monitor composition performance
    LaunchedEffect(Unit) {
        benchmark.measureScreenLoad("compose_screen") {
            // Screen composition measurement
        }
    }
    
    content()
    
    // Show performance warnings in debug builds
    if (BuildConfig.DEBUG) {
        LaunchedEffect(performanceState) {
            if (!performanceState.isStartupTargetMet) {
                Logger.w("Performance") { "Startup target not met: ${performanceState.startupTime}ms" }
            }
            if (!performanceState.isMemoryTargetMet) {
                Logger.w("Performance") { "Memory target not met: ${(performanceState.currentMemoryUsage * 100).toInt()}%" }
            }
        }
    }
}

/**
 * Extension function for measuring composable performance
 */
@Composable
fun <T> measureComposablePerformance(
    name: String,
    benchmark: PerformanceBenchmark,
    content: @Composable () -> T
): T {
    val startTime = remember { System.currentTimeMillis() }
    
    val result = content()
    
    LaunchedEffect(Unit) {
        val renderTime = System.currentTimeMillis() - startTime
        benchmark.recordMetric("composable_render_$name", renderTime.toDouble(), "ms")
        
        if (renderTime > 16) { // More than one frame at 60fps
            Logger.w("Performance") { "Slow composable render: $name took ${renderTime}ms" }
        }
    }
    
    return result
}