package com.github.mystery2099.voxlib

import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.fml.common.Mod
import org.slf4j.LoggerFactory

/**
 * Main entry point for the VoxLib mod.
 * This library provides utilities for manipulating, creating, and rotating voxel shapes.
 */
@Mod("voxlib")
@EventBusSubscriber
object VoxLib {
    private val logger = LoggerFactory.getLogger("voxlib")
    private const val VERSION = "1.2.0-neo"
    private const val MINECRAFT_VERSION = "1.21.1"

    @SubscribeEvent
	@JvmStatic fun log() {
        logger.info("Initializing VoxLib for $MINECRAFT_VERSION")
    }
}