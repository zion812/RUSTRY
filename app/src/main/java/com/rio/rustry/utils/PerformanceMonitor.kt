package com.rio.rustry.utils

import android.os.Build
import android.os.Debug
import android.os.SystemClock
import androidx.compose.runtime.*
import androidx.tracing.Trace
import com.rio.rustry.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

/**
 * Advanced performance monitoring utility for the Rooster Platform
 * 
 * Features:
 * - Method execution time tracking
 * - Memory usage monitoring
 * - Compose recomposition tracking
 * - Network performance metrics
 * - Database operation timing
 * - Custom performance traces
 */
object PerformanceMonitor {
    
    private val performanceMetrics = ConcurrentHashMap<String, PerformanceMetric>()
    private val activeTraces = ConcurrentHashMap<String, Long>()
    private val recompositionCounts = ConcurrentHashMap<String, AtomicLong>()
    
    data class PerformanceMetric(
        val name: String,
        val totalTime: AtomicLong = AtomicLong(0),
        val callCount: AtomicLong = AtomicLong(0),
        val maxTime: AtomicLong = AtomicLong(0),
        val minTime: AtomicLong = AtomicLong(Long.MAX_VALUE)
    ) {
        val averageTime: Long
            get() = if (callCount.get() > 0) totalTime.get() / callCount.get() else 0
    }
    
