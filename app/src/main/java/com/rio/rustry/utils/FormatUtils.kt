package com.rio.rustry.utils

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * Utility functions for formatting data
 */

/**
 * Format currency amount in Indian Rupees
 */
fun formatCurrency(amount: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    return formatter.format(amount)
}

/**
 * Format date in a readable format
 */
fun formatDate(date: Date): String {
    val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return formatter.format(date)
}

/**
 * Format date with time
 */
fun formatDateTime(date: Date): String {
    val formatter = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    return formatter.format(date)
}

/**
 * Format date for display in short format
 */
fun formatDateShort(date: Date): String {
    val formatter = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
    return formatter.format(date)
}

/**
 * Format number with proper separators
 */
fun formatNumber(number: Long): String {
    val formatter = NumberFormat.getNumberInstance(Locale("en", "IN"))
    return formatter.format(number)
}

/**
 * Format decimal number with specified decimal places
 */
fun formatDecimal(number: Double, decimalPlaces: Int = 2): String {
    val formatter = NumberFormat.getNumberInstance(Locale.getDefault())
    formatter.minimumFractionDigits = decimalPlaces
    formatter.maximumFractionDigits = decimalPlaces
    return formatter.format(number)
}

/**
 * Format percentage
 */
fun formatPercentage(value: Double): String {
    val formatter = NumberFormat.getPercentInstance(Locale.getDefault())
    formatter.minimumFractionDigits = 1
    formatter.maximumFractionDigits = 1
    return formatter.format(value / 100)
}

/**
 * Format weight with unit
 */
fun formatWeight(weight: Double, unit: String = "kg"): String {
    return "${formatDecimal(weight)} $unit"
}

/**
 * Format age in appropriate units
 */
fun formatAge(ageInDays: Int): String {
    return when {
        ageInDays < 7 -> "$ageInDays days"
        ageInDays < 30 -> "${ageInDays / 7} weeks"
        ageInDays < 365 -> "${ageInDays / 30} months"
        else -> "${ageInDays / 365} years"
    }
}

/**
 * Format phone number for display
 */
fun formatPhoneNumber(phoneNumber: String): String {
    return when {
        phoneNumber.length == 10 -> {
            "${phoneNumber.substring(0, 5)} ${phoneNumber.substring(5)}"
        }
        phoneNumber.length == 13 && phoneNumber.startsWith("+91") -> {
            "+91 ${phoneNumber.substring(3, 8)} ${phoneNumber.substring(8)}"
        }
        else -> phoneNumber
    }
}

/**
 * Format file size
 */
fun formatFileSize(sizeInBytes: Long): String {
    val units = arrayOf("B", "KB", "MB", "GB")
    var size = sizeInBytes.toDouble()
    var unitIndex = 0
    
    while (size >= 1024 && unitIndex < units.size - 1) {
        size /= 1024
        unitIndex++
    }
    
    return "${formatDecimal(size, 1)} ${units[unitIndex]}"
}

/**
 * Format duration in minutes to hours and minutes
 */
fun formatDuration(minutes: Int): String {
    val hours = minutes / 60
    val remainingMinutes = minutes % 60
    
    return when {
        hours == 0 -> "${remainingMinutes}m"
        remainingMinutes == 0 -> "${hours}h"
        else -> "${hours}h ${remainingMinutes}m"
    }
}

/**
 * Format relative time (e.g., "2 hours ago")
 */
fun formatRelativeTime(date: Date): String {
    val now = Date()
    val diffInMillis = now.time - date.time
    val diffInSeconds = diffInMillis / 1000
    val diffInMinutes = diffInSeconds / 60
    val diffInHours = diffInMinutes / 60
    val diffInDays = diffInHours / 24
    
    return when {
        diffInSeconds < 60 -> "Just now"
        diffInMinutes < 60 -> "${diffInMinutes.toInt()} minutes ago"
        diffInHours < 24 -> "${diffInHours.toInt()} hours ago"
        diffInDays < 7 -> "${diffInDays.toInt()} days ago"
        else -> formatDate(date)
    }
}