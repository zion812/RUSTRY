package com.rio.rustry.security

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import okhttp3.CertificatePinner
import okhttp3.OkHttpClient
import java.security.KeyStore
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager
import com.rio.rustry.security.EncryptedData
import com.rio.rustry.security.SecurityThreat

/**
 * Comprehensive Security Manager for RUSTRY
 * Handles SSL pinning, data encryption, secure storage, and authentication security
 */
class SecurityManager(private val context: Context) {
    
    companion object {
        private const val KEYSTORE_ALIAS = "RustrySecretKey"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val ENCRYPTED_PREFS_NAME = "rustry_secure_prefs"
        
        // Firebase and API certificate pins (example - replace with actual)
        private const val FIREBASE_PIN = "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA="
        private const val API_PIN = "sha256/BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB="
    }
    
    private val masterKey: MasterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }
    
    private val encryptedSharedPreferences: SharedPreferences by lazy {
        EncryptedSharedPreferences.create(
            context,
            ENCRYPTED_PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
    
    /**
     * Initialize security components
     */
    fun initialize() {
        generateSecretKey()
        setupSSLPinning()
    }
    
    /**
     * Generate or retrieve secret key from Android Keystore
     */
    private fun generateSecretKey() {
        try {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
            keyStore.load(null)
            
            if (!keyStore.containsAlias(KEYSTORE_ALIAS)) {
                val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
                val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                    KEYSTORE_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setUserAuthenticationRequired(false)
                    .setRandomizedEncryptionRequired(true)
                    .build()
                
                keyGenerator.init(keyGenParameterSpec)
                keyGenerator.generateKey()
            }
        } catch (e: Exception) {
            throw SecurityException("Failed to generate secret key", e)
        }
    }
    
    /**
     * Encrypt sensitive data using Android Keystore
     */
    fun encryptData(plainText: String): EncryptedData {
        try {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
            keyStore.load(null)
            
            val secretKey = keyStore.getKey(KEYSTORE_ALIAS, null) as SecretKey
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            
            val iv = cipher.iv
            val encryptedBytes = cipher.doFinal(plainText.toByteArray())
            
            return EncryptedData(
                encryptedData = android.util.Base64.encodeToString(encryptedBytes, android.util.Base64.DEFAULT),
                iv = android.util.Base64.encodeToString(iv, android.util.Base64.DEFAULT)
            )
        } catch (e: Exception) {
            throw SecurityException("Failed to encrypt data", e)
        }
    }
    
    /**
     * Decrypt sensitive data using Android Keystore
     */
    fun decryptData(encryptedData: EncryptedData): String {
        try {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
            keyStore.load(null)
            
            val secretKey = keyStore.getKey(KEYSTORE_ALIAS, null) as SecretKey
            val cipher = Cipher.getInstance(TRANSFORMATION)
            
            val iv = android.util.Base64.decode(encryptedData.iv, android.util.Base64.DEFAULT)
            val gcmParameterSpec = GCMParameterSpec(128, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmParameterSpec)
            
            val encryptedBytes = android.util.Base64.decode(encryptedData.encryptedData, android.util.Base64.DEFAULT)
            val decryptedBytes = cipher.doFinal(encryptedBytes)
            
            return String(decryptedBytes)
        } catch (e: Exception) {
            throw SecurityException("Failed to decrypt data", e)
        }
    }
    
    /**
     * Store sensitive data securely
     */
    fun storeSecureData(key: String, value: String) {
        encryptedSharedPreferences.edit()
            .putString(key, value)
            .apply()
    }
    
    /**
     * Retrieve sensitive data securely
     */
    fun getSecureData(key: String, defaultValue: String = ""): String {
        return encryptedSharedPreferences.getString(key, defaultValue) ?: defaultValue
    }
    
    /**
     * Remove sensitive data
     */
    fun removeSecureData(key: String) {
        encryptedSharedPreferences.edit()
            .remove(key)
            .apply()
    }
    
    /**
     * Clear all secure data
     */
    fun clearAllSecureData() {
        encryptedSharedPreferences.edit()
            .clear()
            .apply()
    }
    
    /**
     * Setup SSL Certificate Pinning
     */
    private fun setupSSLPinning() {
        // Certificate pinning configuration will be used in network client
    }
    
    /**
     * Create secure OkHttpClient with SSL pinning
     */
    fun createSecureHttpClient(): OkHttpClient {
        val certificatePinner = CertificatePinner.Builder()
            .add("*.googleapis.com", FIREBASE_PIN)
            .add("*.firebaseio.com", FIREBASE_PIN)
            .add("api.rustry.com", API_PIN) // Replace with actual API domain
            .build()
        
        return OkHttpClient.Builder()
            .certificatePinner(certificatePinner)
            .addInterceptor(SecurityInterceptor())
            .addNetworkInterceptor(NetworkSecurityInterceptor())
            .build()
    }
    
    /**
     * Validate user session
     */
    fun validateSession(): Boolean {
        val sessionToken = getSecureData("session_token")
        val sessionExpiry = getSecureData("session_expiry").toLongOrNull() ?: 0
        
        return sessionToken.isNotEmpty() && System.currentTimeMillis() < sessionExpiry
    }
    
    /**
     * Store user session securely
     */
    fun storeSession(token: String, expiryTime: Long) {
        storeSecureData("session_token", token)
        storeSecureData("session_expiry", expiryTime.toString())
    }
    
    /**
     * Clear user session
     */
    fun clearSession() {
        removeSecureData("session_token")
        removeSecureData("session_expiry")
    }
    
    /**
     * Generate secure random token
     */
    fun generateSecureToken(length: Int = 32): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return (1..length)
            .map { chars.random() }
            .joinToString("")
    }
    
    /**
     * Hash sensitive data (passwords, etc.)
     */
    fun hashData(data: String, salt: String = generateSecureToken(16)): HashedData {
        val messageDigest = java.security.MessageDigest.getInstance("SHA-256")
        val saltedData = data + salt
        val hashedBytes = messageDigest.digest(saltedData.toByteArray())
        val hashedString = android.util.Base64.encodeToString(hashedBytes, android.util.Base64.DEFAULT)
        
        return HashedData(hashedString, salt)
    }
    
    /**
     * Verify hashed data
     */
    fun verifyHashedData(data: String, hashedData: HashedData): Boolean {
        val newHash = hashData(data, hashedData.salt)
        return newHash.hash == hashedData.hash
    }
    
    /**
     * Detect potential security threats
     */
    fun detectSecurityThreats(): SecurityThreatReport {
        val threats = mutableListOf<SecurityThreat>()
        
        // Check for rooted device
        if (isDeviceRooted()) {
            threats.add(SecurityThreat.UNAUTHORIZED_ACCESS)
        }
        
        // Check for debugging
        if (isDebuggingEnabled()) {
            threats.add(SecurityThreat.SUSPICIOUS_ACTIVITY)
        }
        
        // Check for emulator
        if (isRunningOnEmulator()) {
            threats.add(SecurityThreat.SUSPICIOUS_ACTIVITY)
        }
        
        // Check for suspicious apps
        if (hasSuspiciousApps()) {
            threats.add(SecurityThreat.MALWARE_DETECTED)
        }
        
        return SecurityThreatReport(
            threats = threats,
            riskLevel = calculateRiskLevel(threats),
            timestamp = System.currentTimeMillis()
        )
    }
    
    private fun isDeviceRooted(): Boolean {
        // Check for common root indicators
        val rootPaths = arrayOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su"
        )
        
        return rootPaths.any { java.io.File(it).exists() }
    }
    
    private fun isDebuggingEnabled(): Boolean {
        return (context.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0
    }
    
    private fun isRunningOnEmulator(): Boolean {
        return (android.os.Build.FINGERPRINT.startsWith("generic") ||
                android.os.Build.FINGERPRINT.startsWith("unknown") ||
                android.os.Build.MODEL.contains("google_sdk") ||
                android.os.Build.MODEL.contains("Emulator") ||
                android.os.Build.MODEL.contains("Android SDK built for x86") ||
                android.os.Build.MANUFACTURER.contains("Genymotion") ||
                android.os.Build.BRAND.startsWith("generic") && android.os.Build.DEVICE.startsWith("generic") ||
                "google_sdk" == android.os.Build.PRODUCT)
    }
    
    private fun hasSuspiciousApps(): Boolean {
        val suspiciousPackages = arrayOf(
            "com.noshufou.android.su",
            "com.noshufou.android.su.elite",
            "eu.chainfire.supersu",
            "com.koushikdutta.superuser",
            "com.thirdparty.superuser",
            "com.yellowes.su"
        )
        
        val packageManager = context.packageManager
        return suspiciousPackages.any { packageName ->
            try {
                packageManager.getPackageInfo(packageName, 0)
                true
            } catch (e: android.content.pm.PackageManager.NameNotFoundException) {
                false
            }
        }
    }
    
    private fun calculateRiskLevel(threats: List<SecurityThreat>): RiskLevel {
        return when {
            threats.isEmpty() -> RiskLevel.LOW
            threats.size <= 2 -> RiskLevel.MEDIUM
            else -> RiskLevel.HIGH
        }
    }
}

