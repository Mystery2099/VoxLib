package com.github.mystery2099.voxlib.debug

import com.github.mystery2099.voxlib.config.VoxLibConfig
import com.github.mystery2099.voxlib.optimization.ShapeCache
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import net.minecraft.world.phys.shapes.BooleanOp
import net.minecraft.world.phys.shapes.VoxelShape
import net.minecraft.world.phys.shapes.Shapes
import java.awt.Color
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.core.BlockPos
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState

/**
 * Client-only entry point for VoxLib debug features.
 * Handles client-side initialization and debug state management.
 */
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
        val client = Minecraft.getInstance()
        client.player?.displayClientMessage(
            Component.literal("[VoxLib] Debug mode: ${if (config.debugModeEnabled) "ENABLED" else "DISABLED"}"),
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
    }

    /** Returns whether the loader should render the vanilla block highlight. */
    fun renderTargetedShapes(
        matrices: PoseStack,
        consumers: MultiBufferSource,
        world: Level,
        pos: BlockPos,
        state: BlockState,
        entity: Entity,
        cameraX: Double,
        cameraY: Double,
        cameraZ: Double
    ): Boolean {
        val config = getConfig()
        if (!config.debugModeEnabled || (!config.showTargetedOutline && !config.showTargetedCollision)) {
            return true
        }

        val shapeContext = CollisionContext.of(entity)
        val outlineShape = if (config.showTargetedOutline) {
            state.getShape(world, pos, shapeContext)
        } else {
            null
        }
        val collisionShape = if (config.showTargetedCollision) {
            state.getCollisionShape(world, pos, shapeContext)
        } else {
            null
        }

        val color = Color(config.debugShapeColor and 0xFFFFFF)
        matrices.pushPose()
        matrices.translate(-cameraX, -cameraY, -cameraZ)
        try {
            outlineShape?.let {
                VoxelShapeDebug.renderShape(matrices, consumers, it, pos, color, config.debugShapeAlpha)
            }
            if (collisionShape != null && shapesDiffer(outlineShape, collisionShape)) {
                VoxelShapeDebug.renderShape(matrices, consumers, collisionShape, pos, color, config.debugShapeAlpha)
            }
        } finally {
            matrices.popPose()
        }

        return false
    }

    private fun shapesDiffer(first: VoxelShape?, second: VoxelShape): Boolean =
        first == null || Shapes.joinIsNotEmpty(first, second, BooleanOp.NOT_SAME)
}