    /**
     * Start a performance trace
     */
    @JvmStatic
    fun startTrace(traceName: String) {
        if (BuildConfig.ENABLE_LOGGING) {
            activeTraces[traceName] = SystemClock.elapsedRealtime()
            
            // Use Android's built-in tracing for systrace
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                Trace.beginSection(traceName)
            }
        }
    }
    
    /**
     * Stop a performance trace and record metrics
     */
    @JvmStatic
    fun stopTrace(traceName: String) {
        if (BuildConfig.ENABLE_LOGGING) {
            val startTime = activeTraces.remove(traceName)
            if (startTime != null) {
                val duration = SystemClock.elapsedRealtime() - startTime
                recordMetric(traceName, duration)
                
                Logger.d("Performance") { "$traceName completed in ${duration}ms" }
            }
        }
    }
    
    /**
     * Measure execution time of a block
     */
    @JvmStatic
    inline fun <T> measureExecutionTime(
        operationName: String,
        block: () -> T
    ): T {
        return if (BuildConfig.ENABLE_LOGGING) {
            val startTime = SystemClock.elapsedRealtime()
            val result = block()
            val duration = SystemClock.elapsedRealtime() - startTime
            recordMetric(operationName, duration)
            result
        } else {
            block()
        }
    }
    
    /**
     * Record a performance metric
     */
    @JvmStatic
    fun recordMetric(name: String, duration: Long) {
        if (BuildConfig.ENABLE_LOGGING) {
            val metric = performanceMetrics.getOrPut(name) { PerformanceMetric(name) }
            
            metric.totalTime.addAndGet(duration)
            metric.callCount.incrementAndGet()
            
            // Update max time
            var currentMax = metric.maxTime.get()
            while (duration > currentMax && !metric.maxTime.compareAndSet(currentMax, duration)) {
                currentMax = metric.maxTime.get()
            }
            
            // Update min time
            var currentMin = metric.minTime.get()
            while (duration < currentMin && !metric.minTime.compareAndSet(currentMin, duration)) {
                currentMin = metric.minTime.get()
            }
        }
    }
    
    /**
     * Get performance metrics for a specific operation
     */
    @JvmStatic
    fun getMetric(name: String): PerformanceMetric? = performanceMetrics[name]
    
    /**
     * Get all performance metrics
     */
    @JvmStatic
    fun getAllMetrics(): Map<String, PerformanceMetric> = performanceMetrics.toMap()
    
    /**
     * Clear all performance metrics
     */
    @JvmStatic
    fun clearMetrics() {
        performanceMetrics.clear()
        recompositionCounts.clear()
    }
    
    /**
     * Log current memory usage
     */
    @JvmStatic
    fun logMemoryUsage(tag: String = "Memory") {
        if (BuildConfig.ENABLE_LOGGING) {
            val runtime = Runtime.getRuntime()
            val usedMemory = runtime.totalMemory() - runtime.freeMemory()
            val maxMemory = runtime.maxMemory()
            val nativeHeap = Debug.getNativeHeapAllocatedSize()
            
            Logger.d(tag) {
                buildString {
                    appendLine("Memory Usage Report:")
                    appendLine("  Java Heap: ${formatBytes(usedMemory)} / ${formatBytes(maxMemory)}")
                    appendLine("  Native Heap: ${formatBytes(nativeHeap)}")
                    appendLine("  Available: ${formatBytes(runtime.freeMemory())}")
                    appendLine("  Usage: ${(usedMemory * 100 / maxMemory)}%")
                }
            }
        }
    }
    
    /**
     * Monitor network operation performance
     */
    @JvmStatic
    fun monitorNetworkOperation(
        operation: String,
        url: String,
        method: String = "GET"
    ): NetworkMonitor {
        return NetworkMonitor(operation, url, method)
    }
    
    /**
     * Monitor database operation performance
     */
    @JvmStatic
    fun monitorDatabaseOperation(
        operation: String,
        table: String
    ): DatabaseMonitor {
        return DatabaseMonitor(operation, table)
    }
    
    /**
     * Track Compose recompositions
     */
    @JvmStatic
    fun trackRecomposition(composableName: String) {
        if (BuildConfig.ENABLE_LOGGING) {
            val count = recompositionCounts.getOrPut(composableName) { AtomicLong(0) }
            val newCount = count.incrementAndGet()
            
            if (newCount % 10 == 0L) { // Log every 10th recomposition
                Logger.w("Compose", { "$composableName has recomposed $newCount times" })
            }
        }
    }
    
    /**
     * Get recomposition count for a composable
     */
    @JvmStatic
    fun getRecompositionCount(composableName: String): Long {
        return recompositionCounts[composableName]?.get() ?: 0
    }
    
    /**
     * Generate performance report
     */
    @JvmStatic
    fun generatePerformanceReport(): String {
        return buildString {
            appendLine("=== ROOSTER PLATFORM PERFORMANCE REPORT ===")
            appendLine()
            
            // Memory info
            val runtime = Runtime.getRuntime()
            val usedMemory = runtime.totalMemory() - runtime.freeMemory()
            val maxMemory = runtime.maxMemory()
            
            appendLine("MEMORY USAGE:")
            appendLine("  Used: ${formatBytes(usedMemory)}")
            appendLine("  Max: ${formatBytes(maxMemory)}")
            appendLine("  Usage: ${(usedMemory * 100 / maxMemory)}%")
            appendLine()
            
            // Performance metrics
            appendLine("OPERATION METRICS:")
            performanceMetrics.values.sortedByDescending { it.averageTime }.forEach { metric ->
                appendLine("  ${metric.name}:")
                appendLine("    Calls: ${metric.callCount.get()}")
                appendLine("    Avg: ${metric.averageTime}ms")
                appendLine("    Min: ${metric.minTime.get()}ms")
                appendLine("    Max: ${metric.maxTime.get()}ms")
                appendLine("    Total: ${metric.totalTime.get()}ms")
                appendLine()
            }
            
            // Recomposition counts
            if (recompositionCounts.isNotEmpty()) {
                appendLine("COMPOSE RECOMPOSITIONS:")
                recompositionCounts.entries.sortedByDescending { it.value.get() }.forEach { (name, count) ->
                    appendLine("  $name: ${count.get()} times")
                }
                appendLine()
            }
            
            appendLine("=== END REPORT ===")
        }
    }
    
    /**
     * Send performance report to analytics
     */
    @JvmStatic
    fun sendPerformanceReport() {
        if (BuildConfig.ENABLE_ANALYTICS) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val report = generatePerformanceReport()
                    Logger.i("Performance") { "Performance Report:\n$report" }
                    
                    // Send critical metrics to Firebase Performance
                    performanceMetrics.values.forEach { metric ->
                        if (metric.callCount.get() > 0) {
                            // Custom trace for Firebase Performance Monitoring
                            // This would be sent to Firebase Performance in a real implementation
                        }
                    }
                } catch (e: Exception) {
                    Logger.e("Performance", "Failed to send performance report", e)
                }
            }
        }
    }
    
    private fun formatBytes(bytes: Long): String {
        val kb = bytes / 1024.0
        val mb = kb / 1024.0
        val gb = mb / 1024.0
        
        return when {
            gb >= 1 -> "%.2f GB".format(gb)
            mb >= 1 -> "%.2f MB".format(mb)
            kb >= 1 -> "%.2f KB".format(kb)
            else -> "$bytes B"
        }
    }
    
    /**
     * Network operation monitor
     */
    class NetworkMonitor(
        private val operation: String,
        private val url: String,
        private val method: String
    ) {
        private val startTime = SystemClock.elapsedRealtime()
        
        fun complete(responseCode: Int, bytesReceived: Long = 0) {
            val duration = SystemClock.elapsedRealtime() - startTime
            recordMetric("network_$operation", duration)
            
            Logger.logNetworkCall(
                method = method,
                url = url,
                responseCode = responseCode,
                duration = duration
            )
        }
        
        fun failed(error: Throwable) {
            val duration = SystemClock.elapsedRealtime() - startTime
            recordMetric("network_${operation}_failed", duration)
            
            Logger.e("Network", "Network operation failed: $method $url", error)
        }
    }
    
    /**
     * Database operation monitor
     */
    class DatabaseMonitor(
        private val operation: String,
        private val table: String
    ) {
        private val startTime = SystemClock.elapsedRealtime()
        
        fun complete(rowsAffected: Int = 0) {
            val duration = SystemClock.elapsedRealtime() - startTime
            recordMetric("db_$operation", duration)
            
            Logger.logDatabaseOperation(operation, table, duration)
        }
        
        fun failed(error: Throwable) {
            val duration = SystemClock.elapsedRealtime() - startTime
            recordMetric("db_${operation}_failed", duration)
            
            Logger.e("Database", "Database operation failed: $operation on $table", error)
        }
    }
}

/**
 * Compose function to track recompositions
 */
@Composable
fun TrackRecomposition(name: String) {
    LaunchedEffect(Unit) {
        PerformanceMonitor.trackRecomposition(name)
    }
}

/**
 * Extension function for measuring coroutine execution time
 */
suspend inline fun <T> measureSuspendTime(
    operationName: String,
    crossinline block: suspend () -> T
): T {
    return if (BuildConfig.ENABLE_LOGGING) {
        val startTime = SystemClock.elapsedRealtime()
        val result = block()
        val duration = SystemClock.elapsedRealtime() - startTime
        PerformanceMonitor.recordMetric(operationName, duration)
        result
    } else {
        block()
    }
}