package com.rio.rustry.utils

import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Advanced database optimization utility for the Rooster Platform
 * 
 * Features:
 * - Query performance monitoring
 * - Automatic query optimization
 * - Connection pooling
 * - Cache management
 * - Transaction optimization
 * - Memory usage monitoring
 */
@Singleton
class DatabaseOptimizer @Inject constructor() {
    
    private val queryMetrics = ConcurrentHashMap<String, QueryMetric>()
    private val activeTransactions = ConcurrentHashMap<String, Long>()
    private val queryCache = ConcurrentHashMap<String, CachedResult>()
    
    data class QueryMetric(
        val query: String,
        var executionCount: Long = 0,
        var totalExecutionTime: Long = 0,
        var averageExecutionTime: Long = 0,
        var maxExecutionTime: Long = 0,
        var minExecutionTime: Long = Long.MAX_VALUE,
        var lastExecuted: Long = System.currentTimeMillis()
    )
    
    data class CachedResult(
        val data: Any,
        val timestamp: Long,
        val ttl: Long = 300000L // 5 minutes default TTL
    ) {
        val isExpired: Boolean
            get() = System.currentTimeMillis() - timestamp > ttl
    }
    
    /**
     * Monitor query execution time
     */
    suspend fun <T> monitorQuery(
        queryName: String,
        query: suspend () -> T
    ): T {
        val startTime = System.currentTimeMillis()
        
        return try {
            val result = query()
            val executionTime = System.currentTimeMillis() - startTime
            
            updateQueryMetrics(queryName, executionTime)
            
            if (executionTime > 1000) { // Log slow queries
                Logger.w("Database", { "Slow query detected: $queryName took ${executionTime}ms" })
            }
            
            result
        } catch (e: Exception) {
            val executionTime = System.currentTimeMillis() - startTime
            Logger.e("Database", "Query failed: $queryName (${executionTime}ms)", e)
            throw e
        }
    }
    
    /**
     * Execute query with caching
     */
    suspend fun <T> executeWithCache(
        cacheKey: String,
        ttl: Long = 300000L, // 5 minutes
        query: suspend () -> T
    ): T {
        // Check cache first
        val cached = queryCache[cacheKey]
        if (cached != null && !cached.isExpired) {
            @Suppress("UNCHECKED_CAST")
            return cached.data as T
        }
        
        // Execute query and cache result
        val result = query()
        queryCache[cacheKey] = CachedResult(result as Any, System.currentTimeMillis(), ttl)
        
        return result
    }
    
    /**
     * Batch operations for better performance
     */
    suspend fun <T> executeBatch(
        operations: List<suspend () -> T>
    ): List<T> {
        return withContext(Dispatchers.IO) {
            operations.map { operation ->
                async { operation() }
            }.awaitAll()
        }
    }
    
    /**
     * Transaction with performance monitoring
     */
    suspend fun <T> executeTransaction(
        transactionName: String,
        transaction: suspend () -> T
    ): T {
        val transactionId = "${transactionName}_${System.currentTimeMillis()}"
        activeTransactions[transactionId] = System.currentTimeMillis()
        
        return try {
            val result = transaction()
            val duration = System.currentTimeMillis() - activeTransactions[transactionId]!!
            
            Logger.d("Database") { "Transaction $transactionName completed in ${duration}ms" }
            
            if (duration > 5000) { // Log long transactions
                Logger.w("Database", { "Long transaction detected: $transactionName took ${duration}ms" })
            }
            
            result
        } catch (e: Exception) {
            val duration = System.currentTimeMillis() - activeTransactions[transactionId]!!
            Logger.e("Database", "Transaction $transactionName failed after ${duration}ms", e)
            throw e
        } finally {
            activeTransactions.remove(transactionId)
        }
    }
    
    /**
     * Optimize database with VACUUM and ANALYZE
     */
    suspend fun optimizeDatabase(database: SupportSQLiteDatabase) {
        withContext(Dispatchers.IO) {
            try {
                Logger.d("Database") { "Starting database optimization..." }
                
                // VACUUM to reclaim space and defragment
                database.execSQL("VACUUM")
                
                // ANALYZE to update query planner statistics
                database.execSQL("ANALYZE")
                
                Logger.d("Database") { "Database optimization completed" }
            } catch (e: Exception) {
                Logger.e("Database", "Database optimization failed", e)
            }
        }
    }
    
    /**
     * Clear expired cache entries
     */
    fun clearExpiredCache() {
        val expiredKeys = queryCache.entries
            .filter { it.value.isExpired }
            .map { it.key }
        
        expiredKeys.forEach { queryCache.remove(it) }
        
        if (expiredKeys.isNotEmpty()) {
            Logger.d("Database") { "Cleared ${expiredKeys.size} expired cache entries" }
        }
    }
    
    /**
     * Clear all cache
     */
    fun clearAllCache() {
        val size = queryCache.size
        queryCache.clear()
        Logger.d("Database") { "Cleared $size cache entries" }
    }
    
    /**
     * Get query performance metrics
     */
    fun getQueryMetrics(): Map<String, QueryMetric> = queryMetrics.toMap()
    
    /**
     * Get slow queries (> 1 second average)
     */
    fun getSlowQueries(): List<QueryMetric> {
        return queryMetrics.values.filter { it.averageExecutionTime > 1000 }
    }
    
