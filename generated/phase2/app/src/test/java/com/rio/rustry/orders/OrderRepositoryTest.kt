// generated/phase2/app/src/test/java/com/rio/rustry/orders/OrderRepositoryTest.kt

package com.rio.rustry.orders

import com.google.common.truth.Truth.assertThat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import com.rio.rustry.data.local.dao.CartDao
import com.rio.rustry.data.model.CartItem
import com.rio.rustry.payment.CheckoutData
import com.rio.rustry.payment.PaymentMethod
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class OrderRepositoryTest {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var functions: FirebaseFunctions
    private lateinit var cartDao: CartDao
    private lateinit var orderRepository: OrderRepository
    private lateinit var mockUser: FirebaseUser

    @Before
    fun setup() {
        firestore = mockk(relaxed = true)
        auth = mockk()
        functions = mockk(relaxed = true)
        cartDao = mockk(relaxed = true)
        mockUser = mockk()
        orderRepository = OrderRepository(firestore, auth, functions, cartDao)
    }

    @Test
    fun `addToCart adds item successfully`() = runTest {
        // Given
        val fowlId = "fowl123"
        val quantity = 2
        val userId = "user123"
        
        every { auth.currentUser } returns mockUser
        every { mockUser.uid } returns userId

        // When
        val result = orderRepository.addToCart(fowlId, quantity)

        // Then
        assertThat(result.isSuccess).isTrue()
        verify { cartDao.insertCartItem(any()) }
    }

    @Test
    fun `addToCart fails when user not authenticated`() = runTest {
        // Given
        val fowlId = "fowl123"
        val quantity = 2
        
        every { auth.currentUser } returns null

        // When
        val result = orderRepository.addToCart(fowlId, quantity)

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).contains("not authenticated")
    }

    @Test
    fun `getCart returns user cart items`() = runTest {
        // Given
        val userId = "user123"
        val cartItems = listOf(
            CartItem(
                fowlId = "fowl1",
                fowlName = "Chicken (Adult)",
                price = 25.0,
                quantity = 1,
                userId = userId
            ),
            CartItem(
                fowlId = "fowl2",
                fowlName = "Duck (Juvenile)",
                price = 15.0,
                quantity = 2,
                userId = userId
            )
        )
        
        every { auth.currentUser } returns mockUser
        every { mockUser.uid } returns userId
        coEvery { cartDao.getCartItems(userId) } returns cartItems

        // When
        val results = orderRepository.getCart().toList()

        // Then
        assertThat(results).hasSize(1)
        val result = results.first()
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo(cartItems)
    }

    @Test
    fun `updateCartQuantity updates item quantity`() = runTest {
        // Given
        val fowlId = "fowl123"
        val quantity = 3
        val userId = "user123"
        
        every { auth.currentUser } returns mockUser
        every { mockUser.uid } returns userId

        // When
        val result = orderRepository.updateCartQuantity(fowlId, quantity)

        // Then
        assertThat(result.isSuccess).isTrue()
        verify { cartDao.updateQuantity(fowlId, userId, quantity) }
    }

    @Test
    fun `removeFromCart removes item from cart`() = runTest {
        // Given
        val fowlId = "fowl123"
        val userId = "user123"
        
        every { auth.currentUser } returns mockUser
        every { mockUser.uid } returns userId

        // When
        val result = orderRepository.removeFromCart(fowlId)

        // Then
        assertThat(result.isSuccess).isTrue()
        verify { cartDao.removeCartItem(fowlId, userId) }
    }

    @Test
    fun `checkout creates order successfully with COD`() = runTest {
        // Given
        val userId = "user123"
        val checkoutData = CheckoutData(
            items = listOf(
                CartItem(
                    fowlId = "fowl1",
                    fowlName = "Chicken (Adult)",
                    price = 25.0,
                    quantity = 1,
                    userId = userId
                )
            ),
            paymentMethod = PaymentMethod.COD,
            deliveryAddress = "123 Main St",
            total = 25.0
        )
        
        every { auth.currentUser } returns mockUser
        every { mockUser.uid } returns userId

        // When
        val result = orderRepository.checkout(checkoutData)

        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isNotEmpty()
        verify { cartDao.clearCart(userId) }
    }

    @Test
    fun `checkout creates order successfully with Google Pay`() = runTest {
        // Given
        val userId = "user123"
        val checkoutData = CheckoutData(
            items = listOf(
                CartItem(
                    fowlId = "fowl1",
                    fowlName = "Chicken (Adult)",
                    price = 25.0,
                    quantity = 1,
                    userId = userId
                )
            ),
            paymentMethod = PaymentMethod.GOOGLE_PAY,
            deliveryAddress = "123 Main St",
            total = 25.0
        )
        
        every { auth.currentUser } returns mockUser
        every { mockUser.uid } returns userId

        // When
        val result = orderRepository.checkout(checkoutData)

        // Then
        assertThat(result.isSuccess).isTrue()
        verify { cartDao.clearCart(userId) }
    }

    @Test
    fun `checkout fails when user not authenticated`() = runTest {
        // Given
        val checkoutData = CheckoutData(
            items = emptyList(),
            paymentMethod = PaymentMethod.COD,
            deliveryAddress = "123 Main St",
            total = 0.0
        )
        
        every { auth.currentUser } returns null

        // When
        val result = orderRepository.checkout(checkoutData)

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).contains("not authenticated")
    }

    @Test
    fun `getOrders returns user orders`() = runTest {
        // Given
        val userId = "user123"
        
        every { auth.currentUser } returns mockUser
        every { mockUser.uid } returns userId

        // When
        val results = orderRepository.getOrders().toList()

        // Then
        assertThat(results).hasSize(1)
        val result = results.first()
        assertThat(result.isSuccess).isTrue()
    }
}