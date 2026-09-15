package com.github.mystery2099.voxlib.optimization

import com.github.benmanes.caffeine.cache.Cache
import com.github.mystery2099.voxlib.rotation.VoxelShapeTransformation
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Duration
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Function

class ShapeCacheTest {
    @BeforeEach
    fun setUp() {
        ShapeCache.clearCache()
    }

    @Test
    fun `getOrCompute supports Kotlin lambda`() {
        val shape = ShapeCache.getOrCompute(ShapeCacheKey(1, "lambda")) {
            VoxelShapes.fullCube()
        }

        assertEquals(VoxelShapes.fullCube(), shape)
    }

    @Test
    fun `getOrCompute supports Java Function`() {
        val key = ShapeCacheKey(2, "function")
        val compute = Function<ShapeCacheKey, VoxelShape> { VoxelShapes.fullCube() }

        assertEquals(VoxelShapes.fullCube(), ShapeCache.getOrCompute(key, compute))
    }

    @Test
    fun `cache hit prevents recomputation`() {
        val key = ShapeCacheKey(3, "hit")
        val computeCount = AtomicInteger()
        val compute = {
            computeCount.incrementAndGet()
            VoxelShapes.fullCube()
        }

        val first = ShapeCache.getOrCompute(key, compute)
        val second = ShapeCache.getOrCompute(key, compute)

        assertEquals(1, computeCount.get())
        assertSame(first, second)
    }

    @Test
    fun `invalidate forces recomputation`() {
        val key = ShapeCacheKey(4, "invalidate")
        val computeCount = AtomicInteger()
        val compute = {
            computeCount.incrementAndGet()
            VoxelShapes.fullCube()
        }

        ShapeCache.getOrCompute(key, compute)
        ShapeCache.invalidate(key)
        ShapeCache.getOrCompute(key, compute)

        assertEquals(2, computeCount.get())
    }

    @Test
    fun `clearCache removes every entry`() {
        ShapeCache.getOrCompute(ShapeCacheKey(5, "first")) { VoxelShapes.fullCube() }
        ShapeCache.getOrCompute(ShapeCacheKey(6, "second")) { VoxelShapes.empty() }

        ShapeCache.clearCache()

        assertEquals(0, ShapeCache.size())
    }

    @Test
    fun `clearCache invalidates automatic operation entries`() {
        val source = VoxelShapes.cuboid(0.0, 0.0, 0.0, 0.5, 0.5, 0.5)
        val computeCount = AtomicInteger()
        val compute = {
            computeCount.incrementAndGet()
            VoxelShapes.fullCube()
        }

        ShapeCache.getOrComputeTransformation(
            source,
            VoxelShapeTransformation.ROTATE_RIGHT,
            compute
        )
        ShapeCache.getOrComputeTransformation(
            source,
            VoxelShapeTransformation.ROTATE_RIGHT,
            compute
        )
        assertEquals(1, computeCount.get())

        ShapeCache.clearCache()
        ShapeCache.getOrComputeTransformation(
            source,
            VoxelShapeTransformation.ROTATE_RIGHT,
            compute
        )

        assertEquals(2, computeCount.get())
    }

    @Test
    fun `size reports entry count`() {
        ShapeCache.getOrCompute(ShapeCacheKey(7, "first")) { VoxelShapes.fullCube() }
        ShapeCache.getOrCompute(ShapeCacheKey(8, "second")) { VoxelShapes.empty() }

        assertEquals(2, ShapeCache.size())
    }

    @Test
    fun `stats reports cache activity`() {
        ShapeCache.getOrCompute(ShapeCacheKey(9, "stats")) { VoxelShapes.fullCube() }

        assertTrue(ShapeCache.stats().isNotEmpty())
    }

    @Test
    fun `binary admitted path remains visible in cache statistics`() {
        val first = VoxelShapes.cuboid(0.0, 0.0, 0.0, 0.25, 0.25, 0.25)
        val second = VoxelShapes.cuboid(0.75, 0.75, 0.75, 1.0, 1.0, 1.0)
        val result = VoxelShapes.union(first, second)

        ShapeCache.getOrComputeUnion(first, second) { result }
        ShapeCache.getOrComputeUnion(first, second) { result }
        val hitsBefore = internalCache().stats().hitCount()
        ShapeCache.getOrComputeUnion(first, second) { error("cache miss") }

        assertTrue(internalCache().stats().hitCount() > hitsBefore)
    }

