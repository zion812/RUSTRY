// generated/phase2/app/src/main/java/com/rio/rustry/orders/OrderRepository.kt

package com.rio.rustry.orders

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import com.rio.rustry.data.local.dao.CartDao
import com.rio.rustry.data.model.*
import com.rio.rustry.payment.CheckoutData
import com.rio.rustry.payment.PaymentMethod
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrderRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val functions: FirebaseFunctions,
    private val cartDao: CartDao
) {

    suspend fun addToCart(fowlId: String, quantity: Int): Result<Unit> {
        return try {
            val currentUser = auth.currentUser ?: throw Exception("User not authenticated")
            
            // Get fowl details
            val fowlDoc = firestore.collection("fowls").document(fowlId).get().await()
            val fowl = fowlDoc.toObject(Fowl::class.java) ?: throw Exception("Fowl not found")
            
            val cartItem = CartItem(
                fowlId = fowlId,
                fowlName = "${fowl.breed.displayName} (${fowl.ageGroup.displayName})",
                price = fowl.price,
                quantity = quantity,
                userId = currentUser.uid
            )
            
            cartDao.insertCartItem(cartItem)
            
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun getCart(): Flow<Result<List<CartItem>>> = flow {
        try {
            val currentUser = auth.currentUser ?: throw Exception("User not authenticated")
            val items = cartDao.getCartItems(currentUser.uid)
            emit(Result.Success(items))
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }

    suspend fun updateCartQuantity(fowlId: String, quantity: Int): Result<Unit> {
        return try {
            val currentUser = auth.currentUser ?: throw Exception("User not authenticated")
            cartDao.updateQuantity(fowlId, currentUser.uid, quantity)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun removeFromCart(fowlId: String): Result<Unit> {
        return try {
            val currentUser = auth.currentUser ?: throw Exception("User not authenticated")
            cartDao.removeCartItem(fowlId, currentUser.uid)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun checkout(checkoutData: CheckoutData): Result<String> {
        return try {
            val currentUser = auth.currentUser ?: throw Exception("User not authenticated")
            
            val orderId = UUID.randomUUID().toString()
            val order = Order(
                id = orderId,
                userId = currentUser.uid,
                items = checkoutData.items.map { cartItem ->
                    OrderItem(
                        fowlId = cartItem.fowlId,
                        fowlName = cartItem.fowlName,
                        price = cartItem.price,
                        quantity = cartItem.quantity
                    )
                },
                total = checkoutData.total,
                status = OrderStatus.PENDING,
                paymentMethod = checkoutData.paymentMethod,
                deliveryAddress = checkoutData.deliveryAddress,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            
            // Create order in Firestore
            firestore.collection("orders").document(orderId).set(order).await()
            
            // Process payment based on method
            when (checkoutData.paymentMethod) {
                PaymentMethod.STRIPE -> {
                    // Create Stripe payment intent via Cloud Function
                    val data = mapOf(
                        "orderId" to orderId,
                        "amount" to (checkoutData.total * 100).toInt() // Convert to cents
                    )
                    functions.getHttpsCallable("createStripePaymentIntent")
                        .call(data)
                        .await()
                }
                PaymentMethod.GOOGLE_PAY -> {
                    // Google Pay integration would be handled in the UI
                    // For now, mark as confirmed
                    firestore.collection("orders")
                        .document(orderId)
                        .update("status", OrderStatus.CONFIRMED)
                        .await()
                }
                PaymentMethod.COD -> {
                    // Cash on delivery - mark as confirmed
                    firestore.collection("orders")
                        .document(orderId)
                        .update("status", OrderStatus.CONFIRMED)
                        .await()
                }
            }
            
            // Clear cart
            cartDao.clearCart(currentUser.uid)
            
            Result.Success(orderId)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun getOrders(): Flow<Result<List<Order>>> = flow {
        try {
            val currentUser = auth.currentUser ?: throw Exception("User not authenticated")
            
            val snapshot = firestore.collection("orders")
                .whereEqualTo("userId", currentUser.uid)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
            
            val orders = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Order::class.java)?.copy(id = doc.id)
            }
            
            emit(Result.Success(orders))
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }
}