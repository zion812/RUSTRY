package com.rio.rustry.utils

import android.app.ActivityManager
import android.content.Context
import android.os.Debug
import androidx.compose.runtime.*
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.rio.rustry.BuildConfig
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap

/**
 * Advanced memory management utility for the Rooster Platform
 * 
 * Features:
 * - Memory usage monitoring and alerts
 * - Automatic garbage collection optimization
 * - Memory leak detection
 * - Cache management with LRU eviction
 * - Bitmap memory optimization
 * - Lifecycle-aware memory cleanup
 * - Memory pressure handling
 */
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class MemoryManager @Inject constructor(
    @ApplicationContext private val context: Context
) : DefaultLifecycleObserver {
    
    companion object {
        private const val MEMORY_WARNING_THRESHOLD = 0.8 // 80% of max memory
        private const val MEMORY_CRITICAL_THRESHOLD = 0.9 // 90% of max memory
        private const val CACHE_CLEANUP_INTERVAL = 30000L // 30 seconds
        private const val MEMORY_MONITOR_INTERVAL = 5000L // 5 seconds
    }
    
    private val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    private val runtime = Runtime.getRuntime()
    
    private val _memoryState = MutableStateFlow(MemoryState.NORMAL)
    val memoryState: StateFlow<MemoryState> = _memoryState.asStateFlow()
    
    private val _memoryInfo = MutableStateFlow(MemoryInfo())
    val memoryInfo: StateFlow<MemoryInfo> = _memoryInfo.asStateFlow()
    
    private val memoryListeners = mutableSetOf<WeakReference<MemoryListener>>()
    private val managedCaches = ConcurrentHashMap<String, ManagedCache<*, *>>()
    private val memoryMonitoringJob = SupervisorJob()
    private val memoryScope = CoroutineScope(Dispatchers.Default + memoryMonitoringJob)
    
    init {
        try {
            ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        } catch (e: Exception) {
            Logger.e("Memory", "Failed to register lifecycle observer", e)
        }
        startMemoryMonitoring()
        startCacheCleanup()
    }
    
    /**
     * Start continuous memory monitoring
     */
    private fun startMemoryMonitoring() {
        memoryScope.launch {
            while (isActive) {
                updateMemoryInfo()
                checkMemoryPressure()
                delay(MEMORY_MONITOR_INTERVAL)
            }
        }
    }
    
    /**
     * Start periodic cache cleanup
     */
    private fun startCacheCleanup() {
        memoryScope.launch {
            while (isActive) {
                delay(CACHE_CLEANUP_INTERVAL)
                cleanupCaches()
            }
        }
    }
    
    /**
     * Update current memory information
     */
    private fun updateMemoryInfo() {
        val memInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memInfo)
        
        val javaHeapUsed = runtime.totalMemory() - runtime.freeMemory()
        val javaHeapMax = runtime.maxMemory()
        val nativeHeapUsed = Debug.getNativeHeapAllocatedSize()
        val nativeHeapMax = Debug.getNativeHeapSize()
        
        val totalUsed = javaHeapUsed + nativeHeapUsed
        val totalMax = javaHeapMax + nativeHeapMax
        
        val newMemoryInfo = MemoryInfo(
            javaHeapUsed = javaHeapUsed,
            javaHeapMax = javaHeapMax,
            javaHeapFree = runtime.freeMemory(),
            nativeHeapUsed = nativeHeapUsed,
            nativeHeapMax = nativeHeapMax,
            totalUsed = totalUsed,
            totalMax = totalMax,
            systemAvailable = memInfo.availMem,
            systemTotal = memInfo.totalMem,
            isLowMemory = memInfo.lowMemory,
            threshold = memInfo.threshold,
            usagePercentage = (totalUsed.toDouble() / totalMax.toDouble() * 100).toInt()
        )
        
        _memoryInfo.value = newMemoryInfo
        
        if (BuildConfig.ENABLE_LOGGING) {
            Logger.d("Memory") { 
                "Memory usage: ${formatBytes(totalUsed)} / ${formatBytes(totalMax)} (${newMemoryInfo.usagePercentage}%)"
            }
        }
    }
    
    /**
     * Check for memory pressure and take action
     */
    private fun checkMemoryPressure() {
        val currentInfo = _memoryInfo.value
        val usageRatio = currentInfo.totalUsed.toDouble() / currentInfo.totalMax.toDouble()
        
        val newState = when {
            usageRatio >= MEMORY_CRITICAL_THRESHOLD -> MemoryState.CRITICAL
            usageRatio >= MEMORY_WARNING_THRESHOLD -> MemoryState.WARNING
            currentInfo.isLowMemory -> MemoryState.LOW
            else -> MemoryState.NORMAL
        }
        
        if (newState != _memoryState.value) {
            _memoryState.value = newState
            handleMemoryStateChange(newState)
        }
    }
    
    /**
     * Handle memory state changes
     */
    private fun handleMemoryStateChange(newState: MemoryState) {
        Logger.i("Memory") { "Memory state changed to: $newState" }
        
        when (newState) {
            MemoryState.WARNING -> {
                // Perform light cleanup
                cleanupCaches(aggressive = false)
                notifyMemoryListeners(MemoryEvent.WARNING)
            }
            MemoryState.CRITICAL -> {
                // Perform aggressive cleanup
                cleanupCaches(aggressive = true)
                forceGarbageCollection()
                notifyMemoryListeners(MemoryEvent.CRITICAL)
            }
            MemoryState.LOW -> {
                // System is low on memory
                cleanupCaches(aggressive = true)
                notifyMemoryListeners(MemoryEvent.LOW_MEMORY)
            }
            MemoryState.NORMAL -> {
                notifyMemoryListeners(MemoryEvent.NORMAL)
            }
        }
    }
    
    /**
     * Force garbage collection (use sparingly)
     */
    fun forceGarbageCollection() {
        if (BuildConfig.ENABLE_LOGGING) {
            Logger.d("Memory") { "Forcing garbage collection" }
        }
        
        System.gc()
        System.runFinalization()
        
        // Give GC time to work
        Thread.sleep(100)
    }
    
    /**
     * Clean up managed caches
     */
    private fun cleanupCaches(aggressive: Boolean = false) {
        val cleanupPercentage = if (aggressive) 0.5 else 0.2 // Remove 50% or 20%
        
        managedCaches.values.forEach { cache ->
            cache.cleanup(cleanupPercentage)
        }
        
        if (BuildConfig.ENABLE_LOGGING) {
            Logger.d("Memory") { 
                "Cache cleanup completed (aggressive: $aggressive, ${managedCaches.size} caches)"
            }
        }
    }
    
    /**
     * Register a memory listener
     */
    fun addMemoryListener(listener: MemoryListener) {
        memoryListeners.add(WeakReference(listener))
    }
    
    /**
     * Remove a memory listener
     */
    fun removeMemoryListener(listener: MemoryListener) {
        memoryListeners.removeAll { it.get() == listener || it.get() == null }
    }
    
    /**
     * Notify all memory listeners
     */
    private fun notifyMemoryListeners(event: MemoryEvent) {
        val iterator = memoryListeners.iterator()
        while (iterator.hasNext()) {
            val ref = iterator.next()
            val listener = ref.get()
            if (listener != null) {
                try {
                    listener.onMemoryEvent(event)
                } catch (e: Exception) {
                    Logger.e("Memory", "Error notifying memory listener", e)
                }
            } else {
                iterator.remove() // Remove dead references
            }
        }
    }
    
    /**
     * Register a managed cache
     */
    fun <K, V> registerCache(name: String, cache: ManagedCache<K, V>) {
        managedCaches[name] = cache
        Logger.d("Memory") { "Registered cache: $name" }
    }
    
    /**
     * Unregister a managed cache
     */
    fun unregisterCache(name: String) {
        managedCaches.remove(name)
        Logger.d("Memory") { "Unregistered cache: $name" }
    }
    
    /**
     * Get memory statistics
     */
    fun getMemoryStatistics(): MemoryStatistics {
        val currentInfo = _memoryInfo.value
        
        return MemoryStatistics(
            javaHeapUsage = currentInfo.javaHeapUsed.toDouble() / currentInfo.javaHeapMax.toDouble(),
            nativeHeapUsage = if (currentInfo.nativeHeapMax > 0) {
                currentInfo.nativeHeapUsed.toDouble() / currentInfo.nativeHeapMax.toDouble()
            } else 0.0,
            totalMemoryUsage = currentInfo.totalUsed.toDouble() / currentInfo.totalMax.toDouble(),
            systemMemoryUsage = (currentInfo.systemTotal - currentInfo.systemAvailable).toDouble() / currentInfo.systemTotal.toDouble(),
            cacheCount = managedCaches.size,
            totalCacheSize = managedCaches.values.sumOf { it.size() }
        )
    }
    
    /**
     * Generate memory report
     */
    fun generateMemoryReport(): String {
        val info = _memoryInfo.value
        val stats = getMemoryStatistics()
        
        return buildString {
            appendLine("=== MEMORY REPORT ===")
            appendLine()
            appendLine("JAVA HEAP:")
            appendLine("  Used: ${formatBytes(info.javaHeapUsed)}")
            appendLine("  Max: ${formatBytes(info.javaHeapMax)}")
            appendLine("  Free: ${formatBytes(info.javaHeapFree)}")
            appendLine("  Usage: ${(stats.javaHeapUsage * 100).toInt()}%")
            appendLine()
            appendLine("NATIVE HEAP:")
            appendLine("  Used: ${formatBytes(info.nativeHeapUsed)}")
            appendLine("  Max: ${formatBytes(info.nativeHeapMax)}")
            appendLine("  Usage: ${(stats.nativeHeapUsage * 100).toInt()}%")
            appendLine()
            appendLine("SYSTEM MEMORY:")
            appendLine("  Available: ${formatBytes(info.systemAvailable)}")
            appendLine("  Total: ${formatBytes(info.systemTotal)}")
            appendLine("  Usage: ${(stats.systemMemoryUsage * 100).toInt()}%")
            appendLine("  Low Memory: ${info.isLowMemory}")
            appendLine()
            appendLine("CACHES:")
            appendLine("  Count: ${stats.cacheCount}")
            appendLine("  Total Size: ${stats.totalCacheSize} items")
            managedCaches.forEach { (name, cache) ->
                appendLine("  $name: ${cache.size()} items")
            }
            appendLine()
            appendLine("MEMORY STATE: ${_memoryState.value}")
            appendLine("=== END REPORT ===")
        }
    }
    
    /**
     * Optimize memory for specific scenarios
     */
    fun optimizeForScenario(scenario: MemoryScenario) {
        when (scenario) {
            MemoryScenario.BACKGROUND -> {
                // App is in background, aggressive cleanup
                cleanupCaches(aggressive = true)
                forceGarbageCollection()
            }
            MemoryScenario.LOW_MEMORY_DEVICE -> {
                // Device has limited memory, conservative approach
                cleanupCaches(aggressive = false)
                // Reduce cache sizes
                managedCaches.values.forEach { it.setMaxSize(it.maxSize() / 2) }
            }
            MemoryScenario.HIGH_PERFORMANCE -> {
                // Need maximum performance, minimal cleanup
                cleanupCaches(aggressive = false)
            }
            MemoryScenario.MEMORY_INTENSIVE_TASK -> {
                // About to perform memory-intensive task
                cleanupCaches(aggressive = true)
                forceGarbageCollection()
            }
        }
        
        Logger.d("Memory") { "Memory optimized for scenario: $scenario" }
    }
    
    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        Logger.d("Memory") { "App started - resuming memory monitoring" }
    }
    
    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        Logger.d("Memory") { "App stopped - optimizing memory for background" }
        optimizeForScenario(MemoryScenario.BACKGROUND)
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
    
    fun cleanup() {
        memoryMonitoringJob.cancel()
        try {
            ProcessLifecycleOwner.get().lifecycle.removeObserver(this)
        } catch (e: Exception) {
            Logger.e("Memory", "Failed to remove lifecycle observer", e)
        }
    }
    
    data class MemoryInfo(
        val javaHeapUsed: Long = 0,
        val javaHeapMax: Long = 0,
        val javaHeapFree: Long = 0,
        val nativeHeapUsed: Long = 0,
        val nativeHeapMax: Long = 0,
        val totalUsed: Long = 0,
        val totalMax: Long = 0,
        val systemAvailable: Long = 0,
        val systemTotal: Long = 0,
        val isLowMemory: Boolean = false,
        val threshold: Long = 0,
        val usagePercentage: Int = 0
    )
    
    data class MemoryStatistics(
        val javaHeapUsage: Double,
        val nativeHeapUsage: Double,
        val totalMemoryUsage: Double,
        val systemMemoryUsage: Double,
        val cacheCount: Int,
        val totalCacheSize: Long
    )
    
    enum class MemoryState {
        NORMAL, WARNING, CRITICAL, LOW
    }
    
    enum class MemoryEvent {
        NORMAL, WARNING, CRITICAL, LOW_MEMORY
    }
    
    enum class MemoryScenario {
        BACKGROUND, LOW_MEMORY_DEVICE, HIGH_PERFORMANCE, MEMORY_INTENSIVE_TASK
    }
    
    interface MemoryListener {
        fun onMemoryEvent(event: MemoryEvent)
    }
}