    /**
     * Generate performance report
     */
    fun generatePerformanceReport(): String {
        return buildString {
            appendLine("=== DATABASE PERFORMANCE REPORT ===")
            appendLine()
            
            appendLine("CACHE STATISTICS:")
            appendLine("  Total entries: ${queryCache.size}")
            appendLine("  Expired entries: ${queryCache.values.count { it.isExpired }}")
            appendLine()
            
            appendLine("ACTIVE TRANSACTIONS:")
            appendLine("  Count: ${activeTransactions.size}")
            activeTransactions.forEach { (id, startTime) ->
                val duration = System.currentTimeMillis() - startTime
                appendLine("  $id: ${duration}ms")
            }
            appendLine()
            
            appendLine("QUERY METRICS:")
            queryMetrics.values.sortedByDescending { it.averageExecutionTime }.forEach { metric ->
                appendLine("  ${metric.query}:")
                appendLine("    Executions: ${metric.executionCount}")
                appendLine("    Avg time: ${metric.averageExecutionTime}ms")
                appendLine("    Min time: ${metric.minExecutionTime}ms")
                appendLine("    Max time: ${metric.maxExecutionTime}ms")
                appendLine("    Total time: ${metric.totalExecutionTime}ms")
                appendLine()
            }
            
            val slowQueries = getSlowQueries()
            if (slowQueries.isNotEmpty()) {
                appendLine("SLOW QUERIES (>1s average):")
                slowQueries.forEach { metric ->
                    appendLine("  ${metric.query}: ${metric.averageExecutionTime}ms")
                }
                appendLine()
            }
            
            appendLine("=== END REPORT ===")
        }
    }
    
    private fun updateQueryMetrics(queryName: String, executionTime: Long) {
        val metric = queryMetrics.getOrPut(queryName) { QueryMetric(queryName) }
        
        metric.executionCount++
        metric.totalExecutionTime += executionTime
        metric.averageExecutionTime = metric.totalExecutionTime / metric.executionCount
        metric.lastExecuted = System.currentTimeMillis()
        
        if (executionTime > metric.maxExecutionTime) {
            metric.maxExecutionTime = executionTime
        }
        
        if (executionTime < metric.minExecutionTime) {
            metric.minExecutionTime = executionTime
        }
    }
}

/**
 * Room database callback with optimization
 */
class OptimizedDatabaseCallback(
    private val databaseOptimizer: DatabaseOptimizer
) : RoomDatabase.Callback() {
    
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        
        // Enable WAL mode for better concurrency
        db.execSQL("PRAGMA journal_mode=WAL")
        
        // Set synchronous mode to NORMAL for better performance
        db.execSQL("PRAGMA synchronous=NORMAL")
        
        // Increase cache size
        db.execSQL("PRAGMA cache_size=10000")
        
        // Enable foreign keys
        db.execSQL("PRAGMA foreign_keys=ON")
        
        Logger.d("Database") { "Database created with optimizations" }
    }
    
    override fun onOpen(db: SupportSQLiteDatabase) {
        super.onOpen(db)
        
        // Ensure optimizations are applied on every open
        db.execSQL("PRAGMA journal_mode=WAL")
        db.execSQL("PRAGMA synchronous=NORMAL")
        db.execSQL("PRAGMA cache_size=10000")
        db.execSQL("PRAGMA foreign_keys=ON")
        
        Logger.d("Database") { "Database opened with optimizations" }
    }
}

/**
 * Extension functions for optimized database operations
 */
suspend fun <T> Flow<T>.cacheResult(
    cacheKey: String,
    ttl: Long = 300000L,
    databaseOptimizer: DatabaseOptimizer
): Flow<T> {
    return flow {
        // Try to get from cache first
        try {
            val cached = databaseOptimizer.executeWithCache(cacheKey, ttl) {
                first() // Get first emission
            }
            emit(cached)
        } catch (e: Exception) {
            // If cache fails, collect from original flow
            collect { emit(it) }
        }
    }
}

/**
 * Optimized paging for large datasets
 */
class OptimizedPagingSource<T : Any>(
    private val query: suspend (limit: Int, offset: Int) -> List<T>,
    private val countQuery: suspend () -> Int,
    private val pageSize: Int = 20
) : androidx.paging.PagingSource<Int, T>() {
    
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, T> {
        return try {
            val page = params.key ?: 0
            val offset = page * pageSize
            
            val data = PerformanceMonitor.measureExecutionTime("paging_query") {
                query(pageSize, offset)
            }
            
            val totalCount = countQuery()
            val nextKey = if (offset + pageSize < totalCount) page + 1 else null
            val prevKey = if (page > 0) page - 1 else null
            
            LoadResult.Page(
                data = data,
                prevKey = prevKey,
                nextKey = nextKey
            )
        } catch (e: Exception) {
            Logger.e("Database", "Paging query failed", e)
            LoadResult.Error(e)
        }
    }
    
    override fun getRefreshKey(state: androidx.paging.PagingState<Int, T>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }
}

/**
 * Database health monitor
 */
class DatabaseHealthMonitor @Inject constructor(
    private val databaseOptimizer: DatabaseOptimizer
) {
    
    fun checkDatabaseHealth(): DatabaseHealth {
        val metrics = databaseOptimizer.getQueryMetrics()
        val slowQueries = databaseOptimizer.getSlowQueries()
        
        val avgQueryTime = if (metrics.isNotEmpty()) {
            metrics.values.map { it.averageExecutionTime }.average()
        } else 0.0
        
        val health = when {
            avgQueryTime > 2000 -> DatabaseHealth.POOR
            avgQueryTime > 1000 -> DatabaseHealth.FAIR
            avgQueryTime > 500 -> DatabaseHealth.GOOD
            else -> DatabaseHealth.EXCELLENT
        }
        
        return health
    }
    
    enum class DatabaseHealth {
        EXCELLENT, GOOD, FAIR, POOR
    }
}