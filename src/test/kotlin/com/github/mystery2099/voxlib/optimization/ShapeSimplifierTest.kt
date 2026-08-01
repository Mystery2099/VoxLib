package com.github.mystery2099.voxlib.optimization

import com.github.mystery2099.voxlib.assertExactShape
import net.minecraft.util.math.Box
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
        val exception = assertThrows(IllegalArgumentException::class.java) {
            ShapeSimplifier.simplify(VoxelShapes.fullCube(), maxBoxes = 0)
        }
        assertEquals("maxBoxes must be at least 1", exception.message)
    }

    @Test
    fun `simplify preserves empty shape`() {
        assertTrue(ShapeSimplifier.simplify(VoxelShapes.empty()).isEmpty)
    }

    @Test
    fun `priority queue preserves legacy merge order for equal distances`() {
        val shape = (0 until 100)
            .map { index ->
                val minX = index * 0.125
                VoxelShapes.cuboid(minX, 0.0, 0.0, minX + 0.0625, 0.0625, 0.0625)
            }
            .fold(VoxelShapes.empty(), VoxelShapes::union)

        assertTrue(countBoxes(shape) in 96..256, "fixture must exercise the priority-queue path")
        assertExactShape(legacySimplify(shape, 3), ShapeSimplifier.simplify(shape, 3))
    }

    @Test
    fun `hybrid simplifier matches legacy simplifier for deterministic shapes`() {
        for (boxCount in listOf(16, 32, 64, 128, 256)) {
            val shape = (0 until boxCount)
                .map { index ->
                    val x = (index % 8) * 0.125
                    val y = (index / 8) * 0.125
                    VoxelShapes.cuboid(x, y, 0.0, x + 0.05, y + 0.05, 0.05)
                }
                .fold(VoxelShapes.empty(), VoxelShapes::union)

            for (limit in listOf(1, 8)) {
                assertExactShape(
                    legacySimplify(shape, limit),
                    ShapeSimplifier.simplify(shape, limit),
                    "boxCount=$boxCount, limit=$limit"
                )
            }
        }
    }

    private fun countBoxes(shape: VoxelShape): Int {
        var count = 0
        shape.forEachBox { _, _, _, _, _, _ -> count++ }
        return count
    }

    // Independent oracle matching the JMH legacy baseline; do not share simplifier code here.
    private fun legacySimplify(shape: VoxelShape, maxBoxes: Int): VoxelShape {
        val boxes = mutableListOf<Box>()
        shape.forEachBox { minX, minY, minZ, maxX, maxY, maxZ ->
            boxes.add(Box(minX, minY, minZ, maxX, maxY, maxZ))
        }
        while (boxes.size > maxBoxes) {
            var firstIndex = 0
            var secondIndex = 1
            var minimumDistance = Double.MAX_VALUE
            for (first in 0 until boxes.size - 1) {
                for (second in first + 1 until boxes.size) {
                    val distance = boxDistance(boxes[first], boxes[second])
                    if (distance < minimumDistance) {
                        minimumDistance = distance
                        firstIndex = first
                        secondIndex = second
                    }
                }
            }
            val first = boxes[firstIndex]
            val second = boxes[secondIndex]
            val merged = Box(
                minOf(first.minX, second.minX),
                minOf(first.minY, second.minY),
                minOf(first.minZ, second.minZ),
                maxOf(first.maxX, second.maxX),
                maxOf(first.maxY, second.maxY),
                maxOf(first.maxZ, second.maxZ)
            )
            boxes.removeAt(secondIndex)
            boxes.removeAt(firstIndex)
            boxes.add(merged)
        }
        return boxes.fold(VoxelShapes.empty()) { result, box ->
            VoxelShapes.union(result, VoxelShapes.cuboid(box))
        }
    }

    private fun boxDistance(first: Box, second: Box): Double {
        if (first.intersects(second)) return 0.0
        val dx = maxOf(0.0, maxOf(first.minX - second.maxX, second.minX - first.maxX))
        val dy = maxOf(0.0, maxOf(first.minY - second.maxY, second.minY - first.maxY))
        val dz = maxOf(0.0, maxOf(first.minZ - second.maxZ, second.minZ - first.maxZ))
        return dx * dx + dy * dy + dz * dz
    }

    private companion object {
        const val DELTA = 0.001
    }
}
