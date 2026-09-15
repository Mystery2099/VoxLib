package com.github.mystery2099.voxlib.combination

import com.github.mystery2099.voxlib.assertExactShape
import com.github.mystery2099.voxlib.combination.VoxelAssembly.combine
import com.github.mystery2099.voxlib.combination.VoxelAssembly.plus
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
import kotlin.random.Random

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

    @Test
    fun `binary operator is exactly equivalent to vanilla union`() {
        val first = VoxelShapes.cuboid(0.03125, 0.125, 0.25, 0.5625, 0.875, 0.75)
        val second = VoxelShapes.cuboid(0.4375, 0.0, 0.5, 0.96875, 0.625, 1.0)

        assertExactShape(VoxelShapes.union(first, second), first + second)
    }

    @Test
    fun `multi union preserves duplicate and disjoint inputs exactly`() {
        val first = VoxelShapes.cuboid(0.0, 0.0, 0.0, 0.2, 0.2, 0.2)
        val second = VoxelShapes.cuboid(0.8, 0.8, 0.8, 1.0, 1.0, 1.0)
        val expected = VoxelShapes.union(first, first, second)

        assertExactShape(expected, union(first, first, second))
        assertExactShape(expected, union(second, first, first))
    }

    @Test
    fun `large multi union is exactly equivalent to vanilla`() {
        val shapes = Array(32) { index ->
            val coordinate = index * 0.04
            VoxelShapes.cuboid(
                coordinate, 0.0, 0.0,
                coordinate + 0.02, 0.5, 0.5
            )
        }

        assertExactShape(vanillaUnion(shapes), union(*shapes))
    }

    @Test
    fun `deterministic random unions match vanilla`() {
        val random = Random(2099)
        repeat(25) {
            val shapes = Array(12) {
                val minX = random.nextDouble(0.0, 0.8)
                val minY = random.nextDouble(0.0, 0.8)
                val minZ = random.nextDouble(0.0, 0.8)
                VoxelShapes.cuboid(
                    minX, minY, minZ,
                    minX + random.nextDouble(0.05, 0.2),
                    minY + random.nextDouble(0.05, 0.2),
                    minZ + random.nextDouble(0.05, 0.2)
                )
            }
            assertExactShape(vanillaUnion(shapes), union(*shapes), "Random union iteration $it")
        }
    }

    private fun vanillaUnion(shapes: Array<out net.minecraft.util.shape.VoxelShape>) =
        shapes.drop(1).fold(shapes.first()) { result, shape ->
            VoxelShapes.union(result, shape)
        }
}
