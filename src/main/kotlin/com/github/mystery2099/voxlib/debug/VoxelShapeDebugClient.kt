package com.github.mystery2099.voxlib.debug

import com.github.mystery2099.voxlib.config.VoxLibConfig
import com.github.mystery2099.voxlib.optimization.ShapeCache
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text
import kotlin.math.roundToInt

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
        val config = getConfig()
        val rgb = config.debugShapeColor and 0xFFFFFF
        val alpha = (config.debugShapeColor shr 24) and 0xFF
        val scaledAlpha = (alpha * config.debugShapeAlpha).roundToInt().coerceIn(0, 255)
        return java.awt.Color(
            (rgb shr 16) and 0xFF,
            (rgb shr 8) and 0xFF,
            rgb and 0xFF,
            scaledAlpha
        )
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
}
