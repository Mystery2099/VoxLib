package com.github.mystery2099.voxlib.optimization

import com.github.mystery2099.voxlib.rotation.VoxelShapeTransformation
import net.minecraft.world.phys.shapes.VoxelShape
import net.minecraft.world.phys.shapes.Shapes

/**
 * Minecraft-version-specific shape operations used by VoxLib's hot paths.
 *
 * Keeping vanilla calls here makes future ports easier without exposing a
 * version abstraction to callers.
 */
internal object MinecraftShapeOps {
    fun union(shapes: Array<out VoxelShape>, size: Int = shapes.size): VoxelShape =
        unionRange(shapes, 0, size) { it }

    fun unionNullable(shapes: Array<VoxelShape?>, size: Int): VoxelShape =
        unionRange(shapes, 0, size) { requireNotNull(it) }

    fun transformBoxes(
        shape: VoxelShape,
        transformation: VoxelShapeTransformation
    ): VoxelShape {
        val transformed = VoxelShapeBuffer()
        shape.forAllBoxes { minX, minY, minZ, maxX, maxY, maxZ ->
            transformed.add(
                when (transformation) {
                    VoxelShapeTransformation.ROTATE_LEFT ->
                        Shapes.box(
                            1.0 - maxZ, minY, minX,
                            1.0 - minZ, maxY, maxX
                        )
                    VoxelShapeTransformation.ROTATE_RIGHT ->
                        Shapes.box(
                            minZ, minY, 1.0 - maxX,
                            maxZ, maxY, 1.0 - minX
                        )
                    VoxelShapeTransformation.FLIP_HORIZONTAL ->
                        Shapes.box(
                            1.0 - maxX, minY, 1.0 - maxZ,
                            1.0 - minX, maxY, 1.0 - minZ
                        )
                    VoxelShapeTransformation.FLIP_VERTICAL ->
                        Shapes.box(
                            minX, 1.0 - maxY, minZ,
                            maxX, 1.0 - minY, maxZ
                        )
                    VoxelShapeTransformation.FLIP_Z ->
                        Shapes.box(
                            minX, minY, 1.0 - maxZ,
                            maxX, maxY, 1.0 - minZ
                        )
                }
            )
        }
        return transformed.union()
    }

    /**
     * Divide-and-conquer union over `[fromIndex, toIndex)`.
     * [resolve] maps each slot to a non-null shape (identity for dense arrays,
     * [requireNotNull] for growable nullable buffers).
     */
    private fun <T> unionRange(
        shapes: Array<T>,
        fromIndex: Int,
        toIndex: Int,
        resolve: (T) -> VoxelShape
    ): VoxelShape {
        val size = toIndex - fromIndex
        if (size == 0) return Shapes.empty()
        if (size == 1) return resolve(shapes[fromIndex])
        if (size <= DIRECT_UNION_LIMIT) {
            var result = resolve(shapes[fromIndex])
            for (index in fromIndex + 1 until toIndex) {
                result = Shapes.or(result, resolve(shapes[index]))
            }
            return result
        }

        val middle = fromIndex + size / 2
        return Shapes.or(
            unionRange(shapes, fromIndex, middle, resolve),
            unionRange(shapes, middle, toIndex, resolve)
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
