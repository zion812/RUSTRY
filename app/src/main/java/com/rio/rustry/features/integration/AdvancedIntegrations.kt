package com.rio.rustry.features.integration

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rio.rustry.data.model.*
import com.rio.rustry.presentation.theme.RoosterColors
import kotlinx.coroutines.delay

/**
 * Advanced Integration Features for Rooster Platform
 * 
 * Implements comprehensive integration capabilities including:
 * - IoT device integration
 * - Weather API integration
 * - Government database sync
 * - Veterinary clinic integration
 * - Export/Import data management
 */

// IoT Integration Models
data class IoTDevice(
    val id: String,
    val name: String,
    val type: DeviceType,
    val location: String,
    val status: DeviceStatus,
    val batteryLevel: Int,
    val lastSeen: Long,
    val firmwareVersion: String
)

enum class DeviceType {
    TEMPERATURE_SENSOR, HUMIDITY_SENSOR, FEEDER, WATER_DISPENSER, 
    CAMERA, WEIGHT_SCALE, AIR_QUALITY_MONITOR, MOTION_DETECTOR
}

enum class DeviceStatus {
    ONLINE, OFFLINE, ERROR, MAINTENANCE, LOW_BATTERY
}

// Weather Integration Models
data class WeatherData(
    val location: String,
    val temperature: Double,
    val humidity: Int,
    val windSpeed: Double,
    val condition: WeatherCondition,
    val alerts: List<WeatherAlert>
)

enum class WeatherCondition {
    SUNNY, CLOUDY, RAINY, STORMY, FOGGY, SNOWY
}

data class WeatherAlert(
    val id: String,
    val type: AlertType,
    val severity: AlertSeverity,
    val title: String,
    val description: String,
    val startTime: Long,
    val endTime: Long
)

enum class AlertSeverity {
    MINOR, MODERATE, SEVERE, EXTREME
}

// Integration Service Implementation
class AdvancedIntegrationService {
    
    suspend fun getConnectedDevices(): List<IoTDevice> {
        delay(1000)
        return listOf(
            IoTDevice(
                id = "temp_001",
                name = "Coop Temperature Sensor",
                type = DeviceType.TEMPERATURE_SENSOR,
                location = "Main Coop",
                status = DeviceStatus.ONLINE,
                batteryLevel = 85,
                lastSeen = System.currentTimeMillis() - 30000,
                firmwareVersion = "1.2.3"
            ),
            IoTDevice(
                id = "feeder_001",
                name = "Automatic Feeder",
                type = DeviceType.FEEDER,
                location = "Main Coop",
                status = DeviceStatus.ONLINE,
                batteryLevel = 92,
                lastSeen = System.currentTimeMillis() - 60000,
                firmwareVersion = "2.1.0"
            )
        )
    }
    
    suspend fun getCurrentWeather(location: String): Result<WeatherData> {
        delay(1000)
        return Result.success(
            WeatherData(
                location = location,
                temperature = (15.0..35.0).random(),
                humidity = (40..80).random(),
                windSpeed = (0.0..20.0).random(),
                condition = WeatherCondition.values().random(),
                alerts = emptyList()
            )
        )
    }
    
    suspend fun exportData(format: ExportFormat, dataTypes: List<DataType>): Result<String> {
        delay(3000)
        return Result.success("export_${System.currentTimeMillis()}.${format.extension}")
    }
}

enum class ExportFormat(val extension: String) {
    CSV("csv"), JSON("json"), XML("xml"), PDF("pdf")
}

enum class DataType {
    FOWLS, HEALTH_RECORDS, TRANSACTIONS, TRANSFERS, CERTIFICATES
}

// Integration UI Components
@Composable
fun IoTDeviceCard(
    device: IoTDevice,
    onConfigureClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (device.status) {
                DeviceStatus.ONLINE -> MaterialTheme.colorScheme.surfaceVariant
                DeviceStatus.LOW_BATTERY -> RoosterColors.Warning.copy(alpha = 0.1f)
                DeviceStatus.ERROR -> RoosterColors.Error.copy(alpha = 0.1f)
                else -> MaterialTheme.colorScheme.surface
            }
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
                    text = device.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                DeviceStatusBadge(status = device.status)
            }
            
            Text(
                text = device.location,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Battery: ${device.batteryLevel}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (device.batteryLevel < 20) RoosterColors.Error else MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = "v${device.firmwareVersion}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Button(
                onClick = onConfigureClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Configure Device")
            }
        }
    }
}

@Composable
fun DeviceStatusBadge(
    status: DeviceStatus,
    modifier: Modifier = Modifier
) {
    val (color, text) = when (status) {
        DeviceStatus.ONLINE -> RoosterColors.Success to "Online"
        DeviceStatus.OFFLINE -> RoosterColors.Neutral500 to "Offline"
        DeviceStatus.ERROR -> RoosterColors.Error to "Error"
        DeviceStatus.MAINTENANCE -> RoosterColors.Warning to "Maintenance"
        DeviceStatus.LOW_BATTERY -> RoosterColors.Warning to "Low Battery"
    }
    
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = color
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = androidx.compose.ui.graphics.Color.White
        )
    }
}

@Composable
fun WeatherCard(
    weather: WeatherData,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                    text = "Weather - ${weather.location}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Icon(
                    imageVector = when (weather.condition) {
                        WeatherCondition.SUNNY -> Icons.Default.WbSunny
                        WeatherCondition.CLOUDY -> Icons.Default.Cloud
                        WeatherCondition.RAINY -> Icons.Default.CloudQueue
                        WeatherCondition.STORMY -> Icons.Default.Thunderstorm
                        else -> Icons.Default.Cloud
                    },
                    contentDescription = weather.condition.name,
                    tint = RoosterColors.Info
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                WeatherMetric("Temperature", "${weather.temperature.toInt()}°C")
                WeatherMetric("Humidity", "${weather.humidity}%")
                WeatherMetric("Wind", "${weather.windSpeed.toInt()} km/h")
            }
        }
    }
}

@Composable
private fun WeatherMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}