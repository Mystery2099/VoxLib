package com.github.mystery2099.voxlib.benchmark;

import com.github.mystery2099.voxlib.rotation.VoxelShapeTransformation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;

final class LegacyShapeOperations {
    private LegacyShapeOperations() {
    }

    static VoxelShape rotate(VoxelShape shape, VoxelShapeTransformation transformation) {
        if (shape.isEmpty() || shape == VoxelShapes.fullCube()) {
            return shape;
        }

        List<VoxelShape> boxes = new ArrayList<>();
        shape.forEachBox((minX, minY, minZ, maxX, maxY, maxZ) -> {
            double[] coordinates = transform(
                transformation, minX, minY, minZ, maxX, maxY, maxZ
            );
            boxes.add(VoxelShapes.cuboid(
                coordinates[0], coordinates[1], coordinates[2],
                coordinates[3], coordinates[4], coordinates[5]
            ));
        });
        return balancedUnion(boxes, 0, boxes.size());
    }

    static VoxelShape simplify(VoxelShape shape, int maxBoxes) {
        List<Box> boxes = new ArrayList<>();
        shape.forEachBox((minX, minY, minZ, maxX, maxY, maxZ) ->
            boxes.add(new Box(minX, minY, minZ, maxX, maxY, maxZ))
        );
        while (boxes.size() > maxBoxes) {
            mergeClosestBoxes(boxes);
        }

        VoxelShape result = VoxelShapes.empty();
        for (Box box : boxes) {
            result = VoxelShapes.union(result, VoxelShapes.cuboid(box));
        }
        return result;
    }

    static VoxelShape objectQueueSimplify(VoxelShape shape, int maxBoxes) {
        List<ActiveBox> boxes = new ArrayList<>();
        shape.forEachBox((minX, minY, minZ, maxX, maxY, maxZ) ->
            boxes.add(new ActiveBox(
                boxes.size(),
                new Box(minX, minY, minZ, maxX, maxY, maxZ)
            ))
        );
        PriorityQueue<MergeCandidate> candidates = new PriorityQueue<>(MergeCandidate.ORDER);
        for (int first = 0; first < boxes.size() - 1; first++) {
            for (int second = first + 1; second < boxes.size(); second++) {
                candidates.add(new MergeCandidate(boxes.get(first), boxes.get(second)));
            }
        }

        int activeCount = boxes.size();
        int nextPosition = boxes.size();
        while (activeCount > maxBoxes) {
            MergeCandidate closest = pollActiveCandidate(candidates);
            closest.first.active = false;
            closest.second.active = false;
            ActiveBox merged = new ActiveBox(
                nextPosition++,
                encompass(closest.first.box, closest.second.box)
            );
            for (ActiveBox box : boxes) {
                if (box.active) {
                    candidates.add(new MergeCandidate(box, merged));
                }
            }
            boxes.add(merged);
            activeCount--;
        }

        VoxelShape result = VoxelShapes.empty();
        for (ActiveBox box : boxes) {
            if (box.active) {
                result = VoxelShapes.union(result, VoxelShapes.cuboid(box.box));
            }
        }
        return result;
    }

    static VoxelShape leftFoldUnion(VoxelShape[] shapes) {
        VoxelShape result = shapes[0];
        for (int index = 1; index < shapes.length; index++) {
            result = VoxelShapes.union(result, shapes[index]);
        }
        return result;
    }

    static VoxelShape divideAndConquerUnion(VoxelShape[] shapes) {
        return legacyBalancedUnion(Arrays.asList(shapes));
    }

    static VoxelShape allocationFreeBalancedUnion(VoxelShape[] shapes) {
        return allocationFreeBalancedUnion(shapes, 0, shapes.length);
    }

    static VoxelShape table(int legWidth, int topThickness) {
        int legOffset = 16 - legWidth;
        return VoxelShapes.union(
            cuboid(0, 16 - topThickness, 0, 16, 16, 16),
            cuboid(0, 0, 0, legWidth, 16 - topThickness, legWidth),
            cuboid(legOffset, 0, 0, 16, 16 - topThickness, legWidth),
            cuboid(0, 0, legOffset, legWidth, 16 - topThickness, 16),
            cuboid(legOffset, 0, legOffset, 16, 16 - topThickness, 16)
        );
    }

    static VoxelShape slab(int height) {
        return cuboid(0, 0, 0, 16, height, 16);
    }

    static VoxelShape pillar(int width, boolean centered) {
        if (!centered) {
            return cuboid(0, 0, 0, width, 16, width);
        }
        int offset = (16 - width) / 2;
        return cuboid(offset, 0, offset, offset + width, 16, offset + width);
    }

    static VoxelShape chair(int seatHeight, boolean hasBackrest, int backrestHeight) {
        VoxelShape seat = cuboid(1, seatHeight, 1, 15, seatHeight + 2, 15);
        VoxelShape legs = cuboid(2, 0, 2, 14, seatHeight, 14);
        if (!hasBackrest) {
            return VoxelShapes.union(seat, legs);
        }
        VoxelShape backrest = cuboid(
            2, seatHeight + 2, 12, 14, seatHeight + 2 + backrestHeight, 15
        );
        return VoxelShapes.union(seat, legs, backrest);
    }

    private static VoxelShape cuboid(
        double minX, double minY, double minZ,
        double maxX, double maxY, double maxZ
    ) {
        return VoxelShapes.cuboid(
            minX / 16.0, minY / 16.0, minZ / 16.0,
            maxX / 16.0, maxY / 16.0, maxZ / 16.0
        );
    }

