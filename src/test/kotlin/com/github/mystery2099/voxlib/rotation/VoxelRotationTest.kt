package com.github.mystery2099.voxlib.rotation

import com.github.mystery2099.voxlib.assertExactShape
import com.github.mystery2099.voxlib.optimization.ShapeCache
import com.github.mystery2099.voxlib.rotation.VoxelRotation.flip
import com.github.mystery2099.voxlib.rotation.VoxelRotation.flipHorizontal
import com.github.mystery2099.voxlib.rotation.VoxelRotation.rotate
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.random.Random

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

    @ParameterizedTest
    @EnumSource(VoxelShapeTransformation::class)
    fun `deterministic random transformations preserve exact geometry`(
        transformation: VoxelShapeTransformation
    ) {
        val random = Random(2099 + transformation.ordinal)
        repeat(20) { iteration ->
            val source = randomShape(random, 12)
            val expected = referenceTransform(source, transformation)
            assertExactShape(
                expected,
                source.rotate(transformation),
                "$transformation random iteration $iteration"
            )
        }
    }

    @Test
    fun `four right rotations return exact original geometry`() {
        val original = randomShape(Random(2099), 24)
        val rotated = original
            .rotate(VoxelShapeTransformation.ROTATE_RIGHT)
            .rotate(VoxelShapeTransformation.ROTATE_RIGHT)
            .rotate(VoxelShapeTransformation.ROTATE_RIGHT)
            .rotate(VoxelShapeTransformation.ROTATE_RIGHT)

        assertExactShape(original, rotated)
    }

    @Test
    fun `repeated transformation returns cached instance`() {
        val shape = randomShape(Random(42), 8)

        assertSame(shape.rotate(VoxelShapeTransformation.ROTATE_LEFT), shape.rotate(VoxelShapeTransformation.ROTATE_LEFT))
    }

    @Test
    fun `concurrent transformations remain exact`() {
        val shape = randomShape(Random(42), 16)
        val expected = referenceTransform(shape, VoxelShapeTransformation.FLIP_Z)
        val executor = Executors.newFixedThreadPool(8)
        try {
            val results = (0 until 64).map {
                executor.submit<VoxelShape> {
                    shape.rotate(VoxelShapeTransformation.FLIP_Z)
                }
            }
            results.forEach { assertExactShape(expected, it.get(10, TimeUnit.SECONDS)) }
        } finally {
            executor.shutdownNow()
        }
    }

    private fun assertBox(shape: VoxelShape, expected: DoubleArray) {
        val box = shape.boundingBox
        val actual = doubleArrayOf(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ)
        assertArrayEquals(expected, actual, DELTA)
    }

    private fun randomShape(random: Random, boxCount: Int): VoxelShape =
        Array(boxCount) {
            val minX = random.nextDouble(0.0, 0.8)
            val minY = random.nextDouble(0.0, 0.8)
            val minZ = random.nextDouble(0.0, 0.8)
            VoxelShapes.cuboid(
                minX, minY, minZ,
                minX + random.nextDouble(0.05, 0.2),
                minY + random.nextDouble(0.05, 0.2),
                minZ + random.nextDouble(0.05, 0.2)
            )
        }.fold(VoxelShapes.empty(), VoxelShapes::union)

    private fun referenceTransform(
        shape: VoxelShape,
        transformation: VoxelShapeTransformation
    ): VoxelShape {
        var result = VoxelShapes.empty()
        shape.forEachBox { minX, minY, minZ, maxX, maxY, maxZ ->
            val transformed = when (transformation) {
                VoxelShapeTransformation.ROTATE_LEFT ->
                    doubleArrayOf(1.0 - maxZ, minY, minX, 1.0 - minZ, maxY, maxX)
                VoxelShapeTransformation.ROTATE_RIGHT ->
                    doubleArrayOf(minZ, minY, 1.0 - maxX, maxZ, maxY, 1.0 - minX)
                VoxelShapeTransformation.FLIP_HORIZONTAL ->
                    doubleArrayOf(1.0 - maxX, minY, 1.0 - maxZ, 1.0 - minX, maxY, 1.0 - minZ)
                VoxelShapeTransformation.FLIP_VERTICAL ->
                    doubleArrayOf(minX, 1.0 - maxY, minZ, maxX, 1.0 - minY, maxZ)
                VoxelShapeTransformation.FLIP_Z ->
                    doubleArrayOf(minX, minY, 1.0 - maxZ, maxX, maxY, 1.0 - minZ)
            }
            result = VoxelShapes.union(
                result,
                VoxelShapes.cuboid(
                    transformed[0], transformed[1], transformed[2],
                    transformed[3], transformed[4], transformed[5]
                )
            )
        }
        return result
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
