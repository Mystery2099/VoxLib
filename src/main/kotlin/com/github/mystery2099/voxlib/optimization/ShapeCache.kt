package com.github.mystery2099.voxlib.optimization

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.Cache
import com.github.mystery2099.voxlib.rotation.VoxelShapeTransformation
import net.minecraft.util.shape.VoxelShape
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong
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
    private val cache: Cache<Any, VoxelShape> = Caffeine.newBuilder()
        .maximumSize(MAX_CACHE_SIZE)
        .expireAfterAccess(10, TimeUnit.MINUTES)
        .recordStats()
        .build()
    private val cacheGeneration = AtomicLong()
    private val lastBinaryUnion = ThreadLocal.withInitial(::LastBinaryUnion)
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

    internal fun getOrComputeUnion(
        first: VoxelShape,
        second: VoxelShape,
        computeFunction: () -> VoxelShape
    ): VoxelShape {
        val lastUnion = lastBinaryUnion.get()
        lastUnion.resetIfStale(cacheGeneration.get())
        if (!lastUnion.matches(first, second)) {
            lastUnion.rememberSources(first, second)
            return computeFunction()
        }

        val key = lastUnion.key ?: BinaryUnionKey(first, second).also { lastUnion.key = it }
        val cached = cache.getIfPresent(key)
        if (cached != null) return cached
        return getOrComputeOperation(key, computeFunction)
    }

    internal fun getOrComputeUnion(
        shapes: Array<out VoxelShape>,
        size: Int,
        computeFunction: () -> VoxelShape
    ): VoxelShape = getOrComputeOperation(MultipleUnionKey(shapes, size), computeFunction)

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
     * Clears the entire cache.
     */
    fun clearCache() {
        cache.invalidateAll()
        cacheGeneration.incrementAndGet()
        lastBinaryUnion.remove()
        lastTransformation.remove()
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

private class LastBinaryUnion {
    private var generation = Long.MIN_VALUE
    private var first: VoxelShape? = null
    private var second: VoxelShape? = null
    var key: BinaryUnionKey? = null

    fun resetIfStale(currentGeneration: Long) {
        if (generation == currentGeneration) return
        generation = currentGeneration
        first = null
        second = null
        key = null
    }

    fun matches(first: VoxelShape, second: VoxelShape): Boolean =
        this.first === first && this.second === second

    fun rememberSources(first: VoxelShape, second: VoxelShape) {
        this.first = first
        this.second = second
        key = null
    }
}

private class LastTransformation {
    private var generation = Long.MIN_VALUE
    private var shape: VoxelShape? = null
    private var transformation: VoxelShapeTransformation? = null
    var key: TransformationKey? = null

    fun resetIfStale(currentGeneration: Long) {
        if (generation == currentGeneration) return
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
 * A key for the shape cache. This combines the original shape's hashcode with
 * an operation identifier to create a unique key for each transformed shape.
 *
 * @param originalShapeHash The hashcode of the original shape.
 * @param operationId A string identifier for the operation performed.
 * @param parameters Additional parameters that affect the transformation.
 */
data class ShapeCacheKey(
    val originalShapeHash: Int,
    val operationId: String,
    val parameters: List<Any> = emptyList()
)

private class BinaryUnionKey(
    private val first: VoxelShape,
    private val second: VoxelShape
) {
    private val hash = 31 * System.identityHashCode(first) + System.identityHashCode(second)

    override fun hashCode(): Int = hash

    override fun equals(other: Any?): Boolean =
        other is BinaryUnionKey && first === other.first && second === other.second
}

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
