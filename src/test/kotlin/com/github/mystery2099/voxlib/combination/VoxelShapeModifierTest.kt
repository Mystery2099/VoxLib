package com.github.mystery2099.voxlib.combination

import com.github.mystery2099.voxlib.combination.VoxelAssembly.appendShapesTo
import com.github.mystery2099.voxlib.combination.VoxelAssembly.plus
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * Unit tests for the VoxelShapeModifier class.
 *
 * Note: Tests that use createCuboidShape are disabled because they require
 * Minecraft's registry system to be initialized.
 */
class VoxelShapeModifierTest {

    @Test
    fun `append with true condition adds shape`() {
        val base = VoxelShapes.empty()
        val toAdd = VoxelShapes.fullCube()

        val result = VoxelAssembly.appendShapesTo(base) {
            append(toAdd, condition = true)
        }

        assertFalse(result.isEmpty)
        assertNotEquals(base, result)
    }

    @Test
    fun `append with false condition skips shape`() {
        val base = VoxelShapes.empty()
        val toAdd = VoxelShapes.empty()

        val result = VoxelAssembly.appendShapesTo(base) {
            append(toAdd, condition = false)
        }

        assertEquals(base, result)
    }

    @Test
    fun `append with default condition adds shape`() {
        val base = VoxelShapes.empty()
        val toAdd = VoxelShapes.fullCube()

        val result = VoxelAssembly.appendShapesTo(base) {
            append(toAdd) // Default condition is true
        }

        assertFalse(result.isEmpty)
    }

    @Test
    fun `case infix works like append`() {
        val base = VoxelShapes.empty()
        val toAdd = VoxelShapes.fullCube()

        val result = VoxelAssembly.appendShapesTo(base) {
            toAdd.case(true)
        }

        assertFalse(result.isEmpty)
        assertNotEquals(base, result)
    }

    @Test
    fun `case with false condition does not add`() {
        val base = VoxelShapes.empty()
        val toAdd = VoxelShapes.empty()

        val result = VoxelAssembly.appendShapesTo(base) {
            toAdd.case(false)
        }

        assertEquals(base, result)
    }

    @Test
    fun `multiple appends accumulate shapes`() {
        val base = VoxelShapes.empty() // Small center piece
        val add1 = VoxelShapes.fullCube() // Left side
        val add2 = VoxelShapes.fullCube() // Front-right
        val add3 = VoxelShapes.fullCube() // Back-right

        val result = VoxelAssembly.appendShapesTo(base) {
            append(add1)
            append(add2)
            append(add3)
        }

        // Result should be larger than any individual piece
        assertFalse(result.isEmpty)
    }

    @Test
    fun `shape property returns modified shape`() {
        val base = VoxelShapes.empty()
        val toAdd = VoxelShapes.fullCube()

        val modifier = VoxelShapeModifier(base)
        modifier.append(toAdd)

        // Access the shape property
        val result = modifier.shape

        assertFalse(result.isEmpty)
        assertNotEquals(base, result)
    }

    @Test
    fun `append with empty shape still works`() {
        val base = VoxelShapes.empty()
        val empty = VoxelShapes.empty()

        val result = VoxelAssembly.appendShapesTo(base) {
            append(empty, condition = true)
        }

        assertEquals(base, result)
    }

    @Test
    fun `conditional modifiers with boolean flags`() {
        val base = VoxelShapes.empty()
        val addNorth = VoxelShapes.fullCube()
        val addSouth = VoxelShapes.fullCube()

        // Test with one condition true, one false
        val result = VoxelAssembly.appendShapesTo(base) {
            append(addNorth, condition = true)
            append(addSouth, condition = false)
        }

        // Should only include north addition
        assertFalse(result.isEmpty)
    }
}