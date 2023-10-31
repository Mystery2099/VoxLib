package com.github.mystery2099.voxlib.combination

import net.minecraft.block.Block
import net.minecraft.util.function.BooleanBiFunction
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes

object VoxelAssembly {

    /**
     * Create a cuboid shape using any type of number
     * @param minX
     * @param minY
     * @param minZ
     * @param maxX
     * @param maxY
     * @param maxZ
     * @see Block.createCuboidShape
    **/
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
     * Combines 2 the receiver and [otherShape].
     * @param otherShape The [VoxelShape] to be unified with the receiver.
     * @receiver [VoxelShape]
     * @see VoxelShapes.union
     * @see plus
    * */
    infix fun VoxelShape.and(otherShape: VoxelShape): VoxelShape = VoxelShapes.union(this, otherShape)

    /**
     * Allows the use of + and += to combine [VoxelShape]s
     * @param otherShape
     * @see VoxelShapes.union
     * @see and
     * */
    infix operator fun VoxelShape.plus(otherShape: VoxelShape): VoxelShape = this and otherShape

    /**
     * Combines a single [VoxelShape] with all the given [VoxelShape]s
     * @see union
     * */
    fun VoxelShape.UnifyWith(vararg otherShapes: VoxelShape): VoxelShape = union(this, *otherShapes)


    /**
     * Combines a list of shape using the given function.
     * @param function
     * @param voxelShapes
     * @return
     * @see VoxelShapes.combine
     * */
    fun combine(function: BooleanBiFunction, vararg voxelShapes: VoxelShape): VoxelShape {
        return voxelShapes.reduce { a, b -> VoxelShapes.combine(a, b, function) }
    }

    /**
     * Unifies or combines the given list of [voxelShapes].
     * @param voxelShapes A list of [VoxelShape] which should be unified.
     * @return the unified [voxelShapes]
     * @see VoxelShapes.union
     * */
    fun union(vararg voxelShapes: VoxelShape): VoxelShape = voxelShapes.reduce(VoxelShapes::union)

    /**
     * Stores the given [shape] in a new [VoxelShapeModifier] instance
     * which can then be used to cleanly combine shapes conditionally.
     * @return the new [shape] after being modified using [configure].
     * @param shape The [VoxelShape] which should be stored in [configure] to be modified.
     * @param configure The function used to modify [shape] and return the modified [VoxelShape]
     * @see appendShapes
     **/
    fun appendShapesTo(shape: VoxelShape, configure: VoxelShapeModifier.() -> VoxelShape): VoxelShape {
        return configure(VoxelShapeModifier(shape))
    }

    /**
     * The [VoxelShape] version of [appendShapesTo].
     * Stores the receiver in a new [VoxelShapeModifier] instance which can then be used to cleanly and conditionally combine [VoxelShape]s.
     * @return The modified version of the receiver.
     * @param configurer The function which will be used to modify the receiver
     * @see appendShapes
     * */
    infix fun VoxelShape.appendShapes(configurer: VoxelShapeModifier.() -> VoxelShape): VoxelShape {
        return appendShapesTo(this, configurer)
    }
}