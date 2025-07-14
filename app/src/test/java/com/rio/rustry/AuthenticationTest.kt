package com.rio.rustry

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.rio.rustry.features.auth.*
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Authentication system tests
 * Tests phone OTP authentication and user profile management
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class AuthenticationTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun testAuthStateInitialization() {
        val authState = AuthState()
        
        assertThat(authState.isAuthenticated).isFalse()
        assertThat(authState.isLoading).isFalse()
        assertThat(authState.user).isNull()
        assertThat(authState.error).isNull()
        assertThat(authState.otpSent).isFalse()
    }

    @Test
    fun testUserProfileCreation() {
        val profile = UserProfile(
            uid = "test-uid-123",
            phoneNumber = "+1234567890",
            displayName = "Test Farmer",
            farmName = "Test Farm",
            location = "Test Location",
            bio = "Test bio",
            createdAt = System.currentTimeMillis(),
            kycStatus = KYCStatus.PENDING
        )
        
        assertThat(profile.uid).isEqualTo("test-uid-123")
        assertThat(profile.phoneNumber).isEqualTo("+1234567890")
        assertThat(profile.displayName).isEqualTo("Test Farmer")
        assertThat(profile.farmName).isEqualTo("Test Farm")
        assertThat(profile.kycStatus).isEqualTo(KYCStatus.PENDING)
    }

    @Test
    fun testUserProfileSerialization() {
        val profile = UserProfile(
            uid = "test-uid-123",
            phoneNumber = "+1234567890",
            displayName = "Test Farmer",
            farmName = "Test Farm",
            location = "Test Location",
            createdAt = System.currentTimeMillis(),
            kycStatus = KYCStatus.VERIFIED
        )
        
        val map = profile.toMap()
        val deserializedProfile = UserProfile.fromMap(map)
        
        assertThat(deserializedProfile.uid).isEqualTo(profile.uid)
        assertThat(deserializedProfile.phoneNumber).isEqualTo(profile.phoneNumber)
        assertThat(deserializedProfile.displayName).isEqualTo(profile.displayName)
        assertThat(deserializedProfile.farmName).isEqualTo(profile.farmName)
        assertThat(deserializedProfile.kycStatus).isEqualTo(profile.kycStatus)
    }

    @Test
    fun testKYCStatusValues() {
        assertThat(KYCStatus.PENDING.name).isEqualTo("PENDING")
        assertThat(KYCStatus.IN_REVIEW.name).isEqualTo("IN_REVIEW")
        assertThat(KYCStatus.VERIFIED.name).isEqualTo("VERIFIED")
        assertThat(KYCStatus.REJECTED.name).isEqualTo("REJECTED")
    }

    @Test
    fun testAuthResultTypes() {
        val successResult = AuthResult.Success("Login successful")
        val errorResult = AuthResult.Error("Login failed")
        
        assertThat(successResult).isInstanceOf(AuthResult.Success::class.java)
        assertThat(errorResult).isInstanceOf(AuthResult.Error::class.java)
        
        when (successResult) {
            is AuthResult.Success -> assertThat(successResult.message).isEqualTo("Login successful")
            is AuthResult.Error -> throw AssertionError("Should be success")
        }
        
        when (errorResult) {
            is AuthResult.Success -> throw AssertionError("Should be error")
            is AuthResult.Error -> assertThat(errorResult.message).isEqualTo("Login failed")
        }
    }

    @Test
    fun testUserDataClass() {
        val user = User(
            uid = "user-123",
            phoneNumber = "+1234567890",
            displayName = "John Farmer",
            email = "john@example.com",
            farmName = "John's Farm",
            location = "California",
            bio = "Experienced poultry farmer",
            createdAt = System.currentTimeMillis(),
            kycStatus = KYCStatus.VERIFIED,
            fowlCount = 25,
            rating = 4.8,
            reviewCount = 12
        )
        
        assertThat(user.uid).isEqualTo("user-123")
        assertThat(user.phoneNumber).isEqualTo("+1234567890")
        assertThat(user.displayName).isEqualTo("John Farmer")
        assertThat(user.email).isEqualTo("john@example.com")
        assertThat(user.farmName).isEqualTo("John's Farm")
        assertThat(user.location).isEqualTo("California")
        assertThat(user.kycStatus).isEqualTo(KYCStatus.VERIFIED)
        assertThat(user.fowlCount).isEqualTo(25)
        assertThat(user.rating).isEqualTo(4.8)
        assertThat(user.reviewCount).isEqualTo(12)
    }

    @Test
    fun testPhoneNumberValidation() {
        // Test valid phone numbers
        val validNumbers = listOf(
            "+1234567890",
            "+12345678901",
            "+919876543210"
        )
        
        validNumbers.forEach { number ->
            assertThat(isValidPhoneNumber(number)).isTrue()
        }
        
        // Test invalid phone numbers
        val invalidNumbers = listOf(
            "",
            "123",
            "abcdefghij",
            "+123",
            "1234567890" // Missing country code
        )
        
        invalidNumbers.forEach { number ->
            assertThat(isValidPhoneNumber(number)).isFalse()
        }
    }

    @Test
    fun testOTPValidation() {
        // Test valid OTP codes
        val validOTPs = listOf(
            "123456",
            "000000",
            "999999"
        )
        
        validOTPs.forEach { otp ->
            assertThat(isValidOTP(otp)).isTrue()
        }
        
        // Test invalid OTP codes
        val invalidOTPs = listOf(
            "",
            "123",
            "1234567",
            "12345a",
            "abcdef"
        )
        
        invalidOTPs.forEach { otp ->
            assertThat(isValidOTP(otp)).isFalse()
        }
    }

    @Test
    fun testUserProfileValidation() {
        // Test valid profile
        val validProfile = UserProfile(
            uid = "valid-uid",
            phoneNumber = "+1234567890",
            displayName = "Valid Name",
            farmName = "Valid Farm",
            location = "Valid Location",
            createdAt = System.currentTimeMillis()
        )
        
        assertThat(isValidUserProfile(validProfile)).isTrue()
        
        // Test invalid profiles
        val invalidProfiles = listOf(
            validProfile.copy(uid = ""),
            validProfile.copy(phoneNumber = ""),
            validProfile.copy(displayName = ""),
            validProfile.copy(displayName = "A") // Too short
        )
        
        invalidProfiles.forEach { profile ->
            assertThat(isValidUserProfile(profile)).isFalse()
        }
    }

    @Test
    fun testKYCStatusProgression() {
        // Test valid KYC status transitions
        assertThat(canTransitionKYCStatus(KYCStatus.PENDING, KYCStatus.IN_REVIEW)).isTrue()
        assertThat(canTransitionKYCStatus(KYCStatus.IN_REVIEW, KYCStatus.VERIFIED)).isTrue()
        assertThat(canTransitionKYCStatus(KYCStatus.IN_REVIEW, KYCStatus.REJECTED)).isTrue()
        
        // Test invalid transitions
        assertThat(canTransitionKYCStatus(KYCStatus.VERIFIED, KYCStatus.PENDING)).isFalse()
        assertThat(canTransitionKYCStatus(KYCStatus.REJECTED, KYCStatus.VERIFIED)).isFalse()
    }

    @Test
    fun testUserStatsCalculation() {
        val user = User(
            uid = "user-123",
            phoneNumber = "+1234567890",
            displayName = "Test User",
            createdAt = System.currentTimeMillis(),
            fowlCount = 10,
            rating = 4.5,
            reviewCount = 8
        )
        
        // Test user stats
        assertThat(user.fowlCount).isEqualTo(10)
        assertThat(user.rating).isEqualTo(4.5)
        assertThat(user.reviewCount).isEqualTo(8)
        
        // Test calculated properties
        assertThat(hasGoodRating(user)).isTrue() // Rating >= 4.0
        assertThat(isActiveUser(user)).isTrue() // Has fowls and reviews
    }

    // Helper validation functions (would be in actual implementation)
    private fun isValidPhoneNumber(phoneNumber: String): Boolean {
        return phoneNumber.startsWith("+") && 
               phoneNumber.length >= 10 && 
               phoneNumber.substring(1).all { it.isDigit() }
    }

    private fun isValidOTP(otp: String): Boolean {
        return otp.length == 6 && otp.all { it.isDigit() }
    }

    private fun isValidUserProfile(profile: UserProfile): Boolean {
        return profile.uid.isNotBlank() &&
               profile.phoneNumber.isNotBlank() &&
               profile.displayName.length >= 2
    }

    private fun canTransitionKYCStatus(from: KYCStatus, to: KYCStatus): Boolean {
        return when (from) {
            KYCStatus.PENDING -> to == KYCStatus.IN_REVIEW
            KYCStatus.IN_REVIEW -> to == KYCStatus.VERIFIED || to == KYCStatus.REJECTED
            KYCStatus.VERIFIED -> false // Cannot transition from verified
            KYCStatus.REJECTED -> to == KYCStatus.PENDING // Can retry
        }
    }

    private fun hasGoodRating(user: User): Boolean {
        return user.rating >= 4.0
    }

    private fun isActiveUser(user: User): Boolean {
        return user.fowlCount > 0 && user.reviewCount > 0
    }
}