package com.github.mystery2099.voxlib.combination;

import net.minecraft.world.phys.shapes.VoxelShape;
import static com.github.mystery2099.voxlib.combination.VoxelAssembly.and;

public class VoxelShapeModifier {
    private VoxelShape storedShape;

    VoxelShapeModifier(VoxelShape stored) {
        storedShape = stored;
    }

    public VoxelShape append(VoxelShape shape) {
        storedShape = and(storedShape, shape);
        return storedShape;
    }

    public VoxelShape getStoredShape() { return storedShape; }
    public void setStoredShape(VoxelShape s) { this.storedShape = s; }
}
