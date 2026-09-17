package com.github.mystery2099.voxlib.debug

import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.LevelRenderer
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.shapes.VoxelShape
import java.awt.Color

/**
 * A utility object for debugging VoxelShapes by rendering them in the world.
 * This class provides utilities for visualizing VoxelShapes in the game world,
 * logging shape information, and comparing shapes for debugging purposes.
 * Rendering methods are client-only and require a rendering context.
 */
object VoxelShapeDebug {

    /**
     * Renders a VoxelShape at the specified position with the given color.
     * This should be called from a render method.
     *
     * @param matrices The PoseStack to use for rendering.
     * @param vertexConsumers The MultiBufferSource to use for rendering.
     * @param shape The VoxelShape to render.
     * @param pos The position at which to render the shape.
     * @param color The color to use for rendering (default is red).
     * @param alpha The alpha value for transparency (0.0-1.0, default is 0.4).
     * @param lineWidth Unused; retained for compatibility. RenderType.lines controls line width.
     */
    fun renderShape(
        matrices: PoseStack,
        vertexConsumers: MultiBufferSource,
        shape: VoxelShape,
        pos: BlockPos,
        color: Color = Color.RED,
        alpha: Float = 0.4f,
        lineWidth: Float = 2.0f
    ) {
        val vertexConsumer = vertexConsumers.getBuffer(RenderType.lines())
        val offsetX = pos.x.toDouble()
        val offsetY = pos.y.toDouble()
        val offsetZ = pos.z.toDouble()

        val red = color.red / 255.0f
        val green = color.green / 255.0f
        val blue = color.blue / 255.0f

        // Preserve box decomposition and a uniform color without allocating AABBs.
        shape.forAllBoxes { minX, minY, minZ, maxX, maxY, maxZ ->
            LevelRenderer.renderLineBox(
                matrices, vertexConsumer,
                minX + offsetX, minY + offsetY, minZ + offsetZ,
                maxX + offsetX, maxY + offsetY, maxZ + offsetZ,
                red, green, blue, alpha
            )
        }
    }

    /**
     * Logs information about a VoxelShape to the console.
     * This can be useful for debugging shapes without rendering them.
     *
     * @param shape The VoxelShape to log information about.
     * @param name An optional name to identify the shape in the log.
     */
    fun logShapeInfo(shape: VoxelShape, name: String = "VoxelShape") {
        // Count boxes by iterating through them
        var boxCount = 0
        shape.forAllBoxes { _, _, _, _, _, _ -> boxCount++ }
        println("$name contains $boxCount boxes:")

        shape.forAllBoxes { minX, minY, minZ, maxX, maxY, maxZ ->
            println("  AABB: ($minX, $minY, $minZ) to ($maxX, $maxY, $maxZ)")
        }
    }

    /**
     * Compares two VoxelShapes and logs the differences.
     * This is useful for debugging transformations.
     *
     * @param shape1 The first VoxelShape to compare.
     * @param shape2 The second VoxelShape to compare.
     * @param name1 A name to identify the first shape.
     * @param name2 A name to identify the second shape.
     */
    fun compareShapes(
        shape1: VoxelShape,
        shape2: VoxelShape,
        name1: String = "Shape 1",
        name2: String = "Shape 2"
    ) {
        // Count boxes by iterating through them
        var boxCount1 = 0
        var boxCount2 = 0
        shape1.forAllBoxes { _, _, _, _, _, _ -> boxCount1++ }
        shape2.forAllBoxes { _, _, _, _, _, _ -> boxCount2++ }

        println("Comparing $name1 ($boxCount1 boxes) with $name2 ($boxCount2 boxes):")

        if (shape1 == shape2) {
            println("  The shapes are identical.")
            return
        }

        println("  $name1 boxes:")
        shape1.forAllBoxes { minX, minY, minZ, maxX, maxY, maxZ ->
            println("    AABB: ($minX, $minY, $minZ) to ($maxX, $maxY, $maxZ)")
        }

        println("  $name2 boxes:")
        shape2.forAllBoxes { minX, minY, minZ, maxX, maxY, maxZ ->
            println("    AABB: ($minX, $minY, $minZ) to ($maxX, $maxY, $maxZ)")
        }
    }

    /**
     * Renders a VoxelShape using current debug settings from client config.
     *
     * @param matrices The PoseStack to use for rendering.
     * @param vertexConsumers The MultiBufferSource to use for rendering.
     * @param shape The VoxelShape to render.
     * @param pos The position at which to render the shape.
     */
    fun renderShapeWithConfig(
        matrices: PoseStack,
        vertexConsumers: MultiBufferSource,
        shape: VoxelShape,
        pos: BlockPos
    ) {
        if (!VoxelShapeDebugClient.isDebugModeEnabled()) return

        renderShape(
            matrices = matrices,
            vertexConsumers = vertexConsumers,
            shape = shape,
            pos = pos,
            color = VoxelShapeDebugClient.getDebugColor(),
            alpha = VoxelShapeDebugClient.getAlpha()
        )
    }
}
