package com.github.mystery2099.voxelshapeutils

import net.fabricmc.api.ModInitializer
import net.minecraft.block.Block
import net.minecraft.util.shape.VoxelShape
import org.slf4j.LoggerFactory

object VoxelShapeUtils : ModInitializer {
    private val logger = LoggerFactory.getLogger("voxelshapeutils")

	override fun onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		logger.info("Hello Fabric world, this is VoxelShapeUtils!")
	}
}