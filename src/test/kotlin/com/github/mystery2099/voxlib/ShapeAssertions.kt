package com.github.mystery2099.voxlib

import net.minecraft.util.function.BooleanBiFunction
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import org.junit.jupiter.api.Assertions.assertFalse

fun assertExactShape(expected: VoxelShape, actual: VoxelShape, message: String? = null) {
    assertFalse(
        VoxelShapes.matchesAnywhere(expected, actual, BooleanBiFunction.NOT_SAME),
        message ?: "VoxelShapes contain different occupied volumes"
    )
}
