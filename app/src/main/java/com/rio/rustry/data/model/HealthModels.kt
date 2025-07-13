package com.rio.rustry.data.model

import java.util.Date

/**
 * Health-related data models for the Rooster Platform
 */

data class HealthRecord(
    val id: String = "",
    val fowlId: String = "",
    val type: HealthEventType = HealthEventType.CHECKUP,
    val eventType: HealthEventType = HealthEventType.CHECKUP,
    val title: String = "",
    val severity: HealthSeverity = HealthSeverity.LOW,
    val description: String = "",
    val symptoms: List<String> = emptyList(),
    val treatment: String = "",
    val medications: List<String> = emptyList(),
    val medication: String = "",
    val dosage: String = "",
    val veterinarianName: String = "",
    val vetName: String = "",
    val vetLicense: String = "",
    val veterinarianContact: String = "",
    val date: Date = Date(),
    val followUpRequired: Boolean = false,
    val followUpDate: Date? = null,
    val nextDueDate: Long? = null,
    val attachments: List<String> = emptyList(), // URLs to images/documents
    val proofImageUrls: List<String> = emptyList(),
    val certificateUrls: List<String> = emptyList(),
    val cost: Double = 0.0,
    val temperature: Double? = null,
    val weight: Double? = null,
    val notes: String = "",
    val createdBy: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class HealthSummary(
    val fowlId: String = "",
    val overallHealth: HealthStatus = HealthStatus.GOOD,
    val lastCheckupDate: Date? = null,
    val totalRecords: Int = 0,
    val activeIssues: Int = 0,
    val upcomingReminders: Int = 0,
    val vaccinationStatus: VaccinationStatus = VaccinationStatus.UP_TO_DATE,
    val lastVaccinationDate: Date? = null,
    val nextVaccinationDue: Date? = null,
    val commonIssues: List<HealthEventType> = emptyList(),
    val healthScore: Int = 100, // 0-100 scale
    val riskFactors: List<String> = emptyList(),
    val recommendations: List<String> = emptyList()
)

data class HealthReminder(
    val id: String = "",
    val fowlId: String = "",
    val title: String = "",
    val description: String = "",
    val type: ReminderType = ReminderType.VACCINATION,
    val dueDate: Date = Date(),
    val isCompleted: Boolean = false,
    val priority: HealthSeverity = HealthSeverity.MEDIUM,
    val createdAt: Long = System.currentTimeMillis()
)

data class HealthAlert(
    val id: String = "",
    val fowlId: String = "",
    val alertType: AlertType = AlertType.HEALTH_ISSUE,
    val title: String = "",
    val message: String = "",
    val severity: HealthSeverity = HealthSeverity.MEDIUM,
    val isRead: Boolean = false,
    val actionRequired: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

data class AIHealthTip(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val description: String = "",
    val category: TipCategory = TipCategory.GENERAL,
    val priority: TipPriority = TipPriority.MEDIUM,
    val urgencyLevel: Int = 0, // 0-10 scale
    val dueDate: Long? = null,
    val symptoms: List<String> = emptyList(),
    val prevention: List<String> = emptyList(),
    val fowlAge: Int = 0, // Age in weeks
    val ageRange: String = "",
    val frequency: String = "",
    val estimatedCost: Double = 0.0,
    val actionRequired: Boolean = false,
    val vetConsultationRequired: Boolean = false,
    val applicableBreeds: List<String> = emptyList(),
    val seasonalRelevance: List<String> = emptyList(), // Spring, Summer, Fall, Winter
    val tags: List<String> = emptyList(),
    val imageUrl: String = "",
    val sourceUrl: String = "",
    val confidence: Double = 0.0, // AI confidence score 0-1
    val createdAt: Long = System.currentTimeMillis(),
    val isBookmarked: Boolean = false
)

data class CertificateHealthSummary(
    val overallStatus: HealthStatus = HealthStatus.GOOD,
    val lastCheckupDate: Long? = null,
    val lastVaccinationDate: Long? = null,
    val vaccinationStatus: VaccinationStatus = VaccinationStatus.UP_TO_DATE,
    val vaccinationCount: Int = 0,
    val activeIssues: List<String> = emptyList(),
    val healthScore: Int = 100,
    val healthRecordsCount: Int = 0,
    val vetCertificates: Int = 0,
    val currentMedications: List<String> = emptyList(),
    val veterinarianCertification: String = "",
    val certificationDate: Long = System.currentTimeMillis()
)

// Enums
enum class HealthEventType(val displayName: String) {
    CHECKUP("Regular Checkup"),
    VACCINATION("Vaccination"),
    ILLNESS("Illness"),
    INJURY("Injury"),
    MEDICATION("Medication"),
    TREATMENT("Treatment"),
    SURGERY("Surgery"),
    EMERGENCY("Emergency"),
    PREVENTIVE("Preventive Care"),
    BREEDING("Breeding Related"),
    NUTRITION("Nutrition Issue"),
    BEHAVIORAL("Behavioral Issue"),
    OTHER("Other")
}

enum class HealthSeverity(val displayName: String, val color: String) {
    LOW("Low", "#4CAF50"),
    MEDIUM("Medium", "#FF9800"),
    HIGH("High", "#F44336"),
    CRITICAL("Critical", "#D32F2F")
}

enum class HealthStatus(val displayName: String) {
    EXCELLENT("Excellent"),
    GOOD("Good"),
    FAIR("Fair"),
    POOR("Poor"),
    CRITICAL("Critical")
}

enum class VaccinationStatus(val displayName: String) {
    UP_TO_DATE("Up to Date"),
    DUE_SOON("Due Soon"),
    OVERDUE("Overdue"),
    NOT_STARTED("Not Started"),
    INCOMPLETE("Incomplete")
}

enum class ReminderType(val displayName: String) {
    VACCINATION("Vaccination"),
    CHECKUP("Health Checkup"),
    MEDICATION("Medication"),
    FOLLOW_UP("Follow-up"),
    BREEDING("Breeding"),
    NUTRITION("Nutrition"),
    OTHER("Other")
}

enum class AlertType(val displayName: String) {
    HEALTH_ISSUE("Health Issue"),
    VACCINATION_DUE("Vaccination Due"),
    MEDICATION_REMINDER("Medication Reminder"),
    EMERGENCY("Emergency"),
    FOLLOW_UP("Follow-up Required"),
    SYSTEM("System Alert")
}

enum class TipCategory(val displayName: String) {
    GENERAL("General Care"),
    GENERAL_CARE("General Care"),
    NUTRITION("Nutrition"),
    BREEDING("Breeding"),
    DISEASE_PREVENTION("Disease Prevention"),
    HOUSING("Housing & Environment"),
    BEHAVIOR("Behavior"),
    EMERGENCY("Emergency Care"),
    SEASONAL("Seasonal Care"),
    SEASONAL_CARE("Seasonal Care"),
    VACCINATION("Vaccination"),
    MEDICATION("Medication"),
    HYGIENE("Hygiene")
}

enum class TipPriority(val displayName: String, val color: String) {
    LOW("Low Priority", "#4CAF50"),
    MEDIUM("Medium Priority", "#FF9800"),
    HIGH("High Priority", "#F44336"),
    URGENT("Urgent", "#9C27B0")
}