package com.rio.rustry.data.model

/**
 * Enum for fowl breeds with display names
 */
enum class FowlBreed(val displayName: String) {
    RHODE_ISLAND_RED("Rhode Island Red"),
    LEGHORN("Leghorn"),
    BRAHMA("Brahma"),
    SUSSEX("Sussex"),
    ORPINGTON("Orpington"),
    WYANDOTTE("Wyandotte"),
    PLYMOUTH_ROCK("Plymouth Rock"),
    AUSTRALORP("Australorp"),
    NEW_HAMPSHIRE("New Hampshire"),
    JERSEY_GIANT("Jersey Giant"),
    CORNISH("Cornish"),
    BANTAM("Bantam"),
    SILKIE("Silkie"),
    POLISH("Polish"),
    COCHIN("Cochin"),
    MARANS("Marans"),
    EASTER_EGGER("Easter Egger"),
    AMERAUCANA("Ameraucana"),
    BUFF_ORPINGTON("Buff Orpington"),
    BLACK_AUSTRALORP("Black Australorp"),
    DESI("Desi"),
    KADAKNATH("Kadaknath"),
    ASEEL("Aseel"),
    CHITTAGONG("Chittagong"),
    NAKED_NECK("Naked Neck"),
    OTHER("Other")
}

/**
 * Price range for filtering
 */
data class PriceRange(
    val min: Double,
    val max: Double,
    val label: String
) {
    companion object {
        val ranges = listOf(
            PriceRange(0.0, 500.0, "Under ₹500"),
            PriceRange(500.0, 1000.0, "₹500 - ₹1000"),
            PriceRange(1000.0, 2000.0, "₹1000 - ₹2000"),
            PriceRange(2000.0, 5000.0, "₹2000 - ₹5000"),
            PriceRange(5000.0, Double.MAX_VALUE, "Above ₹5000")
        )
    }
}