package com.github.mystery2099.voxlib.neoforge

import com.github.mystery2099.voxlib.config.VoxLibConfig
import com.github.mystery2099.voxlib.debug.VoxelShapeDebugClient
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.Screen
import net.neoforged.fml.ModContainer
import net.neoforged.fml.loading.FMLPaths
import net.neoforged.neoforge.client.event.RenderHighlightEvent
import net.neoforged.neoforge.client.gui.IConfigScreenFactory
import net.neoforged.neoforge.common.NeoForge

internal object VoxLibNeoForgeClient {
    fun initialize(modContainer: ModContainer) {
        VoxLibConfig.initialize(FMLPaths.CONFIGDIR.get())
        VoxelShapeDebugClient.initialize()
        modContainer.registerExtensionPoint(
            IConfigScreenFactory::class.java,
            IConfigScreenFactory { _: Minecraft, parent: Screen ->
                VoxLibConfig.createConfigScreen(parent)
            }
        )
        NeoForge.EVENT_BUS.addListener { event: RenderHighlightEvent.Block ->
            renderBlockHighlight(event)
        }
    }

    private fun renderBlockHighlight(event: RenderHighlightEvent.Block) {
        val world = Minecraft.getInstance().level ?: return
        val pos = event.target.blockPos
        val camera = event.camera
        val cameraPos = camera.position
        val renderVanilla = VoxelShapeDebugClient.renderTargetedShapes(
            event.poseStack, event.multiBufferSource, world, pos, world.getBlockState(pos),
            camera.entity, cameraPos.x, cameraPos.y, cameraPos.z
        )
        if (!renderVanilla) event.isCanceled = true
    }
}
