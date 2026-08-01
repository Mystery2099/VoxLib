package com.github.mystery2099.voxlib.shapes

import com.github.mystery2099.voxlib.combination.VoxelAssembly.createCuboidShape
import com.github.mystery2099.voxlib.combination.VoxelAssembly.plus
import net.minecraft.util.math.Direction
import net.minecraft.util.shape.VoxelShape
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.atomic.AtomicReferenceArray

/**
 * Pre-defined common shapes for Minecraft modding.
 *
 * Finite parameter spaces are memoized in fixed [AtomicReferenceArray] slots.
 * Index helpers below document how each parameter packs into those slots.
 */
object CommonShapes {
    // Slot capacities must match the index helpers (ranges × variants).
    private const val SLAB_SLOTS = 16 // height 1..16
    private const val PILLAR_WIDTH_SLOTS = 14 // width 1..14
    private const val PILLAR_SLOTS = PILLAR_WIDTH_SLOTS * 2 // centered + corner
    private const val TABLE_LEG_VARIANTS = 6 // 1..6
    private const val TABLE_TOP_VARIANTS = 6 // 1..6
    private const val TABLE_SLOTS = TABLE_LEG_VARIANTS * TABLE_TOP_VARIANTS
    private const val CHAIR_SEAT_VARIANTS = 12 // 1..12
    private const val CHAIR_BACKREST_VARIANTS = 16 // 1..16
    private const val CHAIR_WITH_BACKREST_SLOTS = CHAIR_SEAT_VARIANTS * CHAIR_BACKREST_VARIANTS
    private const val FENCE_CONNECTION_SLOTS = 16 // 4 direction bits
    private const val STAIR_SLOTS = 4 // N/E/S/W

    private val slabs = AtomicReferenceArray<VoxelShape>(SLAB_SLOTS)
    private val topSlabs = AtomicReferenceArray<VoxelShape>(SLAB_SLOTS)
    private val pillars = AtomicReferenceArray<VoxelShape>(PILLAR_SLOTS)
    private val tables = AtomicReferenceArray<VoxelShape>(TABLE_SLOTS)
    private val chairsWithoutBackrests = AtomicReferenceArray<VoxelShape>(CHAIR_SEAT_VARIANTS)
    private val chairsWithBackrests = AtomicReferenceArray<VoxelShape>(CHAIR_WITH_BACKREST_SLOTS)
    private val fenceConnections = AtomicReferenceArray<VoxelShape>(FENCE_CONNECTION_SLOTS)
    private val stairs = AtomicReferenceArray<VoxelShape>(STAIR_SLOTS)
    private val fencePost = AtomicReference<VoxelShape>()

    /**
     * Creates a slab shape with the specified height.
     *
     * @param height The height of the slab (1-16).
     * @return A VoxelShape representing a slab with the given height.
     */
    fun createSlab(height: Int): VoxelShape {
        require(height in 1..16) { "Height must be between 1 and 16" }
        return slabs.getOrCreate(slabIndex(height)) {
            createCuboidShape(0, 0, 0, 16, height, 16)
        }
    }

