package com.github.mystery2099.voxlib.benchmark;

import com.github.mystery2099.voxlib.combination.VoxelAssembly;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

@State(Scope.Thread)
public class VoxelCreationBenchmark {
    @Benchmark
    public VoxelShape vanillaIntegralCuboid() {
        return Shapes.box(
            1.0 / 16.0, 2.0 / 16.0, 3.0 / 16.0,
            14.0 / 16.0, 15.0 / 16.0, 1.0
        );
    }

    @Benchmark
    public VoxelShape voxLibIntegralCuboid() {
        return VoxelAssembly.INSTANCE.createCuboidShape(1, 2, 3, 14, 15, 16);
    }

    @Benchmark
    public VoxelShape vanillaFractionalCuboid() {
        return Shapes.box(
            1.25 / 16.0, 2.5 / 16.0, 3.75 / 16.0,
            14.25 / 16.0, 15.5 / 16.0, 1.0
        );
    }

    @Benchmark
    public VoxelShape voxLibFractionalCuboid() {
        return VoxelAssembly.INSTANCE.createCuboidShape(1.25, 2.5, 3.75, 14.25, 15.5, 16.0);
    }
}