    private static VoxelShape balancedUnion(List<VoxelShape> shapes, int from, int to) {
        int size = to - from;
        if (size == 0) {
            return VoxelShapes.empty();
        }
        if (size == 1) {
            return shapes.get(from);
        }
        if (size <= 4) {
            VoxelShape result = shapes.get(from);
            for (int index = from + 1; index < to; index++) {
                result = VoxelShapes.union(result, shapes.get(index));
            }
            return result;
        }
        int middle = from + size / 2;
        return VoxelShapes.union(
            balancedUnion(shapes, from, middle),
            balancedUnion(shapes, middle, to)
        );
    }

    private static VoxelShape legacyBalancedUnion(List<VoxelShape> shapes) {
        if (shapes.size() == 1) {
            return shapes.get(0);
        }
        if (shapes.size() <= 4) {
            VoxelShape result = shapes.get(0);
            for (int index = 1; index < shapes.size(); index++) {
                result = VoxelShapes.union(result, shapes.get(index));
            }
            return result;
        }

        int middle = shapes.size() / 2;
        return VoxelShapes.union(
            legacyBalancedUnion(shapes.subList(0, middle)),
            legacyBalancedUnion(shapes.subList(middle, shapes.size()))
        );
    }

    private static VoxelShape allocationFreeBalancedUnion(
        VoxelShape[] shapes,
        int fromIndex,
        int toIndex
    ) {
        int size = toIndex - fromIndex;
        if (size == 1) {
            return shapes[fromIndex];
        }
        if (size <= 4) {
            VoxelShape result = shapes[fromIndex];
            for (int index = fromIndex + 1; index < toIndex; index++) {
                result = VoxelShapes.union(result, shapes[index]);
            }
            return result;
        }

        int middle = fromIndex + size / 2;
        return VoxelShapes.union(
            allocationFreeBalancedUnion(shapes, fromIndex, middle),
            allocationFreeBalancedUnion(shapes, middle, toIndex)
        );
    }

    private static double[] transform(
        VoxelShapeTransformation transformation,
        double minX, double minY, double minZ,
        double maxX, double maxY, double maxZ
    ) {
        return switch (transformation) {
            case ROTATE_LEFT ->
                new double[] {1.0 - maxZ, minY, minX, 1.0 - minZ, maxY, maxX};
            case ROTATE_RIGHT ->
                new double[] {minZ, minY, 1.0 - maxX, maxZ, maxY, 1.0 - minX};
            case FLIP_HORIZONTAL ->
                new double[] {1.0 - maxX, minY, 1.0 - maxZ, 1.0 - minX, maxY, 1.0 - minZ};
            case FLIP_VERTICAL ->
                new double[] {minX, 1.0 - maxY, minZ, maxX, 1.0 - minY, maxZ};
            case FLIP_Z ->
                new double[] {minX, minY, 1.0 - maxZ, maxX, maxY, 1.0 - minZ};
        };
    }

    private static void mergeClosestBoxes(List<Box> boxes) {
        int closestFirst = 0;
        int closestSecond = 1;
        double minimumDistance = Double.MAX_VALUE;
        for (int first = 0; first < boxes.size() - 1; first++) {
            for (int second = first + 1; second < boxes.size(); second++) {
                double distance = distance(boxes.get(first), boxes.get(second));
                if (distance < minimumDistance) {
                    minimumDistance = distance;
                    closestFirst = first;
                    closestSecond = second;
                }
            }
        }

        Box merged = encompass(boxes.get(closestFirst), boxes.get(closestSecond));
        boxes.remove(closestSecond);
        boxes.remove(closestFirst);
        boxes.add(merged);
    }

    private static double distance(Box first, Box second) {
        if (first.intersects(second)) {
            return 0.0;
        }
        double dx = Math.max(0.0, Math.max(first.minX - second.maxX, second.minX - first.maxX));
        double dy = Math.max(0.0, Math.max(first.minY - second.maxY, second.minY - first.maxY));
        double dz = Math.max(0.0, Math.max(first.minZ - second.maxZ, second.minZ - first.maxZ));
        return dx * dx + dy * dy + dz * dz;
    }

    private static Box encompass(Box first, Box second) {
        return new Box(
            Math.min(first.minX, second.minX),
            Math.min(first.minY, second.minY),
            Math.min(first.minZ, second.minZ),
            Math.max(first.maxX, second.maxX),
            Math.max(first.maxY, second.maxY),
            Math.max(first.maxZ, second.maxZ)
        );
    }

    private static MergeCandidate pollActiveCandidate(
        PriorityQueue<MergeCandidate> candidates
    ) {
        while (true) {
            MergeCandidate candidate = candidates.remove();
            if (candidate.first.active && candidate.second.active) {
                return candidate;
            }
        }
    }

    private static final class ActiveBox {
        final int position;
        final Box box;
        boolean active = true;

        ActiveBox(int position, Box box) {
            this.position = position;
            this.box = box;
        }
    }

    private static final class MergeCandidate {
        static final Comparator<MergeCandidate> ORDER = Comparator
            .comparingDouble((MergeCandidate candidate) -> candidate.distance)
            .thenComparingInt(candidate -> candidate.first.position)
            .thenComparingInt(candidate -> candidate.second.position);

        final ActiveBox first;
        final ActiveBox second;
        final double distance;

        MergeCandidate(ActiveBox first, ActiveBox second) {
            this.first = first;
            this.second = second;
            this.distance = LegacyShapeOperations.distance(first.box, second.box);
        }
    }
}
