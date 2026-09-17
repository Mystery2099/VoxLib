package com.github.mystery2099.voxlib

import com.github.mystery2099.voxlib.config.VoxLibConfig
import com.github.mystery2099.voxlib.debug.VoxelShapeDebugClient
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import net.fabricmc.loader.api.FabricLoader

object VoxLibFabric : ModInitializer {
    override fun onInitialize() = VoxLib.onInitialize()
}

@Environment(EnvType.CLIENT)
object VoxLibClient : ClientModInitializer {
    override fun onInitializeClient() {
        VoxLibConfig.initialize(FabricLoader.getInstance().configDir)
        VoxelShapeDebugClient.initialize()
        WorldRenderEvents.BLOCK_OUTLINE.register { context, target ->
            val consumers = context.consumers() ?: return@register true
            val matrices = context.matrixStack() ?: return@register true
            VoxelShapeDebugClient.renderTargetedShapes(
                matrices, consumers, context.world(),
                target.blockPos(), target.blockState(), target.entity(),
                target.cameraX(), target.cameraY(), target.cameraZ()
            )
        }
    }
}
