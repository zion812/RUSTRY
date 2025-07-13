package com.rio.rustry.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.rio.rustry.BuildConfig
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Advanced network management utility for the Rooster Platform
 * 
 * Features:
 * - Network connectivity monitoring
 * - Automatic retry with exponential backoff
 * - Request caching and optimization
 * - Performance monitoring
 * - Error handling and recovery
 * - Offline support
 */
class NetworkManager(
    private val context: Context
) {
    
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    
    private val _networkState = MutableStateFlow(NetworkState.Unknown)
    val networkState: StateFlow<NetworkState> = _networkState.asStateFlow()
    
    private val _isOnline = MutableStateFlow(false)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()
    
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            updateNetworkState()
        }
        
        override fun onLost(network: Network) {
            super.onLost(network)
            updateNetworkState()
        }
        
        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            super.onCapabilitiesChanged(network, networkCapabilities)
            updateNetworkState()
        }
    }
    
    // Optimized OkHttp client
    val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(BuildConfig.NETWORK_TIMEOUT.toLong(), TimeUnit.MILLISECONDS)
            .readTimeout(BuildConfig.NETWORK_TIMEOUT.toLong(), TimeUnit.MILLISECONDS)
            .writeTimeout(BuildConfig.NETWORK_TIMEOUT.toLong(), TimeUnit.MILLISECONDS)
            .cache(
                Cache(
                    directory = context.cacheDir.resolve("http_cache"),
                    maxSize = 50L * 1024L * 1024L // 50 MB
                )
            )
            .addInterceptor(createLoggingInterceptor())
            .addInterceptor(createCacheInterceptor())
            .addInterceptor(createRetryInterceptor())
            .addNetworkInterceptor(createNetworkCacheInterceptor())
            .build()
    }
    
    init {
        startNetworkMonitoring()
        updateNetworkState()
    }
    
    private fun startNetworkMonitoring() {
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .build()
        
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }
    
    private fun updateNetworkState() {
        val activeNetwork = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        
        val state = when {
            networkCapabilities == null -> NetworkState.Disconnected
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkState.WiFi
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkState.Cellular
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> NetworkState.Ethernet
            else -> NetworkState.Unknown
        }
        
        _networkState.value = state
        _isOnline.value = state != NetworkState.Disconnected
        
        Logger.d("Network") { "Network state changed to: $state" }
    }
    
    private fun createLoggingInterceptor(): Interceptor {
        return if (BuildConfig.ENABLE_LOGGING) {
            HttpLoggingInterceptor { message ->
                Logger.d("OkHttp") { message }
            }.apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
        } else {
            Interceptor { chain -> chain.proceed(chain.request()) }
        }
    }
    
    private fun createCacheInterceptor(): Interceptor {
        return Interceptor { chain ->
            var request = chain.request()
            
            // If offline, force cache
            if (!isOnline.value) {
                request = request.newBuilder()
                    .cacheControl(
                        CacheControl.Builder()
                            .onlyIfCached()
                            .maxStale(7, TimeUnit.DAYS)
                            .build()
                    )
                    .build()
            }
            
            chain.proceed(request)
        }
    }
    
    private fun createNetworkCacheInterceptor(): Interceptor {
        return Interceptor { chain ->
            val response = chain.proceed(chain.request())
            
            // Cache responses for 5 minutes when online
            val cacheControl = CacheControl.Builder()
                .maxAge(5, TimeUnit.MINUTES)
                .build()
            
            response.newBuilder()
                .header("Cache-Control", cacheControl.toString())
                .build()
        }
    }
    
    private fun createRetryInterceptor(): Interceptor {
        return Interceptor { chain ->
            val request = chain.request()
            var response: Response? = null
            var exception: IOException? = null
            
            var tryCount = 0
            val maxRetries = 3
            
            while (tryCount < maxRetries) {
                try {
                    response = chain.proceed(request)
                    if (response.isSuccessful) {
                        break
                    }
                } catch (e: IOException) {
                    exception = e
                    Logger.w("Network", { "Network request failed (attempt ${tryCount + 1}): ${e.message}" })
                }
                
                tryCount++
                
                if (tryCount < maxRetries) {
                    // Exponential backoff
                    val delay = (1000 * Math.pow(2.0, tryCount.toDouble())).toLong()
                    Thread.sleep(delay)
                }
            }
            
            response ?: throw (exception ?: IOException("Unknown network error"))
        }
    }
    
    /**
     * Execute a network request with automatic retry and error handling
     */
    suspend fun <T> executeRequest(
        request: suspend () -> T,
        maxRetries: Int = 3,
        initialDelay: Long = 1000L,
        maxDelay: Long = 10000L,
        factor: Double = 2.0
    ): Result<T> {
        return withContext(Dispatchers.IO) {
            var currentDelay = initialDelay
            repeat(maxRetries) { attempt ->
                try {
                    val result = request()
                    return@withContext Result.success(result)
                } catch (e: Exception) {
                    Logger.w("Network", { "Request failed (attempt ${attempt + 1}): ${e.message}" })
                    
                    if (attempt == maxRetries - 1) {
                        return@withContext Result.failure(e)
                    }
                    
                    delay(currentDelay)
                    currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelay)
                }
            }
            Result.failure(IOException("Max retries exceeded"))
        }
    }
    
    /**
     * Check if a specific host is reachable
     */
    suspend fun isHostReachable(host: String, timeout: Int = 5000): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url("https://$host")
                    .head()
                    .build()
                
                val response = okHttpClient.newCall(request).execute()
                response.isSuccessful
            } catch (e: Exception) {
                false
            }
        }
    }
    
    /**
     * Get network quality information
     */
    fun getNetworkQuality(): NetworkQuality {
        val activeNetwork = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        
        return when {
            networkCapabilities == null -> NetworkQuality.NO_CONNECTION
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                val signalStrength = networkCapabilities.signalStrength
                when {
                    signalStrength >= -50 -> NetworkQuality.EXCELLENT
                    signalStrength >= -60 -> NetworkQuality.GOOD
                    signalStrength >= -70 -> NetworkQuality.FAIR
                    else -> NetworkQuality.POOR
                }
            }
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                // Cellular quality estimation based on capabilities
                when {
                    networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED) -> NetworkQuality.GOOD
                    networkCapabilities.linkDownstreamBandwidthKbps > 10000 -> NetworkQuality.GOOD
                    networkCapabilities.linkDownstreamBandwidthKbps > 1000 -> NetworkQuality.FAIR
                    else -> NetworkQuality.POOR
                }
            }
            else -> NetworkQuality.UNKNOWN
        }
    }
    
    /**
     * Clear network cache
     */
    fun clearCache() {
        try {
            okHttpClient.cache?.evictAll()
            Logger.d("Network") { "Network cache cleared" }
        } catch (e: Exception) {
            Logger.e("Network", "Failed to clear network cache", e)
        }
    }
    
    /**
     * Get cache size information
     */
    fun getCacheSize(): Long {
        return try {
            okHttpClient.cache?.size() ?: 0L
        } catch (e: Exception) {
            0L
        }
    }
    
    fun stopNetworkMonitoring() {
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }
    
    enum class NetworkState {
        WiFi, Cellular, Ethernet, Disconnected, Unknown
    }
    
    enum class NetworkQuality {
        EXCELLENT, GOOD, FAIR, POOR, NO_CONNECTION, UNKNOWN
    }
}

