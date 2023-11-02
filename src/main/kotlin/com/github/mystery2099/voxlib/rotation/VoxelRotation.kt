package com.github.mystery2099.voxlib.rotation

import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes

/**
 * A utility object for performing rotations and flips on VoxelShapes.
 * It provides methods to rotate left, rotate right, and flip a VoxelShape.
 */
object VoxelRotation {

    /**
     * Rotates the given VoxelShape to the left.
     *
     * @return A new VoxelShape after being rotated left.
     *
     * @see flip
     * @see rotateRight
     * @see rotate
     */
    fun VoxelShape.rotateLeft(): VoxelShape = this.rotate(VoxelShapeTransformation.ROTATE_LEFT)

    /**
     * Flips the given VoxelShape horizontally.
     *
     * @return A new VoxelShape after being flipped.
     *
     * @see rotateLeft
     * @see rotateRight
     * @see rotate
     */
    fun VoxelShape.flip(): VoxelShape = this.rotate(VoxelShapeTransformation.FLIP_HORIZONTAL)

    /**
     * Rotates the given VoxelShape to the right.
     *
     * @return A new VoxelShape after being rotated right.
     *
     * @see rotateLeft
     * @see flip
     * @see rotate
     */
    fun VoxelShape.rotateRight(): VoxelShape = this.rotate(VoxelShapeTransformation.ROTATE_RIGHT)

    /**
     * Rotates or flips the given VoxelShape using the specified transformation.
     *
     * @param transformation The transformation to apply.
     *
     * @return A new VoxelShape after being rotated or flipped.
     *
     * @see rotateLeft
     * @see rotateRight
     * @see flip
     * @see VoxelShapeTransformation
     */
    private fun VoxelShape.rotate(transformation: VoxelShapeTransformation): VoxelShape {
        val shapes = mutableListOf(VoxelShapes.empty())
        this.forEachBox { minX, minY, minZ, maxX, maxY, maxZ ->
            val adjustedValues = adjustValues(transformation, minX, minZ, maxX, maxZ)
            shapes += VoxelShapes.cuboid(
                adjustedValues[0], minY,
                adjustedValues[1], adjustedValues[2], maxY, adjustedValues[3]
            )
        }
        return shapes.reduce { a, b -> VoxelShapes.union(a, b) }
    }

    /**
     * Adjusts values based on the given transformation, simulating the rotation or flip.
     *
     * @param direction The transformation to apply.
     * @param minX The minimum X-coordinate.
     * @param minZ The minimum Z-coordinate.
     * @param maxX The maximum X-coordinate.
     * @param maxZ The maximum Z-coordinate.
     *
     * @return An array of adjusted values.
     *
     * @see rotate
     * @see VoxelShapeTransformation
     */
    private fun adjustValues(
        direction: VoxelShapeTransformation,
        minX: Double,
        minZ: Double,
        maxX: Double,
        maxZ: Double
    ) = when (direction) {
        VoxelShapeTransformation.FLIP_HORIZONTAL -> doubleArrayOf(1.0f - maxX, 1.0f - maxZ, 1.0f - minX, 1.0f - minZ)
        VoxelShapeTransformation.ROTATE_RIGHT -> doubleArrayOf(minZ, 1.0f - maxX, maxZ, 1.0f - minX)
        VoxelShapeTransformation.ROTATE_LEFT -> doubleArrayOf(1.0f - maxZ, minX, 1.0f - minZ, maxX)
    }
}