    @Test
    fun `interleaved binary pairs are admitted after reuse`() {
        val first = VoxelShapes.cuboid(0.0, 0.0, 0.0, 0.2, 0.2, 0.2)
        val second = VoxelShapes.cuboid(0.2, 0.0, 0.0, 0.4, 0.2, 0.2)
        val third = VoxelShapes.cuboid(0.4, 0.0, 0.0, 0.6, 0.2, 0.2)
        val fourth = VoxelShapes.cuboid(0.6, 0.0, 0.0, 0.8, 0.2, 0.2)
        val firstResult = VoxelShapes.union(first, second)
        val secondResult = VoxelShapes.union(third, fourth)

        ShapeCache.getOrComputeUnion(first, second) { firstResult }
        ShapeCache.getOrComputeUnion(third, fourth) { secondResult }
        ShapeCache.getOrComputeUnion(first, second) { firstResult }
        ShapeCache.getOrComputeUnion(third, fourth) { secondResult }

        assertSame(firstResult, ShapeCache.getOrComputeUnion(first, second) { error("cache miss") })
        assertSame(secondResult, ShapeCache.getOrComputeUnion(third, fourth) { error("cache miss") })
    }

    @Test
    fun `ShapeCacheKey equality includes every property`() {
        val key = ShapeCacheKey(10, "operation", listOf("parameter"))

        assertEquals(key, ShapeCacheKey(10, "operation", listOf("parameter")))
        assertNotEquals(key, ShapeCacheKey(11, "operation", listOf("parameter")))
        assertNotEquals(key, ShapeCacheKey(10, "other", listOf("parameter")))
        assertNotEquals(key, ShapeCacheKey(10, "operation", listOf("other")))
    }

    @Test
    fun `operation cache keys use source identity`() {
        val firstSource = VoxelShapes.cuboid(0.0, 0.0, 0.0, 0.25, 0.25, 0.25)
        val secondSource = VoxelShapes.cuboid(0.75, 0.75, 0.75, 1.0, 1.0, 1.0)
        val sharedSource = VoxelShapes.cuboid(0.25, 0.25, 0.25, 0.75, 0.75, 0.75)

        assertSame(
            firstSource,
            ShapeCache.getOrComputeUnion(firstSource, sharedSource) { firstSource }
        )
        assertSame(
            firstSource,
            ShapeCache.getOrComputeUnion(firstSource, sharedSource) { firstSource }
        )
        assertSame(
            firstSource,
            ShapeCache.getOrComputeUnion(firstSource, sharedSource) { error("cache miss") }
        )
        assertSame(
            secondSource,
            ShapeCache.getOrComputeUnion(secondSource, sharedSource) { secondSource }
        )
        assertSame(
            secondSource,
            ShapeCache.getOrComputeUnion(secondSource, sharedSource) { secondSource }
        )
    }

    @Test
    fun `cache retains configured bounds and expiration`() {
        val cache = internalCache()

        assertEquals(500, cache.policy().eviction().orElseThrow().maximum)
        assertEquals(
            Duration.ofMinutes(10),
            cache.policy().expireAfterAccess().orElseThrow().expiresAfter
        )
    }

    @Test
    fun `cache remains bounded after many writes`() {
        repeat(750) { index ->
            ShapeCache.getOrCompute(ShapeCacheKey(index, "bounded")) {
                VoxelShapes.cuboid(index.toDouble(), 0.0, 0.0, index + 0.5, 0.5, 0.5)
            }
        }

        internalCache().cleanUp()
        assertTrue(ShapeCache.size() <= 500)
    }

    @Test
    fun `concurrent lookup computes a key once`() {
        val executor = Executors.newFixedThreadPool(8)
        val computeCount = AtomicInteger()
        val key = ShapeCacheKey(42, "concurrent")
        try {
            val results = (0 until 64).map {
                executor.submit<VoxelShape> {
                    ShapeCache.getOrCompute(key) {
                        computeCount.incrementAndGet()
                        VoxelShapes.fullCube()
                    }
                }
            }
            results.forEach {
                assertSame(VoxelShapes.fullCube(), it.get(10, TimeUnit.SECONDS))
            }
            assertEquals(1, computeCount.get())
        } finally {
            executor.shutdownNow()
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun internalCache(): Cache<Any, VoxelShape> {
        val cacheField = ShapeCache::class.java.getDeclaredField("cache")
        cacheField.isAccessible = true
        return cacheField.get(ShapeCache) as Cache<Any, VoxelShape>
    }
}
