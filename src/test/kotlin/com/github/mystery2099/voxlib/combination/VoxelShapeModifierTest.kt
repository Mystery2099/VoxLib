package com.github.mystery2099.voxlib.combination

import com.github.mystery2099.voxlib.combination.VoxelAssembly.appendShapesTo
import net.minecraft.util.shape.VoxelShapes
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test

class VoxelShapeModifierTest {
    @Test
    fun `append uses true as its default condition`() {
        val result = appendShapesTo(VoxelShapes.empty()) {
            append(VoxelShapes.fullCube())
        }

        assertFalse(result.isEmpty)
    }

    @Test
    fun `append skips shape when condition is false`() {
        val base = VoxelShapes.fullCube()

        val result = appendShapesTo(base) {
            append(VoxelShapes.empty(), condition = false)
        }

        assertEquals(base, result)
    }

    @Test
    fun `case provides infix conditional append`() {
        val result = appendShapesTo(VoxelShapes.empty()) {
            VoxelShapes.fullCube().case(true)
        }

        assertFalse(result.isEmpty)
    }

    @Test
    fun `multiple appends accumulate shapes`() {
        val left = VoxelShapes.cuboid(0.0, 0.0, 0.0, 0.5, 1.0, 1.0)
        val right = VoxelShapes.cuboid(0.5, 0.0, 0.0, 1.0, 1.0, 1.0)

        val result = appendShapesTo(VoxelShapes.empty()) {
            append(left)
            append(right)
        }

        assertEquals(VoxelShapes.fullCube().boundingBox, result.boundingBox)
    }

    @Test
    fun `appending empty shape preserves base`() {
        val base = VoxelShapes.fullCube()

        val result = appendShapesTo(base) {
            append(VoxelShapes.empty())
        }

        assertEquals(base, result)
    }
}
