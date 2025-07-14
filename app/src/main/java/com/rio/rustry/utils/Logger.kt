package com.rio.rustry.utils

import android.util.Log

/**
 * Logger utility for consistent logging across the app
 */
object Logger {
    
    fun d(tag: String, message: () -> String) {
        Log.d(tag, message())
    }
    
    fun i(tag: String, message: () -> String) {
        Log.i(tag, message())
    }
    
    fun w(tag: String, message: () -> String) {
        Log.w(tag, message())
    }
    
    fun e(tag: String, message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            Log.e(tag, message, throwable)
        } else {
            Log.e(tag, message)
        }
    }
}