/**
 * Data classes for security operations
 */
data class HashedData(
    val hash: String,
    val salt: String
)

data class SecurityThreatReport(
    val threats: List<SecurityThreat>,
    val riskLevel: RiskLevel,
    val timestamp: Long
)

enum class RiskLevel {
    LOW, MEDIUM, HIGH, CRITICAL
}

/**
 * Security Interceptor for network requests
 */
class SecurityInterceptor : okhttp3.Interceptor {
    override fun intercept(chain: okhttp3.Interceptor.Chain): okhttp3.Response {
        val originalRequest = chain.request()
        
        // Add security headers
        val secureRequest = originalRequest.newBuilder()
            .addHeader("X-Requested-With", "XMLHttpRequest")
            .addHeader("X-App-Version", android.os.Build.VERSION.RELEASE)
            .addHeader("X-Device-ID", getDeviceId())
            .build()
        
        return chain.proceed(secureRequest)
    }
    
    private fun getDeviceId(): String {
        // Generate a secure device identifier
        return "RUSTRY_" + java.util.UUID.randomUUID().toString()
    }
}

/**
 * Network Security Interceptor
 */
class NetworkSecurityInterceptor : okhttp3.Interceptor {
    override fun intercept(chain: okhttp3.Interceptor.Chain): okhttp3.Response {
        val request = chain.request()
        val response = chain.proceed(request)
        
        // Validate response headers for security
        val contentType = response.header("Content-Type")
        if (contentType != null && !isValidContentType(contentType)) {
            throw SecurityException("Invalid content type received: $contentType")
        }
        
        return response
    }
    
    private fun isValidContentType(contentType: String): Boolean {
        val validTypes = listOf(
            "application/json",
            "application/x-protobuf",
            "text/plain",
            "image/jpeg",
            "image/png",
            "image/webp"
        )
        return validTypes.any { contentType.startsWith(it) }
    }
}

/**
 * Biometric Authentication Helper
 * Currently disabled - requires biometric dependencies
 */
class BiometricAuthHelper(private val context: Context) {
    
    fun isBiometricAvailable(): Boolean {
        // Biometric authentication is currently disabled
        // Uncomment and add biometric dependencies to enable
        return false
    }
    
    fun authenticateWithBiometric(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        // Biometric authentication is currently disabled
        onError("Biometric authentication not available")
    }
}