// generated/phase3/app/src/main/java/com/rio/rustry/domain/repository/CouponRepository.kt

package com.rio.rustry.domain.repository

import com.rio.rustry.promotions.Coupon

interface CouponRepository {
    suspend fun getAvailableCoupons(): List<Coupon>
    suspend fun applyCoupon(couponCode: String): Boolean
    suspend fun validateCoupon(couponCode: String, orderAmount: Double): Boolean
    suspend fun getCouponDiscount(couponCode: String, orderAmount: Double): Double
}