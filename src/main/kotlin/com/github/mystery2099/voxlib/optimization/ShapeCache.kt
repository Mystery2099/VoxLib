package com.github.mystery2099.voxlib.optimization

import net.minecraft.util.shape.VoxelShape
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Function

/**
 * A utility class for caching VoxelShapes to improve performance.
 *
 * VoxelShape operations can be expensive, especially when performed frequently.
 * This cache helps reduce the overhead by storing and reusing previously created shapes.
 */
object ShapeCache {
    // Thread-safe map for storing shapes
    private val cache = ConcurrentHashMap<ShapeCacheKey, VoxelShape>()

    /**
     * Maximum size of the cache to prevent memory leaks
     */
    private const val MAX_CACHE_SIZE = 500

    /**
     * Gets a shape from the cache or computes it if not present.
     *
     * @param key The key to identify the shape in the cache.
     * @param computeFunction The function to compute the shape if not in cache.
     * @return The cached or newly computed VoxelShape.
     */
    fun getOrCompute(key: ShapeCacheKey, computeFunction: Function<ShapeCacheKey, VoxelShape>): VoxelShape {
        // Clean cache if it gets too large
        if (cache.size > MAX_CACHE_SIZE) {
            cleanCache()
        }

        return cache.computeIfAbsent(key, computeFunction)
    }

    /**
     * Clears the entire cache.
     */
    fun clearCache() {
        cache.clear()
    }

    /**
     * Removes half of the entries from the cache when it gets too large.
     * This is a simple strategy to prevent the cache from growing indefinitely.
     */
    private fun cleanCache() {
        val keysToRemove = cache.keys.take(MAX_CACHE_SIZE / 2)
        keysToRemove.forEach { cache.remove(it) }
    }

    /**
     * Removes a specific shape from the cache.
     *
     * @param key The key of the shape to remove.
     */
    fun invalidate(key: ShapeCacheKey) {
        cache.remove(key)
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
