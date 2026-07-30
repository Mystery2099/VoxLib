package com.github.mystery2099.voxlib.config

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class VoxLibConfigTest {
    @Test
    fun `normalized clamps alpha to valid range`() {
        assertEquals(0.0f, VoxLibConfig(debugShapeAlpha = -1.0f).normalized().debugShapeAlpha)
        assertEquals(1.0f, VoxLibConfig(debugShapeAlpha = 2.0f).normalized().debugShapeAlpha)
    }

    @Test
    fun `default color is opaque red`() {
        assertEquals(0xFFFF0000.toInt(), VoxLibConfig.default().debugShapeColor)
    }
}
