package com.rio.rustry.domain.usecase

import com.rio.rustry.domain.model.Sale
import com.rio.rustry.domain.repository.FarmRepository
import javax.inject.Inject
import java.util.regex.Pattern

class RecordSaleUseCase @Inject constructor(
    private val farmRepository: FarmRepository
) {
    suspend operator fun invoke(sale: Sale): String {
        // Validation
        if (sale.amount < 0.01) throw IllegalArgumentException("Sale amount must be at least ₹0.01")
        if (sale.amount > 10000000.0) throw IllegalArgumentException("Sale amount cannot exceed ₹10000000.0")

        if (sale.buyerName.isBlank()) throw IllegalArgumentException("Buyer name is required")
        if (sale.buyerName.length < 2) throw IllegalArgumentException("Buyer name must be at least 2 characters")
        if (sale.buyerName.length > 100) throw IllegalArgumentException("Buyer name must be less than 100 characters")
        val namePattern = Pattern.compile("^[a-zA-Z\\s]{2,50}$")
        if (!namePattern.matcher(sale.buyerName).matches()) throw IllegalArgumentException("Buyer name contains invalid characters")

        val phonePattern = Pattern.compile("^[+]?[0-9]{10,15}$")
        if (sale.buyerContact.isNotBlank() && !phonePattern.matcher(sale.buyerContact).matches()) throw IllegalArgumentException("Invalid phone number format")

        val validMethods = listOf("CASH", "UPI", "BANK_TRANSFER", "CHEQUE", "CARD")
        if (sale.paymentMethod.isBlank()) throw IllegalArgumentException("Payment method is required")
        if (!validMethods.contains(sale.paymentMethod.uppercase())) throw IllegalArgumentException("Invalid payment method")

        val currentTime = System.currentTimeMillis()
        if (sale.date > currentTime) throw IllegalArgumentException("Date cannot be in the future")
        if (sale.date < (currentTime - (365L * 24 * 60 * 60 * 1000 * 10))) throw IllegalArgumentException("Date cannot be more than 10 years ago")

        // Business logic
        val saleToAdd = sale.copy(
            updatedAt = System.currentTimeMillis(),
            createdAt = if (sale.createdAt == 0L) System.currentTimeMillis() else sale.createdAt
        )

        return farmRepository.addSale(saleToAdd)
    }
}