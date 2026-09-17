package com.github.mystery2099.voxlib.benchmark;

import com.github.mystery2099.voxlib.combination.VoxelAssembly;
import com.github.mystery2099.voxlib.optimization.ShapeCache;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Group;
import org.openjdk.jmh.annotations.GroupThreads;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

public class VoxelUnionBenchmark {
    @State(Scope.Benchmark)
    public static class BinaryUnionState {
        @Param({"1", "8", "32"})
        public int boxCount;

        VoxelShape left;
        VoxelShape right;

        @Setup(Level.Trial)
        public void setUp() {
            ShapeCache.INSTANCE.clearCache();
            left = BenchmarkFixtures.complexShape(boxCount, 0.0);
            right = BenchmarkFixtures.complexShape(boxCount, 0.025);
        }
    }

    @Benchmark
    public VoxelShape vanillaBinaryUnion(BinaryUnionState state) {
        return VoxelShapes.union(state.left, state.right);
    }

    @Benchmark
    public VoxelShape voxLibWarmBinaryUnion(BinaryUnionState state) {
        return VoxelAssembly.INSTANCE.and(state.left, state.right);
    }

    @State(Scope.Thread)
    public static class ColdBinaryUnionState {
        @Param({"1", "8", "32"})
        public int boxCount;

        VoxelShape left;
        VoxelShape right;

        @Setup(Level.Invocation)
        public void setUp() {
            ShapeCache.INSTANCE.clearCache();
            left = BenchmarkFixtures.complexShape(boxCount, 0.0);
            right = BenchmarkFixtures.complexShape(boxCount, 0.025);
        }
    }

    @State(Scope.Thread)
    public static class DisjointBinaryUnionState {
        VoxelShape left;
        VoxelShape right;

        @Setup(Level.Trial)
        public void setUp() {
            ShapeCache.INSTANCE.clearCache();
            left = BenchmarkFixtures.complexShape(8, 0.0);
            right = BenchmarkFixtures.complexShape(8, 1.0);
        }
    }

    @Benchmark
    public VoxelShape vanillaColdBinaryUnion(ColdBinaryUnionState state) {
        return VoxelShapes.union(state.left, state.right);
    }

    @Benchmark
    public VoxelShape voxLibColdBinaryUnion(ColdBinaryUnionState state) {
        return VoxelAssembly.INSTANCE.and(state.left, state.right);
    }

    @Benchmark
    public VoxelShape vanillaDisjointBinaryUnion(DisjointBinaryUnionState state) {
        return VoxelShapes.union(state.left, state.right);
    }

    @Benchmark
    public VoxelShape voxLibDisjointBinaryUnion(DisjointBinaryUnionState state) {
        return VoxelAssembly.INSTANCE.and(state.left, state.right);
    }

    @Benchmark
    @Group("contendedWarmUnion")
    @GroupThreads(4)
    public VoxelShape contendedWarmUnion(BinaryUnionState state) {
        return VoxelAssembly.INSTANCE.and(state.left, state.right);
    }

    @State(Scope.Thread)
    public static class MultiUnionState {
        @Param({"4", "8", "16", "32", "64"})
        public int shapeCount;

        VoxelShape[] shapes;
        VoxelShape first;
        VoxelShape[] remaining;

        @Setup(Level.Trial)
        public void setUp() {
            shapes = BenchmarkFixtures.componentShapes(shapeCount, 0.0);
            first = shapes[0];
            remaining = new VoxelShape[shapes.length - 1];
            System.arraycopy(shapes, 1, remaining, 0, remaining.length);
        }
    }

    @State(Scope.Thread)
    public static class ColdMultiUnionState {
        @Param({"4", "8", "16", "32", "64"})
        public int shapeCount;

        VoxelShape[] shapes;
        VoxelShape first;
        VoxelShape[] remaining;

        @Setup(Level.Invocation)
        public void setUp() {
            ShapeCache.INSTANCE.clearCache();
            shapes = BenchmarkFixtures.componentShapes(shapeCount, 0.0);
            first = shapes[0];
            remaining = new VoxelShape[shapes.length - 1];
            System.arraycopy(shapes, 1, remaining, 0, remaining.length);
        }
    }

    @Benchmark
    public VoxelShape vanillaMultiUnion(MultiUnionState state) {
        return VoxelShapes.union(state.first, state.remaining);
    }

    @Benchmark
    public VoxelShape voxLibMultiUnion(MultiUnionState state) {
        return VoxelAssembly.INSTANCE.union(state.shapes);
    }

    @Benchmark
    public VoxelShape legacyLeftFoldMultiUnion(MultiUnionState state) {
        return LegacyShapeOperations.leftFoldUnion(state.shapes);
    }

    @Benchmark
    public VoxelShape legacyDivideAndConquerMultiUnion(MultiUnionState state) {
        return LegacyShapeOperations.divideAndConquerUnion(state.shapes);
    }

    @Benchmark
    public VoxelShape allocationFreeBalancedMultiUnion(MultiUnionState state) {
        return LegacyShapeOperations.allocationFreeBalancedUnion(state.shapes);
    }

    @Benchmark
    public VoxelShape vanillaColdMultiUnion(ColdMultiUnionState state) {
        return VoxelShapes.union(state.first, state.remaining);
    }

    @Benchmark
    public VoxelShape voxLibColdMultiUnion(ColdMultiUnionState state) {
        return VoxelAssembly.INSTANCE.union(state.shapes);
    }
}
