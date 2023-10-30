package com.github.mystery2099.voxlib

import net.fabricmc.api.ModInitializer
import org.slf4j.LoggerFactory

object VoxLib : ModInitializer {
    private val LOGGER = LoggerFactory.getLogger("voxlib")

	override fun onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		LOGGER.info("Hello Fabric world, this is VoxLib!")
	}
}