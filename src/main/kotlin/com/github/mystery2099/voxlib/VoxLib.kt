package com.github.mystery2099.voxlib

import net.fabricmc.api.ModInitializer
import org.slf4j.LoggerFactory

object VoxLib : ModInitializer {
    private val logger = LoggerFactory.getLogger("voxlib")

	override fun onInitialize() {
		logger.info("Hello Fabric world, this is VoxLib!")
	}
}