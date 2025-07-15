// generated/phase2/app/src/main/java/com/rio/rustry/data/local/database/Converters.kt

package com.rio.rustry.data.local.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.rio.rustry.data.model.AgeGroup
import com.rio.rustry.data.model.Breed
import com.rio.rustry.data.model.FowlStatus
import com.rio.rustry.data.model.Lineage

class Converters {
    
    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return Gson().toJson(value)
    }
    
    @TypeConverter
    fun toStringList(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson(value, listType)
    }
    
    @TypeConverter
    fun fromBreed(breed: Breed): String {
        return breed.name
    }
    
    @TypeConverter
    fun toBreed(breed: String): Breed {
        return Breed.valueOf(breed)
    }
    
    @TypeConverter
    fun fromAgeGroup(ageGroup: AgeGroup): String {
        return ageGroup.name
    }
    
    @TypeConverter
    fun toAgeGroup(ageGroup: String): AgeGroup {
        return AgeGroup.valueOf(ageGroup)
    }
    
    @TypeConverter
    fun fromFowlStatus(status: FowlStatus): String {
        return status.name
    }
    
    @TypeConverter
    fun toFowlStatus(status: String): FowlStatus {
        return FowlStatus.valueOf(status)
    }
    
    @TypeConverter
    fun fromLineage(lineage: Lineage): String {
        return Gson().toJson(lineage)
    }
    
    @TypeConverter
    fun toLineage(lineage: String): Lineage {
        return Gson().fromJson(lineage, Lineage::class.java)
    }
}