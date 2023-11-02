package com.github.mystery2099.voxlib.combination

import net.minecraft.block.Block
import net.minecraft.util.function.BooleanBiFunction
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes

/**
 * A utility object for working with VoxelShapes in Minecraft game development.
 *
 * This object provides various functions for creating, combining, and modifying VoxelShapes,
 * which are used to represent the collision and shape of objects in the game world.
 */
object VoxelAssembly {

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
     * @return A VoxelShape representing the cuboid shape.
     *
     * @see Block.createCuboidShape
     */
    fun createCuboidShape(
        minX: Number,
        minY: Number,
        minZ: Number,
        maxX: Number,
        maxY: Number,
        maxZ: Number
    ): VoxelShape = Block.createCuboidShape(
        minX.toDouble(),
        minY.toDouble(),
        minZ.toDouble(),
        maxX.toDouble(),
        maxY.toDouble(),
        maxZ.toDouble()
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
    infix fun VoxelShape.and(otherShape: VoxelShape): VoxelShape = VoxelShapes.union(this, otherShape)

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
    infix operator fun VoxelShape.plus(otherShape: VoxelShape): VoxelShape = this and otherShape

    /**
     * Combines a single VoxelShape with a list of other VoxelShapes using the [VoxelAssembly.union] operation.
     *
     * @param otherShapes A list of VoxelShapes to be unified with the receiver.
     *
     * @return A new VoxelShape representing the union of all input shapes.
     *
     * @see union
     */
    fun VoxelShape.UnifyWith(vararg otherShapes: VoxelShape): VoxelShape = union(this, *otherShapes)


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
        return voxelShapes.reduce { a, b -> VoxelShapes.combine(a, b, function) }
    }

    /**
     * Unifies or combines a list of VoxelShapes into a single VoxelShape.
     *
     * @param voxelShapes A list of VoxelShapes to be unified.
     *
     * @return A new VoxelShape representing the union of all input shapes.
     *
     * @see VoxelShapes.union
     */
    fun union(vararg voxelShapes: VoxelShape): VoxelShape = voxelShapes.reduce(VoxelShapes::union)

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
        return builder.storedShape
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

}