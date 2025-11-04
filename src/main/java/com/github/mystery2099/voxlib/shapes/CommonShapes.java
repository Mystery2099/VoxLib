package com.github.mystery2099.voxlib.shapes;

import com.github.mystery2099.voxlib.combination.VoxelAssembly;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CommonShapes {
    /**
     * Creates a slab shape with the specified height.
     *
     * @param height The height of the slab (1-16).
     * @return A VoxelShape representing a slab with the given height.
     */
    public static VoxelShape createBottomSlab(int height) {
        assert height > 0 && height < 17; //height in [0,17]
        return Shapes.box(0, 0, 0, 16, height, 16);
    }

    /**
     * Creates a top slab shape with the specified height.
     *
     * @param height The height of the slab (1-16).
     * @return A VoxelShape representing a top slab with the given height.
     */
    public static VoxelShape createTopSlab(int height) {
        assert height > 0 && height < 17;
        return Shapes.box(0, 16-height, 0, 16, 16, 16);
    }

    /**
     * Creates a pillar shape with the specified width.
     *
     * @param width The width of the pillar (1-14).
     * @param centered Whether the pillar should be centered in the block.
     * @return A VoxelShape representing a pillar with the given width.
     */
    public static VoxelShape createPillar(int width, boolean centered) {
        assert width > 0 && width < 15;
        if(centered) {
            double offset = (16 - width) / 2.0;
            return Shapes.box(offset, 0, offset, offset + width, 16, offset + width);
        } else return Shapes.box(0, 0, 0, width, 16, width);
    }

    public static VoxelShape createPillar(int width) { return createPillar(width, true); }

    /**
     * Creates a fence post shape.
     *
     * @return A VoxelShape representing a fence post.
     */
    public static VoxelShape createFencePost() {
        return Shapes.box(6, 0, 6, 10, 16, 10);
    }

    /**
     * Creates a stair shape.
     *
     * @param facing The direction the stairs are facing (0=north, 1=east, 2=south, 3=west).
     * @return A VoxelShape representing stairs facing the specified direction.
     */
    public static VoxelShape createStairs(Direction facing) throws Exception {
        VoxelShape bottom = Shapes.box(0, 0, 0, 16, 8, 16);
        VoxelShape top = switch(facing) {
            case NORTH -> Shapes.box(0, 8, 0, 16, 16, 8);
            case SOUTH -> Shapes.box(0, 8, 8, 16, 16, 16);
            case WEST -> Shapes.box(0, 8, 0, 8, 16, 16);
            case EAST -> Shapes.box(8, 8, 0, 16, 16, 16);
            default -> throw new Exception();
        };
        return VoxelAssembly.and(bottom, top);
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
    public static VoxelShape createFenceConnections(boolean north, boolean south, boolean east, boolean west) {
        VoxelShape ret = createFencePost();
        if(north) ret = VoxelAssembly.and(ret, Shapes.box(7, 6, 0, 9, 15, 6));
        if(south) ret = VoxelAssembly.and(ret, Shapes.box(7, 6, 10, 9, 15, 16));
        if(east) ret = VoxelAssembly.and(ret, Shapes.box(10, 6, 7, 16, 15, 9));
        if(west) ret = VoxelAssembly.and(ret, Shapes.box(0, 6, 7, 6, 15, 9));
        return ret;
    }

    /**
     * Creates a table shape with the specified leg width and top thickness.
     *
     * @param legWidth The width of the table legs (1-6).
     * @param topThickness The thickness of the table top (1-6).
     * @return A VoxelShape representing a table.
     */
    public static VoxelShape createTable(int legWidth, int topThickness) {
        assert legWidth > 0 && legWidth < 7;
        assert topThickness > 0 && topThickness < 7;
        VoxelShape tableTop = Shapes.box(0, 16-topThickness, 0, 16, 16, 16);
        int legOs = 16 - legWidth;
        VoxelShape leg1 = Shapes.box(0, 0, 0, legWidth, 16-topThickness, legWidth);
        VoxelShape leg2 = Shapes.box(legOs, 0, 0, 16, 16-topThickness, 16);
        VoxelShape leg3 = Shapes.box(0, 0, legOs, legWidth, 16-topThickness, 16);
        VoxelShape leg4 = Shapes.box(legOs, 0, legOs, 16, 16-topThickness, 16);
        return VoxelAssembly.union(tableTop, leg1, leg2, leg3, leg4);
    }

    /**
     * Creates a chair shape with the specified seat height and optional backrest.
     *
     * @param seatHeight The height of the chair seat (1-12).
     * @param hasBackrest Whether the chair should have a backrest.
     * @return A VoxelShape representing a chair.
     */

    public static VoxelShape createChair(int seatHeight, boolean hasBackrest) {
        assert seatHeight > 0 && seatHeight < 13;
        VoxelShape seat = Shapes.box(1, seatHeight, 1, 15, seatHeight+2, 15);
        VoxelShape legs = Shapes.box(2, 0, 2, 14, seatHeight, 14);
        VoxelShape backrest = Shapes.box(2, seatHeight+2, 12, 14, seatHeight+10, 15);
        VoxelShape chair = VoxelAssembly.and(seat, legs);
        if(hasBackrest) chair = VoxelAssembly.and(chair, backrest);
        return chair;
    }
}