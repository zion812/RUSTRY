package com.rio.rustry.features.realtime

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rio.rustry.data.model.*
import com.rio.rustry.presentation.theme.RoosterColors
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

/**
 * Real-Time Features for Rooster Platform
 * 
 * Implements real-time capabilities including:
 * - Live health monitoring
 * - Real-time notifications
 * - Live chat and messaging
 * - Market price updates
 * - Activity feeds
 * - Push notifications
 * - WebSocket connections
 */

// Real-Time Data Models
data class RealTimeUpdate(
    val id: String,
    val type: UpdateType,
    val title: String,
    val message: String,
    val data: Map<String, Any>,
    val timestamp: Long = System.currentTimeMillis(),
    val priority: UpdatePriority = UpdatePriority.NORMAL,
    val userId: String? = null,
    val fowlId: String? = null
)

enum class UpdateType {
    HEALTH_ALERT, MARKET_CHANGE, MESSAGE_RECEIVED, TRANSFER_UPDATE, 
    PAYMENT_STATUS, VACCINATION_REMINDER, WEATHER_ALERT, SYSTEM_NOTIFICATION
}

enum class UpdatePriority {
    LOW, NORMAL, HIGH, CRITICAL
}

data class LiveHealthMetrics(
    val fowlId: String,
    val temperature: Double,
    val heartRate: Int,
    val activityLevel: ActivityLevel,
    val feedingStatus: FeedingStatus,
    val lastUpdated: Long = System.currentTimeMillis()
)

enum class ActivityLevel {
    VERY_LOW, LOW, NORMAL, HIGH, VERY_HIGH
}

enum class FeedingStatus {
    NOT_FED, FEEDING, FED_RECENTLY, OVERFED
}

data class LiveMarketData(
    val breed: String,
    val currentPrice: Double,
    val priceChange: Double,
    val percentageChange: Double,
    val volume: Int,
    val lastUpdated: Long = System.currentTimeMillis()
)

data class ChatMessage(
    val id: String,
    val senderId: String,
    val senderName: String,
    val receiverId: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val messageType: MessageType = MessageType.TEXT,
    val isRead: Boolean = false,
    val attachments: List<MessageAttachment> = emptyList()
)

enum class MessageType {
    TEXT, IMAGE, VOICE, LOCATION, FOWL_SHARE, PAYMENT_REQUEST
}

data class MessageAttachment(
    val id: String,
    val type: AttachmentType,
    val url: String,
    val fileName: String,
    val fileSize: Long
)

enum class AttachmentType {
    IMAGE, VOICE, DOCUMENT, FOWL_DETAILS
}

// Real-Time Service Implementation
class RealTimeService {
    
    private val _updates = MutableSharedFlow<RealTimeUpdate>()
    val updates: SharedFlow<RealTimeUpdate> = _updates.asSharedFlow()
    
    private val _healthMetrics = MutableStateFlow<Map<String, LiveHealthMetrics>>(emptyMap())
    val healthMetrics: StateFlow<Map<String, LiveHealthMetrics>> = _healthMetrics.asStateFlow()
    
    private val _marketData = MutableStateFlow<Map<String, LiveMarketData>>(emptyMap())
    val marketData: StateFlow<Map<String, LiveMarketData>> = _marketData.asStateFlow()
    
    private val _chatMessages = MutableSharedFlow<ChatMessage>()
    val chatMessages: SharedFlow<ChatMessage> = _chatMessages.asSharedFlow()
    
    private val _connectionStatus = MutableStateFlow(ConnectionStatus.DISCONNECTED)
    val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus.asStateFlow()
    
    // Simulate real-time health monitoring
    fun startHealthMonitoring(fowlIds: List<String>) {
        fowlIds.forEach { fowlId ->
            // Simulate continuous health data updates
            kotlinx.coroutines.GlobalScope.launch {
                while (true) {
                    delay((5..15).random() * 1000L) // Random intervals
                    
                    val metrics = LiveHealthMetrics(
                        fowlId = fowlId,
                        temperature = (38.0..42.0).random(),
                        heartRate = (250..400).random(),
                        activityLevel = ActivityLevel.values().random(),
                        feedingStatus = FeedingStatus.values().random()
                    )
                    
                    _healthMetrics.value = _healthMetrics.value + (fowlId to metrics)
                    
                    // Generate alerts for abnormal readings
                    if (metrics.temperature > 41.0 || metrics.temperature < 39.0) {
                        _updates.emit(
                            RealTimeUpdate(
                                id = UUID.randomUUID().toString(),
                                type = UpdateType.HEALTH_ALERT,
                                title = "Temperature Alert",
                                message = "Abnormal temperature detected: ${metrics.temperature}°C",
                                data = mapOf("fowlId" to fowlId, "temperature" to metrics.temperature),
                                priority = UpdatePriority.HIGH,
                                fowlId = fowlId
                            )
                        )
                    }
                }
            }
        }
    }
    
