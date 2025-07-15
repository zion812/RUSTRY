// generated/phase2/app/src/main/java/com/rio/rustry/di/Phase2Module.kt

package com.rio.rustry.di

import android.content.Context
import androidx.room.Room
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.storage.FirebaseStorage
import com.rio.rustry.data.local.dao.CartDao
import com.rio.rustry.data.local.dao.ChatMessageDao
import com.rio.rustry.data.local.dao.FowlDao
import com.rio.rustry.data.local.database.RustryDatabase
import com.rio.rustry.health.EnhancedAIHealthService
import com.rio.rustry.listing.ListingRepository
import com.rio.rustry.marketplace.MarketplaceRepository
import com.rio.rustry.orders.OrderRepository
import com.rio.rustry.social.SocialRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object Phase2Module {

    @Provides
    @Singleton
    fun provideRustryDatabase(@ApplicationContext context: Context): RustryDatabase {
        return Room.databaseBuilder(
            context,
            RustryDatabase::class.java,
            "rustry_database"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    fun provideFowlDao(database: RustryDatabase): FowlDao {
        return database.fowlDao()
    }

    @Provides
    fun provideChatMessageDao(database: RustryDatabase): ChatMessageDao {
        return database.chatMessageDao()
    }

    @Provides
    fun provideCartDao(database: RustryDatabase): CartDao {
        return database.cartDao()
    }

    @Provides
    @Singleton
    fun provideMarketplaceRepository(
        firestore: FirebaseFirestore,
        fowlDao: FowlDao
    ): MarketplaceRepository {
        return MarketplaceRepository(firestore, fowlDao)
    }

    @Provides
    @Singleton
    fun provideListingRepository(
        firestore: FirebaseFirestore,
        storage: FirebaseStorage,
        auth: FirebaseAuth
    ): ListingRepository {
        return ListingRepository(firestore, storage, auth)
    }

    @Provides
    @Singleton
    fun provideSocialRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth,
        chatMessageDao: ChatMessageDao
    ): SocialRepository {
        return SocialRepository(firestore, auth, chatMessageDao)
    }

    @Provides
    @Singleton
    fun provideOrderRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth,
        functions: FirebaseFunctions,
        cartDao: CartDao
    ): OrderRepository {
        return OrderRepository(firestore, auth, functions, cartDao)
    }

    @Provides
    @Singleton
    fun provideEnhancedAIHealthService(
        @ApplicationContext context: Context
    ): EnhancedAIHealthService {
        return EnhancedAIHealthService(context)
    }
}