package com.github.mystery2099.voxlib.shapes

import com.github.mystery2099.voxlib.combination.VoxelAssembly.createCuboidShape
import com.github.mystery2099.voxlib.combination.VoxelAssembly.plus
import net.minecraft.util.shape.VoxelShape

/**
 * A utility object providing pre-defined common shapes used in Minecraft modding.
 * These shapes can be used as a starting point for more complex block shapes.
 */
object CommonShapes {

    /**
     * Creates a slab shape with the specified height.
     *
     * @param height The height of the slab (1-16).
     * @return A VoxelShape representing a slab with the given height.
     */
    fun createSlab(height: Int): VoxelShape {
        require(height in 1..16) { "Height must be between 1 and 16" }
        return createCuboidShape(0, 0, 0, 16, height, 16)
    }

    /**
     * Creates a top slab shape with the specified height.
     *
     * @param height The height of the slab (1-16).
     * @return A VoxelShape representing a top slab with the given height.
     */
    fun createTopSlab(height: Int): VoxelShape {
        require(height in 1..16) { "Height must be between 1 and 16" }
        val slabHeight = 16 - height
        return createCuboidShape(0, slabHeight, 0, 16, 16, 16)
    }

    /**
     * Creates a pillar shape with the specified width.
     *
     * @param width The width of the pillar (1-14).
     * @param centered Whether the pillar should be centered in the block.
     * @return A VoxelShape representing a pillar with the given width.
     */
    fun createPillar(width: Int, centered: Boolean = true): VoxelShape {
        require(width in 1..14) { "Width must be between 1 and 14" }

        return if (centered) {
            val offset = (16 - width) / 2
            createCuboidShape(offset, 0, offset, offset + width, 16, offset + width)
        } else {
            createCuboidShape(0, 0, 0, width, 16, width)
        }
    }

    /**
     * Creates a table shape with the specified leg width and top thickness.
     *
     * @param legWidth The width of the table legs (1-6).
     * @param topThickness The thickness of the table top (1-6).
     * @return A VoxelShape representing a table.
     */
    fun createTable(legWidth: Int = 2, topThickness: Int = 2): VoxelShape {
        require(legWidth in 1..6) { "Leg width must be between 1 and 6" }
        require(topThickness in 1..6) { "Top thickness must be between 1 and 6" }

        val tableTop = createCuboidShape(0, 16 - topThickness, 0, 16, 16, 16)
        val legOffset = 16 - legWidth

        val leg1 = createCuboidShape(0, 0, 0, legWidth, 16 - topThickness, legWidth)
        val leg2 = createCuboidShape(legOffset, 0, 0, 16, 16 - topThickness, legWidth)
        val leg3 = createCuboidShape(0, 0, legOffset, legWidth, 16 - topThickness, 16)
        val leg4 = createCuboidShape(legOffset, 0, legOffset, 16, 16 - topThickness, 16)

        return tableTop + leg1 + leg2 + leg3 + leg4
    }

    /**
     * Creates a chair shape with the specified seat height and optional backrest.
     *
     * @param seatHeight The height of the chair seat (1-12).
     * @param hasBackrest Whether the chair should have a backrest.
     * @param backrestHeight The height of the backrest above the seat (1-16).
     * @return A VoxelShape representing a chair.
     */
    fun createChair(seatHeight: Int = 8, hasBackrest: Boolean = true, backrestHeight: Int = 8): VoxelShape {
        require(seatHeight in 1..12) { "Seat height must be between 1 and 12" }
        require(backrestHeight in 1..16) { "Backrest height must be between 1 and 16" }

        val seat = createCuboidShape(1, seatHeight, 1, 15, seatHeight + 2, 15)
        val legs = createCuboidShape(2, 0, 2, 14, seatHeight, 14)

        return if (hasBackrest) {
            val backrest = createCuboidShape(2, seatHeight + 2, 12, 14, seatHeight + 2 + backrestHeight, 15)
            seat + legs + backrest
        } else {
            seat + legs
        }
    }

    /**
     * Creates a fence post shape.
     *
     * @return A VoxelShape representing a fence post.
     */
    fun createFencePost(): VoxelShape {
        return createCuboidShape(6, 0, 6, 10, 16, 10)
    }

    /**
     * Creates a fence connection shape in the specified direction.
     *
     * @param north Whether to include a connection to the north.
     * @param east Whether to include a connection to the east.
     * @param south Whether to include a connection to the south.
     * @param west Whether to include a connection to the west.
     * @return A VoxelShape representing fence connections in the specified directions.
     */
    fun createFenceConnections(
        north: Boolean = false, east: Boolean = false,
        south: Boolean = false, west: Boolean = false
    ): VoxelShape {
        val post = createFencePost()
        var shape = post

        if (north) {
            shape += createCuboidShape(7, 6, 0, 9, 15, 6)
        }
        if (east) {
            shape += createCuboidShape(10, 6, 7, 16, 15, 9)
        }
        if (south) {
            shape += createCuboidShape(7, 6, 10, 9, 15, 16)
        }
        if (west) {
            shape += createCuboidShape(0, 6, 7, 6, 15, 9)
        }

        return shape
    }

    /**
     * Creates a stair shape.
     *
     * @param facing The direction the stairs are facing (0=north, 1=east, 2=south, 3=west).
     * @return A VoxelShape representing stairs facing the specified direction.
     */
    fun createStairs(facing: Int): VoxelShape {
        val bottom = createCuboidShape(0, 0, 0, 16, 8, 16)

        val top = when (facing) {
            0 -> createCuboidShape(0, 8, 0, 16, 16, 8)    // North
            1 -> createCuboidShape(8, 8, 0, 16, 16, 16)   // East
            2 -> createCuboidShape(0, 8, 8, 16, 16, 16)   // South
            3 -> createCuboidShape(0, 8, 0, 8, 16, 16)    // West
            else -> throw IllegalArgumentException("Facing must be between 0 and 3")
        }

        return bottom + top
    }
}
