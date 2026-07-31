package com.github.mystery2099.voxlib.benchmark;

import com.github.mystery2099.voxlib.optimization.ShapeSimplifier;
import net.minecraft.util.shape.VoxelShape;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

@State(Scope.Thread)
public class ShapeSimplifierBenchmark {
    @Param({"8", "16", "32", "64", "256"})
    public int boxCount;

    @Param({"1", "8"})
    public int maxBoxes;

    private VoxelShape shape;

    @Setup(Level.Trial)
    public void setUp() {
        shape = BenchmarkFixtures.simplifierShape(boxCount);
    }

    @Benchmark
    public VoxelShape legacySimplifier() {
        return LegacyShapeOperations.simplify(shape, maxBoxes);
    }

    @Benchmark
    public VoxelShape objectQueueSimplifier() {
        return LegacyShapeOperations.objectQueueSimplify(shape, maxBoxes);
    }

    @Benchmark
    public VoxelShape voxLibSimplifier() {
        return ShapeSimplifier.INSTANCE.simplify(shape, maxBoxes);
    }
}
