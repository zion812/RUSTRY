package com.rio.rustry.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Network Manager for monitoring connectivity and managing network operations
 */
class NetworkManager(private val context: Context) {
    
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()
    
    private val _networkType = MutableStateFlow(NetworkType.NONE)
    val networkType: StateFlow<NetworkType> = _networkType.asStateFlow()
    
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            _isConnected.value = true
            updateNetworkType()
        }
        
        override fun onLost(network: Network) {
            super.onLost(network)
            _isConnected.value = false
            _networkType.value = NetworkType.NONE
        }
        
        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            super.onCapabilitiesChanged(network, networkCapabilities)
            updateNetworkType()
        }
    }
    
    init {
        startNetworkMonitoring()
        updateConnectionStatus()
    }
    
    private fun startNetworkMonitoring() {
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }
    
    fun stopNetworkMonitoring() {
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }
    
    private fun updateConnectionStatus() {
        val activeNetwork = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        
        _isConnected.value = networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        updateNetworkType()
    }
    
    private fun updateNetworkType() {
        val activeNetwork = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        
        _networkType.value = when {
            networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> NetworkType.WIFI
            networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> NetworkType.CELLULAR
            networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) == true -> NetworkType.ETHERNET
            else -> NetworkType.NONE
        }
    }
    
    fun clearCache() {
        // Clear network-related cache if any
    }
    
    enum class NetworkType {
        NONE, WIFI, CELLULAR, ETHERNET
    }
}