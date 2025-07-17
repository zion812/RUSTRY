package com.rio.rustry.utils

import android.content.Context
import android.graphics.Bitmap
import android.util.LruCache
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import java.io.FileOutputStream
import android.util.Log

/**
 * Specialized cache manager for images with memory and disk caching
 */
class ImageCacheManager(private val context: Context) {
    
    companion object {
        private const val TAG = "ImageCacheManager"
        private const val CACHE_DIR = "image_cache"
        private const val MEMORY_CACHE_SIZE = 20 * 1024 * 1024 // 20MB
        private const val DISK_CACHE_SIZE = 100 * 1024 * 1024L // 100MB
    }
    
    private val cacheMutex = Mutex()
    
    // Memory cache for bitmaps
    private val memoryCache = object : LruCache<String, Bitmap>(MEMORY_CACHE_SIZE) {
        override fun sizeOf(key: String, bitmap: Bitmap): Int {
            return bitmap.byteCount
        }
    }
    
    // Disk cache directory
    private val diskCacheDir = File(context.cacheDir, CACHE_DIR).apply {
        if (!exists()) mkdirs()
    }
    
    /**
     * Get bitmap from cache
     */
    suspend fun getBitmap(key: String): Bitmap? {
        return cacheMutex.withLock {
            try {
                // Try memory cache first
                var bitmap = memoryCache.get(key)
                
                if (bitmap == null) {
                    // Try disk cache
                    val diskFile = File(diskCacheDir, key.hashCode().toString())
                    if (diskFile.exists()) {
                        bitmap = android.graphics.BitmapFactory.decodeFile(diskFile.absolutePath)
                        if (bitmap != null) {
                            // Put back in memory cache
                            memoryCache.put(key, bitmap)
                        }
                    }
                }
                
                bitmap
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get bitmap for key: $key", e)
                null
            }
        }
    }
    
    /**
     * Put bitmap in cache
     */
    suspend fun putBitmap(key: String, bitmap: Bitmap) {
        cacheMutex.withLock {
            try {
                // Store in memory cache
                memoryCache.put(key, bitmap)
                
                // Store in disk cache
                val diskFile = File(diskCacheDir, key.hashCode().toString())
                FileOutputStream(diskFile).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 90, out)
                }
                
                Log.d(TAG, "Cached bitmap for key: $key")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to cache bitmap for key: $key", e)
            }
        }
    }
    
    /**
     * Remove bitmap from cache
     */
    suspend fun removeBitmap(key: String) {
        cacheMutex.withLock {
            try {
                // Remove from memory cache
                memoryCache.remove(key)
                
                // Remove from disk cache
                val diskFile = File(diskCacheDir, key.hashCode().toString())
                if (diskFile.exists()) {
                    diskFile.delete()
                }
                
                Log.d(TAG, "Removed bitmap for key: $key")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to remove bitmap for key: $key", e)
            }
        }
    }
    
    /**
     * Clear all cached images
     */
    suspend fun clearAll() {
        cacheMutex.withLock {
            try {
                // Clear memory cache
                memoryCache.evictAll()
                
                // Clear disk cache
                diskCacheDir.listFiles()?.forEach { file ->
                    file.delete()
                }
                
                Log.d(TAG, "Cleared all image cache")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to clear image cache", e)
            }
        }
    }
    
    /**
     * Get cache size in bytes
     */
    suspend fun getCacheSize(): Long {
        return cacheMutex.withLock {
            try {
                diskCacheDir.walkTopDown()
                    .filter { it.isFile }
                    .map { it.length() }
                    .sum()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to calculate cache size", e)
                0L
            }
        }
    }
    
    /**
     * Cleanup cache if it exceeds size limit
     */
    suspend fun cleanupIfNeeded() {
        val currentSize = getCacheSize()
        
        if (currentSize > DISK_CACHE_SIZE) {
            Log.d(TAG, "Image cache size ($currentSize bytes) exceeds limit ($DISK_CACHE_SIZE bytes), cleaning up")
            
            // Remove oldest files until we're under the limit
            val files = diskCacheDir.listFiles()?.sortedBy { it.lastModified() } ?: return
            var sizeToRemove = currentSize - (DISK_CACHE_SIZE * 0.8).toLong() // Remove to 80% of limit
            
            for (file in files) {
                if (sizeToRemove <= 0) break
                
                sizeToRemove -= file.length()
                file.delete()
                
                // Also remove from memory cache
                memoryCache.remove(file.name)
            }
            
            Log.d(TAG, "Image cache cleanup completed")
        }
    }
}