package com.github.mystery2099.voxlib.optimization

import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.concurrent.atomic.AtomicInteger

/**
 * Unit tests for the ShapeCache class.
 */
class ShapeCacheTest {

    @BeforeEach
    fun setUp() {
        // Clear the cache before each test
        ShapeCache.clearCache()
    }

    @Test
    fun `test getOrCompute with lambda returns correct shape`() {
        // Create a simple key
        val key = ShapeCacheKey(123, "test")
        
        // Get a shape from the cache (will compute it)
        val shape = ShapeCache.getOrCompute(key) {
            VoxelShapes.fullCube()
        }
        
        // Verify the shape is correct
        assertEquals(VoxelShapes.fullCube(), shape)
    }
    
    @Test
    fun `test getOrCompute with Function returns correct shape`() {
        // Create a simple key
        val key = ShapeCacheKey(456, "test")

        // Get a shape from the cache (will compute it)
        val shape = ShapeCache.getOrCompute(key) {
            VoxelShapes.fullCube()
        }

        // Verify the shape is correct
        assertEquals(VoxelShapes.fullCube(), shape)
    }
    
    @Test
    fun `test cache hit prevents recomputation`() {
        // Create a simple key
        val key = ShapeCacheKey(789, "test")
        
        // Counter to track how many times the compute function is called
        val computeCount = AtomicInteger(0)
        
        // Define a function that increments the counter and returns a shape
        val computeFunction = {
            computeCount.incrementAndGet()
            VoxelShapes.fullCube()
        }
        
        // First call should compute
        val shape1 = ShapeCache.getOrCompute(key, computeFunction)
        assertEquals(1, computeCount.get())
        
        // Second call should use cache
        val shape2 = ShapeCache.getOrCompute(key, computeFunction)
        assertEquals(1, computeCount.get()) // Counter should still be 1
        
        // Shapes should be the same
        assertSame(shape1, shape2)
    }
    
    @Test
    fun `test different keys compute different shapes`() {
        // Create two different keys
        val key1 = ShapeCacheKey(111, "test1")
        val key2 = ShapeCacheKey(222, "test2")
        
        // Counter to track computations
        val computeCount = AtomicInteger(0)
        
        // Get shapes for both keys
        val shape1 = ShapeCache.getOrCompute(key1) {
            computeCount.incrementAndGet()
            VoxelShapes.cuboid(0.0, 0.0, 0.0, 0.5, 0.5, 0.5)
        }
        
        val shape2 = ShapeCache.getOrCompute(key2) {
            computeCount.incrementAndGet()
            VoxelShapes.cuboid(0.5, 0.5, 0.5, 1.0, 1.0, 1.0)
        }
        
        // Should have computed twice
        assertEquals(2, computeCount.get())
        
        // Shapes should be different
        assertNotEquals(shape1, shape2)
    }
    
    @Test
    fun `test invalidate removes shape from cache`() {
        // Create a key
        val key = ShapeCacheKey(333, "test")
        
        // Counter to track computations
        val computeCount = AtomicInteger(0)
        
        // Get a shape (will compute)
        ShapeCache.getOrCompute(key) {
            computeCount.incrementAndGet()
            VoxelShapes.fullCube()
        }
        
        // Invalidate the key
        ShapeCache.invalidate(key)
        
        // Get the shape again (should recompute)
        ShapeCache.getOrCompute(key) {
            computeCount.incrementAndGet()
            VoxelShapes.fullCube()
        }
        
        // Should have computed twice
        assertEquals(2, computeCount.get())
    }
    
    @Test
    fun `test clearCache removes all shapes`() {
        // Create keys
        val key1 = ShapeCacheKey(444, "test1")
        val key2 = ShapeCacheKey(555, "test2")
        
        // Counter to track computations
        val computeCount = AtomicInteger(0)
        
        // Get shapes for both keys
        ShapeCache.getOrCompute(key1) {
            computeCount.incrementAndGet()
            VoxelShapes.fullCube()
        }
        
        ShapeCache.getOrCompute(key2) {
            computeCount.incrementAndGet()
            VoxelShapes.fullCube()
        }
        
        // Clear the cache
        ShapeCache.clearCache()
        
        // Get shapes again
        ShapeCache.getOrCompute(key1) {
            computeCount.incrementAndGet()
            VoxelShapes.fullCube()
        }
        
        ShapeCache.getOrCompute(key2) {
            computeCount.incrementAndGet()
            VoxelShapes.fullCube()
        }
        
        // Should have computed 4 times
        assertEquals(4, computeCount.get())
    }
    
    @Test
    fun `test size returns correct cache size`() {
        // Initially cache should be empty
        assertEquals(0, ShapeCache.size())
        
        // Add some shapes
        ShapeCache.getOrCompute(ShapeCacheKey(666, "test1")) { VoxelShapes.fullCube() }
        ShapeCache.getOrCompute(ShapeCacheKey(777, "test2")) { VoxelShapes.empty() }
        
        // Size should be 2
        assertEquals(2, ShapeCache.size())
    }
    
    @Test
    fun `test stats returns non-empty string`() {
        // Add a shape to generate some stats
        ShapeCache.getOrCompute(ShapeCacheKey(888, "test")) { VoxelShapes.fullCube() }
        
        // Stats should return a non-empty string
        val stats = ShapeCache.stats()
        assertNotNull(stats)
        assertTrue(stats.isNotEmpty())
    }
    
    @Test
    fun `test ShapeCacheKey equality`() {
        // Create identical keys
        val key1 = ShapeCacheKey(999, "test", listOf("param1", "param2"))
        val key2 = ShapeCacheKey(999, "test", listOf("param1", "param2"))
        
        // Create different keys
        val key3 = ShapeCacheKey(999, "different", listOf("param1", "param2"))
        val key4 = ShapeCacheKey(888, "test", listOf("param1", "param2"))
        val key5 = ShapeCacheKey(999, "test", listOf("different"))
        
        // Test equality
        assertEquals(key1, key2)
        assertNotEquals(key1, key3)
        assertNotEquals(key1, key4)
        assertNotEquals(key1, key5)
        
        // Test hashCode
        assertEquals(key1.hashCode(), key2.hashCode())
    }
}
