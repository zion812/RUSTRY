package com.rio.rustry.presentation.marketplace

/**
 * Price range enum for marketplace filtering
 */
enum class PriceRange(val displayName: String, val min: Double, val max: Double) {
    UNDER_500("Under ₹500", 0.0, 500.0),
    RANGE_500_1000("₹500 - ₹1000", 500.0, 1000.0),
    RANGE_1000_2000("₹1000 - ₹2000", 1000.0, 2000.0),
    RANGE_2000_5000("₹2000 - ₹5000", 2000.0, 5000.0),
    ABOVE_5000("Above ₹5000", 5000.0, Double.MAX_VALUE);
    
    val label: String get() = displayName
    
    companion object {
        val ranges = values().toList()
    }
}