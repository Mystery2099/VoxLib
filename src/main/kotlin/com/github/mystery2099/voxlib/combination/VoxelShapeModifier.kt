package com.github.mystery2099.voxlib.combination

import com.github.mystery2099.voxlib.combination.VoxelAssembly.appendShapes
import com.github.mystery2099.voxlib.combination.VoxelAssembly.plus
import com.github.mystery2099.voxlib.combination.VoxelAssembly.unifyWith
import net.minecraft.util.shape.VoxelShape

/**
 * A utility class for conditionally combining [VoxelShape]s with a stored [VoxelShape].
 * Provides methods for easy and readable VoxelShape combination.
 *
 * @param storedShape The [VoxelShape] stored within the [VoxelShapeModifier].
 * @constructor Creates a [VoxelShapeModifier] with a stored [VoxelShape].
 *
 * @see VoxelAssembly.appendShapes
 * @see VoxelAssembly.appendShapesTo
 */
class VoxelShapeModifier internal constructor(private var storedShape: VoxelShape) {

    /**
     * The stored VoxelShape after modifications.
     */
    val shape: VoxelShape get() = storedShape

    /**
     * Combines the [VoxelShape] it is called on with the [VoxelShapeModifier]'s [storedShape]
     * if the [condition] is met.
     *
     * @receiver [VoxelShape]
     * @param condition The condition required for the receiver to be combined with the [storedShape].
     * @return [storedShape] + [this] if [condition] is met.
     *
     * @see append
     */
    infix fun VoxelShape.case(condition: Boolean) = append(this, condition)

    /**
     * Combines [shape] with the [VoxelShapeModifier]'s [storedShape] if the [condition] is met.
     *
     * @param shape The [VoxelShape] to be combined with the [VoxelShapeModifier]'s [storedShape].
     * @param condition The condition required for the combination (default is true).
     * @return [storedShape] + [shape] if [condition] is met.
     *
     * @see case
     * @see VoxelAssembly.appendShapes
     */
    fun append(shape: VoxelShape, condition: Boolean = true): VoxelShape {
        if (condition) storedShape += shape
        return storedShape
    }
}