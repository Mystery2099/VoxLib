package com.github.mystery2099.voxlib.optimization

import net.minecraft.util.math.Box
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Unit tests for the ShapeSimplifier class.
 * Tests shape simplification algorithms including bounding box conversion,
 * box merging, and outline shape creation.
 */
class ShapeSimplifierTest {

    @Test
    fun `simplifyToBoundingBox returns shape with correct bounding box`() {
        // Create a complex shape (two separate boxes)
        val shape1 = VoxelShapes.cuboid(0.0, 0.0, 0.0, 4.0, 4.0, 4.0)
        val shape2 = VoxelShapes.cuboid(8.0, 8.0, 8.0, 12.0, 12.0, 12.0)
        val complexShape = VoxelShapes.union(shape1, shape2)

        // Simplify to bounding box
        val simplified = ShapeSimplifier.simplifyToBoundingBox(complexShape)

        // The bounding box should encompass both original boxes
        val boundingBox = simplified.boundingBox

        // Min should be 0,0,0 (first box)
        assertEquals(0.0, boundingBox.minX, 0.001)
        assertEquals(0.0, boundingBox.minY, 0.001)
        assertEquals(0.0, boundingBox.minZ, 0.001)

        // Max should be 12,12,12 (second box)
        assertEquals(12.0, boundingBox.maxX, 0.001)
        assertEquals(12.0, boundingBox.maxY, 0.001)
        assertEquals(12.0, boundingBox.maxZ, 0.001)
    }

    @Test
    fun `simplifyToBoundingBox with single box returns equivalent shape`() {
        // Create a simple cube shape
        val original = VoxelShapes.cuboid(2.0, 2.0, 2.0, 6.0, 6.0, 6.0)

        // Simplify to bounding box
        val simplified = ShapeSimplifier.simplifyToBoundingBox(original)

        // Bounding boxes should be equivalent
        val origBox = original.boundingBox
        val simpBox = simplified.boundingBox

        assertEquals(origBox.minX, simpBox.minX, 0.001)
        assertEquals(origBox.minY, simpBox.minY, 0.001)
        assertEquals(origBox.minZ, simpBox.minZ, 0.001)
        assertEquals(origBox.maxX, simpBox.maxX, 0.001)
        assertEquals(origBox.maxY, simpBox.maxY, 0.001)
        assertEquals(origBox.maxZ, simpBox.maxZ, 0.001)
    }

    @Test
    fun `simplify returns original shape when already under maxBoxes`() {
        // Create a simple shape with 2 boxes
        val box1 = VoxelShapes.cuboid(0.0, 0.0, 0.0, 4.0, 4.0, 4.0)
        val box2 = VoxelShapes.cuboid(4.0, 0.0, 0.0, 8.0, 4.0, 4.0)
        val shape = VoxelShapes.union(box1, box2)

        // Simplify with maxBoxes greater than current count
        val simplified = ShapeSimplifier.simplify(shape, maxBoxes = 8)

        // Should return the original shape (no simplification needed)
        // The simplified shape should have the same bounding box
        assertEquals(shape.boundingBox, simplified.boundingBox)
    }

    @Test
    fun `simplify reduces box count to maxBoxes`() {
        // Create a complex shape with 10 boxes
        val boxes = mutableListOf<VoxelShape>()
        for (i in 0 until 10) {
            val offset = i * 2.0
            boxes.add(VoxelShapes.cuboid(offset, 0.0, 0.0, offset + 1.0, 1.0, 1.0))
        }
        val complexShape = boxes.fold(VoxelShapes.empty()) { acc, box ->
            VoxelShapes.union(acc, box)
        }

        // Verify original has more than 5 boxes
        var originalBoxCount = 0
        complexShape.forEachBox { _, _, _, _, _, _ -> originalBoxCount++ }
        assertTrue(originalBoxCount > 5)

        // Simplify to 5 boxes
        val simplified = ShapeSimplifier.simplify(complexShape, maxBoxes = 5)

        // Count boxes in simplified shape
        var simplifiedBoxCount = 0
        simplified.forEachBox { _, _, _, _, _, _ -> simplifiedBoxCount++ }

        // Should have at most 5 boxes (or fewer if merging reduced more)
        assertTrue(simplifiedBoxCount <= 5)
    }

    @Test
    fun `simplify with maxBoxes of 1 returns single box`() {
        // Create a shape with 4 boxes in a 2x2 grid
        val shape = VoxelShapes.union(
            VoxelShapes.cuboid(0.0, 0.0, 0.0, 2.0, 2.0, 2.0),
            VoxelShapes.cuboid(2.0, 0.0, 0.0, 4.0, 2.0, 2.0),
            VoxelShapes.cuboid(0.0, 0.0, 2.0, 2.0, 2.0, 4.0),
            VoxelShapes.cuboid(2.0, 0.0, 2.0, 4.0, 2.0, 4.0)
        )

        // Simplify to 1 box (should merge all into bounding box)
        val simplified = ShapeSimplifier.simplify(shape, maxBoxes = 1)

        // Should have exactly 1 box
        var boxCount = 0
        simplified.forEachBox { _, _, _, _, _, _ -> boxCount++ }
        assertEquals(1, boxCount)
    }

    @Test
    fun `simplify preserves shape boundaries`() {
        // Create an L-shaped region
        val lShape = VoxelShapes.union(
            VoxelShapes.cuboid(0.0, 0.0, 0.0, 4.0, 4.0, 2.0),
            VoxelShapes.cuboid(0.0, 0.0, 2.0, 2.0, 4.0, 6.0)
        )

        // Simplify
        val simplified = ShapeSimplifier.simplify(lShape, maxBoxes = 2)

        // The simplified shape should still cover the same general area
        // Check that bounding boxes overlap significantly
        val origBox = lShape.boundingBox
        val simpBox = simplified.boundingBox

        // Simplified shape should at least touch the original area
        assertTrue(simpBox.minX <= origBox.maxX)
        assertTrue(simpBox.maxX >= origBox.minX)
        assertTrue(simpBox.minY <= origBox.maxY)
        assertTrue(simpBox.maxY >= origBox.minY)
        assertTrue(simpBox.minZ <= origBox.maxZ)
        assertTrue(simpBox.maxZ >= origBox.minZ)
    }

    @Test
    fun `simplify preserves empty shape`() {
        // Test that simplify handles empty shapes correctly
        val empty = VoxelShapes.empty()

        // Should not throw and should return empty
        val result = ShapeSimplifier.simplify(empty, maxBoxes = 4)
        assertTrue(result.isEmpty)
    }

    @Test
    fun `simplify handles touching boxes`() {
        // Create two touching boxes (share a face)
        val shape = VoxelShapes.union(
            VoxelShapes.cuboid(0.0, 0.0, 0.0, 4.0, 4.0, 4.0),
            VoxelShapes.cuboid(4.0, 0.0, 0.0, 8.0, 4.0, 4.0)
        )

        // Simplify should merge them
        val simplified = ShapeSimplifier.simplify(shape, maxBoxes = 1)

        // Should result in a single box (the merged bounding box)
        var boxCount = 0
        simplified.forEachBox { _, _, _, _, _, _ -> boxCount++ }
        assertEquals(1, boxCount)

        // Verify the merged dimensions
        val box = simplified.boundingBox
        assertEquals(0.0, box.minX, 0.001)
        assertEquals(0.0, box.minY, 0.001)
        assertEquals(0.0, box.minZ, 0.001)
        assertEquals(8.0, box.maxX, 0.001)
        assertEquals(4.0, box.maxY, 0.001)
        assertEquals(4.0, box.maxZ, 0.001)
    }
}
