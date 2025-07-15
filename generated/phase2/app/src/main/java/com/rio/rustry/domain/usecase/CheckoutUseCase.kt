// generated/phase2/app/src/main/java/com/rio/rustry/domain/usecase/CheckoutUseCase.kt

package com.rio.rustry.domain.usecase

import com.rio.rustry.orders.OrderRepository
import com.rio.rustry.payment.CheckoutData
import javax.inject.Inject

class CheckoutUseCase @Inject constructor(
    private val orderRepository: OrderRepository
) {
    suspend operator fun invoke(checkoutData: CheckoutData): Result<String> {
        return orderRepository.checkout(checkoutData)
    }
}