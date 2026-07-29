package com.github.mystery2099.voxlib.combination

import com.github.mystery2099.voxlib.combination.VoxelAssembly.combine
import com.github.mystery2099.voxlib.combination.VoxelAssembly.union
import com.github.mystery2099.voxlib.optimization.ShapeCache
import net.minecraft.util.function.BooleanBiFunction
import net.minecraft.util.shape.VoxelShapes
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class VoxelAssemblyTest {
    @BeforeEach
    fun setUp() {
        ShapeCache.clearCache()
    }

    @Test
    fun `union with no shapes returns empty`() {
        assertTrue(union().isEmpty)
    }

    @Test
    fun `union with one shape returns the same instance`() {
        val shape = VoxelShapes.fullCube()

        assertSame(shape, union(shape))
    }

    @Test
    fun `union ignores empty shapes`() {
        val shape = VoxelShapes.cuboid(0.0, 0.0, 0.0, 0.5, 0.5, 0.5)

        assertEquals(shape, union(VoxelShapes.empty(), shape, VoxelShapes.empty()))
    }

    @Test
    fun `union containing full cube returns full cube`() {
        val partial = VoxelShapes.cuboid(0.0, 0.0, 0.0, 0.5, 0.5, 0.5)

        assertEquals(VoxelShapes.fullCube(), union(partial, VoxelShapes.fullCube()))
    }

    @Test
    fun `combine with no shapes returns empty`() {
        assertTrue(combine(BooleanBiFunction.OR).isEmpty)
    }

    @Test
    fun `combine with one shape returns that shape`() {
        val shape = VoxelShapes.fullCube()

        assertSame(shape, combine(BooleanBiFunction.OR, shape))
    }

    @Test
    fun `combine applies boolean operation across multiple shapes`() {
        val left = VoxelShapes.cuboid(0.0, 0.0, 0.0, 0.5, 1.0, 1.0)
        val right = VoxelShapes.cuboid(0.5, 0.0, 0.0, 1.0, 1.0, 1.0)

        val result = combine(BooleanBiFunction.OR, left, right)

        assertFalse(result.isEmpty)
        assertEquals(VoxelShapes.fullCube().boundingBox, result.boundingBox)
    }
}
