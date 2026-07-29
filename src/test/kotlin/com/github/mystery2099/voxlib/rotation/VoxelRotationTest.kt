package com.github.mystery2099.voxlib.rotation

import com.github.mystery2099.voxlib.optimization.ShapeCache
import com.github.mystery2099.voxlib.rotation.VoxelRotation.flip
import com.github.mystery2099.voxlib.rotation.VoxelRotation.flipHorizontal
import com.github.mystery2099.voxlib.rotation.VoxelRotation.rotate
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class VoxelRotationTest {
    @BeforeEach
    fun setUp() {
        ShapeCache.clearCache()
    }

    @ParameterizedTest
    @EnumSource(VoxelShapeTransformation::class)
    fun `every transformation preserves empty shape`(transformation: VoxelShapeTransformation) {
        assertEquals(VoxelShapes.empty(), VoxelShapes.empty().rotate(transformation))
    }

    @ParameterizedTest
    @EnumSource(VoxelShapeTransformation::class)
    fun `every transformation preserves full cube`(transformation: VoxelShapeTransformation) {
        assertEquals(VoxelShapes.fullCube(), VoxelShapes.fullCube().rotate(transformation))
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("asymmetricTransformations")
    fun `transformations move asymmetric shape correctly`(
        transformation: VoxelShapeTransformation,
        expected: DoubleArray
    ) {
        val shape = VoxelShapes.cuboid(0.0, 0.0, 0.0, 0.25, 0.5, 0.5)

        assertBox(shape.rotate(transformation), expected)
    }

    @Test
    fun `flip remains alias for flipHorizontal`() {
        val shape = VoxelShapes.cuboid(0.0, 0.0, 0.0, 0.25, 0.5, 0.5)

        assertEquals(shape.flipHorizontal().boundingBox, shape.flip().boundingBox)
    }

    private fun assertBox(shape: VoxelShape, expected: DoubleArray) {
        val box = shape.boundingBox
        val actual = doubleArrayOf(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ)
        assertArrayEquals(expected, actual, DELTA)
    }

    private companion object {
        const val DELTA = 0.0001

        @JvmStatic
        fun asymmetricTransformations(): Stream<Arguments> = Stream.of(
            Arguments.of(
                VoxelShapeTransformation.ROTATE_LEFT,
                doubleArrayOf(0.5, 0.0, 0.0, 1.0, 0.5, 0.25)
            ),
            Arguments.of(
                VoxelShapeTransformation.ROTATE_RIGHT,
                doubleArrayOf(0.0, 0.0, 0.75, 0.5, 0.5, 1.0)
            ),
            Arguments.of(
                VoxelShapeTransformation.FLIP_HORIZONTAL,
                doubleArrayOf(0.75, 0.0, 0.5, 1.0, 0.5, 1.0)
            ),
            Arguments.of(
                VoxelShapeTransformation.FLIP_VERTICAL,
                doubleArrayOf(0.0, 0.5, 0.0, 0.25, 1.0, 0.5)
            ),
            Arguments.of(
                VoxelShapeTransformation.FLIP_Z,
                doubleArrayOf(0.0, 0.0, 0.5, 0.25, 0.5, 1.0)
            )
        )
    }
}