    // Simulate real-time market updates
    fun startMarketMonitoring(breeds: List<String>) {
        breeds.forEach { breed ->
            kotlinx.coroutines.GlobalScope.launch {
                var currentPrice = (1000..2000).random().toDouble()
                
                while (true) {
                    delay((10..30).random() * 1000L) // Random intervals
                    
                    val priceChange = (-50..50).random().toDouble()
                    val newPrice = (currentPrice + priceChange).coerceAtLeast(500.0)
                    val percentageChange = ((newPrice - currentPrice) / currentPrice) * 100
                    
                    val marketData = LiveMarketData(
                        breed = breed,
                        currentPrice = newPrice,
                        priceChange = priceChange,
                        percentageChange = percentageChange,
                        volume = (50..200).random()
                    )
                    
                    _marketData.value = _marketData.value + (breed to marketData)
                    
                    // Generate alerts for significant price changes
                    if (kotlin.math.abs(percentageChange) > 5.0) {
                        _updates.emit(
                            RealTimeUpdate(
                                id = UUID.randomUUID().toString(),
                                type = UpdateType.MARKET_CHANGE,
                                title = "Price Alert",
                                message = "$breed price ${if (priceChange > 0) "increased" else "decreased"} by ${String.format("%.1f", kotlin.math.abs(percentageChange))}%",
                                data = mapOf("breed" to breed, "priceChange" to percentageChange),
                                priority = if (kotlin.math.abs(percentageChange) > 10.0) UpdatePriority.HIGH else UpdatePriority.NORMAL
                            )
                        )
                    }
                    
                    currentPrice = newPrice
                }
            }
        }
    }
    
    // Simulate incoming chat messages
    fun simulateIncomingMessages() {
        kotlinx.coroutines.GlobalScope.launch {
            while (true) {
                delay((30..120).random() * 1000L) // Random intervals
                
                val message = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    senderId = "user_${(1..10).random()}",
                    senderName = "User ${(1..10).random()}",
                    receiverId = "current_user",
                    content = generateRandomMessage()
                )
                
                _chatMessages.emit(message)
                
                _updates.emit(
                    RealTimeUpdate(
                        id = UUID.randomUUID().toString(),
                        type = UpdateType.MESSAGE_RECEIVED,
                        title = "New Message",
                        message = "Message from ${message.senderName}",
                        data = mapOf("messageId" to message.id, "senderId" to message.senderId),
                        priority = UpdatePriority.NORMAL
                    )
                )
            }
        }
    }
    
    fun connect() {
        _connectionStatus.value = ConnectionStatus.CONNECTING
        kotlinx.coroutines.GlobalScope.launch {
            delay(2000) // Simulate connection time
            _connectionStatus.value = ConnectionStatus.CONNECTED
            
            _updates.emit(
                RealTimeUpdate(
                    id = UUID.randomUUID().toString(),
                    type = UpdateType.SYSTEM_NOTIFICATION,
                    title = "Connected",
                    message = "Real-time features are now active",
                    data = emptyMap(),
                    priority = UpdatePriority.LOW
                )
            )
        }
    }
    
    fun disconnect() {
        _connectionStatus.value = ConnectionStatus.DISCONNECTED
    }
    
    private fun generateRandomMessage(): String {
        val messages = listOf(
            "Hi! I'm interested in your Rhode Island Red fowl.",
            "Is the vaccination up to date?",
            "What's the best price you can offer?",
            "Can we schedule a visit to see the fowl?",
            "Do you have health certificates available?",
            "I'd like to make an offer.",
            "Is the fowl still available?",
            "Can you share more photos?"
        )
        return messages.random()
    }
}

enum class ConnectionStatus {
    DISCONNECTED, CONNECTING, CONNECTED, ERROR
}

