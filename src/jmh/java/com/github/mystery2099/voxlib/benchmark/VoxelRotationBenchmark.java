package com.github.mystery2099.voxlib.benchmark;

import com.github.mystery2099.voxlib.optimization.ShapeCache;
import com.github.mystery2099.voxlib.rotation.VoxelRotation;
import com.github.mystery2099.voxlib.rotation.VoxelShapeTransformation;
import net.minecraft.util.shape.VoxelShape;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Group;
import org.openjdk.jmh.annotations.GroupThreads;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

@State(Scope.Benchmark)
public class VoxelRotationBenchmark {
    @Param({"1", "8", "32", "64"})
    public int boxCount;

    @Param({"ROTATE_LEFT", "ROTATE_RIGHT", "FLIP_HORIZONTAL", "FLIP_VERTICAL", "FLIP_Z"})
    public VoxelShapeTransformation transformation;

    private VoxelShape shape;

    @Setup(Level.Trial)
    public void setUp() {
        ShapeCache.INSTANCE.clearCache();
        shape = BenchmarkFixtures.complexShape(boxCount, 0.0);
    }

    @Benchmark
    public VoxelShape legacyUncachedRotation() {
        return LegacyShapeOperations.rotate(shape, transformation);
    }

    @Benchmark
    public VoxelShape voxLibWarmRotation() {
        return VoxelRotation.INSTANCE.rotateWithTransformation(shape, transformation);
    }

    @Benchmark
    @Group("contendedWarmRotation")
    @GroupThreads(4)
    public VoxelShape contendedWarmRotation() {
        return VoxelRotation.INSTANCE.rotateWithTransformation(shape, transformation);
    }
}
