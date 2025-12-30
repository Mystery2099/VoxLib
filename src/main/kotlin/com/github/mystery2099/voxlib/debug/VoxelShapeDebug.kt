package com.github.mystery2099.voxlib.debug

import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.WorldRenderer
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.shape.VoxelShape
import java.awt.Color

/**
 * A utility object for debugging VoxelShapes by rendering them in the world.
 */
object VoxelShapeDebug {

    /**
     * Renders a VoxelShape at the specified position with the given color.
     * This should be called from a render method.
     *
     * @param matrices The MatrixStack to use for rendering.
     * @param vertexConsumers The VertexConsumerProvider to use for rendering.
     * @param shape The VoxelShape to render.
     * @param pos The position at which to render the shape.
     * @param color The color to use for rendering (default is red).
     * @param alpha The alpha value for transparency (0.0-1.0, default is 0.4).
     * @param lineWidth The width of the lines (default is 2.0).
     */
    @Environment(EnvType.CLIENT)
    fun renderShape(
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        shape: VoxelShape,
        pos: BlockPos,
        color: Color = Color.RED,
        alpha: Float = 0.4f,
        lineWidth: Float = 2.0f
    ) {
        val vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getLines())
        val offsetX = pos.x.toDouble()
        val offsetY = pos.y.toDouble()
        val offsetZ = pos.z.toDouble()

        // Draw each box in the shape
        shape.forEachBox { minX, minY, minZ, maxX, maxY, maxZ ->
            val box = Box(
                minX + offsetX, minY + offsetY, minZ + offsetZ,
                maxX + offsetX, maxY + offsetY, maxZ + offsetZ
            )
            drawBox(matrices, vertexConsumer, box, color, alpha, lineWidth)
        }
    }

    /**
     * Draws a box with the specified color and alpha.
     *
     * @param matrices The MatrixStack to use for rendering.
     * @param vertexConsumer The VertexConsumer to use for rendering.
     * @param box The Box to render.
     * @param color The color to use for rendering.
     * @param alpha The alpha value for transparency.
     * @param lineWidth The width of the lines (unused, kept for API compatibility).
     */
    private fun drawBox(
        matrices: MatrixStack,
        vertexConsumer: VertexConsumer,
        box: Box,
        color: Color,
        alpha: Float,
        lineWidth: Float
    ) {
        val red = color.red / 255.0f
        val green = color.green / 255.0f
        val blue = color.blue / 255.0f

        WorldRenderer.drawBox(
            matrices,
            vertexConsumer,
            box.minX, box.minY, box.minZ,
            box.maxX, box.maxY, box.maxZ,
            red, green, blue, alpha
        )
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
        shape.forEachBox { _, _, _, _, _, _ -> boxCount++ }
        println("$name contains $boxCount boxes:")

        shape.forEachBox { minX, minY, minZ, maxX, maxY, maxZ ->
            println("  Box: ($minX, $minY, $minZ) to ($maxX, $maxY, $maxZ)")
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
        shape1.forEachBox { _, _, _, _, _, _ -> boxCount1++ }
        shape2.forEachBox { _, _, _, _, _, _ -> boxCount2++ }

        println("Comparing $name1 ($boxCount1 boxes) with $name2 ($boxCount2 boxes):")

        if (shape1 == shape2) {
            println("  The shapes are identical.")
            return
        }

        println("  $name1 boxes:")
        shape1.forEachBox { minX, minY, minZ, maxX, maxY, maxZ ->
            println("    Box: ($minX, $minY, $minZ) to ($maxX, $maxY, $maxZ)")
        }

        println("  $name2 boxes:")
        shape2.forEachBox { minX, minY, minZ, maxX, maxY, maxZ ->
            println("    Box: ($minX, $minY, $minZ) to ($maxX, $maxY, $maxZ)")
        }
    }
}
