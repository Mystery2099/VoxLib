package com.github.mystery2099.voxlib.combination

import com.github.mystery2099.voxlib.combination.VoxelAssembly.plus
import net.minecraft.util.shape.VoxelShape

/**
 * Used to easily and simply add [VoxelShape]s to a [VoxelShape] conditionally.
 * Provides methods for combining [VoxelShape]s in a very easy to read way
 * @param storedShape the [VoxelShape] stored within [VoxelShapeModifier]]
 * @constructor creates a [VoxelShapeModifier] with a stored [VoxelShape]
 * @see VoxelAssembly.appendShapes
 * @see VoxelAssembly.appendShapesTo
* */
class VoxelShapeModifier internal constructor(private var storedShape: VoxelShape) {

    /**
     * combines the [VoxelShape] it is called on with the [VoxelShapeModifier]'s [storedShape] if the [condition] is met
     * @receiver [VoxelShape]
     * @param condition The condition required for the receiver to be combined with the [storedShape]
     * @return [storedShape] + [this] if [condition] is met.
     * @see append
     */
    infix fun VoxelShape.case(condition: Boolean) = append(this, condition)

    /**
     * Combines [shape] with the [VoxelShapeModifier]'s [storedShape] if the [condition] is met. 
     * See [case] for a function which does the same as this but in a more readable way.
     * @param shape
     * @param condition
     * @return [storedShape] + [shape] if [condition] is met.
     * @see case
     * @see VoxelAssembly.appendShapes
     * @see VoxelAssembly.appendShapesTo
     * @see VoxelAssembly.plus
     * @see VoxelAssembly.UnifyWith
     * @see VoxelAssembly.union
     * @see VoxelAssembly.combine
     * 
     */
    fun append(shape: VoxelShape, condition: Boolean = true): VoxelShape {
        if (condition) storedShape += shape
        return storedShape
    }
}