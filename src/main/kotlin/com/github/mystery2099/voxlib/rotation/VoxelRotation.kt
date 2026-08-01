package com.github.mystery2099.voxlib.rotation

import com.github.mystery2099.voxlib.optimization.Minecraft1194ShapeOps
import com.github.mystery2099.voxlib.optimization.ShapeCache
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes

/**
 * A utility object for performing rotations and flips on VoxelShapes.
 * It provides methods to rotate and flip VoxelShapes in various ways.
 *
 * This class uses caching to improve performance for frequently transformed shapes.
 */
object VoxelRotation {

    /**
     * Rotates the given VoxelShape to the left (90 degrees counterclockwise around the Y axis).
     *
     * @return A new VoxelShape after being rotated left.
     *
     * @see flipHorizontal
     * @see rotateRight
     */
    fun VoxelShape.rotateLeft(): VoxelShape {
        return rotateWithCache(this, VoxelShapeTransformation.ROTATE_LEFT)
    }

    /**
     * Flips the given VoxelShape horizontally (180 degrees around the Y axis).
     * This is equivalent to rotating the shape 180 degrees.
     *
     * @return A new VoxelShape after being flipped horizontally.
     *
     * @see rotateLeft
     * @see rotateRight
     * @see flipVertical
     * @see flipZ
     */
    fun VoxelShape.flipHorizontal(): VoxelShape {
        return rotateWithCache(this, VoxelShapeTransformation.FLIP_HORIZONTAL)
    }

    /**
     * Alias for [flipHorizontal] for backward compatibility.
     *
     * @return A new VoxelShape after being flipped horizontally.
     *
     * @see flipHorizontal
     */
    fun VoxelShape.flip(): VoxelShape = this.flipHorizontal()

    /**
     * Rotates the given VoxelShape to the right (90 degrees clockwise around the Y axis).
     *
     * @return A new VoxelShape after being rotated right.
     *
     * @see rotateLeft
     * @see flipHorizontal
     */
    fun VoxelShape.rotateRight(): VoxelShape {
        return rotateWithCache(this, VoxelShapeTransformation.ROTATE_RIGHT)
    }

    /**
     * Flips the given VoxelShape vertically (180 degrees around the X axis).
     * This transforms the top face to the bottom and vice versa.
     *
     * @return A new VoxelShape after being flipped vertically.
     *
     * @see flipHorizontal
     * @see flipZ
     * @see rotate
     */
    fun VoxelShape.flipVertical(): VoxelShape {
        return rotateWithCache(this, VoxelShapeTransformation.FLIP_VERTICAL)
    }

    /**
     * Flips the given VoxelShape along the Z axis (180 degrees around the Z axis).
     * This transforms the front face to the back and vice versa.
     *
     * @return A new VoxelShape after being flipped along the Z axis.
     *
     * @see flipHorizontal
     * @see flipVertical
     * @see rotate
     */
    fun VoxelShape.flipZ(): VoxelShape {
        return rotateWithCache(this, VoxelShapeTransformation.FLIP_Z)
    }

    /**
     * Rotates or flips the given VoxelShape using the specified transformation.
     * This is a general-purpose method that can apply any transformation defined in [VoxelShapeTransformation].
     *
     * Note: This method was added in version 1.2.0 to provide a more flexible API.
     * For backward compatibility, you can continue to use the specific methods like
     * [rotateLeft], [rotateRight], [flipHorizontal], etc.
     *
     * @param transformation The transformation to apply.
     * @return A new VoxelShape after the transformation has been applied.
     */
    @JvmName("rotateWithTransformation")
    fun VoxelShape.rotate(transformation: VoxelShapeTransformation): VoxelShape {
        return when (transformation) {
            VoxelShapeTransformation.ROTATE_LEFT -> this.rotateLeft()
            VoxelShapeTransformation.ROTATE_RIGHT -> this.rotateRight()
            VoxelShapeTransformation.FLIP_HORIZONTAL -> this.flipHorizontal()
            VoxelShapeTransformation.FLIP_VERTICAL -> this.flipVertical()
            VoxelShapeTransformation.FLIP_Z -> this.flipZ()
        }
    }

    /**
     * Helper method to rotate a shape with caching.
     *
     * @param shape The shape to rotate.
     * @param transformation The transformation to apply.
     * @return The rotated shape.
     */
    private fun rotateWithCache(
        shape: VoxelShape,
        transformation: VoxelShapeTransformation
    ): VoxelShape {
        // Handle special cases for better performance
        if (shape.isEmpty) return VoxelShapes.empty()
        if (shape === VoxelShapes.fullCube()) return VoxelShapes.fullCube()

        return ShapeCache.getOrComputeTransformation(shape, transformation) {
            Minecraft1194ShapeOps.transformBoxes(shape, transformation)
        }
    }
}