/**
 * Composable to observe network state
 */
@Composable
fun rememberNetworkState(): NetworkManager.NetworkState {
    val context = LocalContext.current
    val networkManager = remember { NetworkManager(context) }
    
    return networkManager.networkState.collectAsState().value
}

/**
 * Composable to observe online state
 */
@Composable
fun rememberIsOnline(): Boolean {
    val context = LocalContext.current
    val networkManager = remember { NetworkManager(context) }
    
    return networkManager.isOnline.collectAsState().value
}

/**
 * Network-aware composable that shows different content based on connectivity
 */
@Composable
fun NetworkAwareContent(
    onlineContent: @Composable () -> Unit,
    offlineContent: @Composable () -> Unit
) {
    val isOnline = rememberIsOnline()
    
    if (isOnline) {
        onlineContent()
    } else {
        offlineContent()
    }
}

/**
 * Extension function for Flow to handle network errors
 */
fun <T> Flow<T>.retryOnNetworkError(
    maxRetries: Int = 3,
    initialDelay: Long = 1000L,
    maxDelay: Long = 10000L,
    factor: Double = 2.0
): Flow<T> {
    return retryWhen { cause, attempt ->
        if (attempt < maxRetries && (cause is IOException || cause.cause is IOException)) {
            val delay = (initialDelay * Math.pow(factor, attempt.toDouble())).toLong()
                .coerceAtMost(maxDelay)
            delay(delay)
            true
        } else {
            false
        }
    }
}

/**
 * Suspend function to wait for network connectivity
 */
suspend fun waitForNetwork(
    networkManager: NetworkManager,
    timeout: Long = 30000L
): Boolean {
    return withTimeoutOrNull(timeout) {
        networkManager.isOnline.first { it }
        true
    } ?: false
}