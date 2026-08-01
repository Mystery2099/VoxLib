package com.github.mystery2099.voxlib.shapes

import com.github.mystery2099.voxlib.assertExactShape
import net.minecraft.util.math.Direction
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.ValueSource

class CommonShapesTest {
    @ParameterizedTest
    @ValueSource(ints = [0, 17])
    fun `createSlab rejects height outside valid range`(height: Int) {
        assertThrows(IllegalArgumentException::class.java) {
            CommonShapes.createSlab(height)
        }
    }

    @ParameterizedTest
    @ValueSource(ints = [0, 15])
    fun `createPillar rejects width outside valid range`(width: Int) {
        assertThrows(IllegalArgumentException::class.java) {
            CommonShapes.createPillar(width)
        }
    }

    @Test
    fun `createTable rejects invalid leg width`() {
        assertThrows(IllegalArgumentException::class.java) {
            CommonShapes.createTable(legWidth = 0)
        }
    }

    @Test
    fun `createTable rejects invalid top thickness`() {
        assertThrows(IllegalArgumentException::class.java) {
            CommonShapes.createTable(topThickness = 7)
        }
    }

    @ParameterizedTest
    @ValueSource(ints = [0, 13])
    fun `createChair rejects seat height outside valid range`(seatHeight: Int) {
        assertThrows(IllegalArgumentException::class.java) {
            CommonShapes.createChair(seatHeight = seatHeight)
        }
    }

    @Test
    fun `createChair rejects invalid backrest height`() {
        assertThrows(IllegalArgumentException::class.java) {
            CommonShapes.createChair(backrestHeight = 17)
        }
    }

    @ParameterizedTest
    @EnumSource(value = Direction::class, names = ["UP", "DOWN"])
    fun `createStairs rejects vertical direction`(direction: Direction) {
        assertThrows(IllegalArgumentException::class.java) {
            CommonShapes.createStairs(direction)
        }
    }

    @Test
    fun `every valid finite factory combination is memoized`() {
        for (height in 1..16) {
            assertMemoizedAndExact(
                CommonShapes.createSlab(height),
                CommonShapes.createSlab(height),
                box(0, 0, 0, 16, height, 16),
                "slab height=$height"
            )
            assertMemoizedAndExact(
                CommonShapes.createTopSlab(height),
                CommonShapes.createTopSlab(height),
                box(0, 16 - height, 0, 16, 16, 16),
                "top slab height=$height"
            )
        }
        for (width in 1..14) {
            val offset = (16 - width) / 2
            assertMemoizedAndExact(
                CommonShapes.createPillar(width, true),
                CommonShapes.createPillar(width, true),
                box(offset, 0, offset, offset + width, 16, offset + width),
                "centered pillar width=$width"
            )
            assertMemoizedAndExact(
                CommonShapes.createPillar(width, false),
                CommonShapes.createPillar(width, false),
                box(0, 0, 0, width, 16, width),
                "corner pillar width=$width"
            )
        }
        for (legWidth in 1..6) {
            for (topThickness in 1..6) {
                assertMemoizedAndExact(
                    CommonShapes.createTable(legWidth, topThickness),
                    CommonShapes.createTable(legWidth, topThickness),
                    expectedTable(legWidth, topThickness),
                    "table legs=$legWidth top=$topThickness"
                )
            }
        }
        for (seatHeight in 1..12) {
            val chairWithoutBackrest = CommonShapes.createChair(seatHeight, false, 1)
            for (hasBackrest in listOf(false, true)) {
                for (backrestHeight in 1..16) {
                    val chair = CommonShapes.createChair(seatHeight, hasBackrest, backrestHeight)
                    assertMemoizedAndExact(
                        chair,
                        CommonShapes.createChair(seatHeight, hasBackrest, backrestHeight),
                        expectedChair(seatHeight, hasBackrest, backrestHeight),
                        "chair seat=$seatHeight backrest=$hasBackrest height=$backrestHeight"
                    )
                    if (!hasBackrest) {
                        assertSame(chairWithoutBackrest, chair)
                    }
                }
            }
        }
        for (mask in 0 until 16) {
            assertMemoizedAndExact(
                fenceConnections(mask),
                fenceConnections(mask),
                expectedFenceConnections(mask),
                "fence mask=$mask"
            )
        }
        Direction.entries.filter { it.axis.isHorizontal }.forEach { direction ->
            assertMemoizedAndExact(
                CommonShapes.createStairs(direction),
                CommonShapes.createStairs(direction),
                expectedStairs(direction),
                "stairs direction=$direction"
            )
        }
        assertMemoizedAndExact(
            CommonShapes.createFencePost(),
            CommonShapes.createFencePost(),
            box(6, 0, 6, 10, 16, 10),
            "fence post"
        )
    }

