package com.github.mystery2099.voxlib.optimization

import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
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
    fun `ShapeCacheKey equality includes every property`() {
        val key = ShapeCacheKey(10, "operation", listOf("parameter"))

        assertEquals(key, ShapeCacheKey(10, "operation", listOf("parameter")))
        assertNotEquals(key, ShapeCacheKey(11, "operation", listOf("parameter")))
        assertNotEquals(key, ShapeCacheKey(10, "other", listOf("parameter")))
        assertNotEquals(key, ShapeCacheKey(10, "operation", listOf("other")))
    }

    @Test
    fun `source shapes disambiguate identical hash values`() {
        val firstSource = VoxelShapes.cuboid(0.0, 0.0, 0.0, 0.25, 0.25, 0.25)
        val secondSource = VoxelShapes.cuboid(0.75, 0.75, 0.75, 1.0, 1.0, 1.0)
        val firstKey = ShapeOperationCacheKey(42, "rotate", sourceShapes = listOf(firstSource))
        val secondKey = ShapeOperationCacheKey(42, "rotate", sourceShapes = listOf(secondSource))

        assertNotEquals(firstKey, secondKey)
        assertSame(firstSource, ShapeCache.getOrCompute(firstKey) { firstSource })
        assertSame(secondSource, ShapeCache.getOrCompute(secondKey) { secondSource })
        assertSame(firstSource, ShapeCache.getOrCompute(firstKey) { error("cache miss") })
    }
}
