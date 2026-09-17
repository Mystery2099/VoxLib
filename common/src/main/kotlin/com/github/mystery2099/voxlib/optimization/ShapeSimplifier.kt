package com.github.mystery2099.voxlib.optimization

import com.github.mystery2099.voxlib.combination.VoxelAssembly.createCuboidShape
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.shapes.VoxelShape
import net.minecraft.world.phys.shapes.Shapes

/**
 * A utility class for simplifying complex Shapes to improve performance.
 *
 * Complex shapes with many boxes can cause performance issues, especially
 * when used for outlines that change frequently. This class provides methods
 * to create simplified versions of shapes for use in outlines.
 */
object ShapeSimplifier {

    /**
     * Creates a simplified version of a complex shape by using its bounding box.
     * This is useful for outline shapes that don't need to be as detailed as collision shapes.
     *
     * @param shape The complex shape to simplify.
     * @return A simplified VoxelShape based on the original's bounding box.
     */
    fun simplifyToBoundingBox(shape: VoxelShape): VoxelShape {
        if (shape.isEmpty) return Shapes.empty()

        val boundingBox = shape.bounds()
        return Shapes.create(boundingBox)
    }

    /**
     * Creates a simplified version of a complex shape by reducing the number of boxes.
     * This method merges boxes that are close to each other or overlap.
     *
     * @param shape The complex shape to simplify.
     * @param maxBoxes The maximum number of boxes in the simplified shape.
     * @return A simplified VoxelShape with fewer boxes.
     */
    fun simplify(shape: VoxelShape, maxBoxes: Int = 8): VoxelShape {
        require(maxBoxes >= 1) { "maxBoxes must be at least 1" }

        val boxes = mutableListOf<AABB>()
        shape.forAllBoxes { minX, minY, minZ, maxX, maxY, maxZ ->
            boxes.add(AABB(minX, minY, minZ, maxX, maxY, maxZ))
        }

        if (boxes.size <= maxBoxes) return shape

        // Mid-size inputs use the Java priority-queue merger; tiny/huge inputs use
        // the O(n²) scan (avoids queue candidate storage when n is very large).
        val simplifiedBoxes = if (boxes.size in PRIORITY_QUEUE_BOX_RANGE) {
            mergeClosestBoxesWithQueue(boxes, maxBoxes)
        } else {
            mergeClosestBoxesWithScan(boxes, maxBoxes)
        }

        return simplifiedBoxes.fold(Shapes.empty()) { acc, box ->
            Shapes.or(acc, Shapes.create(box))
        }
    }

    private fun mergeClosestBoxesWithQueue(boxes: List<AABB>, maxBoxes: Int): List<AABB> =
        DeterministicBoxMerger.mergeClosest(boxes, maxBoxes)

    private fun mergeClosestBoxesWithScan(boxes: MutableList<AABB>, maxBoxes: Int): List<AABB> {
        while (boxes.size > maxBoxes) {
            mergeClosestPairWithScan(boxes)
        }
        return boxes
    }

    private fun mergeClosestPairWithScan(boxes: MutableList<AABB>) {
        var closestFirst = 0
        var closestSecond = 1
        var minimumDistance = Double.MAX_VALUE
        for (i in 0 until boxes.size - 1) {
            for (j in i + 1 until boxes.size) {
                val distance = calculateBoxDistance(boxes[i], boxes[j])
                if (distance < minimumDistance) {
                    minimumDistance = distance
                    closestFirst = i
                    closestSecond = j
                }
            }
        }

        val mergedBox = mergeBoxes(boxes[closestFirst], boxes[closestSecond])
        boxes.removeAt(closestSecond)
        boxes.removeAt(closestFirst)
        boxes.add(mergedBox)
    }

    /**
     * Calculates the distance between two boxes.
     * Overlapping boxes have a distance of 0.
     *
     * @param box1 The first box.
     * @param box2 The second box.
     * @return The distance between the boxes.
     */
    private fun calculateBoxDistance(box1: AABB, box2: AABB): Double {
        val dx = maxOf(0.0, maxOf(box1.minX - box2.maxX, box2.minX - box1.maxX))
        val dy = maxOf(0.0, maxOf(box1.minY - box2.maxY, box2.minY - box1.maxY))
        val dz = maxOf(0.0, maxOf(box1.minZ - box2.maxZ, box2.minZ - box1.maxZ))

        return dx * dx + dy * dy + dz * dz
    }

    /**
     * Merges two boxes into one that encompasses both.
     *
     * @param box1 The first box.
     * @param box2 The second box.
     * @return A new box that contains both input boxes.
     */
    private fun mergeBoxes(box1: AABB, box2: AABB): AABB {
        return AABB(
            minOf(box1.minX, box2.minX),
            minOf(box1.minY, box2.minY),
            minOf(box1.minZ, box2.minZ),
            maxOf(box1.maxX, box2.maxX),
            maxOf(box1.maxY, box2.maxY),
            maxOf(box1.maxZ, box2.maxZ)
        )
    }

    /**
     * Creates a simplified outline shape for a block with the given dimensions.
     * This creates a hollow box shape that's more efficient than a complex shape.
     *
     * @param minX The minimum X coordinate.
     * @param minY The minimum Y coordinate.
     * @param minZ The minimum Z coordinate.
     * @param maxX The maximum X coordinate.
     * @param maxY The maximum Y coordinate.
     * @param maxZ The maximum Z coordinate.
     * @param thickness The thickness of the outline (default is 1).
     * @return A simplified hollow box VoxelShape.
     */
    fun createOutlineShape(
        minX: Number, minY: Number, minZ: Number,
        maxX: Number, maxY: Number, maxZ: Number,
        thickness: Number = 1
    ): VoxelShape {
        val t = thickness.toDouble()
        val minXd = minX.toDouble()
        val minYd = minY.toDouble()
        val minZd = minZ.toDouble()
        val maxXd = maxX.toDouble()
        val maxYd = maxY.toDouble()
        val maxZd = maxZ.toDouble()

        val outerBox = createCuboidShape(minXd, minYd, minZd, maxXd, maxYd, maxZd)
        if (maxXd - minXd <= 2 * t || maxYd - minYd <= 2 * t || maxZd - minZd <= 2 * t) {
            return outerBox
        }

        val innerBox = createCuboidShape(
            minXd + t, minYd + t, minZd + t,
            maxXd - t, maxYd - t, maxZd - t
        )
        return Shapes.join(
            outerBox, innerBox,
            net.minecraft.world.phys.shapes.BooleanOp.ONLY_FIRST
        )
    }

    /** AABB counts where the priority-queue merger wins; outside this, use the scan. */
    private val PRIORITY_QUEUE_BOX_RANGE = 96..256
}
