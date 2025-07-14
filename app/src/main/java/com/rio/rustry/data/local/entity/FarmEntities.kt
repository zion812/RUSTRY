package com.rio.rustry.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

/**
 * Enhanced Farm Entity with comprehensive offline support
 */
@Entity(tableName = "farms")
@TypeConverters(Converters::class)
data class FarmEntity(
    @PrimaryKey
    val id: String,
    
    @ColumnInfo(name = "owner_id")
    val ownerId: String,
    
    @ColumnInfo(name = "name")
    val name: String,
    
    @ColumnInfo(name = "location")
    val location: String,
    
    @ColumnInfo(name = "size")
    val size: Double,
    
    @ColumnInfo(name = "photo_url")
    val photoUrl: String = "",
    
    @ColumnInfo(name = "ownership_details")
    val ownershipDetails: String = "",
    
    @ColumnInfo(name = "latitude")
    val latitude: Double? = null,
    
    @ColumnInfo(name = "longitude")
    val longitude: Double? = null,
    
    @ColumnInfo(name = "farm_type")
    val farmType: String = "POULTRY", // POULTRY, MIXED, BREEDING
    
    @ColumnInfo(name = "established_date")
    val establishedDate: Long? = null,
    
    @ColumnInfo(name = "license_number")
    val licenseNumber: String = "",
    
    @ColumnInfo(name = "contact_person")
    val contactPerson: String = "",
    
    @ColumnInfo(name = "contact_phone")
    val contactPhone: String = "",
    
    @ColumnInfo(name = "contact_email")
    val contactEmail: String = "",
    
    @ColumnInfo(name = "facilities")
    val facilities: List<String> = emptyList(),
    
    @ColumnInfo(name = "certifications")
    val certifications: List<String> = emptyList(),
    
    @ColumnInfo(name = "is_organic")
    val isOrganic: Boolean = false,
    
    @ColumnInfo(name = "is_verified")
    val isVerified: Boolean = false,
    
    @ColumnInfo(name = "verification_documents")
    val verificationDocuments: List<String> = emptyList(),
    
    @ColumnInfo(name = "settings")
    val settings: Map<String, String> = emptyMap(),
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,
    
    @ColumnInfo(name = "is_deleted")
    val isDeleted: Boolean = false,
    
    @ColumnInfo(name = "needs_sync")
    val needsSync: Boolean = true,
    
    @ColumnInfo(name = "last_synced_at")
    val lastSyncedAt: Long? = null,
    
    @ColumnInfo(name = "offline_changes")
    val offlineChanges: Map<String, String> = emptyMap()
)

/**
 * Enhanced Flock Entity with detailed tracking
 */
@Entity(tableName = "flocks")
@TypeConverters(Converters::class)
data class FlockEntity(
    @PrimaryKey
    val id: String,
    
    @ColumnInfo(name = "farm_id")
    val farmId: String,
    
    @ColumnInfo(name = "breed")
    val breed: String,
    
    @ColumnInfo(name = "age_months")
    val ageMonths: Int = 0,
    
    @ColumnInfo(name = "quantity")
    val quantity: Int,
    
    @ColumnInfo(name = "male_count")
    val maleCount: Int = 0,
    
    @ColumnInfo(name = "female_count")
    val femaleCount: Int = 0,
    
    @ColumnInfo(name = "notes")
    val notes: String = "",
    
    @ColumnInfo(name = "photo_urls")
    val photoUrls: List<String> = emptyList(),
    
    @ColumnInfo(name = "acquisition_date")
    val acquisitionDate: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "acquisition_source")
    val acquisitionSource: String = "", // PURCHASED, BRED, GIFTED
    
    @ColumnInfo(name = "acquisition_cost")
    val acquisitionCost: Double = 0.0,
    
    @ColumnInfo(name = "current_weight_kg")
    val currentWeightKg: Double = 0.0,
    
    @ColumnInfo(name = "health_status")
    val healthStatus: String = "HEALTHY", // HEALTHY, SICK, QUARANTINE, DECEASED
    
    @ColumnInfo(name = "vaccination_status")
    val vaccinationStatus: String = "UP_TO_DATE", // UP_TO_DATE, OVERDUE, PARTIAL
    
    @ColumnInfo(name = "breeding_status")
    val breedingStatus: String = "NONE", // NONE, BREEDING, BROODING, LAYING
    
    @ColumnInfo(name = "production_type")
    val productionType: String = "MEAT", // MEAT, EGGS, BREEDING, SHOW
    
    @ColumnInfo(name = "housing_type")
    val housingType: String = "COOP", // COOP, FREE_RANGE, CAGE, BARN
    
    @ColumnInfo(name = "feed_type")
    val feedType: String = "COMMERCIAL", // COMMERCIAL, ORGANIC, MIXED, HOMEMADE
    
    @ColumnInfo(name = "tags")
    val tags: List<String> = emptyList(),
    
    @ColumnInfo(name = "parent_flock_id")
    val parentFlockId: String? = null,
    
    @ColumnInfo(name = "is_for_sale")
    val isForSale: Boolean = false,
    
    @ColumnInfo(name = "sale_price")
    val salePrice: Double = 0.0,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,
    
    @ColumnInfo(name = "is_deleted")
    val isDeleted: Boolean = false,
    
    @ColumnInfo(name = "needs_sync")
    val needsSync: Boolean = true,
    
    @ColumnInfo(name = "last_synced_at")
    val lastSyncedAt: Long? = null,
    
    @ColumnInfo(name = "offline_changes")
    val offlineChanges: Map<String, String> = emptyMap()
)

