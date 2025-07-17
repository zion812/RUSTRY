package com.rio.rustry.test

import com.rio.rustry.data.local.dao.*
import com.rio.rustry.presentation.viewmodel.*
import com.rio.rustry.utils.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.database.DatabaseReference
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.mockito.Mockito

/**
 * Koin test module for unit testing with mocked dependencies
 */
val testModule = module {
    
    // Test dispatchers
    single<TestDispatcher> { UnconfinedTestDispatcher() }
    single<CoroutineDispatcher>(named("IO")) { get<TestDispatcher>() }
    single<CoroutineDispatcher>(named("Main")) { get<TestDispatcher>() }
    single<CoroutineDispatcher>(named("Default")) { get<TestDispatcher>() }
    
    // Mock Firebase services
    single<FirebaseAuth> { Mockito.mock(FirebaseAuth::class.java) }
    single<FirebaseFirestore> { Mockito.mock(FirebaseFirestore::class.java) }
    single<DatabaseReference> { Mockito.mock(DatabaseReference::class.java) }
    
    // Mock DAOs
    single<FowlDao> { Mockito.mock(FowlDao::class.java) }
    single<UserDao> { Mockito.mock(UserDao::class.java) }
    single<TransactionDao> { Mockito.mock(TransactionDao::class.java) }
    single<BreedingDao> { Mockito.mock(BreedingDao::class.java) }
    single<HealthDao> { Mockito.mock(HealthDao::class.java) }
    
    // Mock basic services (simplified for testing)
    single { "mock_repository" }
    single { "mock_use_case" }
    
    // Mock utilities
    single<NetworkManager> { Mockito.mock(NetworkManager::class.java) }
    single<ErrorHandler> { Mockito.mock(ErrorHandler::class.java) }
    single<ImageCacheManager> { Mockito.mock(ImageCacheManager::class.java) }
    
    // ViewModels (simplified for testing)
    factory { FarmViewModel() }
}

/**
 * Test utilities for Koin testing
 */
object KoinTestUtils {
    
    /**
     * Create a test module with specific mocks
     */
    fun createTestModuleWith(vararg mocks: Pair<Class<*>, Any>) = module {
        mocks.forEach { (clazz, mock) ->
            single { mock }
        }
    }
    
    /**
     * Mock a repository with default behavior
     */
    inline fun <reified T : Any> mockRepository(): T {
        return Mockito.mock(T::class.java)
    }
    
    /**
     * Mock a use case with default behavior
     */
    inline fun <reified T : Any> mockUseCase(): T {
        return Mockito.mock(T::class.java)
    }
}