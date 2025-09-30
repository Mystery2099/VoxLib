package com.github.mystery2099.voxlib.rotation

import com.github.mystery2099.voxlib.optimization.ShapeCache
import com.github.mystery2099.voxlib.optimization.ShapeCacheKey
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.phys.shapes.VoxelShape
import com.github.mystery2099.voxlib.union

/**
 * A utility object for performing rotations and flips on Shapes.
 * It provides methods to rotate and flip Shapes in various ways.
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
        return rotateVerticalWithCache(this, VoxelShapeTransformation.FLIP_VERTICAL)
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
        return rotateVerticalWithCache(this, VoxelShapeTransformation.FLIP_Z)
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
     * @param useCache Whether to use caching (default: true).
     * @return The rotated shape.
     */
    private fun rotateWithCache(
        shape: VoxelShape,
        transformation: VoxelShapeTransformation,
        useCache: Boolean = true
    ): VoxelShape {
        // Handle special cases for better performance
        if(shape.isEmpty) return Shapes.empty()
        if(shape == Shapes.block()) return Shapes.block()

        if (!useCache) return shape.rotateUncached(transformation)

        val cacheKey = ShapeCacheKey(
            originalShapeHash = shape.hashCode(),
            operationId = transformation.name
        )

        return ShapeCache.getOrCompute(cacheKey) {
            shape.rotateUncached(transformation)
        }
    }

    /**
     * Helper method to rotate a shape vertically with caching.
     *
     * @param shape The shape to rotate.
     * @param transformation The transformation to apply.
     * @param useCache Whether to use caching (default: true).
     * @return The rotated shape.
     */
    private fun rotateVerticalWithCache(
        shape: VoxelShape,
        transformation: VoxelShapeTransformation,
        useCache: Boolean = true
    ): VoxelShape {
        if (shape.isEmpty || shape == Shapes.block()) return shape

        if (!useCache) return shape.rotateVerticalUncached(transformation)

        val cacheKey = ShapeCacheKey(
            originalShapeHash = shape.hashCode(),
            operationId = transformation.name
        )

        return ShapeCache.getOrCompute(cacheKey) {
            shape.rotateVerticalUncached(transformation)
        }
    }

    /**
     * Rotates or flips the given VoxelShape using the specified transformation around the Y axis.
     * This version doesn't use caching and is used internally by the cached methods.
     *
     * @param transformation The transformation to apply.
     * @return A new VoxelShape after being rotated or flipped.
     */
    private fun VoxelShape.rotateUncached(transformation: VoxelShapeTransformation): VoxelShape {
        if (this.isEmpty || this == Shapes.block()) return this

        val shapes = mutableListOf<VoxelShape>()
        forAllBoxes { minX, minY, minZ, maxX, maxY, maxZ ->
            val adjustedValues = adjustValues(transformation, minX, minZ, maxX, maxZ)
            shapes.add(
                Shapes.box(
                adjustedValues[0], minY,
                adjustedValues[1], adjustedValues[2], maxY, adjustedValues[3]
                )
            )
        }

        // Optimize the union operation for better performance
        return optimizedUnion(shapes)
    }

    /**
     * Rotates or flips the given VoxelShape vertically using the specified transformation.
     * This version doesn't use caching and is used internally by the cached methods.
     *
     * @param transformation The transformation to apply.
     * @return A new VoxelShape after being rotated or flipped vertically.
     */
    private fun VoxelShape.rotateVerticalUncached(transformation: VoxelShapeTransformation): VoxelShape {
        if (this.isEmpty || this == Shapes.block()) return this

        val shapes = mutableListOf<VoxelShape>()
        this.forAllBoxes { minX, minY, minZ, maxX, maxY, maxZ ->
            // Apply the appropriate transformation
            val newCoords = when (transformation) {
                VoxelShapeTransformation.FLIP_VERTICAL -> {
                    // Flip around X axis (top to bottom)
                    doubleArrayOf(minX, 1.0 - maxY, minZ, maxX, 1.0 - minY, maxZ)
                }

                VoxelShapeTransformation.FLIP_Z -> {
                    // Flip around Z axis (front to back)
                    doubleArrayOf(minX, minY, 1.0 - maxZ, maxX, maxY, 1.0 - minZ)
                }

                else -> {
                    // This should not happen, but return original coordinates if it does
                    doubleArrayOf(minX, minY, minZ, maxX, maxY, maxZ)
                }
            }

            // Create a new cuboid with the transformed coordinates
            shapes += Shapes.box(
                newCoords[0], newCoords[1], newCoords[2],
                newCoords[3], newCoords[4], newCoords[5]
            )
        }

        // Optimize the union operation for better performance
        return optimizedUnion(shapes)
    }

    /**
     * Optimized method to combine multiple shapes using union operation.
     * This uses a divide-and-conquer approach for better performance with many shapes.
     *
     * @param shapes The list of shapes to combine.
     * @return A single VoxelShape representing the union of all input shapes.
     */
    private fun optimizedUnion(shapes: List<VoxelShape>): VoxelShape {
        if (shapes.isEmpty()) return Shapes.empty()
        if (shapes.size == 1) return shapes[0]

        // Use a divide-and-conquer approach for better performance
        return when {
            shapes.size <= 4 -> shapes.reduce { a, b -> union(a, b) }
            else -> {
                val mid = shapes.size / 2
                val left = optimizedUnion(shapes.subList(0, mid))
                val right = optimizedUnion(shapes.subList(mid, shapes.size))
                union(left, right)
            }
        }
    }

    /**
     * Adjusts values based on the given transformation, simulating the rotation or flip around the Y axis.
     *
     * @param direction The transformation to apply.
     * @param minX The minimum X-coordinate.
     * @param minZ The minimum Z-coordinate.
     * @param maxX The maximum X-coordinate.
     * @param maxZ The maximum Z-coordinate.
     *
     * @return An array of adjusted values.
     */
    private fun adjustValues(
        direction: VoxelShapeTransformation,
        minX: Double,
        minZ: Double,
        maxX: Double,
        maxZ: Double
    ) = when (direction) {
        VoxelShapeTransformation.FLIP_HORIZONTAL -> doubleArrayOf(1.0 - maxX, 1.0 - maxZ, 1.0 - minX, 1.0 - minZ)
        VoxelShapeTransformation.ROTATE_RIGHT -> doubleArrayOf(minZ, 1.0 - maxX, maxZ, 1.0 - minX)
        VoxelShapeTransformation.ROTATE_LEFT -> doubleArrayOf(1.0 - maxZ, minX, 1.0 - minZ, maxX)
        else -> doubleArrayOf(minX, minZ, maxX, maxZ) // Should not happen
    }
}