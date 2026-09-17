package com.github.mystery2099.voxlib.debug

import com.github.mystery2099.voxlib.config.VoxLibConfig
import com.github.mystery2099.voxlib.optimization.ShapeCache
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import net.minecraft.block.ShapeContext
import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text
import net.minecraft.util.function.BooleanBiFunction
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import java.awt.Color

/**
 * Client-only entry point for VoxLib debug features.
 * Handles client-side initialization and debug state management.
 */
@Environment(EnvType.CLIENT)
object VoxelShapeDebugClient {

    /**
     * Gets the current debug configuration.
     */
    fun getConfig(): VoxLibConfig = VoxLibConfig.get()

    /**
     * Updates the debug configuration.
     */
    fun updateConfig(newConfig: VoxLibConfig) {
        VoxLibConfig.update(newConfig)
    }

    /**
     * Toggles debug mode on or off.
     *
     * @return The new debug mode state (true if enabled, false if disabled)
     */
    fun toggleDebugMode(): Boolean {
        val config = getConfig().copy(debugModeEnabled = !isDebugModeEnabled())
        updateConfig(config)
        val client = MinecraftClient.getInstance()
        client.player?.sendMessage(
            Text.literal("[VoxLib] Debug mode: ${if (config.debugModeEnabled) "ENABLED" else "DISABLED"}"),
            false
        )
        return config.debugModeEnabled
    }

    /**
     * Checks if debug mode is currently enabled.
     */
    fun isDebugModeEnabled(): Boolean = getConfig().debugModeEnabled

    /**
     * Gets the current debug color.
     */
    fun getDebugColor(): java.awt.Color {
        val rgb = getConfig().debugShapeColor and 0xFFFFFF
        return java.awt.Color(rgb)
    }

    /**
     * Gets the current alpha value.
     */
    fun getAlpha(): Float = getConfig().debugShapeAlpha

    /**
     * Gets cache statistics as a formatted string.
     */
    fun getCacheStats(): String = ShapeCache.stats()

    /**
     * Initializes client-side debug features.
     */
    fun initialize() {
        VoxLibConfig.getOrCreate()
        WorldRenderEvents.BLOCK_OUTLINE.register { context, target ->
            renderTargetedShapes(context, target)
        }
    }

    private fun renderTargetedShapes(
        context: WorldRenderContext,
        target: WorldRenderContext.BlockOutlineContext
    ): Boolean {
        val config = getConfig()
        if (!config.debugModeEnabled || (!config.showTargetedOutline && !config.showTargetedCollision)) {
            return true
        }

        val consumers = context.consumers() ?: return true
        val pos = target.blockPos()
        val state = target.blockState()
        val shapeContext = ShapeContext.of(target.entity())
        val outlineShape = if (config.showTargetedOutline) {
            state.getOutlineShape(context.world(), pos, shapeContext)
        } else {
            null
        }
        val collisionShape = if (config.showTargetedCollision) {
            state.getCollisionShape(context.world(), pos, shapeContext)
        } else {
            null
        }

        val matrices = context.matrixStack()
        val color = Color(config.debugShapeColor and 0xFFFFFF)
        matrices.push()
        matrices.translate(-target.cameraX(), -target.cameraY(), -target.cameraZ())
        try {
            outlineShape?.let {
                VoxelShapeDebug.renderShape(matrices, consumers, it, pos, color, config.debugShapeAlpha)
            }
            if (collisionShape != null && shapesDiffer(outlineShape, collisionShape)) {
                VoxelShapeDebug.renderShape(matrices, consumers, collisionShape, pos, color, config.debugShapeAlpha)
            }
        } finally {
            matrices.pop()
        }

        return false
    }

    private fun shapesDiffer(first: VoxelShape?, second: VoxelShape): Boolean =
        first == null || VoxelShapes.matchesAnywhere(first, second, BooleanBiFunction.NOT_SAME)
}
