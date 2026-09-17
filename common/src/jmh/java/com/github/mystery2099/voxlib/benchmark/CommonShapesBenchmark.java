package com.github.mystery2099.voxlib.benchmark;

import com.github.mystery2099.voxlib.shapes.CommonShapes;
import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicReferenceArray;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

@State(Scope.Thread)
public class CommonShapesBenchmark {
    @State(Scope.Thread)
    public static class ColdPrimitiveFactoryState {
        private AtomicReferenceArray<VoxelShape> slabs;
        private AtomicReferenceArray<VoxelShape> pillars;

        @SuppressWarnings("unchecked")
        @Setup(Level.Trial)
        public void findCaches() throws ReflectiveOperationException {
            Field slabsField = CommonShapes.class.getDeclaredField("slabs");
            Field pillarsField = CommonShapes.class.getDeclaredField("pillars");
            slabsField.setAccessible(true);
            pillarsField.setAccessible(true);
            slabs = (AtomicReferenceArray<VoxelShape>) slabsField.get(CommonShapes.INSTANCE);
            pillars = (AtomicReferenceArray<VoxelShape>) pillarsField.get(CommonShapes.INSTANCE);
        }

        @Setup(Level.Invocation)
        public void clearEntries() {
            slabs.set(7, null);
            pillars.set(5, null);
        }
    }

    @Benchmark
    public VoxelShape slab() {
        return CommonShapes.INSTANCE.createSlab(8);
    }

    @Benchmark
    public VoxelShape coldSlab(ColdPrimitiveFactoryState state) {
        return CommonShapes.INSTANCE.createSlab(8);
    }

    @Benchmark
    public VoxelShape legacySlab() {
        return LegacyShapeOperations.slab(8);
    }

    @Benchmark
    public VoxelShape pillar() {
        return CommonShapes.INSTANCE.createPillar(6, true);
    }

    @Benchmark
    public VoxelShape coldPillar(ColdPrimitiveFactoryState state) {
        return CommonShapes.INSTANCE.createPillar(6, true);
    }

    @Benchmark
    public VoxelShape legacyPillar() {
        return LegacyShapeOperations.pillar(6, true);
    }

    @Benchmark
    public VoxelShape table() {
        return CommonShapes.INSTANCE.createTable(2, 2);
    }

    @Benchmark
    public VoxelShape legacyTable() {
        return LegacyShapeOperations.table(2, 2);
    }

    @Benchmark
    public VoxelShape chair() {
        return CommonShapes.INSTANCE.createChair(8, true, 8);
    }

    @Benchmark
    public VoxelShape legacyChair() {
        return LegacyShapeOperations.chair(8, true, 8);
    }

    @Benchmark
    public VoxelShape fencePost() {
        return CommonShapes.INSTANCE.createFencePost();
    }

    @Benchmark
    public VoxelShape fenceConnections() {
        return CommonShapes.INSTANCE.createFenceConnections(true, true, false, true);
    }

    @Benchmark
    public VoxelShape stairs() {
        return CommonShapes.INSTANCE.createStairs(Direction.EAST);
    }
}
