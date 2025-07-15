// generated/phase2/app/src/main/java/com/rio/rustry/domain/usecase/LoadOrdersUseCase.kt

package com.rio.rustry.domain.usecase

import com.rio.rustry.data.model.Order
import com.rio.rustry.orders.OrderRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LoadOrdersUseCase @Inject constructor(
    private val orderRepository: OrderRepository
) {
    suspend operator fun invoke(): Flow<Result<List<Order>>> {
        return orderRepository.getOrders()
    }
}