package com.github.mystery2099.voxlib.benchmark;

import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;

final class BenchmarkFixtures {
    private BenchmarkFixtures() {
    }

    static VoxelShape complexShape(int boxCount, double phase) {
        if (boxCount > 64) {
            throw new IllegalArgumentException("complexShape supports at most 64 boxes");
        }
        VoxelShape result = VoxelShapes.empty();
        for (int index = 0; index < boxCount; index++) {
            int x = index & 3;
            int y = (index >> 2) & 3;
            int z = (index >> 4) & 3;
            double minX = phase + x * 0.2;
            double minY = y * 0.2;
            double minZ = z * 0.2;
            VoxelShape box = VoxelShapes.cuboid(
                minX, minY, minZ,
                minX + 0.11, minY + 0.11, minZ + 0.11
            );
            result = VoxelShapes.union(result, box);
        }
        return result;
    }

    static VoxelShape[] componentShapes(int shapeCount, double phase) {
        VoxelShape[] shapes = new VoxelShape[shapeCount];
        for (int index = 0; index < shapeCount; index++) {
            int x = index & 3;
            int y = (index >> 2) & 3;
            int z = (index >> 4) & 3;
            double minX = phase + x * 0.2;
            double minY = y * 0.2;
            double minZ = z * 0.2;
            shapes[index] = VoxelShapes.cuboid(
                minX, minY, minZ,
                minX + 0.11, minY + 0.11, minZ + 0.11
            );
        }
        return shapes;
    }

    static VoxelShape simplifierShape(int boxCount) {
        VoxelShape result = VoxelShapes.empty();
        for (int index = 0; index < boxCount; index++) {
            double minX = index * 0.125;
            result = VoxelShapes.union(
                result,
                VoxelShapes.cuboid(minX, 0.0, 0.0, minX + 0.0625, 0.0625, 0.0625)
            );
        }
        return result;
    }
}
