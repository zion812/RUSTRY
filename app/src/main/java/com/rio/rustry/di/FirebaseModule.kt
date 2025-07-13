package com.rio.rustry.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

/**
 * Manual Firebase dependency provider
 */
object FirebaseModule {
    
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()
    
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()
    
    fun provideFirebaseStorage(): FirebaseStorage = FirebaseStorage.getInstance()
}