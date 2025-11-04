package com.github.mystery2099.voxlib.rotation;

public enum VoxelShapeRotation {
    /**
     * Rotate the VoxelShape to the left (90 degrees counterclockwise around the Y axis).
     */
    ROTATE_LEFT,

    /**
     * Rotate the VoxelShape to the right (90 degrees clockwise around the Y axis).
     */
    ROTATE_RIGHT,

    /**
     * Flip the VoxelShape horizontally (180 degrees around the Y axis).
     */
    FLIP_HORIZONTAL,

    /**
     * Flip the VoxelShape vertically (180 degrees around the X axis).
     */
    FLIP_VERTICAL,

    /**
     * Rotate the VoxelShape 180 degrees around the Z axis.
     */
    FLIP_Z
}