    /**
     * Creates a top slab shape with the specified height.
     *
     * @param height The height of the slab (1-16).
     * @return A VoxelShape representing a top slab with the given height.
     */
    fun createTopSlab(height: Int): VoxelShape {
        require(height in 1..16) { "Height must be between 1 and 16" }
        return topSlabs.getOrCreate(slabIndex(height)) {
            val slabHeight = 16 - height
            createCuboidShape(0, slabHeight, 0, 16, 16, 16)
        }
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

        return pillars.getOrCreate(pillarIndex(width, centered)) {
            if (centered) {
                val offset = (16 - width) / 2
                createCuboidShape(offset, 0, offset, offset + width, 16, offset + width)
            } else {
                createCuboidShape(0, 0, 0, width, 16, width)
            }
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

        return tables.getOrCreate(tableIndex(legWidth, topThickness)) {
            val tableTop = createCuboidShape(0, 16 - topThickness, 0, 16, 16, 16)
            val legOffset = 16 - legWidth
            val leg1 = createCuboidShape(0, 0, 0, legWidth, 16 - topThickness, legWidth)
            val leg2 = createCuboidShape(legOffset, 0, 0, 16, 16 - topThickness, legWidth)
            val leg3 = createCuboidShape(0, 0, legOffset, legWidth, 16 - topThickness, 16)
            val leg4 = createCuboidShape(legOffset, 0, legOffset, 16, 16 - topThickness, 16)

            tableTop + leg1 + leg2 + leg3 + leg4
        }
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

        val cache = if (hasBackrest) chairsWithBackrests else chairsWithoutBackrests
        val index = if (hasBackrest) {
            chairWithBackrestIndex(seatHeight, backrestHeight)
        } else {
            chairSeatIndex(seatHeight)
        }
        return cache.getOrCreate(index) {
            val seat = createCuboidShape(1, seatHeight, 1, 15, seatHeight + 2, 15)
            val legs = createCuboidShape(2, 0, 2, 14, seatHeight, 14)

            if (hasBackrest) {
                val backrest = createCuboidShape(
                    2, seatHeight + 2, 12,
                    14, seatHeight + 2 + backrestHeight, 15
                )
                seat + legs + backrest
            } else {
                seat + legs
            }
        }
    }

    /**
     * Creates a fence post shape.
     *
     * @return A VoxelShape representing a fence post.
     */
    fun createFencePost(): VoxelShape {
        return fencePost.getOrCreate {
            createCuboidShape(6, 0, 6, 10, 16, 10)
        }
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
        return fenceConnections.getOrCreate(fenceConnectionIndex(north, east, south, west)) {
            var shape = createFencePost()
            if (north) shape += createCuboidShape(7, 6, 0, 9, 15, 6)
            if (east) shape += createCuboidShape(10, 6, 7, 16, 15, 9)
            if (south) shape += createCuboidShape(7, 6, 10, 9, 15, 16)
            if (west) shape += createCuboidShape(0, 6, 7, 6, 15, 9)
            shape
        }
    }

    /**
     * Creates a stair shape.
     *
     * @param facing The direction the stairs are facing.
     * @return A VoxelShape representing stairs facing the specified direction.
     * @throws IllegalArgumentException if facing is UP or DOWN (only horizontal directions valid).
     */
    fun createStairs(facing: Direction): VoxelShape {
        require(facing.axis.isHorizontal) {
            "Stairs must face NORTH, EAST, SOUTH, or WEST"
        }

        return stairs.getOrCreate(stairIndex(facing)) {
            val bottom = createCuboidShape(0, 0, 0, 16, 8, 16)
            val top = when (facing) {
                Direction.NORTH -> createCuboidShape(0, 8, 0, 16, 16, 8)
                Direction.EAST -> createCuboidShape(8, 8, 0, 16, 16, 16)
                Direction.SOUTH -> createCuboidShape(0, 8, 8, 16, 16, 16)
                Direction.WEST -> createCuboidShape(0, 8, 0, 8, 16, 16)
                else -> error("Validated horizontal direction became invalid")
            }

            bottom + top
        }
    }

    private fun slabIndex(height: Int): Int = height - 1

    /** Centered widths occupy `[0, 13]`; corner widths occupy `[14, 27]`. */
    private fun pillarIndex(width: Int, centered: Boolean): Int =
        (if (centered) 0 else PILLAR_WIDTH_SLOTS) + width - 1

    private fun tableIndex(legWidth: Int, topThickness: Int): Int =
        (legWidth - 1) * TABLE_TOP_VARIANTS + topThickness - 1

    private fun chairSeatIndex(seatHeight: Int): Int = seatHeight - 1

    private fun chairWithBackrestIndex(seatHeight: Int, backrestHeight: Int): Int =
        (seatHeight - 1) * CHAIR_BACKREST_VARIANTS + backrestHeight - 1

    private fun fenceConnectionIndex(
        north: Boolean,
        east: Boolean,
        south: Boolean,
        west: Boolean
    ): Int =
        (if (north) 1 else 0) or
            (if (east) 2 else 0) or
            (if (south) 4 else 0) or
            (if (west) 8 else 0)

    private fun stairIndex(facing: Direction): Int = when (facing) {
        Direction.NORTH -> 0
        Direction.EAST -> 1
        Direction.SOUTH -> 2
        Direction.WEST -> 3
        else -> error("Validated horizontal direction became invalid")
    }

    private inline fun AtomicReferenceArray<VoxelShape>.getOrCreate(
        index: Int,
        createShape: () -> VoxelShape
    ): VoxelShape {
        get(index)?.let { return it }
        val created = createShape()
        return if (compareAndSet(index, null, created)) created else requireNotNull(get(index))
    }

    private inline fun <T : Any> AtomicReference<T>.getOrCreate(createValue: () -> T): T {
        get()?.let { return it }
        val created = createValue()
        return if (compareAndSet(null, created)) created else requireNotNull(get())
    }
}
