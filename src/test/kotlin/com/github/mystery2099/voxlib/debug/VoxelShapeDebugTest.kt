package com.github.mystery2099.voxlib.debug

import net.minecraft.util.shape.VoxelShapes
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.io.PrintStream

/**
 * Unit tests for the VoxelShapeDebug object.
 *
 * Note: Tests for renderShape() are not included because it requires
 * Minecraft's client rendering system which is not available in a unit test
 * environment. The @Environment(EnvType.CLIENT) annotation ensures this
 * function is only loaded on the client.
 *
 * Tests for logShapeInfo() and compareShapes() are included because they
 * only use server-safe APIs (println and VoxelShape operations).
 */
class VoxelShapeDebugTest {

    @Test
    fun `logShapeInfo with empty shape logs correctly`() {
        val shape = VoxelShapes.empty()

        // Capture stdout
        val outputStream = ByteArrayOutputStream()
        System.setOut(PrintStream(outputStream))

        try {
            VoxelShapeDebug.logShapeInfo(shape, "EmptyShape")
            val output = outputStream.toString()

            assertTrue(output.contains("EmptyShape contains 0 boxes:"))
        } finally {
            System.setOut(System.out)
        }
    }

    @Test
    fun `logShapeInfo with full cube logs correctly`() {
        val shape = VoxelShapes.fullCube()

        val outputStream = ByteArrayOutputStream()
        System.setOut(PrintStream(outputStream))

        try {
            VoxelShapeDebug.logShapeInfo(shape, "FullCube")
            val output = outputStream.toString()

            assertTrue(output.contains("FullCube contains 1 boxes:"))
            assertTrue(output.contains("Box: (0.0, 0.0, 0.0) to (1.0, 1.0, 1.0)"))
        } finally {
            System.setOut(System.out)
        }
    }

    @Test
    fun `logShapeInfo with default name uses VoxelShape`() {
        val shape = VoxelShapes.empty()

        val outputStream = ByteArrayOutputStream()
        System.setOut(PrintStream(outputStream))

        try {
            VoxelShapeDebug.logShapeInfo(shape)
            val output = outputStream.toString()

            assertTrue(output.contains("VoxelShape contains"))
        } finally {
            System.setOut(System.out)
        }
    }

    @Test
    fun `logShapeInfo with custom name uses provided name`() {
        val shape = VoxelShapes.empty()

        val outputStream = ByteArrayOutputStream()
        System.setOut(PrintStream(outputStream))

        try {
            VoxelShapeDebug.logShapeInfo(shape, "CustomName")
            val output = outputStream.toString()

            assertTrue(output.contains("CustomName contains"))
        } finally {
            System.setOut(System.out)
        }
    }

    @Test
    fun `compareShapes with identical shapes reports equality`() {
        val shape1 = VoxelShapes.fullCube()
        val shape2 = VoxelShapes.fullCube()

        val outputStream = ByteArrayOutputStream()
        System.setOut(PrintStream(outputStream))

        try {
            VoxelShapeDebug.compareShapes(shape1, shape2, "ShapeA", "ShapeB")
            val output = outputStream.toString()

            assertTrue(output.contains("Comparing ShapeA (1 boxes) with ShapeB (1 boxes):"))
            assertTrue(output.contains("The shapes are identical."))
        } finally {
            System.setOut(System.out)
        }
    }

    @Test
    fun `compareShapes with different shapes shows differences`() {
        val shape1 = VoxelShapes.fullCube()
        val shape2 = VoxelShapes.empty()

        val outputStream = ByteArrayOutputStream()
        System.setOut(PrintStream(outputStream))

        try {
            VoxelShapeDebug.compareShapes(shape1, shape2, "Full", "Empty")
            val output = outputStream.toString()

            assertTrue(output.contains("Comparing Full (1 boxes) with Empty (0 boxes):"))
            assertFalse(output.contains("The shapes are identical."))
        } finally {
            System.setOut(System.out)
        }
    }

    @Test
    fun `compareShapes with two empty shapes reports equality`() {
        val shape1 = VoxelShapes.empty()
        val shape2 = VoxelShapes.empty()

        val outputStream = ByteArrayOutputStream()
        System.setOut(PrintStream(outputStream))

        try {
            VoxelShapeDebug.compareShapes(shape1, shape2, "Empty1", "Empty2")
            val output = outputStream.toString()

            assertTrue(output.contains("The shapes are identical."))
        } finally {
            System.setOut(System.out)
        }
    }

    @Test
    fun `compareShapes with default names`() {
        val shape1 = VoxelShapes.empty()
        val shape2 = VoxelShapes.empty()

        val outputStream = ByteArrayOutputStream()
        System.setOut(PrintStream(outputStream))

        try {
            VoxelShapeDebug.compareShapes(shape1, shape2)
            val output = outputStream.toString()

            assertTrue(output.contains("Shape 1"))
            assertTrue(output.contains("Shape 2"))
        } finally {
            System.setOut(System.out)
        }
    }

    @Test
    fun `logShapeInfo with empty shape shows no boxes`() {
        val shape = VoxelShapes.empty()

        val outputStream = ByteArrayOutputStream()
        System.setOut(PrintStream(outputStream))

        try {
            VoxelShapeDebug.logShapeInfo(shape, "TestShape")
            val output = outputStream.toString()

            assertTrue(output.contains("contains 0 boxes:"))
        } finally {
            System.setOut(System.out)
        }
    }

    @Test
    fun `compareShapes handles full cube vs empty correctly`() {
        val full = VoxelShapes.fullCube()
        val empty = VoxelShapes.empty()

        val outputStream = ByteArrayOutputStream()
        System.setOut(PrintStream(outputStream))

        try {
            VoxelShapeDebug.compareShapes(full, empty, "Full", "Empty")
            val output = outputStream.toString()

            assertTrue(output.contains("Full (1 boxes)"))
            assertTrue(output.contains("Empty (0 boxes)"))
            assertFalse(output.contains("The shapes are identical."))
        } finally {
            System.setOut(System.out)
        }
    }
}