    @Test
    fun `validation messages remain stable`() {
        assertValidationMessage("Height must be between 1 and 16") {
            CommonShapes.createSlab(0)
        }
        assertValidationMessage("Height must be between 1 and 16") {
            CommonShapes.createTopSlab(17)
        }
        assertValidationMessage("Width must be between 1 and 14") {
            CommonShapes.createPillar(0)
        }
        assertValidationMessage("Leg width must be between 1 and 6") {
            CommonShapes.createTable(legWidth = 7)
        }
        assertValidationMessage("Top thickness must be between 1 and 6") {
            CommonShapes.createTable(topThickness = 0)
        }
        assertValidationMessage("Seat height must be between 1 and 12") {
            CommonShapes.createChair(seatHeight = 13)
        }
        assertValidationMessage("Backrest height must be between 1 and 16") {
            CommonShapes.createChair(backrestHeight = 0)
        }
        assertValidationMessage("Stairs must face NORTH, EAST, SOUTH, or WEST") {
            CommonShapes.createStairs(Direction.UP)
        }
    }

    private fun fenceConnections(mask: Int) = CommonShapes.createFenceConnections(
        north = mask and 1 != 0,
        east = mask and 2 != 0,
        south = mask and 4 != 0,
        west = mask and 8 != 0
    )

    private fun assertMemoizedAndExact(
        first: VoxelShape,
        second: VoxelShape,
        expected: VoxelShape,
        label: String
    ) {
        assertSame(first, second, label)
        assertExactShape(expected, first, label)
    }

    private fun expectedTable(legWidth: Int, topThickness: Int): VoxelShape {
        val offset = 16 - legWidth
        return VoxelShapes.union(
            box(0, 16 - topThickness, 0, 16, 16, 16),
            box(0, 0, 0, legWidth, 16 - topThickness, legWidth),
            box(offset, 0, 0, 16, 16 - topThickness, legWidth),
            box(0, 0, offset, legWidth, 16 - topThickness, 16),
            box(offset, 0, offset, 16, 16 - topThickness, 16)
        )
    }

    private fun expectedChair(
        seatHeight: Int,
        hasBackrest: Boolean,
        backrestHeight: Int
    ): VoxelShape {
        val seat = box(1, seatHeight, 1, 15, seatHeight + 2, 15)
        val legs = box(2, 0, 2, 14, seatHeight, 14)
        if (!hasBackrest) return VoxelShapes.union(seat, legs)
        return VoxelShapes.union(
            seat,
            legs,
            box(2, seatHeight + 2, 12, 14, seatHeight + 2 + backrestHeight, 15)
        )
    }

    private fun expectedFenceConnections(mask: Int): VoxelShape {
        val shapes = mutableListOf(box(6, 0, 6, 10, 16, 10))
        if (mask and 1 != 0) shapes += box(7, 6, 0, 9, 15, 6)
        if (mask and 2 != 0) shapes += box(10, 6, 7, 16, 15, 9)
        if (mask and 4 != 0) shapes += box(7, 6, 10, 9, 15, 16)
        if (mask and 8 != 0) shapes += box(0, 6, 7, 6, 15, 9)
        return shapes.drop(1).fold(shapes.first(), VoxelShapes::union)
    }

    private fun expectedStairs(direction: Direction): VoxelShape {
        val bottom = box(0, 0, 0, 16, 8, 16)
        val top = when (direction) {
            Direction.NORTH -> box(0, 8, 0, 16, 16, 8)
            Direction.EAST -> box(8, 8, 0, 16, 16, 16)
            Direction.SOUTH -> box(0, 8, 8, 16, 16, 16)
            Direction.WEST -> box(0, 8, 0, 8, 16, 16)
            else -> error("Expected horizontal direction")
        }
        return VoxelShapes.union(bottom, top)
    }

    private fun box(
        minX: Int,
        minY: Int,
        minZ: Int,
        maxX: Int,
        maxY: Int,
        maxZ: Int
    ): VoxelShape = VoxelShapes.cuboid(
        minX / 16.0,
        minY / 16.0,
        minZ / 16.0,
        maxX / 16.0,
        maxY / 16.0,
        maxZ / 16.0
    )

    private fun assertValidationMessage(expected: String, operation: () -> Unit) {
        val exception = assertThrows(IllegalArgumentException::class.java) { operation() }
        org.junit.jupiter.api.Assertions.assertEquals(expected, exception.message)
    }
}
