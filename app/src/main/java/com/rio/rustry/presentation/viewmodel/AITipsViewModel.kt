package com.rio.rustry.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rio.rustry.data.model.*
import com.rio.rustry.data.repository.FowlRepository
import com.rio.rustry.data.repository.HealthRepository
import com.rio.rustry.utils.AIHealthService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AITipsViewModel @Inject constructor(
    private val fowlRepository: FowlRepository,
    private val healthRepository: HealthRepository,
    private val aiHealthService: AIHealthService
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AITipsUiState())
    val uiState: StateFlow<AITipsUiState> = _uiState.asStateFlow()
    
    private var allTips: List<AIHealthTip> = emptyList()
    
    fun loadAITips(fowlId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                // Load fowl information
                val fowl = fowlRepository.getFowlById(fowlId).getOrNull()
                if (fowl == null) {
                    _uiState.value = _uiState.value.copy(
                        error = "Fowl not found",
                        isLoading = false
                    )
                    return@launch
                }
                
                // Load health records
                healthRepository.getHealthRecords(fowlId).collect { healthRecords ->
                    // Get AI recommendations
                    val generalTips = aiHealthService.getHealthTipsForFowl(fowl)
                    val vaccinationTips = aiHealthService.getVaccinationReminders(fowl, healthRecords)
                    val trendTips = aiHealthService.analyzeHealthTrends(healthRecords)
                    val seasonalTips = aiHealthService.getSeasonalTips(fowl.location)
                    val breedTips = aiHealthService.getBreedSpecificTips(fowl.breed)
                    
                    // Combine all tips and remove duplicates
                    allTips = (generalTips + vaccinationTips + trendTips + seasonalTips + breedTips)
                        .distinctBy { it.title }
                        .sortedWith(
                            compareByDescending<AIHealthTip> { it.priority.ordinal }
                                .thenByDescending { it.urgencyLevel }
                                .thenBy { it.dueDate ?: Long.MAX_VALUE }
                        )
                    
                    _uiState.value = _uiState.value.copy(
                        fowl = fowl,
                        allTips = allTips,
                        filteredTips = allTips,
                        isLoading = false,
                        error = null
                    )
                }
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to load AI tips",
                    isLoading = false
                )
            }
        }
    }
    
    fun filterByCategory(category: TipCategory?) {
        val filtered = if (category == null) {
            allTips
        } else {
            allTips.filter { it.category == category }
        }
        
        _uiState.value = _uiState.value.copy(
            filteredTips = filtered,
            selectedCategory = category
        )
    }
    
    fun filterByPriority(priority: TipPriority?) {
        val filtered = if (priority == null) {
            allTips
        } else {
            allTips.filter { it.priority == priority }
        }
        
        _uiState.value = _uiState.value.copy(
            filteredTips = filtered,
            selectedPriority = priority
        )
    }
    
    fun searchTips(query: String) {
        val filtered = if (query.isBlank()) {
            allTips
        } else {
            allTips.filter { tip ->
                tip.title.contains(query, ignoreCase = true) ||
                tip.description.contains(query, ignoreCase = true) ||
                tip.category.displayName.contains(query, ignoreCase = true) ||
                tip.symptoms.any { it.contains(query, ignoreCase = true) } ||
                tip.prevention.any { it.contains(query, ignoreCase = true) }
            }
        }
        
        _uiState.value = _uiState.value.copy(
            filteredTips = filtered,
            searchQuery = query
        )
    }
    
    fun markTipAsRead(tipId: String) {
        // In a real implementation, this would mark the tip as read in the database
        // For now, we'll just update the local state
        val updatedTips = allTips.map { tip ->
            if (tip.id == tipId) {
                // Create a copy with read status (would need to add this field to AIHealthTip)
                tip
            } else {
                tip
            }
        }
        
        allTips = updatedTips
        applyCurrentFilters()
    }
    
    fun refreshTips() {
        val fowlId = _uiState.value.fowl?.id
        if (fowlId != null) {
            loadAITips(fowlId)
        }
    }
    
    private fun applyCurrentFilters() {
        var filtered = allTips
        
        _uiState.value.selectedCategory?.let { category ->
            filtered = filtered.filter { it.category == category }
        }
        
        _uiState.value.selectedPriority?.let { priority ->
            filtered = filtered.filter { it.priority == priority }
        }
        
        if (_uiState.value.searchQuery.isNotBlank()) {
            val query = _uiState.value.searchQuery
            filtered = filtered.filter { tip ->
                tip.title.contains(query, ignoreCase = true) ||
                tip.description.contains(query, ignoreCase = true) ||
                tip.category.displayName.contains(query, ignoreCase = true)
            }
        }
        
        _uiState.value = _uiState.value.copy(filteredTips = filtered)
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class AITipsUiState(
    val fowl: Fowl? = null,
    val allTips: List<AIHealthTip> = emptyList(),
    val filteredTips: List<AIHealthTip> = emptyList(),
    val selectedCategory: TipCategory? = null,
    val selectedPriority: TipPriority? = null,
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)