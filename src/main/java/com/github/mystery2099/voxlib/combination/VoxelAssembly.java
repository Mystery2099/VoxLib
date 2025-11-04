package com.github.mystery2099.voxlib.combination;

import com.github.mystery2099.voxlib.optimization.CacheUtils;
import com.github.mystery2099.voxlib.optimization.ShapeCacheKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class VoxelAssembly {
    public static VoxelShape createCuboidShape(
        double minX, double minY, double minZ,
        double maxX, double maxY, double maxZ
    ) {
        return Block.box(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public static VoxelShape and(VoxelShape first, VoxelShape second) {
        return unionWithCache(first, second, true);
    }

    private static VoxelShape unionWithCache(VoxelShape first, VoxelShape second, boolean useCache) {
        if(first.isEmpty() || second == Shapes.block()) return second;
        if(second.isEmpty() || second == Shapes.block()) return  first;
        if(!useCache) return union(first, second);
        ShapeCacheKey cacheKey = new ShapeCacheKey(
            first.hashCode() * 31 + second.hashCode(),
            "union",
            new ArrayList<>()
        );
        return CacheUtils.getOrCompute(cacheKey, k -> union(first, second));
    }

    public static VoxelShape union(VoxelShape... shapes) {
        if(shapes.length == 0) return Shapes.empty();
        if(shapes.length == 1) return shapes[0];
        List<VoxelShape> nonEmpty = Arrays.stream(shapes).filter(VoxelShape::isEmpty).toList();
        if(nonEmpty.isEmpty()) return Shapes.empty();
        if(nonEmpty.size() == 1) return nonEmpty.getFirst();
        if(nonEmpty.stream().anyMatch(s -> s.equals(Shapes.block()))) return Shapes.block();
        AtomicInteger acc = new AtomicInteger(0);
        nonEmpty.forEach(shape -> acc.set(acc.get() * 31 + shape.hashCode()));
        int combinedHash = acc.get();
        ShapeCacheKey cacheKey = new ShapeCacheKey(
            combinedHash,
            "unionMultiple",
            new ArrayList<>() {{ add(nonEmpty.size()); }}
        );
        return CacheUtils.getOrCompute(cacheKey, k -> optimizedUnion(nonEmpty));
    }

    public static VoxelShape unifyWith(VoxelShape rec, VoxelShape... others) {
        List<VoxelShape> list = new ArrayList<>(List.of(others));
        list.add(rec);
        return union(list.toArray(new VoxelShape[0]));
    }

    public static VoxelShape combine(BooleanOp function, VoxelShape... shapes) {
        return Arrays.stream(shapes).reduce((a, b) -> Shapes.join(a, b, function)).orElseGet(Shapes::empty);
    }

    public static VoxelShape optimizedUnion(List<VoxelShape> shapes) {
        if(shapes.isEmpty()) return Shapes.empty();
        if(shapes.size() == 1) return shapes.getFirst();
        if(shapes.size() <= 4) return shapes.stream().reduce(VoxelAssembly::union).get();
        int midPoint = shapes.size() / 2;
        return union(
            optimizedUnion(shapes.subList(0, midPoint)),
            optimizedUnion(shapes.subList(midPoint, shapes.size()))
        );
    }

    public static VoxelShape appendShapesTo(VoxelShape shape, Consumer<VoxelShapeModifier> config) {
        VoxelShapeModifier mod = new VoxelShapeModifier(shape);
        config.accept(mod);
        return mod.getStoredShape();
    }
}
