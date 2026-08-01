package com.github.mystery2099.voxlib.optimization

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.Cache
import com.github.mystery2099.voxlib.rotation.VoxelShapeTransformation
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong
import java.util.function.Function

/**
 * Caffeine-backed cache for expensive VoxelShape operations.
 *
 * ## Hot-path keys (identity-based)
 *
 * Internal union/transform helpers use private keys keyed by **object identity**
 * (`===` / `System.identityHashCode`), not shape content hashes:
 * - [BinaryUnionKey] — two shapes
 * - [TransformationKey] — shape + [VoxelShapeTransformation]
 * - [MultipleUnionKey] — ordered array of shapes
 *
 * ## Admission policies
 *
 * **Binary unions** use a ThreadLocal ring of recently seen pairs
 * ([RecentBinaryUnions]). The first time a pair is seen, no cache key is
 * allocated and the union is computed uncached. On a later repeat of the same
 * pair, a key is created and the result is stored. This avoids key/cache
 * overhead for one-off combinations.
 *
 * **Transformations** use [LastTransformation]: the key is allocated on first
 * sight of a (shape, transform) pair so the common “rotate the same shape
 * again” path can hit without waiting for a second distinct call pattern.
 *
 * ## Invalidation
 *
 * [clearCache] invalidates Caffeine entries and bumps [cacheGeneration].
 * ThreadLocal helpers discard state when their stored generation is stale,
 * so a cleared cache cannot be re-served from retained last-hit keys.
 *
 * ## Public API
 *
 * [ShapeCacheKey] + [getOrCompute] remain for callers that manage their own
 * keys. Hot paths do **not** use [ShapeCacheKey].
 */
object ShapeCache {
    private const val MAX_CACHE_SIZE: Long = 500

    private val cache: Cache<Any, VoxelShape> = Caffeine.newBuilder()
        .maximumSize(MAX_CACHE_SIZE)
        .expireAfterAccess(10, TimeUnit.MINUTES)
        .recordStats()
        .build()
    private val cacheGeneration = AtomicLong()
    private val recentBinaryUnions = ThreadLocal.withInitial(::RecentBinaryUnions)
    private val lastTransformation = ThreadLocal.withInitial(::LastTransformation)

