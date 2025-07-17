package com.rio.rustry.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.rio.rustry.domain.model.UserProfile
import com.rio.rustry.domain.model.UserType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Coin transaction model
 */
data class CoinTransaction(
    val id: String = "",
    val userId: String = "",
    val amount: Int = 0,
    val type: TransactionType = TransactionType.EARNED,
    val source: TransactionSource = TransactionSource.DAILY_LOGIN,
    val description: String = "",
    val relatedEntityId: String = "", // product ID, order ID, etc.
    val relatedEntityType: String = "", // "product", "order", "post", etc.
    val timestamp: Long = System.currentTimeMillis(),
    val balanceAfter: Int = 0,
    val metadata: Map<String, String> = emptyMap()
)

/**
 * Coin package for purchase
 */
data class CoinPackage(
    val id: String = "",
    val name: String = "",
    val coinAmount: Int = 0,
    val price: Double = 0.0,
    val currency: String = "INR",
    val bonusCoins: Int = 0,
    val isPopular: Boolean = false,
    val isActive: Boolean = true,
    val description: String = "",
    val validityDays: Int = 0, // 0 means no expiry
    val createdAt: Long = System.currentTimeMillis()
)

enum class TransactionType {
    EARNED,     // User earned coins
    SPENT,      // User spent coins
    PURCHASED,  // User purchased coins
    REFUNDED,   // Coins refunded
    EXPIRED,    // Coins expired
    BONUS,      // Bonus coins
    PENALTY     // Penalty deduction
}

enum class TransactionSource {
    DAILY_LOGIN,
    PRODUCT_LISTING,
    PRODUCT_SALE,
    SOCIAL_POST,
    SOCIAL_LIKE,
    SOCIAL_COMMENT,
    SOCIAL_SHARE,
    PROFILE_COMPLETION,
    VERIFICATION,
    REFERRAL,
    REVIEW_GIVEN,
    REVIEW_RECEIVED,
    CHAT_ENGAGEMENT,
    PREMIUM_FEATURE,
    BOOST_LISTING,
    FEATURED_POST,
    PURCHASE,
    REFUND,
    ADMIN_ADJUSTMENT,
    CONTEST_REWARD,
    ACHIEVEMENT_UNLOCK
}

/**
 * Service interface for monetization and coin system
 */
interface MonetizationService {
    // Coin balance operations
    suspend fun getUserCoinBalance(userId: String): Result<Int>
    suspend fun addCoins(userId: String, amount: Int, source: TransactionSource, description: String, relatedEntityId: String = ""): Result<Unit>
    suspend fun deductCoins(userId: String, amount: Int, source: TransactionSource, description: String, relatedEntityId: String = ""): Result<Boolean>
    suspend fun transferCoins(fromUserId: String, toUserId: String, amount: Int, description: String): Result<Unit>
    
    // Transaction history
    suspend fun getCoinTransactions(userId: String, limit: Int = 50): Flow<List<CoinTransaction>>
    suspend fun getCoinTransactionsByType(userId: String, type: TransactionType): Flow<List<CoinTransaction>>
    
    // Coin packages
    suspend fun getCoinPackages(): Flow<List<CoinPackage>>
    suspend fun purchaseCoinPackage(userId: String, packageId: String, paymentMethod: String): Result<String>
    suspend fun processCoinPurchase(userId: String, packageId: String, transactionId: String): Result<Unit>
    
    // Premium features
    suspend fun checkPremiumFeatureAccess(userId: String, featureName: String): Result<Boolean>
    suspend fun purchasePremiumFeature(userId: String, featureName: String, coinCost: Int): Result<Unit>
    suspend fun boostProductListing(userId: String, productId: String, boostType: String, duration: Int): Result<Unit>
    suspend fun promotePost(userId: String, postId: String, promotionType: String, duration: Int): Result<Unit>
    
    // Analytics and insights
    suspend fun getCoinAnalytics(userId: String): Result<Map<String, Any>>
    suspend fun getEarningInsights(userId: String): Result<Map<String, Any>>
    suspend fun getSpendingInsights(userId: String): Result<Map<String, Any>>
    
    // Referral system
    suspend fun generateReferralCode(userId: String): Result<String>
    suspend fun processReferral(referrerUserId: String, referredUserId: String): Result<Unit>
    suspend fun getReferralStats(userId: String): Result<Map<String, Any>>
    
    // Admin operations
    suspend fun adjustUserCoins(userId: String, amount: Int, reason: String): Result<Unit>
    suspend fun getSystemCoinStats(): Result<Map<String, Any>>
}

