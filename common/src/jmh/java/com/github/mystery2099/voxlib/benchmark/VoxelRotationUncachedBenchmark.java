package com.github.mystery2099.voxlib.benchmark;

import com.github.mystery2099.voxlib.optimization.Minecraft1194ShapeOps;
import com.github.mystery2099.voxlib.rotation.VoxelShapeTransformation;
import net.minecraft.util.shape.VoxelShape;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

@State(Scope.Thread)
public class VoxelRotationUncachedBenchmark {
    private VoxelShape shape;

    @Setup(Level.Trial)
    public void setUp() {
        shape = BenchmarkFixtures.complexShape(32, 0.0);
    }

    @Benchmark
    public VoxelShape legacyUncachedRotationCore() {
        return LegacyShapeOperations.rotate(shape, VoxelShapeTransformation.ROTATE_RIGHT);
    }

    @Benchmark
    public VoxelShape voxLibUncachedRotationCore() {
        return Minecraft1194ShapeOps.INSTANCE.transformBoxes(
            shape,
            VoxelShapeTransformation.ROTATE_RIGHT
        );
    }
}
