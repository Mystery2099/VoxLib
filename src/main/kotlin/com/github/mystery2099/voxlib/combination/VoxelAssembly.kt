package com.github.mystery2099.voxlib.combination

import com.github.mystery2099.voxlib.optimization.ShapeCache
import com.github.mystery2099.voxlib.optimization.ShapeSimplifier
import com.github.mystery2099.voxlib.optimization.Minecraft1194ShapeOps
import net.minecraft.util.function.BooleanBiFunction
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes

/**
 * A utility object for working with VoxelShapes in Minecraft game development.
 *
 * This object provides various functions for creating, combining, and modifying VoxelShapes,
 * which are used to represent the collision and shape of objects in the game world.
 *
 * This class includes optimizations for better performance with complex shapes.
 * Fixed block shapes should still be constructed once and stored in a field or
 * companion object so shape queries perform no construction or cache lookup.
 */
object VoxelAssembly {
    private const val MAX_CACHED_UNION_SHAPES = 16

    /**
     * Creates a cuboid shape using the provided minimum and maximum coordinates.
     *
     * Each param should be between 0 and 16
     * @param minX The minimum X-coordinate of the cuboid.
     * @param minY The minimum Y-coordinate of the cuboid.
     * @param minZ The minimum Z-coordinate of the cuboid.
     * @param maxX The maximum X-coordinate of the cuboid.
     * @param maxY The maximum Y-coordinate of the cuboid.
     * @param maxZ The maximum Z-coordinate of the cuboid.
     *
     * @return A VoxelShape representing the cuboid with the specified dimensions.
     */
    fun createCuboidShape(
        minX: Number,
        minY: Number,
        minZ: Number,
        maxX: Number,
        maxY: Number,
        maxZ: Number
    ): VoxelShape = VoxelShapes.cuboid(
        minX.toDouble() / BLOCK_COORDINATE_SCALE,
        minY.toDouble() / BLOCK_COORDINATE_SCALE,
        minZ.toDouble() / BLOCK_COORDINATE_SCALE,
        maxX.toDouble() / BLOCK_COORDINATE_SCALE,
        maxY.toDouble() / BLOCK_COORDINATE_SCALE,
        maxZ.toDouble() / BLOCK_COORDINATE_SCALE
    )

    /**
     * Combines the receiver VoxelShape with another VoxelShape using the union operation.
     *
     * @param otherShape The VoxelShape to be unified with the receiver.
     *
     * @return A new VoxelShape representing the union of the two input shapes.
     *
     * @see VoxelShapes.union
     */
    infix fun VoxelShape.and(otherShape: VoxelShape): VoxelShape {
        return unionWithCache(this, otherShape)
    }

    /**
     * Allows the use of the '+' and '+=' operators to combine VoxelShapes.
     *
     * @param otherShape The VoxelShape to be combined with the receiver using the union operation.
     *
     * @return A new VoxelShape representing the union of the two input shapes.
     *
     * @see VoxelShapes.union
     * @see and
     */
    operator fun VoxelShape.plus(otherShape: VoxelShape): VoxelShape =
        this.and(otherShape)

    /**
     * Internal helper function to combine two shapes with caching.
     *
     * @param shape1 The first shape to combine.
     * @param shape2 The second shape to combine.
     * @param useCache Whether to use the shape cache (default: true).
     * @return The combined shape.
     */
    private fun unionWithCache(shape1: VoxelShape, shape2: VoxelShape, useCache: Boolean = true): VoxelShape {
        // Handle special cases for better performance
        if (shape1.isEmpty) return shape2
        if (shape2.isEmpty) return shape1
        if (shape1 == VoxelShapes.fullCube()) return shape1
        if (shape2 == VoxelShapes.fullCube()) return shape2

        if (!useCache) return VoxelShapes.union(shape1, shape2)

        return ShapeCache.getOrComputeUnion(shape1, shape2) {
            VoxelShapes.union(shape1, shape2)
        }
    }

    /**
     * Combines a single VoxelShape with a list of other VoxelShapes using the [VoxelAssembly.union] operation.
     *
     * @param otherShapes A list of VoxelShapes to be unified with the receiver.
     *
     * @return A new VoxelShape representing the union of all input shapes.
     *
     * @see union
     */
    /**
     * Combines a single VoxelShape with a list of other VoxelShapes using the union operation.
     *
     * @param otherShapes Additional VoxelShapes to combine with the receiver.
     * @return A new VoxelShape representing the union of all shapes.
     */
    fun VoxelShape.unifyWith(vararg otherShapes: VoxelShape): VoxelShape = union(this, *otherShapes)


    /**
     * Combines a list of VoxelShapes using the provided boolean function.
     *
     * @param function The boolean function used for combining VoxelShapes.
     * @param voxelShapes A list of VoxelShapes to be combined.
     *
     * @return A new VoxelShape resulting from the combination of the input shapes.
     *
     * @see VoxelShapes.combine
     */
    fun combine(function: BooleanBiFunction, vararg voxelShapes: VoxelShape): VoxelShape {
        if (voxelShapes.isEmpty()) return VoxelShapes.empty()

        return voxelShapes.reduce { a, b -> VoxelShapes.combine(a, b, function) }
    }

