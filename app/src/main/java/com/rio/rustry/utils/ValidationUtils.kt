package com.rio.rustry.utils

import android.net.Uri
import java.util.regex.Pattern

/**
 * Validation utilities for Farm Fetcher feature
 * Provides comprehensive validation for all farm-related data
 */
object ValidationUtils {
    
    // Constants for validation
    private const val MIN_FARM_SIZE = 0.1
    private const val MAX_FARM_SIZE = 10000.0
    private const val MIN_FLOCK_QUANTITY = 1
    private const val MAX_FLOCK_QUANTITY = 100000
    private const val MIN_SALE_AMOUNT = 0.01
    private const val MAX_SALE_AMOUNT = 10000000.0
    private const val MIN_INVENTORY_QUANTITY = 0
    private const val MAX_INVENTORY_QUANTITY = 1000000
    
    // Regex patterns
    private val PHONE_PATTERN = Pattern.compile("^[+]?[0-9]{10,15}$")
    private val EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    private val NAME_PATTERN = Pattern.compile("^[a-zA-Z\\s]{2,50}$")
    
    /**
     * Validate farm name
     */
    fun validateFarmName(name: String): String? {
        return when {
            name.isBlank() -> "Farm name is required"
            name.length < 2 -> "Farm name must be at least 2 characters"
            name.length > 100 -> "Farm name must be less than 100 characters"
            !name.matches(Regex("^[a-zA-Z0-9\\s\\-_'.]+$")) -> "Farm name contains invalid characters"
            else -> null
        }
    }
    
    /**
     * Validate location
     */
    fun validateLocation(location: String): String? {
        return when {
            location.isBlank() -> "Location is required"
            location.length < 3 -> "Location must be at least 3 characters"
            location.length > 200 -> "Location must be less than 200 characters"
            else -> null
        }
    }
    
    /**
     * Validate farm size
     */
    fun validateFarmSize(size: String): String? {
        return when {
            size.isBlank() -> "Farm size is required"
            else -> {
                val sizeValue = size.toDoubleOrNull()
                when {
                    sizeValue == null -> "Farm size must be a valid number"
                    sizeValue < MIN_FARM_SIZE -> "Farm size must be at least $MIN_FARM_SIZE acres"
                    sizeValue > MAX_FARM_SIZE -> "Farm size cannot exceed $MAX_FARM_SIZE acres"
                    else -> null
                }
            }
        }
    }
    
    /**
     * Validate flock breed
     */
    fun validateFlockBreed(breed: String): String? {
        return when {
            breed.isBlank() -> "Breed is required"
            breed.length < 2 -> "Breed must be at least 2 characters"
            breed.length > 50 -> "Breed must be less than 50 characters"
            !breed.matches(Regex("^[a-zA-Z\\s\\-_'.]+$")) -> "Breed contains invalid characters"
            else -> null
        }
    }
    
    /**
     * Validate flock quantity
     */
    fun validateFlockQuantity(quantity: String): String? {
        return when {
            quantity.isBlank() -> "Quantity is required"
            else -> {
                val quantityValue = quantity.toIntOrNull()
                when {
                    quantityValue == null -> "Quantity must be a valid number"
                    quantityValue < MIN_FLOCK_QUANTITY -> "Quantity must be at least $MIN_FLOCK_QUANTITY"
                    quantityValue > MAX_FLOCK_QUANTITY -> "Quantity cannot exceed $MAX_FLOCK_QUANTITY"
                    else -> null
                }
            }
        }
    }
    
    /**
     * Validate flock age
     */
    fun validateFlockAge(age: String): String? {
        return when {
            age.isBlank() -> null // Age is optional
            else -> {
                val ageValue = age.toIntOrNull()
                when {
                    ageValue == null -> "Age must be a valid number"
                    ageValue < 0 -> "Age cannot be negative"
                    ageValue > 120 -> "Age cannot exceed 120 months"
                    else -> null
                }
            }
        }
    }
    
    /**
     * Validate health record type
     */
    fun validateHealthRecordType(type: String): String? {
        val validTypes = listOf("VACCINATION", "TREATMENT", "CHECKUP", "MEDICATION", "SURGERY", "ROUTINE_CARE")
        return when {
            type.isBlank() -> "Health record type is required"
            !validTypes.contains(type.uppercase()) -> "Invalid health record type"
            else -> null
        }
    }
    
    /**
     * Validate date (must be in the past or present)
     */
    fun validatePastDate(timestamp: Long): String? {
        val currentTime = System.currentTimeMillis()
        return when {
            timestamp > currentTime -> "Date cannot be in the future"
            timestamp < (currentTime - (365L * 24 * 60 * 60 * 1000 * 10)) -> "Date cannot be more than 10 years ago"
            else -> null
        }
    }
    
    /**
     * Validate sale amount
     */
    fun validateSaleAmount(amount: String): String? {
        return when {
            amount.isBlank() -> "Sale amount is required"
            else -> {
                val amountValue = amount.toDoubleOrNull()
                when {
                    amountValue == null -> "Sale amount must be a valid number"
                    amountValue < MIN_SALE_AMOUNT -> "Sale amount must be at least ₹$MIN_SALE_AMOUNT"
                    amountValue > MAX_SALE_AMOUNT -> "Sale amount cannot exceed ₹$MAX_SALE_AMOUNT"
                    else -> null
                }
            }
        }
    }
    