/**
 * Enhanced Health Record Entity with comprehensive tracking
 */
@Entity(tableName = "health_records")
@TypeConverters(Converters::class)
data class HealthRecordEntity(
    @PrimaryKey
    val id: String,
    
    @ColumnInfo(name = "flock_id")
    val flockId: String,
    
    @ColumnInfo(name = "farm_id")
    val farmId: String,
    
    @ColumnInfo(name = "type")
    val type: String, // VACCINATION, TREATMENT, CHECKUP, MEDICATION, SURGERY
    
    @ColumnInfo(name = "date")
    val date: Long,
    
    @ColumnInfo(name = "details")
    val details: String = "",
    
    @ColumnInfo(name = "photo_urls")
    val photoUrls: List<String> = emptyList(),
    
    @ColumnInfo(name = "veterinarian_id")
    val veterinarianId: String = "",
    
    @ColumnInfo(name = "veterinarian_name")
    val veterinarianName: String = "",
    
    @ColumnInfo(name = "veterinarian_contact")
    val veterinarianContact: String = "",
    
    @ColumnInfo(name = "medication_name")
    val medicationName: String = "",
    
    @ColumnInfo(name = "medication_dosage")
    val medicationDosage: String = "",
    
    @ColumnInfo(name = "medication_duration")
    val medicationDuration: String = "",
    
    @ColumnInfo(name = "symptoms")
    val symptoms: List<String> = emptyList(),
    
    @ColumnInfo(name = "diagnosis")
    val diagnosis: String = "",
    
    @ColumnInfo(name = "treatment_plan")
    val treatmentPlan: String = "",
    
    @ColumnInfo(name = "cost")
    val cost: Double = 0.0,
    
    @ColumnInfo(name = "next_due_date")
    val nextDueDate: Long? = null,
    
    @ColumnInfo(name = "follow_up_required")
    val followUpRequired: Boolean = false,
    
    @ColumnInfo(name = "follow_up_date")
    val followUpDate: Long? = null,
    
    @ColumnInfo(name = "outcome")
    val outcome: String = "", // SUCCESSFUL, PARTIAL, FAILED, ONGOING
    
    @ColumnInfo(name = "side_effects")
    val sideEffects: List<String> = emptyList(),
    
    @ColumnInfo(name = "notes")
    val notes: String = "",
    
    @ColumnInfo(name = "reminder_sent")
    val reminderSent: Boolean = false,
    
    @ColumnInfo(name = "is_emergency")
    val isEmergency: Boolean = false,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,
    
    @ColumnInfo(name = "is_deleted")
    val isDeleted: Boolean = false,
    
    @ColumnInfo(name = "needs_sync")
    val needsSync: Boolean = true,
    
    @ColumnInfo(name = "last_synced_at")
    val lastSyncedAt: Long? = null,
    
    @ColumnInfo(name = "offline_changes")
    val offlineChanges: Map<String, String> = emptyMap()
)

/**
 * Enhanced Sale Entity with detailed transaction tracking
 */
