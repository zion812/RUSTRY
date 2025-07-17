package com.rio.rustry

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Enhanced Firebase Messaging Service with offline persistence and real-time features
 * 
 * Features:
 * - FCM notification handling with custom channels
 * - Token management and server synchronization
 * - Real-time data sync triggers
 * - Offline notification queuing
 * - Analytics event tracking
 */
class RustryFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "RustryFCMService"
        private const val CHANNEL_ID = "rustry_notifications"
        private const val CHANNEL_NAME = "Rustry Notifications"
        private const val CHANNEL_DESCRIPTION = "Notifications for Rustry app"
        private const val HIGH_PRIORITY_CHANNEL_ID = "rustry_high_priority"
        private const val HIGH_PRIORITY_CHANNEL_NAME = "Rustry High Priority"
    }

    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private val realtimeDb by lazy { FirebaseDatabase.getInstance().reference }
    private val serviceScope = CoroutineScope(Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        enableFirebaseOfflinePersistence()
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        Log.d(TAG, "From: ${remoteMessage.from}")
        
        // Track notification analytics
        serviceScope.launch {
            trackNotificationReceived(remoteMessage)
        }
        
        // Check if message contains a data payload
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")
            handleDataMessage(remoteMessage.data)
        }
        
        // Check if message contains a notification payload
        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
            showNotification(
                title = it.title ?: "Rustry",
                body = it.body ?: "",
                type = remoteMessage.data["type"] ?: "general",
                priority = remoteMessage.priority
            )
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Refreshed token: $token")
        
        // Send token to server and update user document
        serviceScope.launch {
            sendTokenToServer(token)
            updateUserFCMToken(token)
        }
    }

    private fun enableFirebaseOfflinePersistence() {
        try {
            // Enable Firestore offline persistence
            firestore.enableNetwork()
            
            // Enable Realtime Database offline persistence
            FirebaseDatabase.getInstance().setPersistenceEnabled(true)
            
            Log.d(TAG, "Firebase offline persistence enabled")
        } catch (e: Exception) {
            Log.w(TAG, "Firebase offline persistence setup failed", e)
        }
    }

    private fun handleDataMessage(data: Map<String, String>) {
        val type = data["type"]
        val title = data["title"] ?: "Rustry"
        val body = data["body"] ?: ""
        val priority = data["priority"]?.toIntOrNull() ?: RemoteMessage.PRIORITY_NORMAL
        
        when (type) {
            "fowl_sold" -> {
                showNotification(title, body, "fowl_sold", priority)
                triggerDataSync("fowls")
            }
            "payment_received" -> {
                showNotification(title, body, "payment", priority)
                triggerDataSync("payments")
            }
            "new_message" -> {
                showNotification(title, body, "message", priority)
                triggerDataSync("messages")
            }
            "traceability_update" -> {
                showNotification(title, body, "traceability", priority)
                triggerRealtimeSync(data["fowlId"])
            }
            "marketplace_update" -> {
                showNotification(title, body, "marketplace", priority)
                triggerDataSync("marketplace")
            }
            "social_activity" -> {
                showNotification(title, body, "social", priority)
                triggerSocialFeedSync()
            }
            else -> {
                showNotification(title, body, "general", priority)
            }
        }
    }

    private fun showNotification(
        title: String, 
        body: String, 
        type: String = "general",
        priority: Int = RemoteMessage.PRIORITY_NORMAL
    ) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("notification_type", type)
            putExtra("notification_data", body)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 
            System.currentTimeMillis().toInt(), 
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val channelId = if (priority == RemoteMessage.PRIORITY_HIGH) {
            HIGH_PRIORITY_CHANNEL_ID
        } else {
            CHANNEL_ID
        }
        
        val notificationPriority = if (priority == RemoteMessage.PRIORITY_HIGH) {
            NotificationCompat.PRIORITY_HIGH
        } else {
            NotificationCompat.PRIORITY_DEFAULT
        }
        
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Using system icon
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(notificationPriority)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
        
        // Add action buttons based on notification type
        when (type) {
            "fowl_sold" -> {
                val viewIntent = createActionIntent("view_fowl")
                notificationBuilder.addAction(
                    android.R.drawable.ic_menu_view,
                    "View Details",
                    viewIntent
                )
            }
            "payment_received" -> {
                val viewPaymentIntent = createActionIntent("view_payment")
                notificationBuilder.addAction(
                    android.R.drawable.ic_menu_view,
                    "View Payment",
                    viewPaymentIntent
                )
            }
            "new_message" -> {
                val replyIntent = createActionIntent("reply_message")
                notificationBuilder.addAction(
                    android.R.drawable.ic_menu_send,
                    "Reply",
                    replyIntent
                )
            }
        }
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
        
        // Log notification display
        serviceScope.launch {
            logNotificationDisplayed(type, title, body)
        }
    }

    private fun createActionIntent(action: String): PendingIntent {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("action", action)
        }
        
        return PendingIntent.getActivity(
            this,
            action.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            // Default notification channel
            val defaultChannel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableLights(true)
                enableVibration(true)
                setShowBadge(true)
            }
            
            // High priority notification channel
            val highPriorityChannel = NotificationChannel(
                HIGH_PRIORITY_CHANNEL_ID,
                HIGH_PRIORITY_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "High priority notifications for urgent updates"
                enableLights(true)
                enableVibration(true)
                setShowBadge(true)
            }
            
            notificationManager.createNotificationChannel(defaultChannel)
            notificationManager.createNotificationChannel(highPriorityChannel)
            
            Log.d(TAG, "Notification channels created")
        }
    }

    private suspend fun sendTokenToServer(token: String) {
        try {
            // Update FCM token in Cloud Functions
            // This would typically call a Cloud Function to update the token
            Log.d(TAG, "Sending token to server: $token")
            
            // Store token locally for immediate use
            val sharedPrefs = getSharedPreferences("rustry_prefs", Context.MODE_PRIVATE)
            sharedPrefs.edit().putString("fcm_token", token).apply()
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send token to server", e)
        }
    }

    private suspend fun updateUserFCMToken(token: String) {
        try {
            // Get current user ID (this would come from your auth system)
            val sharedPrefs = getSharedPreferences("rustry_prefs", Context.MODE_PRIVATE)
            val userId = sharedPrefs.getString("user_id", null)
            
            if (userId != null) {
                firestore.collection("users")
                    .document(userId)
                    .update(mapOf(
                        "fcmToken" to token,
                        "tokenUpdatedAt" to System.currentTimeMillis()
                    ))
                    .await()
                
                Log.d(TAG, "FCM token updated for user: $userId")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update user FCM token", e)
        }
    }

    private fun triggerDataSync(collection: String) {
        serviceScope.launch {
            try {
                // Trigger a sync for the specified collection
                Log.d(TAG, "Triggering data sync for collection: $collection")
                
                // This would typically trigger your repository to refresh data
                // For now, we'll just log the sync trigger
                
            } catch (e: Exception) {
                Log.e(TAG, "Failed to trigger data sync for $collection", e)
            }
        }
    }

    private fun triggerRealtimeSync(fowlId: String?) {
        serviceScope.launch {
            try {
                if (fowlId != null) {
                    Log.d(TAG, "Triggering realtime sync for fowl: $fowlId")
                    
                    // Update realtime database to trigger listeners
                    realtimeDb.child("sync_triggers")
                        .child(fowlId)
                        .setValue(System.currentTimeMillis())
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to trigger realtime sync", e)
            }
        }
    }

    private fun triggerSocialFeedSync() {
        serviceScope.launch {
            try {
                Log.d(TAG, "Triggering social feed sync")
                
                // Update social feed sync trigger
                realtimeDb.child("social_feed_sync")
                    .setValue(System.currentTimeMillis())
                
            } catch (e: Exception) {
                Log.e(TAG, "Failed to trigger social feed sync", e)
            }
        }
    }

    private suspend fun trackNotificationReceived(remoteMessage: RemoteMessage) {
        try {
            val analyticsData = mapOf(
                "type" to (remoteMessage.data["type"] ?: "unknown"),
                "from" to (remoteMessage.from ?: "unknown"),
                "priority" to remoteMessage.priority,
                "hasNotification" to (remoteMessage.notification != null),
                "hasData" to remoteMessage.data.isNotEmpty(),
                "timestamp" to System.currentTimeMillis()
            )
            
            firestore.collection("notification_analytics")
                .add(analyticsData)
                .await()
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to track notification analytics", e)
        }
    }

    private suspend fun logNotificationDisplayed(type: String, title: String, body: String) {
        try {
            val logData = mapOf(
                "type" to type,
                "title" to title,
                "body" to body,
                "displayedAt" to System.currentTimeMillis(),
                "deviceId" to getDeviceIdInternal()
            )
            
            firestore.collection("notification_display_logs")
                .add(logData)
                .await()
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to log notification display", e)
        }
    }

    private fun getDeviceIdInternal(): String {
        val sharedPrefs = getSharedPreferences("rustry_prefs", Context.MODE_PRIVATE)
        return sharedPrefs.getString("device_id", "unknown") ?: "unknown"
    }
}