// Real-Time UI Components
@Composable
fun RealTimeStatusIndicator(
    connectionStatus: ConnectionStatus,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(
                    when (connectionStatus) {
                        ConnectionStatus.CONNECTED -> RoosterColors.Success
                        ConnectionStatus.CONNECTING -> RoosterColors.Warning
                        ConnectionStatus.DISCONNECTED -> RoosterColors.Neutral500
                        ConnectionStatus.ERROR -> RoosterColors.Error
                    }
                )
        )
        
        Text(
            text = when (connectionStatus) {
                ConnectionStatus.CONNECTED -> "Live"
                ConnectionStatus.CONNECTING -> "Connecting..."
                ConnectionStatus.DISCONNECTED -> "Offline"
                ConnectionStatus.ERROR -> "Error"
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun LiveHealthMetricsCard(
    metrics: LiveHealthMetrics,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Live Health Metrics",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                RealTimeStatusIndicator(connectionStatus = ConnectionStatus.CONNECTED)
            }
            
            // Temperature
            MetricRow(
                icon = Icons.Default.Thermostat,
                label = "Temperature",
                value = "${String.format("%.1f", metrics.temperature)}°C",
                isNormal = metrics.temperature in 39.0..41.0,
                iconColor = if (metrics.temperature in 39.0..41.0) RoosterColors.Success else RoosterColors.Warning
            )
            
            // Heart Rate
            MetricRow(
                icon = Icons.Default.Favorite,
                label = "Heart Rate",
                value = "${metrics.heartRate} BPM",
                isNormal = metrics.heartRate in 250..350,
                iconColor = if (metrics.heartRate in 250..350) RoosterColors.Success else RoosterColors.Warning
            )
            
            // Activity Level
            MetricRow(
                icon = Icons.Default.DirectionsRun,
                label = "Activity",
                value = metrics.activityLevel.name.replace("_", " "),
                isNormal = metrics.activityLevel == ActivityLevel.NORMAL,
                iconColor = when (metrics.activityLevel) {
                    ActivityLevel.NORMAL -> RoosterColors.Success
                    ActivityLevel.HIGH, ActivityLevel.LOW -> RoosterColors.Warning
                    else -> RoosterColors.Error
                }
            )
            
            // Feeding Status
            MetricRow(
                icon = Icons.Default.Restaurant,
                label = "Feeding",
                value = metrics.feedingStatus.name.replace("_", " "),
                isNormal = metrics.feedingStatus == FeedingStatus.FED_RECENTLY,
                iconColor = when (metrics.feedingStatus) {
                    FeedingStatus.FED_RECENTLY -> RoosterColors.Success
                    FeedingStatus.FEEDING -> RoosterColors.Info
                    FeedingStatus.NOT_FED -> RoosterColors.Warning
                    FeedingStatus.OVERFED -> RoosterColors.Error
                }
            )
            
            Text(
                text = "Last updated: ${SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(metrics.lastUpdated))}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MetricRow(
    icon: ImageVector,
    label: String,
    value: String,
    isNormal: Boolean,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(20.dp),
                tint = iconColor
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = if (isNormal) MaterialTheme.colorScheme.onSurface else iconColor
        )
    }
}

@Composable
fun LiveMarketDataCard(
    marketData: LiveMarketData,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (marketData.priceChange >= 0) 
                RoosterColors.Success.copy(alpha = 0.1f) 
            else 
                RoosterColors.Error.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = marketData.breed,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                RealTimeStatusIndicator(connectionStatus = ConnectionStatus.CONNECTED)
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "₹${marketData.currentPrice.toInt()}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = if (marketData.priceChange >= 0) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                        contentDescription = "Price trend",
                        modifier = Modifier.size(16.dp),
                        tint = if (marketData.priceChange >= 0) RoosterColors.Success else RoosterColors.Error
                    )
                    Text(
                        text = "${if (marketData.priceChange >= 0) "+" else ""}${String.format("%.1f", marketData.percentageChange)}%",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = if (marketData.priceChange >= 0) RoosterColors.Success else RoosterColors.Error
                    )
                }
            }
            
            Text(
                text = "Volume: ${marketData.volume} trades today",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun RealTimeNotificationsList(
    updates: List<RealTimeUpdate>,
    onUpdateClick: (RealTimeUpdate) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(updates) { update ->
            RealTimeNotificationCard(
                update = update,
                onClick = { onUpdateClick(update) }
            )
        }
    }
}

@Composable
fun RealTimeNotificationCard(
    update: RealTimeUpdate,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        colors = CardDefaults.cardColors(
            containerColor = when (update.priority) {
                UpdatePriority.CRITICAL -> RoosterColors.Error.copy(alpha = 0.1f)
                UpdatePriority.HIGH -> RoosterColors.Warning.copy(alpha = 0.1f)
                UpdatePriority.NORMAL -> MaterialTheme.colorScheme.surfaceVariant
                UpdatePriority.LOW -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = when (update.type) {
                    UpdateType.HEALTH_ALERT -> Icons.Default.HealthAndSafety
                    UpdateType.MARKET_CHANGE -> Icons.Default.TrendingUp
                    UpdateType.MESSAGE_RECEIVED -> Icons.Default.Message
                    UpdateType.TRANSFER_UPDATE -> Icons.Default.SwapHoriz
                    UpdateType.PAYMENT_STATUS -> Icons.Default.Payment
                    UpdateType.VACCINATION_REMINDER -> Icons.Default.Schedule
                    UpdateType.WEATHER_ALERT -> Icons.Default.Cloud
                    UpdateType.SYSTEM_NOTIFICATION -> Icons.Default.Info
                },
                contentDescription = update.type.name,
                modifier = Modifier.size(24.dp),
                tint = when (update.priority) {
                    UpdatePriority.CRITICAL -> RoosterColors.Error
                    UpdatePriority.HIGH -> RoosterColors.Warning
                    UpdatePriority.NORMAL -> RoosterColors.Info
                    UpdatePriority.LOW -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = update.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                
                Text(
                    text = update.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(update.timestamp)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}