/**
 * Implementation of MonetizationService using Firebase Firestore
 */
@Singleton
class MonetizationServiceImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : MonetizationService {

    companion object {
        private const val COIN_TRANSACTIONS_COLLECTION = "coin_transactions"
        private const val COIN_PACKAGES_COLLECTION = "coin_packages"
        private const val PREMIUM_FEATURES_COLLECTION = "premium_features"
        private const val REFERRALS_COLLECTION = "referrals"
        private const val USER_PROFILES_COLLECTION = "user_profiles"
        
        // Coin costs for premium features
        private const val BOOST_LISTING_COST = 50
        private const val FEATURED_POST_COST = 30
        private const val PREMIUM_CHAT_COST = 20
        
        // Earning rates
        private const val DAILY_LOGIN_REWARD = 10
        private const val PRODUCT_LISTING_REWARD = 25
        private const val SOCIAL_POST_REWARD = 15
        private const val SOCIAL_LIKE_REWARD = 2
        private const val SOCIAL_COMMENT_REWARD = 5
        private const val REFERRAL_REWARD = 100
        private const val VERIFICATION_REWARD = 200
    }

    override suspend fun getUserCoinBalance(userId: String): Result<Int> {
        return try {
            val document = firestore.collection(USER_PROFILES_COLLECTION)
                .document(userId)
                .get()
                .await()
            val coins = document.getLong("coins")?.toInt() ?: 0
            Result.Success(coins)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun addCoins(userId: String, amount: Int, source: TransactionSource, description: String, relatedEntityId: String): Result<Unit> {
        return try {
            val currentBalance = getUserCoinBalance(userId).getOrNull() ?: 0
            val newBalance = currentBalance + amount
            
            // Update user profile
            firestore.collection(USER_PROFILES_COLLECTION)
                .document(userId)
                .update(
                    mapOf(
                        "coins" to newBalance,
                        "totalCoinsEarned" to com.google.firebase.firestore.FieldValue.increment(amount.toLong())
                    )
                )
                .await()
            
            // Record transaction
            val transaction = CoinTransaction(
                userId = userId,
                amount = amount,
                type = TransactionType.EARNED,
                source = source,
                description = description,
                relatedEntityId = relatedEntityId,
                balanceAfter = newBalance
            )
            
            val docRef = firestore.collection(COIN_TRANSACTIONS_COLLECTION).document()
            docRef.set(transaction.copy(id = docRef.id)).await()
            
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun deductCoins(userId: String, amount: Int, source: TransactionSource, description: String, relatedEntityId: String): Result<Boolean> {
        return try {
            val currentBalance = getUserCoinBalance(userId).getOrNull() ?: 0
            
            if (currentBalance < amount) {
                Result.Success(false) // Insufficient balance
            } else {
                val newBalance = currentBalance - amount
                
                // Update user profile
                firestore.collection(USER_PROFILES_COLLECTION)
                    .document(userId)
                    .update(
                        mapOf(
                            "coins" to newBalance,
                            "totalCoinsSpent" to com.google.firebase.firestore.FieldValue.increment(amount.toLong())
                        )
                    )
                    .await()
                
                // Record transaction
                val transaction = CoinTransaction(
                    userId = userId,
                    amount = amount,
                    type = TransactionType.SPENT,
                    source = source,
                    description = description,
                    relatedEntityId = relatedEntityId,
                    balanceAfter = newBalance
                )
                
                val docRef = firestore.collection(COIN_TRANSACTIONS_COLLECTION).document()
                docRef.set(transaction.copy(id = docRef.id)).await()
                
                Result.Success(true)
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun transferCoins(fromUserId: String, toUserId: String, amount: Int, description: String): Result<Unit> {
        return try {
            val deductResult = deductCoins(fromUserId, amount, TransactionSource.ADMIN_ADJUSTMENT, "Transfer to $toUserId: $description")
            
            if (deductResult.getOrNull() == true) {
                addCoins(toUserId, amount, TransactionSource.ADMIN_ADJUSTMENT, "Transfer from $fromUserId: $description")
                Result.Success(Unit)
            } else {
                Result.Error(Exception("Insufficient balance for transfer"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getCoinTransactions(userId: String, limit: Int): Flow<List<CoinTransaction>> = flow {
        try {
            val snapshot = firestore.collection(COIN_TRANSACTIONS_COLLECTION)
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()
            val transactions = snapshot.toObjects(CoinTransaction::class.java)
            emit(transactions)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun getCoinTransactionsByType(userId: String, type: TransactionType): Flow<List<CoinTransaction>> = flow {
        try {
            val snapshot = firestore.collection(COIN_TRANSACTIONS_COLLECTION)
                .whereEqualTo("userId", userId)
                .whereEqualTo("type", type.name)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()
            val transactions = snapshot.toObjects(CoinTransaction::class.java)
            emit(transactions)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun getCoinPackages(): Flow<List<CoinPackage>> = flow {
        try {
            val snapshot = firestore.collection(COIN_PACKAGES_COLLECTION)
                .whereEqualTo("isActive", true)
                .orderBy("price", Query.Direction.ASCENDING)
                .get()
                .await()
            val packages = snapshot.toObjects(CoinPackage::class.java)
            emit(packages)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun purchaseCoinPackage(userId: String, packageId: String, paymentMethod: String): Result<String> {
        return try {
            // In real implementation, integrate with payment gateway
            val transactionId = "TXN_${System.currentTimeMillis()}_${userId}"
            
            // For demo, auto-approve the purchase
            processCoinPurchase(userId, packageId, transactionId)
            
            Result.Success(transactionId)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun processCoinPurchase(userId: String, packageId: String, transactionId: String): Result<Unit> {
        return try {
            val packageDoc = firestore.collection(COIN_PACKAGES_COLLECTION)
                .document(packageId)
                .get()
                .await()
            
            val coinPackage = packageDoc.toObject(CoinPackage::class.java)
            
            if (coinPackage != null) {
                val totalCoins = coinPackage.coinAmount + coinPackage.bonusCoins
                addCoins(
                    userId = userId,
                    amount = totalCoins,
                    source = TransactionSource.PURCHASE,
                    description = "Purchased ${coinPackage.name}",
                    relatedEntityId = transactionId
                )
                Result.Success(Unit)
            } else {
                Result.Error(Exception("Coin package not found"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun checkPremiumFeatureAccess(userId: String, featureName: String): Result<Boolean> {
        return try {
            val document = firestore.collection(PREMIUM_FEATURES_COLLECTION)
                .document("${userId}_${featureName}")
                .get()
                .await()
            
            if (document.exists()) {
                val expiryTime = document.getLong("expiryTime") ?: 0L
                val hasAccess = expiryTime > System.currentTimeMillis()
                Result.Success(hasAccess)
            } else {
                Result.Success(false)
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun purchasePremiumFeature(userId: String, featureName: String, coinCost: Int): Result<Unit> {
        return try {
            val deductResult = deductCoins(
                userId = userId,
                amount = coinCost,
                source = TransactionSource.PREMIUM_FEATURE,
                description = "Purchased premium feature: $featureName"
            )
            
            if (deductResult.getOrNull() == true) {
                val expiryTime = System.currentTimeMillis() + (30 * 24 * 60 * 60 * 1000L) // 30 days
                
                firestore.collection(PREMIUM_FEATURES_COLLECTION)
                    .document("${userId}_${featureName}")
                    .set(
                        mapOf(
                            "userId" to userId,
                            "featureName" to featureName,
                            "purchasedAt" to System.currentTimeMillis(),
                            "expiryTime" to expiryTime
                        )
                    )
                    .await()
                
                Result.Success(Unit)
            } else {
                Result.Error(Exception("Insufficient coins"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun boostProductListing(userId: String, productId: String, boostType: String, duration: Int): Result<Unit> {
        return try {
            val coinCost = BOOST_LISTING_COST * duration
            
            val deductResult = deductCoins(
                userId = userId,
                amount = coinCost,
                source = TransactionSource.BOOST_LISTING,
                description = "Boosted product listing: $productId",
                relatedEntityId = productId
            )
            
            if (deductResult.getOrNull() == true) {
                Result.Success(Unit)
            } else {
                Result.Error(Exception("Insufficient coins"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun promotePost(userId: String, postId: String, promotionType: String, duration: Int): Result<Unit> {
        return try {
            val coinCost = FEATURED_POST_COST * duration
            
            val deductResult = deductCoins(
                userId = userId,
                amount = coinCost,
                source = TransactionSource.FEATURED_POST,
                description = "Promoted social post: $postId",
                relatedEntityId = postId
            )
            
            if (deductResult.getOrNull() == true) {
                Result.Success(Unit)
            } else {
                Result.Error(Exception("Insufficient coins"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getCoinAnalytics(userId: String): Result<Map<String, Any>> {
        return try {
            val userProfile = firestore.collection(USER_PROFILES_COLLECTION)
                .document(userId)
                .get()
                .await()
            
            val analytics = mapOf(
                "currentBalance" to (userProfile.getLong("coins") ?: 0L),
                "totalEarned" to (userProfile.getLong("totalCoinsEarned") ?: 0L),
                "totalSpent" to (userProfile.getLong("totalCoinsSpent") ?: 0L)
            )
            
            Result.Success(analytics)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getEarningInsights(userId: String): Result<Map<String, Any>> {
        return try {
            val earnedTransactions = firestore.collection(COIN_TRANSACTIONS_COLLECTION)
                .whereEqualTo("userId", userId)
                .whereEqualTo("type", TransactionType.EARNED.name)
                .get()
                .await()
            
            val insights = mapOf(
                "totalEarned" to earnedTransactions.documents.sumOf { it.getLong("amount") ?: 0L },
                "earningTransactions" to earnedTransactions.size()
            )
            
            Result.Success(insights)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getSpendingInsights(userId: String): Result<Map<String, Any>> {
        return try {
            val spentTransactions = firestore.collection(COIN_TRANSACTIONS_COLLECTION)
                .whereEqualTo("userId", userId)
                .whereEqualTo("type", TransactionType.SPENT.name)
                .get()
                .await()
            
            val insights = mapOf(
                "totalSpent" to spentTransactions.documents.sumOf { it.getLong("amount") ?: 0L },
                "spendingTransactions" to spentTransactions.size()
            )
            
            Result.Success(insights)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun generateReferralCode(userId: String): Result<String> {
        return try {
            val referralCode = "RUSTRY${userId.takeLast(6).uppercase()}"
            
            firestore.collection(REFERRALS_COLLECTION)
                .document(userId)
                .set(
                    mapOf(
                        "userId" to userId,
                        "referralCode" to referralCode,
                        "createdAt" to System.currentTimeMillis(),
                        "totalReferrals" to 0,
                        "totalRewards" to 0
                    )
                )
                .await()
            
            Result.Success(referralCode)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun processReferral(referrerUserId: String, referredUserId: String): Result<Unit> {
        return try {
            // Award coins to referrer
            addCoins(
                userId = referrerUserId,
                amount = REFERRAL_REWARD,
                source = TransactionSource.REFERRAL,
                description = "Referral reward for inviting user: $referredUserId",
                relatedEntityId = referredUserId
            )
            
            // Award welcome bonus to referred user
            addCoins(
                userId = referredUserId,
                amount = REFERRAL_REWARD / 2,
                source = TransactionSource.REFERRAL,
                description = "Welcome bonus from referral",
                relatedEntityId = referrerUserId
            )
            
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getReferralStats(userId: String): Result<Map<String, Any>> {
        return try {
            val document = firestore.collection(REFERRALS_COLLECTION)
                .document(userId)
                .get()
                .await()
            
            val stats = mapOf(
                "referralCode" to (document.getString("referralCode") ?: ""),
                "totalReferrals" to (document.getLong("totalReferrals") ?: 0L),
                "totalRewards" to (document.getLong("totalRewards") ?: 0L)
            )
            
            Result.Success(stats)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun adjustUserCoins(userId: String, amount: Int, reason: String): Result<Unit> {
        return try {
            if (amount > 0) {
                addCoins(
                    userId = userId,
                    amount = amount,
                    source = TransactionSource.ADMIN_ADJUSTMENT,
                    description = "Admin adjustment: $reason"
                )
            } else {
                deductCoins(
                    userId = userId,
                    amount = -amount,
                    source = TransactionSource.ADMIN_ADJUSTMENT,
                    description = "Admin adjustment: $reason"
                )
            }
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getSystemCoinStats(): Result<Map<String, Any>> {
        return try {
            val allTransactions = firestore.collection(COIN_TRANSACTIONS_COLLECTION)
                .get()
                .await()
            
            val stats = mapOf(
                "totalTransactions" to allTransactions.size(),
                "totalCoinsEarned" to allTransactions.documents
                    .filter { it.getString("type") == TransactionType.EARNED.name }
                    .sumOf { it.getLong("amount") ?: 0L },
                "totalCoinsSpent" to allTransactions.documents
                    .filter { it.getString("type") == TransactionType.SPENT.name }
                    .sumOf { it.getLong("amount") ?: 0L }
            )
            
            Result.Success(stats)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}