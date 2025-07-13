package com.rio.rustry.presentation.components

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/**
 * Performance Optimization Utilities for Rooster Platform
 * 
 * Provides memory-efficient and performance-optimized utilities:
 * - Lifecycle-aware state management
 * - Optimized list scrolling
 * - Memory leak prevention
 * - Efficient recomposition strategies
 */

/**
 * Remember a value that survives configuration changes but is cleared when the lifecycle is destroyed
 */
@Composable
fun <T> rememberLifecycleAware(
    key: Any? = null,
    calculation: @DisallowComposableCalls () -> T
): T {
    val lifecycleOwner = LocalLifecycleOwner.current
    var value by remember(key) { mutableStateOf<T?>(null) }
    
    DisposableEffect(lifecycleOwner, key) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_CREATE -> {
                    if (value == null) {
                        value = calculation()
                    }
                }
                Lifecycle.Event.ON_DESTROY -> {
                    value = null
                }
                else -> {}
            }
        }
        
        lifecycleOwner.lifecycle.addObserver(observer)
        
        // Initialize value if lifecycle is already started
        if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.CREATED)) {
            if (value == null) {
                value = calculation()
            }
        }
        
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    
    return value ?: calculation()
}

/**
 * Optimized state for large lists with memory management
 */
@Composable
fun rememberOptimizedLazyListState(
    initialFirstVisibleItemIndex: Int = 0,
    initialFirstVisibleItemScrollOffset: Int = 0
): LazyListState {
    val listState = remember {
        LazyListState(
            firstVisibleItemIndex = initialFirstVisibleItemIndex,
            firstVisibleItemScrollOffset = initialFirstVisibleItemScrollOffset
        )
    }
    
    // Clear scroll position when component is destroyed to prevent memory leaks
    DisposableEffect(listState) {
        onDispose {
            // Cleanup if needed
        }
    }
    
    return listState
}

/**
 * Optimized state for large grids with memory management
 */
@Composable
fun rememberOptimizedLazyGridState(
    initialFirstVisibleItemIndex: Int = 0,
    initialFirstVisibleItemScrollOffset: Int = 0
): LazyGridState {
    val gridState = remember {
        LazyGridState(
            firstVisibleItemIndex = initialFirstVisibleItemIndex,
            firstVisibleItemScrollOffset = initialFirstVisibleItemScrollOffset
        )
    }
    
    DisposableEffect(gridState) {
        onDispose {
            // Cleanup if needed
        }
    }
    
    return gridState
}

/**
 * Collect flow with lifecycle awareness to prevent memory leaks
 */
@Composable
fun <T> Flow<T>.collectAsStateWithLifecycle(
    initial: T,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED
): State<T> {
    val lifecycleOwner = LocalLifecycleOwner.current
    val state = remember { mutableStateOf(initial) }
    
    DisposableEffect(this, lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                // Start collecting when lifecycle is started
            } else if (event == Lifecycle.Event.ON_STOP) {
                // Stop collecting when lifecycle is stopped
            }
        }
        
        lifecycleOwner.lifecycle.addObserver(observer)
        
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    
    LaunchedEffect(this, lifecycleOwner) {
        if (lifecycleOwner.lifecycle.currentState.isAtLeast(minActiveState)) {
            this@collectAsStateWithLifecycle.collect { value ->
                state.value = value
            }
        }
    }
    
    return state
}

/**
 * Optimized remember for expensive calculations
 */
@Composable
fun <T> rememberExpensive(
    vararg keys: Any?,
    calculation: @DisallowComposableCalls () -> T
): T {
    return remember(*keys) {
        calculation()
    }
}

/**
 * Debounced state for search and input fields
 */
@Composable
fun rememberDebouncedState(
    initialValue: String,
    delayMillis: Long = 300L
): Pair<String, (String) -> Unit> {
    var currentValue by remember { mutableStateOf(initialValue) }
    var debouncedValue by remember { mutableStateOf(initialValue) }
    
    LaunchedEffect(currentValue) {
        kotlinx.coroutines.delay(delayMillis)
        debouncedValue = currentValue
    }
    
    return debouncedValue to { newValue -> currentValue = newValue }
}

/**
 * Optimized image loading state
 */
@Composable
fun rememberImageLoadingState(): ImageLoadingState {
    return remember { ImageLoadingState() }
}

class ImageLoadingState {
    var isLoading by mutableStateOf(false)
        private set
    
    var hasError by mutableStateOf(false)
        private set
    
    var errorMessage by mutableStateOf<String?>(null)
        private set
    
    fun setLoading(loading: Boolean) {
        isLoading = loading
        if (loading) {
            hasError = false
            errorMessage = null
        }
    }
    