@Entity(tableName = "sales")
@TypeConverters(Converters::class)
data class SaleEntity(
    @PrimaryKey
    val id: String,
    
    @ColumnInfo(name = "farm_id")
    val farmId: String,
    
    @ColumnInfo(name = "flock_id")
    val flockId: String? = null,
    
    @ColumnInfo(name = "buyer_id")
    val buyerId: String = "",
    
    @ColumnInfo(name = "buyer_name")
    val buyerName: String,
    
    @ColumnInfo(name = "buyer_contact")
    val buyerContact: String = "",
    
    @ColumnInfo(name = "buyer_address")
    val buyerAddress: String = "",
    
    @ColumnInfo(name = "sale_date")
    val saleDate: Long,
    
    @ColumnInfo(name = "delivery_date")
    val deliveryDate: Long? = null,
    
    @ColumnInfo(name = "amount")
    val amount: Double,
    
    @ColumnInfo(name = "items_sold")
    val itemsSold: String,
    
    @ColumnInfo(name = "quantity")
    val quantity: Int,
    
    @ColumnInfo(name = "unit_price")
    val unitPrice: Double = 0.0,
    
    @ColumnInfo(name = "weight_kg")
    val weightKg: Double = 0.0,
    
    @ColumnInfo(name = "payment_method")
    val paymentMethod: String, // CASH, UPI, BANK_TRANSFER, CHEQUE, CARD
    
    @ColumnInfo(name = "payment_status")
    val paymentStatus: String = "PENDING", // PENDING, COMPLETED, PARTIAL, FAILED, REFUNDED
    
    @ColumnInfo(name = "payment_reference")
    val paymentReference: String = "",
    
    @ColumnInfo(name = "delivery_status")
    val deliveryStatus: String = "PENDING", // PENDING, SHIPPED, DELIVERED, CANCELLED
    
    @ColumnInfo(name = "delivery_method")
    val deliveryMethod: String = "PICKUP", // PICKUP, DELIVERY, COURIER
    
    @ColumnInfo(name = "delivery_cost")
    val deliveryCost: Double = 0.0,
    
    @ColumnInfo(name = "discount_amount")
    val discountAmount: Double = 0.0,
    
    @ColumnInfo(name = "tax_amount")
    val taxAmount: Double = 0.0,
    
    @ColumnInfo(name = "total_amount")
    val totalAmount: Double = 0.0,
    
    @ColumnInfo(name = "invoice_number")
    val invoiceNumber: String = "",
    
    @ColumnInfo(name = "receipt_url")
    val receiptUrl: String = "",
    
    @ColumnInfo(name = "notes")
    val notes: String = "",
    
    @ColumnInfo(name = "rating")
    val rating: Int = 0, // 1-5 stars
    
    @ColumnInfo(name = "feedback")
    val feedback: String = "",
    
    @ColumnInfo(name = "is_repeat_customer")
    val isRepeatCustomer: Boolean = false,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,
    
    @ColumnInfo(name = "is_deleted")
    val isDeleted: Boolean = false,
    
    @ColumnInfo(name = "needs_sync")
    val needsSync: Boolean = true,
    
    @ColumnInfo(name = "last_synced_at")
    val lastSyncedAt: Long? = null,
    
    @ColumnInfo(name = "offline_changes")
    val offlineChanges: Map<String, String> = emptyMap()
)

/**
 * Enhanced Inventory Entity with comprehensive stock management
 */
