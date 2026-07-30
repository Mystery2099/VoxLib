package com.github.mystery2099.voxlib

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class DedicatedServerClassLoadingTest {
    @Test
    fun `core shape APIs load without client class references`() {
        val classLoader = requireNotNull(javaClass.classLoader)
        val coreClasses = listOf(
            "com.github.mystery2099.voxlib.combination.VoxelAssembly",
            "com.github.mystery2099.voxlib.optimization.Minecraft1194ShapeOps",
            "com.github.mystery2099.voxlib.optimization.ShapeCache",
            "com.github.mystery2099.voxlib.optimization.ShapeSimplifier",
            "com.github.mystery2099.voxlib.rotation.VoxelRotation",
            "com.github.mystery2099.voxlib.rotation.VoxelShapeTransformation",
            "com.github.mystery2099.voxlib.shapes.CommonShapes"
        )

        for (className in coreClasses) {
            assertNotNull(Class.forName(className, true, classLoader))
            val resourceName = className.replace('.', '/') + ".class"
            val classBytes = requireNotNull(classLoader.getResourceAsStream(resourceName)).use { it.readBytes() }
            val constantPoolText = classBytes.toString(Charsets.ISO_8859_1)
            assertFalse(
                constantPoolText.contains("net/minecraft/client"),
                "$className has a dedicated-server-unsafe client reference"
            )
        }
    }
}
