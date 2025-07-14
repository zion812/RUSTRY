package com.rio.rustry.utils

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.decode.SvgDecoder
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.size.Size
import coil.transform.CircleCropTransformation
import coil.transform.RoundedCornersTransformation
import com.rio.rustry.BuildConfig
import kotlinx.coroutines.Dispatchers

/**
 * Optimized image loading utility for the Rooster Platform
 * 
 * Features:
 * - Advanced caching strategies
 * - Memory optimization
 * - Progressive loading
 * - Error handling with fallbacks
 * - Performance monitoring
 * - Automatic image resizing
 * - Support for various formats (JPEG, PNG, GIF, SVG)
 */
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class OptimizedImageLoader @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val imageLoader by lazy {
        ImageLoader.Builder(context)
            .memoryCache {
                MemoryCache.Builder(context)
                    .maxSizePercent(0.25) // Use 25% of available memory
                    .strongReferencesEnabled(true)
                    .weakReferencesEnabled(true)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("image_cache"))
                    .maxSizeBytes(BuildConfig.IMAGE_CACHE_SIZE * 1024 * 1024L) // From BuildConfig
                    .build()
            }
            .components {
                // Support for GIFs
                if (android.os.Build.VERSION.SDK_INT >= 28) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
                // Support for SVGs
                add(SvgDecoder.Factory())
            }
            .respectCacheHeaders(false)
            .allowHardware(true) // Enable hardware bitmaps for better performance
            .crossfade(true)
            .crossfade(300) // Smooth transitions
            .dispatcher(Dispatchers.IO)
            .build()
    }
    
    /**
     * Create an optimized image request
     */
    fun createImageRequest(
        data: Any?,
        size: Size = Size.ORIGINAL,
        transformations: List<coil.transform.Transformation> = emptyList(),
        placeholder: Drawable? = null,
        error: Drawable? = null,
        fallback: Drawable? = null,
        memoryCachePolicy: CachePolicy = CachePolicy.ENABLED,
        diskCachePolicy: CachePolicy = CachePolicy.ENABLED,
        networkCachePolicy: CachePolicy = CachePolicy.ENABLED
    ): ImageRequest {
        return ImageRequest.Builder(context)
            .data(data)
            .size(size)
            .transformations(transformations)
            .placeholder(placeholder)
            .error(error)
            .fallback(fallback)
            .memoryCachePolicy(memoryCachePolicy)
            .diskCachePolicy(diskCachePolicy)
            .networkCachePolicy(networkCachePolicy)
            .dispatcher(Dispatchers.IO)
            .build()
    }
    
    /**
     * Preload images for better performance
     */
    suspend fun preloadImages(urls: List<String>) {
        urls.forEach { url ->
            val request = createImageRequest(
                data = url,
                size = Size(400, 400) // Preload at medium resolution
            )
            imageLoader.execute(request)
        }
    }
    
    /**
     * Clear image cache
     */
    fun clearCache() {
        imageLoader.memoryCache?.clear()
        imageLoader.diskCache?.clear()
    }
    
    /**
     * Get cache size information
     */
    fun getCacheInfo(): CacheInfo {
        val memoryCache = imageLoader.memoryCache
        val diskCache = imageLoader.diskCache
        
        return CacheInfo(
            memoryCacheSize = memoryCache?.size?.toLong() ?: 0L,
            memoryCacheMaxSize = 0L, // memoryCache?.maxSize type is incompatible
            diskCacheSize = 0L, // diskCache?.size type is incompatible
            diskCacheMaxSize = 0L // diskCache?.maxSize type is incompatible
        )
    }
    
    data class CacheInfo(
        val memoryCacheSize: Long,
        val memoryCacheMaxSize: Long,
        val diskCacheSize: Long,
        val diskCacheMaxSize: Long
    )
}

/**
 * Optimized image composable with advanced features
 */
