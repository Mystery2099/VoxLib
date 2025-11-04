package com.github.mystery2099.voxlib.optimization;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * A utility class for caching VoxelShapes to improve performance.
 *
 * VoxelShape operations can be expensive, especially when performed frequently.
 * This cache helps reduce the overhead by storing and reusing previously created shapes.
 * Uses Caffeine caching library for high-performance caching with automatic eviction.
 */
public class CacheUtils {
    /**
     * Maximum size of the cache to prevent memory leaks
     */
    private static final long MAX_CACHE_SIZE = 500;

    /**
     * Caffeine cache with automatic eviction policies
     */
    private static final Cache<ShapeCacheKey, VoxelShape> cache = Caffeine.newBuilder()
        .maximumSize(MAX_CACHE_SIZE)
        .expireAfterAccess(10, TimeUnit.MINUTES)
        .recordStats()
        .build();

    /**
     * Gets a shape from the cache or computes it if not present.
     *
     * @param key The key to identify the shape in the cache.
     * @param compute The function to compute the shape if not in cache.
     * @return The cached or newly computed VoxelShape.
     */
    public static VoxelShape getOrCompute(ShapeCacheKey key, Function<ShapeCacheKey, VoxelShape> compute) {
        return cache.get(key, compute);
    }

    /**
     * Clears the entire cache.
     */
    public static void clearCache() {
        cache.invalidateAll();
    }

    /**
     * Removes a specific shape from the cache.
     *
     * @param key The key of the shape to remove.
     */
    public static void invalidate(ShapeCacheKey key) {
        cache.invalidate(key);
    }

    /**
     * Returns the current size of the cache.
     *
     * @return The number of entries in the cache.
     */
    public static long size() {
        return cache.estimatedSize();
    }

    /**
     * Returns statistics about the cache if stats recording is enabled.
     *
     * @return A string representation of cache statistics.
     */
    public static String stats() {
        return cache.stats().toString();
    }
}
