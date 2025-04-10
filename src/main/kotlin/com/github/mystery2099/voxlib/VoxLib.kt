package com.github.mystery2099.voxlib

import net.fabricmc.api.ModInitializer
import org.slf4j.LoggerFactory

/**
 * Main entry point for the VoxLib mod.
 * This library provides utilities for manipulating, creating, and rotating voxel shapes.
 */
object VoxLib : ModInitializer {
    private val logger = LoggerFactory.getLogger("voxlib")

    /**
     * The current version of VoxLib.
     */
    private const val VERSION = "1.2.0"

    /**
     * The Minecraft version this mod is built for.
     */
    private const val MINECRAFT_VERSION = "1.19.4"

	override fun onInitialize() {
        logger.info("Initializing VoxLib v$VERSION for Minecraft $MINECRAFT_VERSION")
        logger.info("VoxLib is ready to help with your voxel shape needs!")
	}
}