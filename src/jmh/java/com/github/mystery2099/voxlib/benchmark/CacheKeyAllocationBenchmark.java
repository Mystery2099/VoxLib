package com.github.mystery2099.voxlib.benchmark;

import com.github.mystery2099.voxlib.combination.VoxelAssembly;
import com.github.mystery2099.voxlib.optimization.ShapeCache;
import com.github.mystery2099.voxlib.optimization.ShapeCacheKey;
import com.github.mystery2099.voxlib.rotation.VoxelRotation;
import com.github.mystery2099.voxlib.rotation.VoxelShapeTransformation;
import java.util.List;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

@State(Scope.Thread)
public class CacheKeyAllocationBenchmark {
    private VoxelShape left;
    private VoxelShape right;
    private VoxelShape rotationSource;

    @Setup(Level.Trial)
    public void setUp() {
        ShapeCache.INSTANCE.clearCache();
        left = BenchmarkFixtures.complexShape(8, 0.0);
        right = BenchmarkFixtures.complexShape(8, 0.025);
        rotationSource = BenchmarkFixtures.complexShape(32, 0.0);

        legacyCachedUnion();
        legacyCachedRotation();
        VoxelAssembly.INSTANCE.and(left, right);
        VoxelAssembly.INSTANCE.and(left, right);
        VoxelRotation.INSTANCE.rotateWithTransformation(
            rotationSource,
            VoxelShapeTransformation.ROTATE_RIGHT
        );
    }

    @Benchmark
    public VoxelShape legacyCachedUnion() {
        ShapeCacheKey key = new ShapeCacheKey(
            left.hashCode() * 31 + right.hashCode(),
            "union",
            List.of(left, right)
        );
        return ShapeCache.INSTANCE.getOrCompute(
            key,
            ignored -> VoxelShapes.union(left, right)
        );
    }

    @Benchmark
    public VoxelShape specializedCachedUnion() {
        return VoxelAssembly.INSTANCE.and(left, right);
    }

    @Benchmark
    public VoxelShape legacyCachedRotation() {
        ShapeCacheKey key = new ShapeCacheKey(
            rotationSource.hashCode(),
            VoxelShapeTransformation.ROTATE_RIGHT.name(),
            List.of(rotationSource)
        );
        return ShapeCache.INSTANCE.getOrCompute(
            key,
            ignored -> LegacyShapeOperations.rotate(
                rotationSource,
                VoxelShapeTransformation.ROTATE_RIGHT
            )
        );
    }

    @Benchmark
    public VoxelShape specializedCachedRotation() {
        return VoxelRotation.INSTANCE.rotateWithTransformation(
            rotationSource,
            VoxelShapeTransformation.ROTATE_RIGHT
        );
    }

}
