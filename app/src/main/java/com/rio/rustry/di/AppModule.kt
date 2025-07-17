package com.rio.rustry.di

import android.content.Context
import androidx.room.Room
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DatabaseReference
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.functions.FirebaseFunctions
import com.rio.rustry.data.local.RustryDatabase
import com.rio.rustry.data.repository.*
import com.rio.rustry.domain.repository.*
import com.rio.rustry.domain.usecase.*
import com.rio.rustry.presentation.viewmodel.*
import com.rio.rustry.security.NetworkSecurityManager
import com.rio.rustry.utils.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * Complete Koin dependency injection module for modernized architecture
 * 
 * Features:
 * - Proper separation of concerns with domain/data layers
 * - Coroutine dispatcher injection for testability
 * - Comprehensive caching strategy
 * - Error handling patterns
 * - Performance monitoring integration
 */

val appModule = module {
    
    // Core Android dependencies
    single<Context> { androidContext() }
    
    // Coroutine dispatchers for proper threading
    single<CoroutineDispatcher>(named("IO")) { Dispatchers.IO }
    single<CoroutineDispatcher>(named("Main")) { Dispatchers.Main }
    single<CoroutineDispatcher>(named("Default")) { Dispatchers.Default }
    single<CoroutineDispatcher>(named("Unconfined")) { Dispatchers.Unconfined }
}

val firebaseModule = module {
    
    // Firebase Auth
    single<FirebaseAuth> { FirebaseAuth.getInstance() }
    
    // Firestore with optimized settings
    single<FirebaseFirestore> {
        val firestore = FirebaseFirestore.getInstance()
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
            .build()
        firestore.firestoreSettings = settings
        firestore
    }
    
    // Realtime Database with persistence
    single<DatabaseReference> {
        val database = FirebaseDatabase.getInstance()
        database.setPersistenceEnabled(true)
        database.setPersistenceCacheSizeBytes(10 * 1024 * 1024) // 10MB
        database.reference
    }
    
    // Other Firebase services
    single<FirebaseStorage> { FirebaseStorage.getInstance() }
    single<FirebaseAnalytics> { FirebaseAnalytics.getInstance(get()) }
    single<FirebaseCrashlytics> { FirebaseCrashlytics.getInstance() }
    single<FirebaseMessaging> { FirebaseMessaging.getInstance() }
    single<FirebaseFunctions> { FirebaseFunctions.getInstance() }
}

val databaseModule = module {
    
    // Room database
    single<RustryDatabase> {
        Room.databaseBuilder(
            get(),
            RustryDatabase::class.java,
            "rustry_database"
        )
        .fallbackToDestructiveMigration()
        .enableMultiInstanceInvalidation()
        .build()
    }
    
    // DAOs
    single { get<RustryDatabase>().fowlDao() }
    // Note: Other DAOs will be added as needed
}

val networkModule = module {
    
    // Network security manager
    single<NetworkSecurityManager> { NetworkSecurityManager(get()) }
    
    // Network manager with caching
    single<NetworkManager> { NetworkManager(get()) }
    
    // API clients would go here
}

val repositoryModule = module {
    
    // Repository implementations
    single<com.rio.rustry.domain.repository.FowlRepository> { 
        FowlRepositoryImpl(
            localFowlDao = get(),
            networkManager = get(),
            ioDispatcher = get(named("IO"))
        )
    }
    
    single<UserRepository> { 
        UserRepositoryImpl(
            firebaseAuth = get(),
            firestore = get(),
            localUserDao = get(),
            ioDispatcher = get(named("IO"))
        )
    }
    
    single<TransactionRepository> { 
        TransactionRepositoryImpl(
            firestore = get(),
            localTransactionDao = get(),
            ioDispatcher = get(named("IO"))
        )
    }
    
    single<BreedingRepository> { 
        BreedingRepositoryImpl(
            firestore = get(),
            realtimeDb = get(),
            localBreedingDao = get(),
            ioDispatcher = get(named("IO"))
        )
    }
    
    single<com.rio.rustry.domain.repository.HealthRepository> { 
        HealthRepositoryImpl(
            firestore = get(),
            localHealthDao = get(),
            ioDispatcher = get(named("IO"))
        )
    }
    
    // Firebase repository (legacy support)
    single<FirebaseFowlRepository> { FirebaseFowlRepository(get(), get()) }
}