@Composable
fun OptimizedImage(
    data: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    placeholder: Painter? = null,
    error: Painter? = null,
    fallback: Painter? = null,
    onLoading: ((AsyncImagePainter.State.Loading) -> Unit)? = null,
    onSuccess: ((AsyncImagePainter.State.Success) -> Unit)? = null,
    onError: ((AsyncImagePainter.State.Error) -> Unit)? = null,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
    filterQuality: androidx.compose.ui.graphics.FilterQuality = androidx.compose.ui.graphics.FilterQuality.None,
    clipToBounds: Boolean = true,
    enablePerformanceMonitoring: Boolean = BuildConfig.ENABLE_LOGGING
) {
    val context = LocalContext.current
    
    // Performance monitoring
    if (enablePerformanceMonitoring) {
        LaunchedEffect(data) {
            PerformanceMonitor.startTrace("image_load_${data.hashCode()}")
        }
    }
    
    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(data)
            .crossfade(true)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .build(),
        contentDescription = contentDescription,
        modifier = modifier,
        placeholder = placeholder,
        error = error,
        fallback = fallback,
        onLoading = { state ->
            onLoading?.invoke(state)
            if (enablePerformanceMonitoring) {
                Logger.d("ImageLoader") { "Loading image: $data" }
            }
        },
        onSuccess = { state ->
            onSuccess?.invoke(state)
            if (enablePerformanceMonitoring) {
                PerformanceMonitor.stopTrace("image_load_${data.hashCode()}")
                Logger.d("ImageLoader") { "Successfully loaded image: $data" }
            }
        },
        onError = { state ->
            onError?.invoke(state)
            if (enablePerformanceMonitoring) {
                PerformanceMonitor.stopTrace("image_load_${data.hashCode()}")
                Logger.e("ImageLoader", "Failed to load image: $data", state.result.throwable)
            }
        },
        alignment = alignment,
        contentScale = contentScale,
        alpha = alpha,
        colorFilter = colorFilter,
        filterQuality = filterQuality
    )
}

/**
 * Circular image with optimized loading
 */
@Composable
fun CircularImage(
    data: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 48.dp,
    placeholder: Painter? = null,
    error: Painter? = null,
    showLoadingIndicator: Boolean = true
) {
    val context = LocalContext.current
    
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        val painter = rememberAsyncImagePainter(
            model = ImageRequest.Builder(context)
                .data(data)
                .size(coil.size.Size(size.value.toInt(), size.value.toInt()))
                .transformations(CircleCropTransformation())
                .crossfade(true)
                .build(),
            placeholder = placeholder,
            error = error
        )
        
        when (painter.state) {
            is AsyncImagePainter.State.Loading -> {
                if (showLoadingIndicator) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(size / 2),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 2.dp
                    )
                }
            }
            is AsyncImagePainter.State.Error -> {
                error?.let { errorPainter ->
                    Image(
                        painter = errorPainter,
                        contentDescription = contentDescription,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
            else -> {
                Image(
                    painter = painter,
                    contentDescription = contentDescription,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

/**
 * Rounded corner image with optimized loading
 */
@Composable
fun RoundedImage(
    data: Any?,
    contentDescription: String?,
    cornerRadius: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier,
    placeholder: Painter? = null,
    error: Painter? = null,
    contentScale: ContentScale = ContentScale.Crop
) {
    val context = LocalContext.current
    
    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(data)
            .transformations(RoundedCornersTransformation(cornerRadius.value))
            .crossfade(true)
            .build(),
        contentDescription = contentDescription,
        modifier = modifier,
        placeholder = placeholder,
        error = error,
        contentScale = contentScale
    )
}

/**
 * Progressive image loading with multiple quality levels
 */
@Composable
fun ProgressiveImage(
    data: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    lowQualityData: Any? = null,
    placeholder: Painter? = null,
    error: Painter? = null,
    contentScale: ContentScale = ContentScale.Fit
) {
    val context = LocalContext.current
    var isHighQualityLoaded by remember { mutableStateOf(false) }
    
    Box(modifier = modifier) {
        // Low quality image (loads first)
        if (!isHighQualityLoaded && lowQualityData != null) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(lowQualityData)
                    .size(coil.size.Size(100, 100)) // Small size for quick loading
                    .build(),
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                contentScale = contentScale,
                alpha = 0.7f // Slightly transparent to indicate low quality
            )
        }
        
        // High quality image
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(data)
                .crossfade(true)
                .build(),
            contentDescription = contentDescription,
            modifier = Modifier.fillMaxSize(),
            placeholder = if (lowQualityData == null) placeholder else null,
            error = error,
            contentScale = contentScale,
            onSuccess = {
                isHighQualityLoaded = true
            }
        )
    }
}

/**
 * Image with automatic retry on failure
 */
@Composable
fun RetryableImage(
    data: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    maxRetries: Int = 3,
    placeholder: Painter? = null,
    error: Painter? = null,
    contentScale: ContentScale = ContentScale.Fit
) {
    var retryCount by remember { mutableStateOf(0) }
    var imageData by remember(data) { mutableStateOf(data) }
    
    OptimizedImage(
        data = imageData,
        contentDescription = contentDescription,
        modifier = modifier,
        placeholder = placeholder,
        error = error,
        contentScale = contentScale,
        onError = { state ->
            if (retryCount < maxRetries) {
                retryCount++
                // Trigger retry by updating the data
                imageData = "$data?retry=$retryCount"
                Logger.d("ImageLoader") { "Retrying image load (attempt $retryCount): $data" }
            } else {
                Logger.e("ImageLoader", "Failed to load image after $maxRetries attempts: $data", state.result.throwable)
            }
        }
    )
}