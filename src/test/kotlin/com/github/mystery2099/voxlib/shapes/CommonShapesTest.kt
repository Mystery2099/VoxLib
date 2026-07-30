package com.github.mystery2099.voxlib.shapes

import net.minecraft.util.math.Direction
import org.junit.jupiter.api.Assertions.assertThrows
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
}
