// generated/phase3/app/src/main/java/com/rio/rustry/dashboard/BreedingAnalyticsViewModel.kt

package com.rio.rustry.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rio.rustry.domain.usecase.GetBreedingAnalyticsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BreedingAnalyticsViewModel @Inject constructor(
    private val getBreedingAnalyticsUseCase: GetBreedingAnalyticsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<BreedingAnalyticsUiState>(BreedingAnalyticsUiState.Loading)
    val uiState: StateFlow<BreedingAnalyticsUiState> = _uiState.asStateFlow()

    fun loadAnalytics(period: AnalyticsPeriod) {
        viewModelScope.launch {
            _uiState.value = BreedingAnalyticsUiState.Loading
            try {
                val analytics = getBreedingAnalyticsUseCase(period)
                _uiState.value = BreedingAnalyticsUiState.Success(analytics)
            } catch (e: Exception) {
                _uiState.value = BreedingAnalyticsUiState.Error(
                    e.message ?: "Failed to load analytics"
                )
            }
        }
    }
}

sealed class BreedingAnalyticsUiState {
    object Loading : BreedingAnalyticsUiState()
    data class Success(val analytics: BreedingAnalytics) : BreedingAnalyticsUiState()
    data class Error(val message: String) : BreedingAnalyticsUiState()
}

data class BreedingAnalytics(
    val hatchRate: Double,
    val mortalityRate: Double,
    val avgWeightGain: Double,
    val trendData: List<Double>,
    val period: AnalyticsPeriod
)