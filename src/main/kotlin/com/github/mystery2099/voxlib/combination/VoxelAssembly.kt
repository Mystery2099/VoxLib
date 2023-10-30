package com.github.mystery2099.voxlib.combination

import net.minecraft.block.Block
import net.minecraft.util.function.BooleanBiFunction
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes

object VoxelAssembly {

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
     * Combines 2 VoxelShapes together and returns the new shape.
     * Basically VoxelShapes.union(this, otherShape) but in extension function form
    * */
    infix fun VoxelShape.and(otherShape: VoxelShape): VoxelShape = VoxelShapes.union(this, otherShape)

    /**
     * Allows the use of + and += to combine voxel shapes
     * */
    infix operator fun VoxelShape.plus(otherShape: VoxelShape): VoxelShape = this and otherShape

    /**
     * Combines a single shape with all the given shapes
     * */
    fun VoxelShape.UnifyWith(vararg otherShapes: VoxelShape): VoxelShape = union(this, *otherShapes)


    /**
     * Combines a list of shape using the given function.
     * Refer to VoxelShapes.combine() to combine just 2 shapes.
     * */
    fun combine(function: BooleanBiFunction, vararg voxelShapes: VoxelShape): VoxelShape {
        return voxelShapes.reduce { a, b -> VoxelShapes.combine(a, b, function) }
    }

    /**
     * Unions a list of voxel shape. Refer to VoxelShapes.union() for only unioning 2 shapes.
     * */
    fun union(vararg voxelShapes: VoxelShape): VoxelShape = voxelShapes.reduce(VoxelShapes::union)

    /**
     * Stores the given VoxelShape in a new VoxelShapeModifier instance
     * which can then be used to cleanly combine shapes conditionally.
     * Returns the new VoxelShape.
     * */
    fun appendShapesTo(shape: VoxelShape, configure: VoxelShapeModifier.() -> VoxelShape): VoxelShape {
        val modifier = VoxelShapeModifier(shape)
        return modifier.configure()
    }

    /**
     * Stores the VoxelShape it is applied in a new VoxelShapeModifier instance
     * which can then be used to cleanly combine shapes conditionally.
     * Returns the new VoxelShape.
     * */
    infix fun VoxelShape.appendShapes(configure: VoxelShapeModifier.() -> VoxelShape): VoxelShape {
        return appendShapesTo(this, configure)
    }
}