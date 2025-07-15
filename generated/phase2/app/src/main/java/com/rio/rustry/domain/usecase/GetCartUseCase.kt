// generated/phase2/app/src/main/java/com/rio/rustry/domain/usecase/GetCartUseCase.kt

package com.rio.rustry.domain.usecase

import com.rio.rustry.data.model.CartItem
import com.rio.rustry.orders.OrderRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCartUseCase @Inject constructor(
    private val orderRepository: OrderRepository
) {
    suspend operator fun invoke(): Flow<Result<List<CartItem>>> {
        return orderRepository.getCart()
    }
}