@Entity(tableName = "inventory")
@TypeConverters(Converters::class)
data class InventoryEntity(
    @PrimaryKey
    val id: String,
    
    @ColumnInfo(name = "farm_id")
    val farmId: String,
    
    @ColumnInfo(name = "type")
    val type: String, // FEED, MEDICINE, EQUIPMENT, SUPPLIES, BEDDING, SUPPLEMENTS
    
    @ColumnInfo(name = "name")
    val name: String,
    
    @ColumnInfo(name = "brand")
    val brand: String = "",
    
    @ColumnInfo(name = "category")
    val category: String = "",
    
    @ColumnInfo(name = "quantity")
    val quantity: Int,
    
    @ColumnInfo(name = "unit")
    val unit: String, // KG, LITERS, PIECES, BAGS, BOTTLES
    
    @ColumnInfo(name = "restock_threshold")
    val restockThreshold: Int,
    
    @ColumnInfo(name = "max_stock_level")
    val maxStockLevel: Int = 0,
    
    @ColumnInfo(name = "supplier_name")
    val supplierName: String = "",
    
    @ColumnInfo(name = "supplier_contact")
    val supplierContact: String = "",
    
    @ColumnInfo(name = "supplier_address")
    val supplierAddress: String = "",
    
    @ColumnInfo(name = "purchase_date")
    val purchaseDate: Long? = null,
    
    @ColumnInfo(name = "last_restock_date")
    val lastRestockDate: Long? = null,
    
    @ColumnInfo(name = "expiry_date")
    val expiryDate: Long? = null,
    
    @ColumnInfo(name = "batch_number")
    val batchNumber: String = "",
    
    @ColumnInfo(name = "cost_per_unit")
    val costPerUnit: Double = 0.0,
    
    @ColumnInfo(name = "total_cost")
    val totalCost: Double = 0.0,
    
    @ColumnInfo(name = "selling_price")
    val sellingPrice: Double = 0.0,
    
    @ColumnInfo(name = "storage_location")
    val storageLocation: String = "",
    
    @ColumnInfo(name = "storage_conditions")
    val storageConditions: String = "",
    
    @ColumnInfo(name = "usage_instructions")
    val usageInstructions: String = "",
    
    @ColumnInfo(name = "safety_notes")
    val safetyNotes: String = "",
    
    @ColumnInfo(name = "is_prescription_required")
    val isPrescriptionRequired: Boolean = false,
    
    @ColumnInfo(name = "prescription_details")
    val prescriptionDetails: String = "",
    
    @ColumnInfo(name = "notes")
    val notes: String = "",
    
    @ColumnInfo(name = "photo_urls")
    val photoUrls: List<String> = emptyList(),
    
    @ColumnInfo(name = "barcode")
    val barcode: String = "",
    
    @ColumnInfo(name = "qr_code")
    val qrCode: String = "",
    
    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true,
    
    @ColumnInfo(name = "alert_sent")
    val alertSent: Boolean = false,
    
    @ColumnInfo(name = "last_used_date")
    val lastUsedDate: Long? = null,
    
    @ColumnInfo(name = "usage_frequency")
    val usageFrequency: String = "", // DAILY, WEEKLY, MONTHLY, AS_NEEDED
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,
    
    @ColumnInfo(name = "is_deleted")
    val isDeleted: Boolean = false,
    
    @ColumnInfo(name = "needs_sync")
    val needsSync: Boolean = true,
    
    @ColumnInfo(name = "last_synced_at")
    val lastSyncedAt: Long? = null,
    
    @ColumnInfo(name = "offline_changes")
    val offlineChanges: Map<String, String> = emptyMap()
)

/**
 * Change Log Entity for audit trail
 */
@Entity(tableName = "change_logs")
@TypeConverters(Converters::class)
data class ChangeLogEntity(
    @PrimaryKey
    val id: String,
    
    @ColumnInfo(name = "farm_id")
    val farmId: String,
    
    @ColumnInfo(name = "user_id")
    val userId: String,
    
    @ColumnInfo(name = "entity_type")
    val entityType: String, // FARM, FLOCK, HEALTH_RECORD, SALE, INVENTORY
    
    @ColumnInfo(name = "entity_id")
    val entityId: String,
    
    @ColumnInfo(name = "action")
    val action: String, // CREATE, UPDATE, DELETE, RESTORE
    
    @ColumnInfo(name = "old_data")
    val oldData: String = "",
    
    @ColumnInfo(name = "new_data")
    val newData: String = "",
    
    @ColumnInfo(name = "changed_fields")
    val changedFields: List<String> = emptyList(),
    
    @ColumnInfo(name = "timestamp")
    val timestamp: Long,
    
    @ColumnInfo(name = "device_id")
    val deviceId: String = "",
    
    @ColumnInfo(name = "app_version")
    val appVersion: String = "",
    
    @ColumnInfo(name = "ip_address")
    val ipAddress: String = "",
    
    @ColumnInfo(name = "user_agent")
    val userAgent: String = "",
    
    @ColumnInfo(name = "session_id")
    val sessionId: String = "",
    
    @ColumnInfo(name = "is_verified")
    val isVerified: Boolean = false,
    
    @ColumnInfo(name = "verified_by")
    val verifiedBy: String = "",
    
    @ColumnInfo(name = "verification_timestamp")
    val verificationTimestamp: Long? = null,
    
    @ColumnInfo(name = "verification_method")
    val verificationMethod: String = "",
    
    @ColumnInfo(name = "notes")
    val notes: String = "",
    
    @ColumnInfo(name = "needs_sync")
    val needsSync: Boolean = true,
    
    @ColumnInfo(name = "last_synced_at")
    val lastSyncedAt: Long? = null
)