package com.github.mystery2099.voxlib

import net.minecraft.world.phys.shapes.BooleanOp
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.phys.shapes.VoxelShape

fun union(shape1: VoxelShape, shape2: VoxelShape): VoxelShape = Shapes.join(shape1, shape2, BooleanOp.OR)