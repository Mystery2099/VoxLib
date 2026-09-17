package com.github.mystery2099.voxlib

import org.slf4j.LoggerFactory

/**
 * Main entry point for the VoxLib mod.
 * This library provides utilities for manipulating, creating, and rotating voxel shapes.
 */
object VoxLib {
    private val logger = LoggerFactory.getLogger("voxlib")

    /**
     * The current version of VoxLib.
     */
    const val VERSION = "1.7.0+1.20.6"

    /**
     * The mod identifier used by both loaders.
     */
    const val MOD_ID = "voxlib"

    /**
     * The Minecraft version this mod is built for.
     */
    private const val MINECRAFT_VERSION = "1.20.6"

	fun onInitialize() {
        logger.info("Initializing VoxLib v$VERSION for Minecraft $MINECRAFT_VERSION")
        logger.info("VoxLib is ready to help with your voxel shape needs!")
	}
}

