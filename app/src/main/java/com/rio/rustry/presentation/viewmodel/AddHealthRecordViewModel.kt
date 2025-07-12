package com.rio.rustry.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rio.rustry.data.model.*
import com.rio.rustry.data.repository.HealthRepository
import com.rio.rustry.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class AddHealthRecordViewModel @Inject constructor(
    private val healthRepository: HealthRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AddHealthRecordUiState())
    val uiState: StateFlow<AddHealthRecordUiState> = _uiState.asStateFlow()
    
    private val _saveResult = MutableSharedFlow<SaveResult>()
    val saveResult: SharedFlow<SaveResult> = _saveResult.asSharedFlow()
    
    fun initializeForFowl(fowlId: String) {
        _uiState.value = _uiState.value.copy(fowlId = fowlId)
    }
    
    fun updateType(type: HealthEventType) {
        _uiState.value = _uiState.value.copy(type = type)
    }
    
    fun updateTitle(title: String) {
        _uiState.value = _uiState.value.copy(title = title)
    }
    
    fun updateDescription(description: String) {
        _uiState.value = _uiState.value.copy(description = description)
    }
    
    fun updateDate(date: Long) {
        _uiState.value = _uiState.value.copy(date = date)
    }
    
    fun updateVetName(vetName: String) {
        _uiState.value = _uiState.value.copy(vetName = vetName)
    }
    
    fun updateVetLicense(vetLicense: String) {
        _uiState.value = _uiState.value.copy(vetLicense = vetLicense)
    }
    
    fun updateSeverity(severity: HealthSeverity) {
        _uiState.value = _uiState.value.copy(severity = severity)
    }
    
    fun updateNotes(notes: String) {
        _uiState.value = _uiState.value.copy(notes = notes)
    }
    
    fun updateMedication(medication: String) {
        _uiState.value = _uiState.value.copy(medication = medication)
    }
    
    fun updateDosage(dosage: String) {
        _uiState.value = _uiState.value.copy(dosage = dosage)
    }
    
    fun updateCost(cost: Double) {
        _uiState.value = _uiState.value.copy(cost = cost)
    }
    
    fun updateWeight(weight: Double?) {
        _uiState.value = _uiState.value.copy(weight = weight)
    }
    
    fun updateTemperature(temperature: Double?) {
        _uiState.value = _uiState.value.copy(temperature = temperature)
    }
    
    fun updateSymptoms(symptoms: List<String>) {
        _uiState.value = _uiState.value.copy(symptoms = symptoms)
    }
    
    fun addSymptom(symptom: String) {
        if (symptom.isNotBlank()) {
            val currentSymptoms = _uiState.value.symptoms.toMutableList()
            if (!currentSymptoms.contains(symptom)) {
                currentSymptoms.add(symptom)
                _uiState.value = _uiState.value.copy(symptoms = currentSymptoms)
            }
        }
    }
    
    fun removeSymptom(symptom: String) {
        val currentSymptoms = _uiState.value.symptoms.toMutableList()
        currentSymptoms.remove(symptom)
        _uiState.value = _uiState.value.copy(symptoms = currentSymptoms)
    }
    
    fun updateTreatment(treatment: String) {
        _uiState.value = _uiState.value.copy(treatment = treatment)
    }
    
    fun updateFollowUpRequired(required: Boolean) {
        _uiState.value = _uiState.value.copy(followUpRequired = required)
    }
    
    fun updateFollowUpDate(date: Long?) {
        _uiState.value = _uiState.value.copy(followUpDate = date)
    }
    
    fun updateNextDueDate(date: Long?) {
        _uiState.value = _uiState.value.copy(nextDueDate = date)
    }
    
    fun addProofImage(imageUrl: String) {
        val currentImages = _uiState.value.proofImageUrls.toMutableList()
        currentImages.add(imageUrl)
        _uiState.value = _uiState.value.copy(proofImageUrls = currentImages)
    }
    
    fun removeProofImage(imageUrl: String) {
        val currentImages = _uiState.value.proofImageUrls.toMutableList()
        currentImages.remove(imageUrl)
        _uiState.value = _uiState.value.copy(proofImageUrls = currentImages)
    }
    
    fun addCertificate(certificateUrl: String) {
        val currentCertificates = _uiState.value.certificateUrls.toMutableList()
        currentCertificates.add(certificateUrl)
        _uiState.value = _uiState.value.copy(certificateUrls = currentCertificates)
    }
    
    fun removeCertificate(certificateUrl: String) {
        val currentCertificates = _uiState.value.certificateUrls.toMutableList()
        currentCertificates.remove(certificateUrl)
        _uiState.value = _uiState.value.copy(certificateUrls = currentCertificates)
    }
    
    fun saveHealthRecord() {
        viewModelScope.launch {
            val state = _uiState.value
            
            // Validation
            val validationErrors = validateHealthRecord(state)
            if (validationErrors.isNotEmpty()) {
                _uiState.value = _uiState.value.copy(
                    validationErrors = validationErrors
                )
                return@launch
            }
            
            _uiState.value = _uiState.value.copy(
                isSaving = true,
                validationErrors = emptyList()
            )
            
            try {
                val currentUser = authRepository.getCurrentUser()
                if (currentUser == null) {
                    _saveResult.emit(SaveResult.Error("User not authenticated"))
                    return@launch
                }
                
                val healthRecord = HealthRecord(
                    fowlId = state.fowlId,
                    type = state.type,
                    title = state.title,
                    description = state.description,
                    date = state.date,
                    vetName = state.vetName,
                    vetLicense = state.vetLicense,
                    proofImageUrls = state.proofImageUrls,
                    certificateUrls = state.certificateUrls,
                    nextDueDate = state.nextDueDate,
                    severity = state.severity,
                    createdBy = currentUser.id,
                    notes = state.notes,
                    medication = state.medication,
                    dosage = state.dosage,
                    cost = state.cost,
                    temperature = state.temperature,
                    weight = state.weight,
                    symptoms = state.symptoms,
                    treatment = state.treatment,
                    followUpRequired = state.followUpRequired,
                    followUpDate = state.followUpDate
                )
                
                val result = healthRepository.addHealthRecord(healthRecord)
                
                if (result.isSuccess) {
                    _saveResult.emit(SaveResult.Success(result.getOrNull()!!))
                    resetForm()
                } else {
                    _saveResult.emit(SaveResult.Error(result.exceptionOrNull()?.message ?: "Failed to save health record"))
                }
                
            } catch (e: Exception) {
                _saveResult.emit(SaveResult.Error(e.message ?: "Unknown error occurred"))
            } finally {
                _uiState.value = _uiState.value.copy(isSaving = false)
            }
        }
    }
    
    private fun validateHealthRecord(state: AddHealthRecordUiState): List<String> {
        val errors = mutableListOf<String>()
        
        if (state.fowlId.isBlank()) {
            errors.add("Fowl ID is required")
        }
        
        if (state.title.isBlank()) {
            errors.add("Title is required")
        }
        
        if (state.description.isBlank()) {
            errors.add("Description is required")
        }
        
        if (state.date > System.currentTimeMillis()) {
            errors.add("Date cannot be in the future")
        }
        
        if (state.type == HealthEventType.VACCINATION && state.vetName.isBlank()) {
            errors.add("Veterinarian name is required for vaccinations")
        }
        
        if (state.followUpRequired && state.followUpDate == null) {
            errors.add("Follow-up date is required when follow-up is needed")
        }
        
        if (state.followUpDate != null && state.followUpDate <= state.date) {
            errors.add("Follow-up date must be after the record date")
        }
        
        if (state.cost < 0) {
            errors.add("Cost cannot be negative")
        }
        
        if (state.weight != null && state.weight <= 0) {
            errors.add("Weight must be positive")
        }
        
        if (state.temperature != null && (state.temperature < 30 || state.temperature > 50)) {
            errors.add("Temperature seems unrealistic (should be between 30-50°C)")
        }
        
        return errors
    }
    
    private fun resetForm() {
        _uiState.value = AddHealthRecordUiState(fowlId = _uiState.value.fowlId)
    }
    
    fun clearValidationErrors() {
        _uiState.value = _uiState.value.copy(validationErrors = emptyList())
    }
}

data class AddHealthRecordUiState(
    val fowlId: String = "",
    val type: HealthEventType = HealthEventType.VACCINATION,
    val title: String = "",
    val description: String = "",
    val date: Long = System.currentTimeMillis(),
    val vetName: String = "",
    val vetLicense: String = "",
    val proofImageUrls: List<String> = emptyList(),
    val certificateUrls: List<String> = emptyList(),
    val nextDueDate: Long? = null,
    val severity: HealthSeverity = HealthSeverity.LOW,
    val notes: String = "",
    val medication: String = "",
    val dosage: String = "",
    val cost: Double = 0.0,
    val temperature: Double? = null,
    val weight: Double? = null,
    val symptoms: List<String> = emptyList(),
    val treatment: String = "",
    val followUpRequired: Boolean = false,
    val followUpDate: Long? = null,
    val isSaving: Boolean = false,
    val validationErrors: List<String> = emptyList()
)

sealed class SaveResult {
    data class Success(val recordId: String) : SaveResult()
    data class Error(val message: String) : SaveResult()
}