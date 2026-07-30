package com.github.mystery2099.voxlib.optimization

import com.github.mystery2099.voxlib.rotation.VoxelShapeTransformation
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes

/**
 * Minecraft-version-specific shape operations used by VoxLib's hot paths.
 *
 * Keeping vanilla calls here makes future ports easier without exposing a
 * version abstraction to callers.
 */
internal object Minecraft1194ShapeOps {
    fun union(shapes: Array<out VoxelShape>, size: Int = shapes.size): VoxelShape =
        unionRange(shapes, 0, size)

    fun unionNullable(shapes: Array<VoxelShape?>, size: Int): VoxelShape =
        unionNullableRange(shapes, 0, size)

    fun transformBoxes(
        shape: VoxelShape,
        transformation: VoxelShapeTransformation
    ): VoxelShape {
        val transformed = VoxelShapeBuffer()
        shape.forEachBox { minX, minY, minZ, maxX, maxY, maxZ ->
            transformed.add(
                when (transformation) {
                    VoxelShapeTransformation.ROTATE_LEFT ->
                        VoxelShapes.cuboid(
                            1.0 - maxZ, minY, minX,
                            1.0 - minZ, maxY, maxX
                        )
                    VoxelShapeTransformation.ROTATE_RIGHT ->
                        VoxelShapes.cuboid(
                            minZ, minY, 1.0 - maxX,
                            maxZ, maxY, 1.0 - minX
                        )
                    VoxelShapeTransformation.FLIP_HORIZONTAL ->
                        VoxelShapes.cuboid(
                            1.0 - maxX, minY, 1.0 - maxZ,
                            1.0 - minX, maxY, 1.0 - minZ
                        )
                    VoxelShapeTransformation.FLIP_VERTICAL ->
                        VoxelShapes.cuboid(
                            minX, 1.0 - maxY, minZ,
                            maxX, 1.0 - minY, maxZ
                        )
                    VoxelShapeTransformation.FLIP_Z ->
                        VoxelShapes.cuboid(
                            minX, minY, 1.0 - maxZ,
                            maxX, maxY, 1.0 - minZ
                        )
                }
            )
        }
        return transformed.union()
    }

    private fun unionRange(shapes: Array<out VoxelShape>, fromIndex: Int, toIndex: Int): VoxelShape {
        val size = toIndex - fromIndex
        if (size == 0) return VoxelShapes.empty()
        if (size == 1) return shapes[fromIndex]
        if (size <= DIRECT_UNION_LIMIT) {
            var result = shapes[fromIndex]
            for (index in fromIndex + 1 until toIndex) {
                result = VoxelShapes.union(result, shapes[index])
            }
            return result
        }

        val middle = fromIndex + size / 2
        return VoxelShapes.union(
            unionRange(shapes, fromIndex, middle),
            unionRange(shapes, middle, toIndex)
        )
    }

    private fun unionNullableRange(
        shapes: Array<VoxelShape?>,
        fromIndex: Int,
        toIndex: Int
    ): VoxelShape {
        val size = toIndex - fromIndex
        if (size == 0) return VoxelShapes.empty()
        if (size == 1) return requireNotNull(shapes[fromIndex])
        if (size <= DIRECT_UNION_LIMIT) {
            var result = requireNotNull(shapes[fromIndex])
            for (index in fromIndex + 1 until toIndex) {
                result = VoxelShapes.union(result, requireNotNull(shapes[index]))
            }
            return result
        }

        val middle = fromIndex + size / 2
        return VoxelShapes.union(
            unionNullableRange(shapes, fromIndex, middle),
            unionNullableRange(shapes, middle, toIndex)
        )
    }

    private class VoxelShapeBuffer(initialCapacity: Int = 32) {
        private var shapes = arrayOfNulls<VoxelShape>(initialCapacity)
        private var size = 0

        fun add(shape: VoxelShape) {
            if (size == shapes.size) shapes = shapes.copyOf(shapes.size * 2)
            shapes[size++] = shape
        }

        fun union(): VoxelShape = unionNullable(shapes, size)
    }

    private const val DIRECT_UNION_LIMIT = 4
}
