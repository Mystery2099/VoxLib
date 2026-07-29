package com.github.mystery2099.voxlib.optimization

import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ShapeSimplifierTest {
    @Test
    fun `simplifyToBoundingBox encompasses complex shape`() {
        val shape = VoxelShapes.union(
            VoxelShapes.cuboid(0.0, 0.0, 0.0, 4.0, 4.0, 4.0),
            VoxelShapes.cuboid(8.0, 8.0, 8.0, 12.0, 12.0, 12.0)
        )

        val box = ShapeSimplifier.simplifyToBoundingBox(shape).boundingBox

        assertEquals(0.0, box.minX, DELTA)
        assertEquals(0.0, box.minY, DELTA)
        assertEquals(0.0, box.minZ, DELTA)
        assertEquals(12.0, box.maxX, DELTA)
        assertEquals(12.0, box.maxY, DELTA)
        assertEquals(12.0, box.maxZ, DELTA)
    }

    @Test
    fun `simplifyToBoundingBox preserves empty shape`() {
        assertTrue(ShapeSimplifier.simplifyToBoundingBox(VoxelShapes.empty()).isEmpty)
    }

    @Test
    fun `simplify returns original shape when already under limit`() {
        val shape = VoxelShapes.union(
            VoxelShapes.cuboid(0.0, 0.0, 0.0, 4.0, 4.0, 4.0),
            VoxelShapes.cuboid(4.0, 0.0, 0.0, 8.0, 4.0, 4.0)
        )

        assertSame(shape, ShapeSimplifier.simplify(shape, maxBoxes = 8))
    }

    @Test
    fun `simplify reduces box count to limit`() {
        val shape = (0 until 10)
            .map { index ->
                val offset = index * 2.0
                VoxelShapes.cuboid(offset, 0.0, 0.0, offset + 1.0, 1.0, 1.0)
            }
            .fold(VoxelShapes.empty(), VoxelShapes::union)

        assertTrue(countBoxes(ShapeSimplifier.simplify(shape, maxBoxes = 5)) <= 5)
    }

    @Test
    fun `simplify with limit one returns one box`() {
        val shape = VoxelShapes.union(
            VoxelShapes.cuboid(0.0, 0.0, 0.0, 2.0, 2.0, 2.0),
            VoxelShapes.cuboid(2.0, 0.0, 0.0, 4.0, 2.0, 2.0),
            VoxelShapes.cuboid(0.0, 0.0, 2.0, 2.0, 2.0, 4.0),
            VoxelShapes.cuboid(2.0, 0.0, 2.0, 4.0, 2.0, 4.0)
        )

        assertEquals(1, countBoxes(ShapeSimplifier.simplify(shape, maxBoxes = 1)))
    }

    @Test
    fun `simplify rejects limit below one`() {
        assertThrows(IllegalArgumentException::class.java) {
            ShapeSimplifier.simplify(VoxelShapes.fullCube(), maxBoxes = 0)
        }
    }

    @Test
    fun `simplify preserves empty shape`() {
        assertTrue(ShapeSimplifier.simplify(VoxelShapes.empty()).isEmpty)
    }

    private fun countBoxes(shape: VoxelShape): Int {
        var count = 0
        shape.forEachBox { _, _, _, _, _, _ -> count++ }
        return count
    }

    private companion object {
        const val DELTA = 0.001
    }
}
