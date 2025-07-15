// generated/phase3/app/src/main/java/com/rio/rustry/analytics/AnalyticsService.kt

package com.rio.rustry.analytics

import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsService @Inject constructor() {

    private val firebaseAnalytics: FirebaseAnalytics = Firebase.analytics

    fun logScreenView(screenName: String, screenClass: String? = null) {
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            screenClass?.let { param(FirebaseAnalytics.Param.SCREEN_CLASS, it) }
        }
    }

    fun logTransferInitiated(fowlId: String, transferMethod: String) {
        firebaseAnalytics.logEvent("transfer_initiated") {
            param("fowl_id", fowlId)
            param("transfer_method", transferMethod)
            param("timestamp", System.currentTimeMillis())
        }
    }

    fun logTransferVerified(transferId: String, verificationMethod: String) {
        firebaseAnalytics.logEvent("transfer_verified") {
            param("transfer_id", transferId)
            param("verification_method", verificationMethod)
            param("timestamp", System.currentTimeMillis())
        }
    }

    fun logTreeExported(fowlId: String, exportFormat: String) {
        firebaseAnalytics.logEvent("tree_exported") {
            param("fowl_id", fowlId)
            param("export_format", exportFormat)
            param("timestamp", System.currentTimeMillis())
        }
    }

    fun logCouponApplied(couponCode: String, orderValue: Double) {
        firebaseAnalytics.logEvent("coupon_applied") {
            param("coupon_code", couponCode)
            param("order_value", orderValue)
            param("timestamp", System.currentTimeMillis())
        }
    }

    fun logListingBoosted(fowlId: String, boostDuration: String, boostCost: Double) {
        firebaseAnalytics.logEvent("listing_boosted") {
            param("fowl_id", fowlId)
            param("boost_duration", boostDuration)
            param("boost_cost", boostCost)
            param("timestamp", System.currentTimeMillis())
        }
    }

    fun logVaccinationScheduled(fowlId: String, vaccineName: String) {
        firebaseAnalytics.logEvent("vaccination_scheduled") {
            param("fowl_id", fowlId)
            param("vaccine_name", vaccineName)
            param("timestamp", System.currentTimeMillis())
        }
    }

    fun logVaccinationCompleted(fowlId: String, vaccineName: String) {
        firebaseAnalytics.logEvent("vaccination_completed") {
            param("fowl_id", fowlId)
            param("vaccine_name", vaccineName)
            param("timestamp", System.currentTimeMillis())
        }
    }

    fun logBreedingAnalyticsViewed(period: String) {
        firebaseAnalytics.logEvent("breeding_analytics_viewed") {
            param("period", period)
            param("timestamp", System.currentTimeMillis())
        }
    }

    fun logFamilyTreeViewed(fowlId: String) {
        firebaseAnalytics.logEvent("family_tree_viewed") {
            param("fowl_id", fowlId)
            param("timestamp", System.currentTimeMillis())
        }
    }

    fun logHealthSnapshotTaken(fowlId: String, riskScore: Double) {
        firebaseAnalytics.logEvent("health_snapshot_taken") {
            param("fowl_id", fowlId)
            param("risk_score", riskScore)
            param("timestamp", System.currentTimeMillis())
        }
    }

    fun logUserEngagement(action: String, screen: String, duration: Long) {
        firebaseAnalytics.logEvent("user_engagement") {
            param("action", action)
            param("screen", screen)
            param("duration_ms", duration)
            param("timestamp", System.currentTimeMillis())
        }
    }

    fun logError(errorType: String, errorMessage: String, screen: String) {
        firebaseAnalytics.logEvent("app_error") {
            param("error_type", errorType)
            param("error_message", errorMessage)
            param("screen", screen)
            param("timestamp", System.currentTimeMillis())
        }
    }

    fun setUserProperty(name: String, value: String) {
        firebaseAnalytics.setUserProperty(name, value)
    }

    fun setUserId(userId: String) {
        firebaseAnalytics.setUserId(userId)
    }
}