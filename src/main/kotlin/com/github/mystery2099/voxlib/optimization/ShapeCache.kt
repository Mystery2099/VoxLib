package com.github.mystery2099.voxlib.optimization

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.Cache
import net.minecraft.util.shape.VoxelShape
import java.util.concurrent.TimeUnit
import java.util.function.Function

/**
 * A utility class for caching VoxelShapes to improve performance.
 *
 * VoxelShape operations can be expensive, especially when performed frequently.
 * This cache helps reduce the overhead by storing and reusing previously created shapes.
 * Uses Caffeine caching library for high-performance caching with automatic eviction.
 */
object ShapeCache {
    /**
     * Maximum size of the cache to prevent memory leaks
     */
    private const val MAX_CACHE_SIZE: Long = 500

    /**
     * Caffeine cache with automatic eviction policies
     */
    private val cache: Cache<ShapeCacheKey, VoxelShape> = Caffeine.newBuilder()
        .maximumSize(MAX_CACHE_SIZE)
        .expireAfterAccess(10, TimeUnit.MINUTES)
        .recordStats() // Optional: enables statistics collection
        .build()

    /**
     * Gets a shape from the cache or computes it if not present.
     *
     * @param key The key to identify the shape in the cache.
     * @param computeFunction The function to compute the shape if not in cache.
     * @return The cached or newly computed VoxelShape.
     */
    fun getOrCompute(key: ShapeCacheKey, computeFunction: Function<ShapeCacheKey, VoxelShape>): VoxelShape {
        return cache.get(key) { k -> computeFunction.apply(k) }
    }

    /**
     * Gets a shape from the cache or computes it if not present.
     * This overload accepts a Kotlin lambda for more idiomatic usage.
     *
     * @param key The key to identify the shape in the cache.
     * @param computeFunction The lambda to compute the shape if not in cache.
     * @return The cached or newly computed VoxelShape.
     */
    fun getOrCompute(key: ShapeCacheKey, computeFunction: () -> VoxelShape): VoxelShape {
        return cache.get(key) { _ -> computeFunction() }
    }

    /**
     * Clears the entire cache.
     */
    fun clearCache() {
        cache.invalidateAll()
    }

    /**
     * Removes a specific shape from the cache.
     *
     * @param key The key of the shape to remove.
     */
    fun invalidate(key: ShapeCacheKey) {
        cache.invalidate(key)
    }

    /**
     * Returns the current size of the cache.
     *
     * @return The number of entries in the cache.
     */
    fun size(): Long {
        return cache.estimatedSize()
    }

    /**
     * Returns statistics about the cache if stats recording is enabled.
     *
     * @return A string representation of cache statistics.
     */
    fun stats(): String {
        return cache.stats().toString()
    }
}

/**
 * A key for the shape cache. This combines the original shape's hashcode with
 * an operation identifier to create a unique key for each transformed shape.
 *
 * @property originalShapeHash The hashcode of the original shape.
 * @property operationId A string identifier for the operation performed.
 * @property parameters Additional parameters that affect the transformation.
 */
data class ShapeCacheKey(
    val originalShapeHash: Int,
    val operationId: String,
    val parameters: List<Any> = emptyList()
)
