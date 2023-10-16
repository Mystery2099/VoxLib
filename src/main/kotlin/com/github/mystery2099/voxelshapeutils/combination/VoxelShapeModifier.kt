package com.github.mystery2099.voxelshapeutils.combination

import com.github.mystery2099.voxelshapeutils.combination.VoxelShapeCombining.plus
import net.minecraft.util.shape.VoxelShape

class VoxelShapeModifier internal constructor(private var baseShape: VoxelShape) {
    /**
     * Case
     *
     * @param condition
     * @return
     */
    infix fun VoxelShape.case(condition: Boolean): VoxelShape {
        return append(this, condition)
    }

    /**
     * Append
     *
     * @param shape
     * @param condition
     * @return
     */
    fun append(shape: VoxelShape, condition: Boolean = true): VoxelShape {
        if (condition) baseShape += shape
        return baseShape
    }
}