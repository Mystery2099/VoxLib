package com.github.mystery2099.voxlib.benchmark;

import com.github.mystery2099.voxlib.optimization.ShapeSimplifier;
import net.minecraft.util.shape.VoxelShape;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

public class ShapeSimplifierBenchmark {
    @State(Scope.Thread)
    public static class SimplifierState {
        @Param({"8", "16", "32", "64", "256"})
        public int boxCount;

        @Param({"1", "8"})
        public int maxBoxes;

        private VoxelShape shape;

        @Setup(Level.Trial)
        public void setUp() {
            shape = BenchmarkFixtures.simplifierShape(boxCount);
        }
    }

    @State(Scope.Thread)
    public static class LegacySimplifierState {
        @Param({"8", "16", "32", "64"})
        public int boxCount;

        @Param({"1", "8"})
        public int maxBoxes;

        private VoxelShape shape;

        @Setup(Level.Trial)
        public void setUp() {
            shape = BenchmarkFixtures.simplifierShape(boxCount);
        }
    }

    @Benchmark
    public VoxelShape legacySimplifier(LegacySimplifierState state) {
        return LegacyShapeOperations.simplify(state.shape, state.maxBoxes);
    }

    @Benchmark
    public VoxelShape objectQueueSimplifier(SimplifierState state) {
        return LegacyShapeOperations.objectQueueSimplify(state.shape, state.maxBoxes);
    }

    @Benchmark
    public VoxelShape voxLibSimplifier(SimplifierState state) {
        return ShapeSimplifier.INSTANCE.simplify(state.shape, state.maxBoxes);
    }
}
