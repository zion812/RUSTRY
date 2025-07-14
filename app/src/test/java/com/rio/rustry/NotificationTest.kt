package com.rio.rustry

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.rio.rustry.features.notifications.*
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Notification system tests
 * Tests push notifications, price alerts, and engagement campaigns
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class NotificationTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun testNotificationStateInitialization() {
        val notificationState = NotificationState()
        
        assertThat(notificationState.isInitializing).isFalse()
        assertThat(notificationState.isEnabled).isFalse()
        assertThat(notificationState.fcmToken).isNull()
        assertThat(notificationState.unreadCount).isEqualTo(0)
        assertThat(notificationState.error).isNull()
        assertThat(notificationState.preferences).isNotNull()
    }

    @Test
    fun testNotificationResultTypes() {
        val successResult = NotificationResult.Success("Operation successful")
        val errorResult = NotificationResult.Error("Operation failed")
        
        assertThat(successResult).isInstanceOf(NotificationResult.Success::class.java)
        assertThat(errorResult).isInstanceOf(NotificationResult.Error::class.java)
        
        when (successResult) {
            is NotificationResult.Success -> assertThat(successResult.message).isEqualTo("Operation successful")
            is NotificationResult.Error -> throw AssertionError("Should be success")
        }
        
        when (errorResult) {
            is NotificationResult.Success -> throw AssertionError("Should be error")
            is NotificationResult.Error -> assertThat(errorResult.message).isEqualTo("Operation failed")
        }
    }

    @Test
    fun testPriceAlertCreation() {
        val alert = PriceAlert(
            id = "alert_123",
            userId = "user_456",
            fowlId = "fowl_789",
            currentPrice = 150.0,
            alertThreshold = 120.0,
            isActive = true,
            createdAt = System.currentTimeMillis()
        )
        
        assertThat(alert.id).isEqualTo("alert_123")
        assertThat(alert.userId).isEqualTo("user_456")
        assertThat(alert.fowlId).isEqualTo("fowl_789")
        assertThat(alert.currentPrice).isEqualTo(150.0)
        assertThat(alert.alertThreshold).isEqualTo(120.0)
        assertThat(alert.isActive).isTrue()
        assertThat(alert.triggeredAt).isNull()
    }

    @Test
    fun testBidNotificationCreation() {
        val notification = BidNotification(
            id = "bid_123",
            sellerId = "seller_456",
            buyerId = "buyer_789",
            fowlId = "fowl_101",
            bidAmount = 200.0,
            fowlName = "Rhode Island Red",
            timestamp = System.currentTimeMillis(),
            isRead = false
        )
        
        assertThat(notification.id).isEqualTo("bid_123")
        assertThat(notification.sellerId).isEqualTo("seller_456")
        assertThat(notification.buyerId).isEqualTo("buyer_789")
        assertThat(notification.fowlId).isEqualTo("fowl_101")
        assertThat(notification.bidAmount).isEqualTo(200.0)
        assertThat(notification.fowlName).isEqualTo("Rhode Island Red")
        assertThat(notification.isRead).isFalse()
        assertThat(notification.readAt).isNull()
    }

    @Test
    fun testNotificationTemplates() {
        // Test price drop alert template
        val priceDropAlert = NotificationTemplates.priceDropAlert("Rhode Island Red", 150.0, 120.0)
        assertThat(priceDropAlert.title).isEqualTo("Price Drop Alert! 📉")
        assertThat(priceDropAlert.body).contains("Rhode Island Red")
        assertThat(priceDropAlert.body).contains("150.0")
        assertThat(priceDropAlert.body).contains("120.0")
        assertThat(priceDropAlert.data["type"]).isEqualTo("price_alert")
        
        // Test new bid template
        val newBid = NotificationTemplates.newBidReceived("Leghorn", 200.0)
        assertThat(newBid.title).isEqualTo("New Bid Received! 💰")
        assertThat(newBid.body).contains("Leghorn")
        assertThat(newBid.body).contains("200.0")
        assertThat(newBid.data["type"]).isEqualTo("bid_update")
        
        // Test bid accepted template
        val bidAccepted = NotificationTemplates.bidAccepted("Silkie")
        assertThat(bidAccepted.title).isEqualTo("Bid Accepted! 🎉")
        assertThat(bidAccepted.body).contains("Silkie")
        assertThat(bidAccepted.data["type"]).isEqualTo("bid_update")
        assertThat(bidAccepted.data["status"]).isEqualTo("accepted")
        
        // Test inactive user engagement
        val inactiveUser = NotificationTemplates.inactiveUserEngagement()
        assertThat(inactiveUser.title).isEqualTo("We miss you! 🐔")
        assertThat(inactiveUser.data["type"]).isEqualTo("engagement")
        assertThat(inactiveUser.data["campaign"]).isEqualTo("inactive_user")
        
        // Test new listings alert
        val newListings = NotificationTemplates.newListingsAlert(5)
        assertThat(newListings.title).isEqualTo("New Fowls Available! ✨")
        assertThat(newListings.body).contains("5")
        assertThat(newListings.data["count"]).isEqualTo("5")
    }

    @Test
    fun testNotificationPreferencesDefaults() {
        val preferences = NotificationPreferences()
        
        assertThat(preferences.priceAlerts).isTrue()
        assertThat(preferences.bidUpdates).isTrue()
        assertThat(preferences.engagementCampaigns).isTrue()
        assertThat(preferences.generalUpdates).isTrue()
        assertThat(preferences.emailNotifications).isFalse()
        assertThat(preferences.quietHoursEnabled).isFalse()
        assertThat(preferences.quietHoursStart).isEqualTo("22:00")
        assertThat(preferences.quietHoursEnd).isEqualTo("08:00")
        assertThat(preferences.soundEnabled).isTrue()
        assertThat(preferences.vibrationEnabled).isTrue()
    }

    @Test
    fun testEngagementCampaignCreation() {
        val campaign = EngagementCampaign(
            id = "campaign_123",
            title = "Welcome Back!",
            message = "Check out new fowls in the marketplace",
            type = CampaignType.INACTIVE_USER,
            action = "browse_marketplace",
            targetAudience = "inactive_users",
            isActive = true,
            startDate = System.currentTimeMillis(),
            endDate = System.currentTimeMillis() + 86400000, // 24 hours
            createdAt = System.currentTimeMillis()
        )
        
        assertThat(campaign.id).isEqualTo("campaign_123")
        assertThat(campaign.title).isEqualTo("Welcome Back!")
        assertThat(campaign.message).isEqualTo("Check out new fowls in the marketplace")
        assertThat(campaign.type).isEqualTo(CampaignType.INACTIVE_USER)
        assertThat(campaign.action).isEqualTo("browse_marketplace")
        assertThat(campaign.targetAudience).isEqualTo("inactive_users")
        assertThat(campaign.isActive).isTrue()
    }

    @Test
    fun testNotificationTypes() {
        val types = NotificationType.values()
        
        assertThat(types).hasLength(6)
        assertThat(types).contains(NotificationType.PRICE_ALERT)
        assertThat(types).contains(NotificationType.BID_UPDATE)
        assertThat(types).contains(NotificationType.ENGAGEMENT)
        assertThat(types).contains(NotificationType.GENERAL)
        assertThat(types).contains(NotificationType.SYSTEM)
        assertThat(types).contains(NotificationType.PROMOTION)
    }

    @Test
    fun testCampaignTypes() {
        val types = CampaignType.values()
        
        assertThat(types).hasLength(5)
        assertThat(types).contains(CampaignType.INACTIVE_USER)
        assertThat(types).contains(CampaignType.NEW_LISTINGS)
        assertThat(types).contains(CampaignType.PRICE_DROPS)
        assertThat(types).contains(CampaignType.SEASONAL)
        assertThat(types).contains(CampaignType.GENERAL)
    }

    @Test
    fun testPriceAlertThresholdLogic() {
        val alert = PriceAlert(
            id = "alert_123",
            userId = "user_456",
            fowlId = "fowl_789",
            currentPrice = 150.0,
            alertThreshold = 120.0,
            isActive = true,
            createdAt = System.currentTimeMillis()
        )
        
        // Test if price drop should trigger alert
        val newPrice = 115.0
        val shouldTrigger = newPrice <= alert.alertThreshold && alert.isActive
        assertThat(shouldTrigger).isTrue()
        
        // Test if price above threshold should not trigger
        val higherPrice = 125.0
        val shouldNotTrigger = higherPrice <= alert.alertThreshold && alert.isActive
        assertThat(shouldNotTrigger).isFalse()
    }

    @Test
    fun testQuietHoursLogic() {
        val preferences = NotificationPreferences(
            quietHoursEnabled = true,
            quietHoursStart = "22:00",
            quietHoursEnd = "08:00"
        )
        
        assertThat(preferences.quietHoursEnabled).isTrue()
        assertThat(preferences.quietHoursStart).isEqualTo("22:00")
        assertThat(preferences.quietHoursEnd).isEqualTo("08:00")
        
        // Test quiet hours validation
        assertThat(isValidTimeFormat(preferences.quietHoursStart)).isTrue()
        assertThat(isValidTimeFormat(preferences.quietHoursEnd)).isTrue()
        assertThat(isValidTimeFormat("25:00")).isFalse()
        assertThat(isValidTimeFormat("12:60")).isFalse()
    }

    @Test
    fun testNotificationStateTransitions() {
        var state = NotificationState()
        
        // Test initialization
        state = state.copy(isInitializing = true)
        assertThat(state.isInitializing).isTrue()
        assertThat(state.isEnabled).isFalse()
        
        // Test enabled state
        state = state.copy(isInitializing = false, isEnabled = true, fcmToken = "test_token")
        assertThat(state.isInitializing).isFalse()
        assertThat(state.isEnabled).isTrue()
        assertThat(state.fcmToken).isEqualTo("test_token")
        
        // Test error state
        state = state.copy(error = "Test error")
        assertThat(state.error).isEqualTo("Test error")
        
        // Test error clearing
        state = state.copy(error = null)
        assertThat(state.error).isNull()
    }

    // Helper functions for testing
    private fun isValidTimeFormat(time: String): Boolean {
        return try {
            val parts = time.split(":")
            if (parts.size != 2) return false
            val hour = parts[0].toInt()
            val minute = parts[1].toInt()
            hour in 0..23 && minute in 0..59
        } catch (e: Exception) {
            false
        }
    }
}