package com.github.mystery2099.voxlib.rotation;

import com.github.mystery2099.voxlib.combination.VoxelAssembly;
import com.github.mystery2099.voxlib.optimization.CacheUtils;
import com.github.mystery2099.voxlib.optimization.ShapeCacheKey;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.List;

/**
 * A utility class for performing rotations and flips on Shapes.
 * It provides methods to rotate and flip Shapes in various ways.
 *
 * This class uses caching to improve performance for frequently transformed shapes.
 */
public class VoxelRotation {
    /**
     * Rotates the given VoxelShape to the left (90 degrees counterclockwise around the Y axis).
     *
     * @return A new VoxelShape after being rotated left.
     *
     * @see VoxelRotation#flipHorizontal
     * @see VoxelRotation#rotateRight
     */
    public static VoxelShape rotateLeft(VoxelShape s) {
        return rotateWithCache(s, VoxelShapeRotation.ROTATE_LEFT);
    }

    /**
     * Flips the given VoxelShape horizontally (180 degrees around the Y axis).
     * This is equivalent to rotating the shape 180 degrees.
     *
     * @return A new VoxelShape after being flipped horizontally.
     *
     * @see VoxelRotation#rotateLeft
     * @see VoxelRotation#rotateRight
     * @see VoxelRotation#flipVertical
     * @see VoxelRotation#flipZ
     */
    public static VoxelShape flipHorizontal(VoxelShape s) {
        return rotateWithCache(s, VoxelShapeRotation.FLIP_HORIZONTAL);
    }

    /**
     * Rotates the given VoxelShape to the right (90 degrees clockwise around the Y axis).
     *
     * @return A new VoxelShape after being rotated right.
     *
     * @see VoxelRotation#rotateLeft
     * @see VoxelRotation#flipHorizontal
     */
    public static VoxelShape rotateRight(VoxelShape s) {
        return rotateWithCache(s, VoxelShapeRotation.ROTATE_RIGHT);
    }

    /**
     * Flips the given VoxelShape vertically (180 degrees around the X axis).
     * This transforms the top face to the bottom and vice versa.
     *
     * @return A new VoxelShape after being flipped vertically.
     *
     * @see VoxelRotation#flipHorizontal
     * @see VoxelRotation#flipZ
     * @see VoxelRotation#rotate
     */
    public static VoxelShape flipVertical(VoxelShape s) {
        return rotateVerticalWithCache(s, VoxelShapeRotation.FLIP_VERTICAL);
    }

    /**
     * Flips the given VoxelShape along the Z axis (180 degrees around the Z axis).
     * This transforms the front face to the back and vice versa.
     *
     * @return A new VoxelShape after being flipped along the Z axis.
     *
     * @see VoxelRotation#flipHorizontal
     * @see VoxelRotation#flipVertical
     * @see VoxelRotation#rotate
     */
    public static VoxelShape flipZ(VoxelShape s) {
        return rotateVerticalWithCache(s, VoxelShapeRotation.FLIP_Z);
    }

    /**
     * Rotates or flips the given VoxelShape using the specified transformation.
     * This is a general-purpose method that can apply any transformation defined in [VoxelShapeTransformation].
     *
     * Note: This method was added in version 1.2.0 to provide a more flexible API.
     * For backward compatibility, you can continue to use the specific methods like
     * [rotateLeft], [rotateRight], [flipHorizontal], etc.
     *
     * @param transform The transformation to apply.
     * @return A new VoxelShape after the transformation has been applied.
     */
    public static VoxelShape rotate(VoxelShape s, VoxelShapeRotation transform) {
        return switch(transform) {
            case ROTATE_LEFT -> rotateLeft(s);
            case ROTATE_RIGHT -> rotateRight(s);
            case FLIP_HORIZONTAL -> flipHorizontal(s);
            case FLIP_VERTICAL -> flipVertical(s);
            case FLIP_Z -> flipZ(s);
        };
    }

