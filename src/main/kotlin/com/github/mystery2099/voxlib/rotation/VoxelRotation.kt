package com.github.mystery2099.voxlib.rotation

import com.github.mystery2099.voxlib.combination.VoxelAssembly.plus
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.max
import kotlin.math.min

object VoxelRotation {
    fun VoxelShape.rotateLeft(): VoxelShape = this.rotate(VoxelShapeTransformation.ROTATE_LEFT)
    fun VoxelShape.flip(): VoxelShape = this.rotate(VoxelShapeTransformation.FLIP_HORIZONTAL)
    fun VoxelShape.rotateRight(): VoxelShape = this.rotate(VoxelShapeTransformation.ROTATE_RIGHT)

    fun setMaxHeight(source: VoxelShape, height: Double): VoxelShape {
        val result = AtomicReference(VoxelShapes.empty())
        source.forEachBox { minX: Double, minY: Double, minZ: Double, maxX: Double, _: Double, maxZ: Double ->
            val shape = VoxelShapes.cuboid(minX, minY, minZ, maxX, height, maxZ)
            result.set(result.get() + shape)
        }
        return result.get()
    }

    fun limitHorizontal(source: VoxelShape): VoxelShape {
        val result = AtomicReference(VoxelShapes.empty())
        source.forEachBox { minX: Double, minY: Double, minZ: Double, maxX: Double, maxY: Double, maxZ: Double ->
            val shape = VoxelShapes.cuboid(minX.limit(), minY, minZ.limit(), maxX.limit(), maxY, maxZ.limit())
            result.set(result.get() + shape)
        }
        return result.get()
    }

    private fun VoxelShape.rotate(direction: VoxelShapeTransformation): VoxelShape {
        val shapes = mutableListOf(VoxelShapes.empty())
        this.forEachBox { minX, minY, minZ, maxX, maxY, maxZ ->
            val adjustedValues = adjustValues(direction, minX, minZ, maxX, maxZ)
            shapes += VoxelShapes.cuboid(
                adjustedValues[0], minY,
                adjustedValues[1], adjustedValues[2], maxY, adjustedValues[3]
            )
        }
        return shapes.reduce { a, b -> VoxelShapes.union(a, b) }
    }

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

    private fun Double.limit() = max(0.0, min(1.0, this))

}