package com.rio.rustry.domain.repository

import com.rio.rustry.domain.model.Result

interface CouponRepository {
    suspend fun getAvailableCoupons(): List<Coupon>
    suspend fun applyCoupon(couponCode: String): Boolean
    suspend fun validateCoupon(couponCode: String): Boolean
    suspend fun getUserCoupons(userId: String): List<Coupon>
}

data class Coupon(
    val id: String,
    val code: String,
    val title: String,
    val description: String,
    val discountType: DiscountType,
    val discountValue: Double,
    val minimumAmount: Double,
    val expiryDate: Long,
    val isActive: Boolean = true,
    val usageLimit: Int = 1,
    val usedCount: Int = 0
)

enum class DiscountType {
    PERCENTAGE, FIXED_AMOUNT
}

enum class BoostDuration(val days: Int, val displayName: String) {
    ONE_DAY(1, "1 Day"),
    THREE_DAYS(3, "3 Days"),
    ONE_WEEK(7, "1 Week"),
    TWO_WEEKS(14, "2 Weeks"),
    ONE_MONTH(30, "1 Month")
}

enum class TransferFilter {
    ALL, SENT, RECEIVED, PENDING, VERIFIED, REJECTED
}

enum class ContactMethod {
    PHONE, EMAIL, IN_PERSON
}

enum class AnalyticsPeriod {
    WEEK, MONTH, QUARTER, YEAR
}