    /**
     * Unifies or combines a list of VoxelShapes into a single VoxelShape.
     * Uses an optimized algorithm for better performance with many shapes.
     *
     * @param voxelShapes A list of VoxelShapes to be unified.
     *
     * @return A new VoxelShape representing the union of all input shapes.
     *
     * @see VoxelShapes.union
     */
    fun union(vararg voxelShapes: VoxelShape): VoxelShape {
        if (voxelShapes.isEmpty()) return VoxelShapes.empty()
        if (voxelShapes.size == 1) return voxelShapes[0]

        var nonEmptyCount = 0
        for (shape in voxelShapes) {
            if (shape === VoxelShapes.fullCube()) return VoxelShapes.fullCube()
            if (!shape.isEmpty) nonEmptyCount++
        }
        if (nonEmptyCount == 0) return VoxelShapes.empty()
        if (nonEmptyCount == 1) return voxelShapes.first { !it.isEmpty }

        val nonEmptyShapes = if (nonEmptyCount == voxelShapes.size) {
            voxelShapes
        } else {
            Array(nonEmptyCount) { VoxelShapes.empty() }.also { filtered ->
                var destinationIndex = 0
                for (shape in voxelShapes) {
                    if (!shape.isEmpty) filtered[destinationIndex++] = shape
                }
            }
        }
        if (nonEmptyCount == 2) {
            val first = nonEmptyShapes[0]
            val second = nonEmptyShapes[1]
            return ShapeCache.getOrComputeUnion(first, second) {
                VoxelShapes.union(first, second)
            }
        }

        if (nonEmptyCount > MAX_CACHED_UNION_SHAPES) {
            return Minecraft1194ShapeOps.union(nonEmptyShapes, nonEmptyCount)
        }
        return ShapeCache.getOrComputeUnion(nonEmptyShapes, nonEmptyCount) {
            Minecraft1194ShapeOps.union(nonEmptyShapes, nonEmptyCount)
        }
    }

    /**
     * Stores the given VoxelShape in a new VoxelShapeModifier instance, which can be used to cleanly combine shapes conditionally.
     *
     * @param shape The VoxelShape to be stored in the VoxelShapeModifier for modification.
     * @param configure The function used to modify the shape and return the modified VoxelShape.
     *
     * @see appendShapes
     */
    fun appendShapesTo(shape: VoxelShape, configure: VoxelShapeModifier.() -> Unit): VoxelShape {
        val builder = VoxelShapeModifier(shape)
        builder.configure()
        return builder.shape
    }

    /**
     * The VoxelShape version of appendShapesTo, allowing the receiver to be modified using a VoxelShapeModifier.
     *
     * @param configurer The function used to modify the receiver VoxelShape.
     *
     * @see appendShapes
     */
    infix fun VoxelShape.appendShapes(configurer: VoxelShapeModifier.() -> Unit): VoxelShape {
        return appendShapesTo(this, configurer)
    }

    /**
     * Creates a simplified version of a complex shape for use as an outline shape.
     * This is useful for improving performance when the shape is used for rendering outlines.
     *
     * @param shape The complex shape to simplify.
     * @param maxBoxes The maximum number of boxes in the simplified shape (default: 8).
     * @return A simplified VoxelShape with fewer boxes.
     *
     * @see ShapeSimplifier.simplify
     */
    fun createSimplifiedOutlineShape(shape: VoxelShape, maxBoxes: Int = 8): VoxelShape {
        return ShapeSimplifier.simplify(shape, maxBoxes)
    }

    /**
     * Creates a simplified version of a complex shape by using its bounding box.
     * This is useful for outline shapes that don't need to be as detailed as collision shapes.
     *
     * @param shape The complex shape to simplify.
     * @return A simplified VoxelShape based on the original's bounding box.
     *
     * @see ShapeSimplifier.simplifyToBoundingBox
     */
    fun createBoundingBoxShape(shape: VoxelShape): VoxelShape {
        return ShapeSimplifier.simplifyToBoundingBox(shape)
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
     *
     * @see ShapeSimplifier.createOutlineShape
     */
    fun createOutlineShape(
        minX: Number, minY: Number, minZ: Number,
        maxX: Number, maxY: Number, maxZ: Number,
        thickness: Number = 1
    ): VoxelShape {
        return ShapeSimplifier.createOutlineShape(minX, minY, minZ, maxX, maxY, maxZ, thickness)
    }

    /**
     * Extension function to simplify a VoxelShape for use as an outline.
     *
     * @param maxBoxes The maximum number of boxes in the simplified shape (default: 8).
     * @return A simplified version of the receiver VoxelShape.
     *
     * @see createSimplifiedOutlineShape
     */
    fun VoxelShape.simplifyForOutline(maxBoxes: Int = 8): VoxelShape {
        return createSimplifiedOutlineShape(this, maxBoxes)
    }

    /**
     * Extension function to convert a VoxelShape to its bounding box.
     *
     * @return A VoxelShape representing the bounding box of the receiver.
     *
     * @see createBoundingBoxShape
     */
    fun VoxelShape.toBoundingBoxShape(): VoxelShape {
        return createBoundingBoxShape(this)
    }

    private const val BLOCK_COORDINATE_SCALE = 16.0
}
