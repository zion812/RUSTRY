package com.rio.rustry.data.local

import androidx.room.TypeConverter
import com.rio.rustry.data.model.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.util.*

/**
 * Room type converters for complex data types
 */
class Converters {
    
    private val json = Json { ignoreUnknownKeys = true }
    
    // List<String> converters
    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return json.encodeToString(value)
    }
    
    @TypeConverter
    fun toStringList(value: String): List<String> {
        return try {
            json.decodeFromString(value)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    // Map<String, String> converters
    @TypeConverter
    fun fromStringMap(value: Map<String, String>): String {
        return json.encodeToString(value)
    }
    
    @TypeConverter
    fun toStringMap(value: String): Map<String, String> {
        return try {
            json.decodeFromString(value)
        } catch (e: Exception) {
            emptyMap()
        }
    }
    
    // Map<String, Int> converters
    @TypeConverter
    fun fromStringIntMap(value: Map<String, Int>): String {
        return json.encodeToString(value)
    }
    
    @TypeConverter
    fun toStringIntMap(value: String): Map<String, Int> {
        return try {
            json.decodeFromString(value)
        } catch (e: Exception) {
            emptyMap()
        }
    }
    
    // BankAccountDetails converter
    @TypeConverter
    fun fromBankAccountDetails(value: BankAccountDetails?): String? {
        return value?.let { json.encodeToString(it) }
    }
    
    @TypeConverter
    fun toBankAccountDetails(value: String?): BankAccountDetails? {
        return value?.let {
            try {
                json.decodeFromString(it)
            } catch (e: Exception) {
                null
            }
        }
    }
    
    // List<Treatment> converters
    @TypeConverter
    fun fromTreatmentList(value: List<Treatment>): String {
        return json.encodeToString(value)
    }
    
    @TypeConverter
    fun toTreatmentList(value: String): List<Treatment> {
        return try {
            json.decodeFromString(value)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    // List<Medication> converters
    @TypeConverter
    fun fromMedicationList(value: List<Medication>): String {
        return json.encodeToString(value)
    }
    
    @TypeConverter
    fun toMedicationList(value: String): List<Medication> {
        return try {
            json.decodeFromString(value)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    // List<Vaccination> converters
    @TypeConverter
    fun fromVaccinationList(value: List<Vaccination>): String {
        return json.encodeToString(value)
    }
    
    @TypeConverter
    fun toVaccinationList(value: String): List<Vaccination> {
        return try {
            json.decodeFromString(value)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    // List<LabTest> converters
    @TypeConverter
    fun fromLabTestList(value: List<LabTest>): String {
        return json.encodeToString(value)
    }
    
    @TypeConverter
    fun toLabTestList(value: String): List<LabTest> {
        return try {
            json.decodeFromString(value)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    // Date converters
    @TypeConverter
    fun fromDate(date: Date): Long {
        return date.time
    }

    @TypeConverter
    fun toDate(timestamp: Long): Date {
        return Date(timestamp)
    }
}