package com.github.mystery2099.voxlib.debug

import net.minecraft.util.shape.VoxelShapes
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.io.PrintStream

class VoxelShapeDebugTest {
    @Test
    fun `logging reports shape names box counts and coordinates`() {
        val output = captureOutput {
            VoxelShapeDebug.logShapeInfo(VoxelShapes.empty())
            VoxelShapeDebug.logShapeInfo(VoxelShapes.fullCube(), "FullCube")
        }

        assertTrue(output.contains("VoxelShape contains 0 boxes:"))
        assertTrue(output.contains("FullCube contains 1 boxes:"))
        assertTrue(output.contains("Box: (0.0, 0.0, 0.0) to (1.0, 1.0, 1.0)"))
    }

    @Test
    fun `comparison reports identical shapes`() {
        val output = captureOutput {
            VoxelShapeDebug.compareShapes(VoxelShapes.fullCube(), VoxelShapes.fullCube())
        }

        assertTrue(output.contains("Comparing Shape 1 (1 boxes) with Shape 2 (1 boxes):"))
        assertTrue(output.contains("The shapes are identical."))
    }

    @Test
    fun `comparison reports different shapes`() {
        val output = captureOutput {
            VoxelShapeDebug.compareShapes(VoxelShapes.fullCube(), VoxelShapes.empty(), "Full", "Empty")
        }

        assertTrue(output.contains("Comparing Full (1 boxes) with Empty (0 boxes):"))
        assertTrue(output.contains("Full boxes:"))
        assertTrue(output.contains("Empty boxes:"))
        assertFalse(output.contains("The shapes are identical."))
    }

    private fun captureOutput(action: () -> Unit): String {
        val original = System.out
        val output = ByteArrayOutputStream()
        PrintStream(output).use { stream ->
            try {
                System.setOut(stream)
                action()
            } finally {
                System.setOut(original)
            }
        }
        return output.toString()
    }
}