    /**
     * Validate buyer information
     */
    fun validateBuyerName(name: String): String? {
        return when {
            name.isBlank() -> "Buyer name is required"
            name.length < 2 -> "Buyer name must be at least 2 characters"
            name.length > 100 -> "Buyer name must be less than 100 characters"
            !NAME_PATTERN.matcher(name).matches() -> "Buyer name contains invalid characters"
            else -> null
        }
    }
    
    /**
     * Validate buyer contact
     */
    fun validateBuyerContact(contact: String): String? {
        return when {
            contact.isBlank() -> null // Contact is optional
            !PHONE_PATTERN.matcher(contact).matches() -> "Invalid phone number format"
            else -> null
        }
    }
    
    /**
     * Validate inventory item type
     */
    fun validateInventoryType(type: String): String? {
        val validTypes = listOf("FEED", "MEDICINE", "EQUIPMENT", "SUPPLIES", "BEDDING", "SUPPLEMENTS")
        return when {
            type.isBlank() -> "Item type is required"
            !validTypes.contains(type.uppercase()) -> "Invalid item type"
            else -> null
        }
    }
    
    /**
     * Validate inventory quantity
     */
    fun validateInventoryQuantity(quantity: String): String? {
        return when {
            quantity.isBlank() -> "Quantity is required"
            else -> {
                val quantityValue = quantity.toIntOrNull()
                when {
                    quantityValue == null -> "Quantity must be a valid number"
                    quantityValue < MIN_INVENTORY_QUANTITY -> "Quantity cannot be negative"
                    quantityValue > MAX_INVENTORY_QUANTITY -> "Quantity cannot exceed $MAX_INVENTORY_QUANTITY"
                    else -> null
                }
            }
        }
    }
    
    /**
     * Validate restock threshold
     */
    fun validateRestockThreshold(threshold: String): String? {
        return when {
            threshold.isBlank() -> null // Threshold is optional
            else -> {
                val thresholdValue = threshold.toIntOrNull()
                when {
                    thresholdValue == null -> "Threshold must be a valid number"
                    thresholdValue < 0 -> "Threshold cannot be negative"
                    thresholdValue > MAX_INVENTORY_QUANTITY -> "Threshold cannot exceed $MAX_INVENTORY_QUANTITY"
                    else -> null
                }
            }
        }
    }
    
    /**
     * Validate photo URI
     */
    fun validatePhotoUri(uri: Uri?): String? {
        return when {
            uri == null -> null // Photo is optional
            uri.toString().isBlank() -> "Invalid photo"
            else -> null
        }
    }
    
    /**
     * Validate email format
     */
    fun validateEmail(email: String): String? {
        return when {
            email.isBlank() -> null // Email is optional
            !EMAIL_PATTERN.matcher(email).matches() -> "Invalid email format"
            else -> null
        }
    }
    
    /**
     * Validate required field
     */
    fun validateRequired(value: String, fieldName: String): String? {
        return when {
            value.isBlank() -> "$fieldName is required"
            else -> null
        }
    }
    
    /**
     * Validate positive number
     */
    fun validatePositiveNumber(value: String, fieldName: String): String? {
        return when {
            value.isBlank() -> "$fieldName is required"
            else -> {
                val numberValue = value.toDoubleOrNull()
                when {
                    numberValue == null -> "$fieldName must be a valid number"
                    numberValue <= 0 -> "$fieldName must be positive"
                    else -> null
                }
            }
        }
    }
    
    /**
     * Validate non-negative number
     */
    fun validateNonNegativeNumber(value: String, fieldName: String): String? {
        return when {
            value.isBlank() -> "$fieldName is required"
            else -> {
                val numberValue = value.toDoubleOrNull()
                when {
                    numberValue == null -> "$fieldName must be a valid number"
                    numberValue < 0 -> "$fieldName cannot be negative"
                    else -> null
                }
            }
        }
    }
    
    /**
     * Validate text length
     */
    fun validateTextLength(text: String, fieldName: String, minLength: Int, maxLength: Int): String? {
        return when {
            text.length < minLength -> "$fieldName must be at least $minLength characters"
            text.length > maxLength -> "$fieldName must be less than $maxLength characters"
            else -> null
        }
    }
    
    /**
     * Validate future date
     */
    fun validateFutureDate(timestamp: Long): String? {
        val currentTime = System.currentTimeMillis()
        return when {
            timestamp <= currentTime -> "Date must be in the future"
            timestamp > (currentTime + (365L * 24 * 60 * 60 * 1000 * 5)) -> "Date cannot be more than 5 years in the future"
            else -> null
        }
    }
    
    /**
     * Validate payment method
     */
    fun validatePaymentMethod(method: String): String? {
        val validMethods = listOf("CASH", "UPI", "BANK_TRANSFER", "CHEQUE", "CARD")
        return when {
            method.isBlank() -> "Payment method is required"
            !validMethods.contains(method.uppercase()) -> "Invalid payment method"
            else -> null
        }
    }
    
    /**
     * Comprehensive form validation
     */
    fun validateForm(validations: List<String?>): List<String> {
        return validations.filterNotNull()
    }
    
    /**
     * Check if form has errors
     */
    fun hasValidationErrors(validations: List<String?>): Boolean {
        return validations.any { it != null }
    }
}