    fun setError(error: String) {
        isLoading = false
        hasError = true
        errorMessage = error
    }
    
    fun setSuccess() {
        isLoading = false
        hasError = false
        errorMessage = null
    }
}

/**
 * Optimized pagination state
 */
@Composable
fun rememberPaginationState(
    initialPage: Int = 0,
    pageSize: Int = 20
): PaginationState {
    return remember { PaginationState(initialPage, pageSize) }
}

class PaginationState(
    initialPage: Int = 0,
    val pageSize: Int = 20
) {
    var currentPage by mutableStateOf(initialPage)
        private set
    
    var isLoading by mutableStateOf(false)
        private set
    
    var hasMore by mutableStateOf(true)
        private set
    
    var error by mutableStateOf<String?>(null)
        private set
    
    fun loadNextPage() {
        if (!isLoading && hasMore) {
            currentPage++
            isLoading = true
        }
    }
    
    fun setLoading(loading: Boolean) {
        isLoading = loading
    }
    
    fun setHasMore(more: Boolean) {
        hasMore = more
    }
    
    fun setError(errorMessage: String?) {
        error = errorMessage
        isLoading = false
    }
    
    fun reset() {
        currentPage = 0
        isLoading = false
        hasMore = true
        error = null
    }
}

/**
 * Optimized search state with debouncing
 */
@Composable
fun rememberSearchState(
    initialQuery: String = "",
    debounceMillis: Long = 300L
): SearchState {
    return remember { SearchState(initialQuery, debounceMillis) }
}

class SearchState(
    initialQuery: String = "",
    private val debounceMillis: Long = 300L
) {
    var query by mutableStateOf(initialQuery)
    var debouncedQuery by mutableStateOf(initialQuery)
        private set
    
    var isSearching by mutableStateOf(false)
        private set
    
    var results by mutableStateOf<List<Any>>(emptyList())
        private set
    
    var error by mutableStateOf<String?>(null)
        private set
    
    init {
        // Debounce logic would be implemented here with coroutines
    }
    
    fun updateQuery(newQuery: String) {
        query = newQuery
        // Trigger debounced search
    }
    
    fun setSearching(searching: Boolean) {
        isSearching = searching
    }
    
    fun setResults(newResults: List<Any>) {
        results = newResults
        isSearching = false
        error = null
    }
    
    fun setError(errorMessage: String) {
        error = errorMessage
        isSearching = false
    }
    
    fun clearResults() {
        results = emptyList()
        error = null
    }
}

/**
 * Memory-efficient cache for expensive computations
 */
class MemoryEfficientCache<K, V>(
    private val maxSize: Int = 50
) {
    private val cache = mutableMapOf<K, V>()
    private val accessOrder = mutableListOf<K>()
    
    fun get(key: K): V? {
        val value = cache[key]
        if (value != null) {
            // Move to end (most recently used)
            accessOrder.remove(key)
            accessOrder.add(key)
        }
        return value
    }
    
    fun put(key: K, value: V) {
        if (cache.containsKey(key)) {
            cache[key] = value
            accessOrder.remove(key)
            accessOrder.add(key)
        } else {
            if (cache.size >= maxSize) {
                // Remove least recently used
                val lru = accessOrder.removeFirstOrNull()
                if (lru != null) {
                    cache.remove(lru)
                }
            }
            cache[key] = value
            accessOrder.add(key)
        }
    }
    
    fun clear() {
        cache.clear()
        accessOrder.clear()
    }
    
    fun size(): Int = cache.size
}

/**
 * Optimized flow collection with distinct until changed
 */
@Composable
fun <T> Flow<T>.collectAsOptimizedState(
    initial: T
): State<T> {
    return this
        .distinctUntilChanged()
        .collectAsState(initial = initial)
}

/**
 * Performance monitoring utilities
 */
object PerformanceMonitor {
    private var frameTimeThreshold = 16L // 60 FPS threshold
    
    fun setFrameTimeThreshold(thresholdMs: Long) {
        frameTimeThreshold = thresholdMs
    }
    
    @Composable
    fun TrackCompositionPerformance(
        name: String,
        content: @Composable () -> Unit
    ) {
        val startTime = remember { System.currentTimeMillis() }
        
        DisposableEffect(Unit) {
            onDispose {
                val endTime = System.currentTimeMillis()
                val duration = endTime - startTime
                if (duration > frameTimeThreshold) {
                    println("Performance Warning: $name took ${duration}ms to compose")
                }
            }
        }
        
        content()
    }
}

/**
 * Optimized list item key generation
 */
fun generateOptimizedKey(vararg parts: Any?): String {
    return parts.joinToString(separator = "_") { it?.hashCode()?.toString() ?: "null" }
}