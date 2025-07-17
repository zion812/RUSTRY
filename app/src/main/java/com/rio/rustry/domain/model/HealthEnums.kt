package com.rio.rustry.domain.model

/**
 * Health-related enums and data classes for the health management system
 */

enum class HealthEventType {
    VACCINATION,
    CHECKUP,
    TREATMENT,
    MEDICATION,
    SURGERY,
    INJURY,
    ILLNESS,
    RECOVERY,
    DEATH,
    QUARANTINE,
    HEALTH_CERTIFICATE,
    BREEDING_CHECK,
    WEIGHT_CHECK,
    BLOOD_TEST,
    OTHER
}

enum class HealthSeverity {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

enum class TipCategory {
    NUTRITION,
    VACCINATION,
    BREEDING,
    HOUSING,
    DISEASE_PREVENTION,
    GENERAL_CARE,
    EMERGENCY,
    SEASONAL,
    BREED_SPECIFIC
}

enum class TipPriority {
    LOW,
    MEDIUM,
    HIGH,
    URGENT
}

enum class ReminderType {
    VACCINATION,
    MEDICATION,
    CHECKUP,
    FEEDING,
    CLEANING,
    BREEDING,
    TREATMENT,
    FOLLOW_UP
}

/**
 * AI Health Tip data class
 */
data class AIHealthTip(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val category: TipCategory = TipCategory.GENERAL_CARE,
    val priority: TipPriority = TipPriority.MEDIUM,
    val fowlId: String? = null,
    val breedSpecific: String? = null,
    val ageRange: String? = null,
    val seasonalRelevance: String? = null,
    val actionRequired: Boolean = false,
    val estimatedCost: Double? = null,
    val timeToImplement: String? = null,
    val relatedSymptoms: List<String> = emptyList(),
    val preventiveMeasures: List<String> = emptyList(),
    val references: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long? = null,
    val isRead: Boolean = false,
    val isImplemented: Boolean = false
)

/**
 * Health Reminder data class
 */
data class HealthReminder(
    val id: String = "",
    val fowlId: String = "",
    val title: String = "",
    val description: String = "",
    val type: ReminderType = ReminderType.CHECKUP,
    val scheduledDate: Long = 0L,
    val severity: HealthSeverity = HealthSeverity.MEDIUM,
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isRecurring: Boolean = false,
    val recurringInterval: String? = null, // "weekly", "monthly", "yearly"
    val nextReminderDate: Long? = null,
    val veterinarianId: String? = null,
    val estimatedCost: Double? = null,
    val isUrgent: Boolean = false
)

