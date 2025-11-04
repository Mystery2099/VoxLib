package com.github.mystery2099.voxlib.optimization;

import com.github.mystery2099.voxlib.combination.VoxelAssembly;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import oshi.util.tuples.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class ShapeSimplifier {

    /**
     * Creates a simplified version of a complex shape by using its bounding box.
     * This is useful for outline shapes that don't need to be as detailed as collision shapes.
     *
     * @param shape The complex shape to simplify.
     * @return A simplified VoxelShape based on the original's bounding box.
     */
    public static VoxelShape simplifyToBoundingBox(VoxelShape shape) {
        return Shapes.create(shape.bounds());
    }

    /**
     * Creates a simplified version of a complex shape by reducing the number of boxes.
     * This method merges boxes that are close to each other or overlap.
     *
     * @param shape    The complex shape to simplify.
     * @param maxBoxes The maximum number of boxes in the simplified shape.
     * @return A simplified VoxelShape with fewer boxes.
     */
    public static VoxelShape simplify(VoxelShape shape, int maxBoxes) {
        AtomicInteger boxCount = new AtomicInteger();
        shape.forAllBoxes((q, w, e, r, t, y) -> boxCount.getAndIncrement());
        if (boxCount.get() <= maxBoxes) return shape;
        List<AABB> boxes = new ArrayList<>();
        shape.forAllBoxes((minx, miny, minz, maxx, maxy, maxz) -> boxes.add(new AABB(minx, miny, minz, maxx, maxy, maxz)));
        while (boxes.size() > maxBoxes) mergeClosestBoxes(boxes);
        AtomicReference<VoxelShape> ret = new AtomicReference<>(Shapes.empty());
        boxes.forEach(box -> ret.set(VoxelAssembly.union(ret.get(), Shapes.create(box))));
        return ret.get();
    }

    public static VoxelShape simplify(VoxelShape shape) {
        return simplify(shape, 8);
    }

    /**
     * Finds the two closest boxes in the list and merges them.
     *
     * @param boxes The list of boxes to process.
     */
    private static void mergeClosestBoxes(List<AABB> boxes) {
        if (boxes.size() <= 1) return;
        Pair<Integer, Integer> closest = null;
        double minDist = Double.MAX_VALUE;
        for (int i = 0; i < boxes.size() - 1; i++)
            for (int j = i + 1; j < boxes.size(); j++) {
                double dist = calculateBoxDistance(boxes.get(i), boxes.get(j));
                if (dist < minDist) {
                    minDist = dist;
                    closest = new Pair<>(i, j);
                }
            }
        if (closest != null) {
            int i = closest.getA();
            int j = closest.getB();
            AABB merged = mergeBoxes(boxes.get(i), boxes.get(j));
            boxes.remove(j);
            boxes.remove(i);
            boxes.add(merged);
        }
    }

    /**
     * Merges two boxes into one that encompasses both.
     *
     * @param box1 The first box.
     * @param box2 The second box.
     * @return A new box that contains both input boxes.
     */
    private static AABB mergeBoxes(AABB box1, AABB box2) {
        return new AABB(
            min(box1.minX, box2.minX),
            min(box1.minY, box2.minY),
            min(box1.minZ, box2.minZ),
            max(box1.maxX, box2.maxX),
            max(box1.maxY, box2.maxY),
            max(box1.maxZ, box2.maxZ)
        );
    }

    /**
     * Calculates the distance between two boxes.
     * Overlapping boxes have a distance of 0.
     *
     * @param box1 The first box.
     * @param box2 The second box.
     * @return The distance between the boxes.
     */
    private static double calculateBoxDistance(AABB box1, AABB box2) {
        if (box1.intersects(box2)) return 0;
        double dx = max(0.0, max(box1.minX - box2.maxX, box2.minX - box1.maxX));
        double dy = max(0.0, max(box1.minY - box2.maxY, box2.minY - box1.maxY));
        double dz = max(0.0, max(box1.minZ - box2.maxZ, box2.minZ - box1.maxZ));
        return dx * dx + dy * dy + dz * dz;
    }


    /**
     * Creates a simplified outline shape for a block with the given dimensions.
     * This creates a hollow box shape that's more efficient than a complex shape.
     *
     * @param minX The minimum X coordinate.
     * @param minY The minimum Y coordinate.
     * @param minZ The minimum Z coordinate.
     * @param maxX The maximum X coordinate.
     * @param maxY The maximum Y coordinate.
     * @param maxZ The maximum Z coordinate.
     * @param thickness The thickness of the outline (default is 1).
     * @return A simplified hollow box VoxelShape.
     */
    public static VoxelShape createOutlineShape(
        double minX, double minY, double minZ,
        double maxX, double maxY, double maxZ,
        double thickness
    ) {
        VoxelShape outer = Shapes.box(minX, minY, minZ, maxX, maxY, maxZ);
        if(maxX - minX <= 2 * thickness ||
            maxY - minY <= 2 * thickness ||
            maxZ - minZ <= 2 * thickness
        ) return outer;
        VoxelShape inner = Shapes.box(
            minX + thickness, minY + thickness, minZ + thickness,
            maxX - thickness, maxY - thickness, maxZ - thickness
        );
        return Shapes.join(outer, inner, BooleanOp.ONLY_FIRST);
    }

    public static VoxelShape createOutlineShape(
        double minX, double minY, double minZ,
        double maxX, double maxY, double maxZ
    ) { return createOutlineShape(minX, minY, minZ, maxX, maxY, maxZ, 1); }
}
