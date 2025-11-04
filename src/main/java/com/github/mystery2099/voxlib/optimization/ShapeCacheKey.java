package com.github.mystery2099.voxlib.optimization;

import java.util.List;

/**
 * A key for the shape cache. This combines the original shape's hashcode with
 * an operation identifier to create a unique key for each transformed shape.
 *
 * @property originalShapeHash The hashcode of the original shape.
 * @property operationId A string identifier for the operation performed.
 * @property parameters Additional parameters that affect the transformation.
 */
public record ShapeCacheKey(
    int originalHash,
    String operationId,
    List<Object> parameters
) {}