    /**
     * Helper method to rotate a shape with caching.
     *
     * @param shape The shape to rotate.
     * @param transformation The transformation to apply.
     * @return The rotated shape.
     */
    private static VoxelShape rotateWithCache(VoxelShape shape, VoxelShapeRotation transformation) {
        if(shape.isEmpty()) return Shapes.empty();
        if(shape.equals(Shapes.block())) return Shapes.block();
        ShapeCacheKey key = new ShapeCacheKey(
            shape.hashCode(),
            transformation.name(),
            new ArrayList<>()
        );
        return CacheUtils.getOrCompute(key, (k) -> rotateUncached(shape, transformation));
    }

    /**
     * Rotates or flips the given VoxelShape using the specified transformation around the Y axis.
     * This version doesn't use caching and is used internally by the cached methods.
     *
     * @param transformation The transformation to apply.
     * @return A new VoxelShape after being rotated or flipped.
     */
    private static VoxelShape rotateUncached(VoxelShape s, VoxelShapeRotation transformation) {
        if(s.isEmpty() || s.equals(Shapes.block())) return s;
        List<VoxelShape> shapes = new ArrayList<>();
        s.forAllBoxes((x, y, z, X, Y, Z) -> {
           double[] adjusted = adjustValues(transformation, x, y, z, X, Y, Z);
           shapes.add(Shapes.box(
               adjusted[0], y, adjusted[1],
               adjusted[2], Y, adjusted[3]
           ));
        });
        return VoxelAssembly.optimizedUnion(shapes);
    }

    /**
     * Helper method to rotate a shape vertically with caching.
     *
     * @param shape The shape to rotate.
     * @param transformation The transformation to apply.
     * @return The rotated shape.
     */
    private static VoxelShape rotateVerticalWithCache(VoxelShape shape, VoxelShapeRotation transformation) {
        if(shape.isEmpty() || shape.equals(Shapes.block())) return shape;
        ShapeCacheKey key = new ShapeCacheKey(
            shape.hashCode(),
            transformation.name(),
            new ArrayList<>()
        );
        return CacheUtils.getOrCompute(key, (k) -> rotateVerticalUncached(shape, transformation));
    }

    /**
     * Rotates or flips the given VoxelShape vertically using the specified transformation.
     * This version doesn't use caching and is used internally by the cached methods.
     * @param shape The shape being rotated
     * @param transformation The transformation to apply.
     * @return A new VoxelShape after being rotated or flipped vertically.
     */
    private static VoxelShape rotateVerticalUncached(VoxelShape shape, VoxelShapeRotation transformation) {
        if(shape.isEmpty() || shape.equals(Shapes.block())) return shape;
        List<VoxelShape> shapes = new ArrayList<>();
        shape.forAllBoxes((x, y, z, X, Y, Z) -> {
            double[] adjusted = adjustValues(transformation, x, y, z, X, Y, Z);
            shapes.add(Shapes.box(
                x, adjusted[0], adjusted[1],
                X, adjusted[2], adjusted[3]
            ));
        });
        return VoxelAssembly.optimizedUnion(shapes);
    }

    /**
     * Adjusts values based on the given transformation, simulating the rotation or flip around the Y axis.
     *
     * @param direction The transformation to apply.
     * @param minX The minimum X-coordinate.
     * @param minZ The minimum Z-coordinate.
     * @param maxX The maximum X-coordinate.
     * @param maxZ The maximum Z-coordinate.
     *
     * @return An array of adjusted values.
     */
    private static double[] adjustValues(
        VoxelShapeRotation direction,
        double minX, double minY, double minZ,
        double maxX, double maxY, double maxZ
    ) {
        return switch(direction) {
            case FLIP_HORIZONTAL -> new double[] {1-maxX, 1-maxZ, 1-minX, 1-minZ};
            case ROTATE_RIGHT -> new double[] {minZ, 1-maxX, maxZ, 1-minX};
            case ROTATE_LEFT -> new double[] {1-maxZ, minX, 1-minZ, maxX};
            case FLIP_VERTICAL -> new double[] {1-maxY, minZ, 1-minY, maxX};
            case FLIP_Z -> new double[] {minY, 1-maxZ, maxY, 1-minZ};
        };
    }
}
