package com.github.mystery2099.voxlib.rotation

import com.github.mystery2099.voxlib.optimization.ShapeCache
import net.minecraft.util.shape.VoxelShapes
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

// Extension function imports
import com.github.mystery2099.voxlib.rotation.VoxelRotation.flip
import com.github.mystery2099.voxlib.rotation.VoxelRotation.flipHorizontal
import com.github.mystery2099.voxlib.rotation.VoxelRotation.flipVertical
import com.github.mystery2099.voxlib.rotation.VoxelRotation.flipZ
import com.github.mystery2099.voxlib.rotation.VoxelRotation.rotate
import com.github.mystery2099.voxlib.rotation.VoxelRotation.rotateLeft
import com.github.mystery2099.voxlib.rotation.VoxelRotation.rotateRight

/**
 * Unit tests for the VoxelRotation object.
 */
class VoxelRotationTest {

    @BeforeEach
    fun setUp() {
        ShapeCache.clearCache()
    }

    @Test
    fun `rotation of empty shape returns empty`() {
        val empty = VoxelShapes.empty()

        val rotated = empty.rotateLeft()

        assertEquals(VoxelShapes.empty(), rotated)
    }

    @Test
    fun `rotation of full cube returns full cube`() {
        val fullCube = VoxelShapes.fullCube()

        val rotated = fullCube.rotateLeft()

        assertEquals(VoxelShapes.fullCube(), rotated)
    }

    @Test
    fun `rotateLeft of full cube returns full cube`() {
        val fullCube = VoxelShapes.fullCube()

        val rotated = fullCube.rotateLeft()

        assertEquals(VoxelShapes.fullCube(), rotated)
    }

    @Test
    fun `rotateRight of full cube returns full cube`() {
        val fullCube = VoxelShapes.fullCube()

        val rotated = fullCube.rotateRight()

        assertEquals(VoxelShapes.fullCube(), rotated)
    }

    @Test
    fun `flipHorizontal of full cube returns full cube`() {
        val fullCube = VoxelShapes.fullCube()

        val flipped = fullCube.flipHorizontal()

        assertEquals(VoxelShapes.fullCube(), flipped)
    }

    @Test
    fun `flipVertical of full cube returns full cube`() {
        val fullCube = VoxelShapes.fullCube()

        val flipped = fullCube.flipVertical()

        assertEquals(VoxelShapes.fullCube(), flipped)
    }

    @Test
    fun `flipZ of full cube returns full cube`() {
        val fullCube = VoxelShapes.fullCube()

        val flipped = fullCube.flipZ()

        assertEquals(VoxelShapes.fullCube(), flipped)
    }

    @Test
    fun `rotateLeft of empty shape returns empty`() {
        val empty = VoxelShapes.empty()

        val result = empty.rotateLeft()

        assertEquals(VoxelShapes.empty(), result)
    }

    @Test
    fun `rotateRight of empty shape returns empty`() {
        val empty = VoxelShapes.empty()

        val result = empty.rotateRight()

        assertEquals(VoxelShapes.empty(), result)
    }

    @Test
    fun `flip of empty shape returns empty`() {
        val empty = VoxelShapes.empty()

        val result = empty.flip()

        assertEquals(VoxelShapes.empty(), result)
    }

    @Test
    fun `rotate with ROTATE_LEFT on empty returns empty`() {
        val empty = VoxelShapes.empty()

        val result = empty.rotate(VoxelShapeTransformation.ROTATE_LEFT)

        assertEquals(VoxelShapes.empty(), result)
    }

    @Test
    fun `rotate with ROTATE_RIGHT on empty returns empty`() {
        val empty = VoxelShapes.empty()

        val result = empty.rotate(VoxelShapeTransformation.ROTATE_RIGHT)

        assertEquals(VoxelShapes.empty(), result)
    }

    @Test
    fun `rotate with FLIP_HORIZONTAL on empty returns empty`() {
        val empty = VoxelShapes.empty()

        val result = empty.rotate(VoxelShapeTransformation.FLIP_HORIZONTAL)

        assertEquals(VoxelShapes.empty(), result)
    }

    @Test
    fun `rotate with FLIP_VERTICAL on empty returns empty`() {
        val empty = VoxelShapes.empty()

        val result = empty.rotate(VoxelShapeTransformation.FLIP_VERTICAL)

        assertEquals(VoxelShapes.empty(), result)
    }

    @Test
    fun `rotate with FLIP_Z on empty returns empty`() {
        val empty = VoxelShapes.empty()

        val result = empty.rotate(VoxelShapeTransformation.FLIP_Z)

        assertEquals(VoxelShapes.empty(), result)
    }

    @Test
    fun `VoxelShapeTransformation has 5 values`() {
        assertEquals(5, VoxelShapeTransformation.entries.size)
    }

    @Test
    fun `flip is alias for flipHorizontal`() {
        val fullCube = VoxelShapes.fullCube()

        val flipped = fullCube.flip()
        val flippedHorizontal = fullCube.flipHorizontal()

        assertEquals(flippedHorizontal, flipped)
    }
}