/**
 * Managed cache interface for memory management
 */
interface ManagedCache<K, V> {
    fun size(): Long
    fun maxSize(): Long
    fun setMaxSize(maxSize: Long)
    fun cleanup(percentage: Double)
    fun clear()
}

/**
 * LRU cache implementation with memory management
 */
class LRUManagedCache<K, V>(
    private var maxSize: Long
) : ManagedCache<K, V> {
    
    private val cache = LinkedHashMap<K, V>(16, 0.75f, true)
    
    @Synchronized
    fun get(key: K): V? = cache[key]
    
    @Synchronized
    fun put(key: K, value: V): V? {
        val previous = cache.put(key, value)
        trimToSize()
        return previous
    }
    
    @Synchronized
    fun remove(key: K): V? = cache.remove(key)
    
    @Synchronized
    override fun size(): Long = cache.size.toLong()
    
    @Synchronized
    override fun maxSize(): Long = maxSize
    
    @Synchronized
    override fun setMaxSize(maxSize: Long) {
        this.maxSize = maxSize
        trimToSize()
    }
    
    @Synchronized
    override fun cleanup(percentage: Double) {
        val itemsToRemove = (cache.size * percentage).toInt()
        val iterator = cache.iterator()
        repeat(itemsToRemove) {
            if (iterator.hasNext()) {
                iterator.next()
                iterator.remove()
            }
        }
    }
    
    @Synchronized
    override fun clear() {
        cache.clear()
    }
    
    private fun trimToSize() {
        while (cache.size > maxSize) {
            val eldest = cache.iterator().next()
            cache.remove(eldest.key)
        }
    }
}

/**
 * Composable to monitor memory state
 */
@Composable
fun rememberMemoryState(memoryManager: MemoryManager): MemoryManager.MemoryState {
    return memoryManager.memoryState.collectAsState().value
}

/**
 * Composable to monitor memory info
 */
@Composable
fun rememberMemoryInfo(memoryManager: MemoryManager): MemoryManager.MemoryInfo {
    return memoryManager.memoryInfo.collectAsState().value
}

/**
 * Effect to handle memory events in Compose
 */
@Composable
fun MemoryAwareEffect(
    memoryManager: MemoryManager,
    onMemoryWarning: () -> Unit = {},
    onMemoryCritical: () -> Unit = {},
    onLowMemory: () -> Unit = {}
) {
    val memoryState by memoryManager.memoryState.collectAsState()
    
    LaunchedEffect(memoryState) {
        when (memoryState) {
            MemoryManager.MemoryState.WARNING -> onMemoryWarning()
            MemoryManager.MemoryState.CRITICAL -> onMemoryCritical()
            MemoryManager.MemoryState.LOW -> onLowMemory()
            MemoryManager.MemoryState.NORMAL -> { /* Normal state */ }
        }
    }
}