    /**
     * Gets a shape from the cache or computes it if not present.
     *
     * @param key The key to identify the shape in the cache.
     * @param computeFunction The function to compute the shape if not in cache.
     * @return The cached or newly computed VoxelShape.
     */
    fun getOrCompute(key: ShapeCacheKey, computeFunction: Function<ShapeCacheKey, VoxelShape>): VoxelShape {
        return cache.get(key) { computeFunction.apply(key) }
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
     * Binary union with recent-pair admission. Equivalent to
     * `getOrComputeUnion(first, second) { VoxelShapes.union(first, second) }`.
     */
    internal fun getOrComputeUnion(first: VoxelShape, second: VoxelShape): VoxelShape =
        getOrComputeUnion(first, second) { VoxelShapes.union(first, second) }

    /**
     * Binary union with recent-pair admission.
     *
     * Returns [computeFunction]'s result without caching until the same
     * identity pair has been seen again on this thread (see [RecentBinaryUnions]).
     */
    internal fun getOrComputeUnion(
        first: VoxelShape,
        second: VoxelShape,
        computeFunction: () -> VoxelShape
    ): VoxelShape {
        val key = recentBinaryUnions.get()
            .keyForRepeated(first, second, cacheGeneration.get())
            ?: return computeFunction()
        val cached = cache.getIfPresent(key)
        if (cached != null) return cached
        return getOrComputeOperation(key, computeFunction)
    }

    internal fun getOrComputeUnion(
        shapes: Array<out VoxelShape>,
        size: Int,
        computeFunction: () -> VoxelShape
    ): VoxelShape = getOrComputeOperation(MultipleUnionKey(shapes, size), computeFunction)

    /**
     * Transformation with last-hit key reuse.
     *
     * Allocates an identity key on first sight of (shape, transformation) so
     * immediate repeats can hit the cache without a second admission step.
     */
    internal fun getOrComputeTransformation(
        shape: VoxelShape,
        transformation: VoxelShapeTransformation,
        computeFunction: () -> VoxelShape
    ): VoxelShape {
        val last = lastTransformation.get()
        last.resetIfStale(cacheGeneration.get())
        val key = if (last.matches(shape, transformation)) {
            requireNotNull(last.key)
        } else {
            TransformationKey(shape, transformation).also {
                last.remember(shape, transformation, it)
            }
        }

        val cached = cache.getIfPresent(key)
        if (cached != null) return cached
        return getOrComputeOperation(key, computeFunction)
    }

    private fun getOrComputeOperation(key: Any, computeFunction: () -> VoxelShape): VoxelShape =
        cache.get(key) { computeFunction() }

    /**
     * Clears the entire cache and invalidates ThreadLocal admission state.
     */
    fun clearCache() {
        cache.invalidateAll()
        val currentGeneration = cacheGeneration.incrementAndGet()
        recentBinaryUnions.get().clear(currentGeneration)
        lastTransformation.get().clear(currentGeneration)
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
 * ThreadLocal ring of recently seen binary-union pairs.
 *
 * First sight of a pair records the sources and returns null (no key, no cache).
 * A later match returns (and lazily allocates) a [BinaryUnionKey].
 */
private class RecentBinaryUnions {
    private var generation = Long.MIN_VALUE
    private val firstSources = arrayOfNulls<VoxelShape>(RECENT_BINARY_UNION_CAPACITY)
    private val secondSources = arrayOfNulls<VoxelShape>(RECENT_BINARY_UNION_CAPACITY)
    private val keys = arrayOfNulls<BinaryUnionKey>(RECENT_BINARY_UNION_CAPACITY)
    private var size = 0
    private var nextIndex = 0

    fun keyForRepeated(
        first: VoxelShape,
        second: VoxelShape,
        currentGeneration: Long
    ): BinaryUnionKey? {
        resetIfStale(currentGeneration)
        for (index in 0 until size) {
            if (firstSources[index] === first && secondSources[index] === second) {
                return keys[index] ?: BinaryUnionKey(first, second).also { keys[index] = it }
            }
        }

        firstSources[nextIndex] = first
        secondSources[nextIndex] = second
        keys[nextIndex] = null
        nextIndex = (nextIndex + 1) % RECENT_BINARY_UNION_CAPACITY
        if (size < RECENT_BINARY_UNION_CAPACITY) size++
        return null
    }

    fun clear(currentGeneration: Long) {
        generation = currentGeneration
        firstSources.fill(null)
        secondSources.fill(null)
        keys.fill(null)
        size = 0
        nextIndex = 0
    }

    private fun resetIfStale(currentGeneration: Long) {
        if (generation == currentGeneration) return
        clear(currentGeneration)
    }
}

/**
 * ThreadLocal last (shape, transformation) pair for key reuse on immediate repeats.
 */
private class LastTransformation {
    private var generation = Long.MIN_VALUE
    private var shape: VoxelShape? = null
    private var transformation: VoxelShapeTransformation? = null
    var key: TransformationKey? = null

    fun resetIfStale(currentGeneration: Long) {
        if (generation == currentGeneration) return
        clear(currentGeneration)
    }

    fun clear(currentGeneration: Long) {
        generation = currentGeneration
        shape = null
        transformation = null
        key = null
    }

    fun matches(shape: VoxelShape, transformation: VoxelShapeTransformation): Boolean =
        this.shape === shape && this.transformation == transformation

    fun remember(
        shape: VoxelShape,
        transformation: VoxelShapeTransformation,
        key: TransformationKey
    ) {
        this.shape = shape
        this.transformation = transformation
        this.key = key
    }
}

/**
 * General-purpose public cache key for [ShapeCache.getOrCompute].
 *
 * Hot-path unions and transforms use private identity keys instead; prefer those
 * internal helpers when adding new cached shape operations inside VoxLib.
 *
 * @param originalShapeHash Caller-chosen hash of the source shape (or related state).
 * @param operationId A string identifier for the operation performed.
 * @param parameters Additional parameters that affect the result.
 */
data class ShapeCacheKey(
    val originalShapeHash: Int,
    val operationId: String,
    val parameters: List<Any> = emptyList()
)

/** Identity key for a binary union of two shapes. */
private class BinaryUnionKey(
    private val first: VoxelShape,
    private val second: VoxelShape
) {
    private val hash = 31 * System.identityHashCode(first) + System.identityHashCode(second)

    override fun hashCode(): Int = hash

    override fun equals(other: Any?): Boolean =
        other is BinaryUnionKey && first === other.first && second === other.second
}

/** Identity key for a shape plus a transformation enum. */
private class TransformationKey(
    private val shape: VoxelShape,
    private val transformation: VoxelShapeTransformation
) {
    private val hash = 31 * System.identityHashCode(shape) + transformation.hashCode()

    override fun hashCode(): Int = hash

    override fun equals(other: Any?): Boolean =
        other is TransformationKey &&
            shape === other.shape &&
            transformation == other.transformation
}

/** Identity key for an ordered multi-shape union. */
private class MultipleUnionKey(sourceShapes: Array<out VoxelShape>, size: Int) {
    private val shapes = Array(size) { sourceShapes[it] }
    private val hash = shapes.fold(1) { result, shape ->
        31 * result + System.identityHashCode(shape)
    }

    override fun hashCode(): Int = hash

    override fun equals(other: Any?): Boolean {
        if (other !is MultipleUnionKey || shapes.size != other.shapes.size) return false
        return shapes.indices.all { shapes[it] === other.shapes[it] }
    }
}

private const val RECENT_BINARY_UNION_CAPACITY = 4