val useCaseModule = module {
    
    // Fowl use cases
    factory<GetFowlsUseCase> { GetFowlsUseCase(get()) }
    factory<AddFowlUseCase> { AddFowlUseCase(get(), get(named("IO"))) }
    factory<UpdateFowlUseCase> { UpdateFowlUseCase(get(), get(named("IO"))) }
    factory<DeleteFowlUseCase> { DeleteFowlUseCase(get(), get(named("IO"))) }
    factory<SearchFowlsUseCase> { SearchFowlsUseCase(get()) }
    
    // User use cases
    factory<LoginUseCase> { LoginUseCase(get()) }
    factory<RegisterUseCase> { RegisterUseCase(get()) }
    factory<LogoutUseCase> { LogoutUseCase(get()) }
    factory<GetUserProfileUseCase> { GetUserProfileUseCase(get()) }
    factory<UpdateUserProfileUseCase> { UpdateUserProfileUseCase(get()) }
    
    // Transaction use cases
    factory<ProcessPaymentUseCase> { ProcessPaymentUseCase(get()) }
    factory<GetTransactionHistoryUseCase> { GetTransactionHistoryUseCase(get()) }
    factory<RefundTransactionUseCase> { RefundTransactionUseCase(get()) }
    
    // Breeding use cases
    factory<CreateBreedingRecordUseCase> { CreateBreedingRecordUseCase(get()) }
    factory<GetBreedingAnalyticsUseCase> { GetBreedingAnalyticsUseCase(get()) }
    factory<GenerateFamilyTreeUseCase> { GenerateFamilyTreeUseCase(get()) }
    
    // Health use cases
    // factory<AddHealthRecordUseCase> { AddHealthRecordUseCase(get()) }
    factory<GetHealthHistoryUseCase> { GetHealthHistoryUseCase(get()) }
    factory<ScheduleVaccinationUseCase> { ScheduleVaccinationUseCase(get()) }
}

val viewModelModule = module {
    
    // ViewModels with proper dependency injection
    // viewModel { FlockViewModel(get(), get(), get(), get(), get()) }
    viewModel { SalesViewModel(get()) }
    // viewModel { HealthViewModel(get()) }
    viewModel { SettingsViewModel(get()) }
    viewModel { TutorialViewModel(get()) }
    viewModel { InventoryViewModel(get()) }
    // viewModel { MarketplaceViewModel(get(), get()) }
    // viewModel { BreedingViewModel(get(), get(), get()) }
    // viewModel { TransferViewModel(get(), get()) }
    // viewModel { PromotionsViewModel(get()) }
    // viewModel { BreedingAnalyticsViewModel(get()) }
}

val utilityModule = module {
    
    // Utility classes
    single<MemoryManager> { MemoryManager(get()) }
    single<com.rio.rustry.security.SecurityManager> { com.rio.rustry.security.SecurityManager(get()) }
    single<DatabaseOptimizer> { DatabaseOptimizer() }
    single<OptimizedImageLoader> { OptimizedImageLoader(get()) }
    single<PerformanceMonitor> { PerformanceMonitor }
    single<FeatureFlagManager> { FeatureFlagManager() }
    
    // Cache managers
    // single<CacheManager> { CacheManager(get()) }
    // single<ImageCacheManager> { ImageCacheManager(get()) }
    // single<DataCacheManager> { DataCacheManager(get()) }
}

// Combine all modules
val allModules = listOf(
    appModule,
    firebaseModule,
    databaseModule,
    networkModule,
    repositoryModule,
    useCaseModule,
    viewModelModule,
    utilityModule
)