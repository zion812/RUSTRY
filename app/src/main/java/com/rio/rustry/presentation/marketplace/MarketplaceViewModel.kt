package com.rio.rustry.presentation.marketplace

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rio.rustry.data.model.Fowl
import com.rio.rustry.data.model.FowlBreed
import com.rio.rustry.data.repository.FowlRepository as DataFowlRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

data class MarketplaceUiState(
    val fowls: List<Fowl> = emptyList(),
    val filteredFowls: List<Fowl> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val selectedBreed: FowlBreed? = null,
    val selectedPriceRange: PriceRange? = null,
    val selectedLocation: String? = null,
    val isVerifiedOnly: Boolean = false,
    val isNearbyOnly: Boolean = false,
    val favoriteFowlIds: Set<String> = emptySet(),
    val hasMoreData: Boolean = true,
    val currentPage: Int = 0
)

class MarketplaceViewModel(
    private val fowlRepository: DataFowlRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MarketplaceUiState())
    val uiState: StateFlow<MarketplaceUiState> = _uiState.asStateFlow()

    init {
        loadMarketplaceFowls()
    }

    fun loadMarketplaceFowls() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val fowls = fowlRepository.getAvailableFowlsFlow().first()
                _uiState.value = _uiState.value.copy(
                    fowls = fowls,
                    filteredFowls = fowls,
                    isLoading = false,
                    currentPage = 0,
                    hasMoreData = false // Using Flow, no pagination needed
                )
                applyFilters()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load fowls"
                )
            }
        }
    }

    fun searchFowls(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        applyFilters()
    }

    fun filterByBreed(breed: FowlBreed?) {
        _uiState.value = _uiState.value.copy(selectedBreed = breed)
        applyFilters()
    }

    fun filterByPriceRange(priceRange: PriceRange?) {
        _uiState.value = _uiState.value.copy(selectedPriceRange = priceRange)
        applyFilters()
    }

    fun filterByLocation(location: String?) {
        _uiState.value = _uiState.value.copy(selectedLocation = location)
        applyFilters()
    }

    fun toggleVerifiedFilter() {
        _uiState.value = _uiState.value.copy(isVerifiedOnly = !_uiState.value.isVerifiedOnly)
        applyFilters()
    }

    fun toggleNearbyFilter() {
        _uiState.value = _uiState.value.copy(isNearbyOnly = !_uiState.value.isNearbyOnly)
        applyFilters()
    }

    fun toggleFavorite(fowlId: String) {
        val currentFavorites = _uiState.value.favoriteFowlIds
        val newFavorites = if (currentFavorites.contains(fowlId)) {
            currentFavorites - fowlId
        } else {
            currentFavorites + fowlId
        }
        _uiState.value = _uiState.value.copy(favoriteFowlIds = newFavorites)
    }

    private fun applyFilters() {
        val currentState = _uiState.value
        var filteredFowls = currentState.fowls

        // Apply search filter
        if (currentState.searchQuery.isNotBlank()) {
            filteredFowls = filteredFowls.filter { fowl ->
                fowl.breed.contains(currentState.searchQuery, ignoreCase = true) ||
                fowl.description.contains(currentState.searchQuery, ignoreCase = true) ||
                fowl.location.contains(currentState.searchQuery, ignoreCase = true) ||
                fowl.ownerName.contains(currentState.searchQuery, ignoreCase = true)
            }
        }

        // Apply breed filter
        currentState.selectedBreed?.let { breed ->
            filteredFowls = filteredFowls.filter { fowl ->
                fowl.breed.equals(breed.displayName, ignoreCase = true)
            }
        }

        // Apply price range filter
        currentState.selectedPriceRange?.let { priceRange ->
            filteredFowls = filteredFowls.filter { fowl ->
                fowl.price >= priceRange.min && fowl.price <= priceRange.max
            }
        }

        // Apply location filter
        currentState.selectedLocation?.let { location ->
            filteredFowls = filteredFowls.filter { fowl ->
                fowl.location.contains(location, ignoreCase = true)
            }
        }

        // Apply verified filter
        if (currentState.isVerifiedOnly) {
            filteredFowls = filteredFowls.filter { it.isTraceable }
        }

        // Apply nearby filter (mock implementation)
        if (currentState.isNearbyOnly) {
            // In a real app, this would use location services
            filteredFowls = filteredFowls.filter { fowl ->
                fowl.location.contains("Hyderabad", ignoreCase = true) ||
                fowl.location.contains("Secunderabad", ignoreCase = true)
            }
        }

        _uiState.value = currentState.copy(filteredFowls = filteredFowls)
    }

    fun clearFilters() {
        _uiState.value = _uiState.value.copy(
            searchQuery = "",
            selectedBreed = null,
            selectedPriceRange = null,
            selectedLocation = null,
            isVerifiedOnly = false,
            isNearbyOnly = false,
            filteredFowls = _uiState.value.fowls
        )
    }
}