package com.github.mystery2099.voxlib.neoforge;

import com.github.mystery2099.voxlib.config.VoxLibConfig;
import com.github.mystery2099.voxlib.debug.VoxelShapeDebugClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;

final class VoxLibNeoForgeClient {
    static void initialize(ModContainer modContainer) {
        VoxLibConfig.Companion.initialize(FMLPaths.CONFIGDIR.get());
        VoxelShapeDebugClient.INSTANCE.initialize();
        modContainer.registerExtensionPoint(IConfigScreenFactory.class,
            new IConfigScreenFactory() {
                @Override
                public Screen createScreen(Minecraft minecraft, Screen parent) {
                    return VoxLibConfig.Companion.createConfigScreen(parent);
                }
            });
        NeoForge.EVENT_BUS.addListener(VoxLibNeoForgeClient::renderBlockHighlight);
    }

    private static void renderBlockHighlight(RenderHighlightEvent.Block event) {
        var world = Minecraft.getInstance().level;
        if (world == null) return;
        var pos = event.getTarget().getBlockPos();
        var camera = event.getCamera();
        var cameraPos = camera.getPosition();
        boolean renderVanilla = VoxelShapeDebugClient.INSTANCE.renderTargetedShapes(
            event.getPoseStack(), event.getMultiBufferSource(), world, pos, world.getBlockState(pos),
            camera.getEntity(), cameraPos.x, cameraPos.y, cameraPos.z);
        if (!renderVanilla) event.setCanceled(true);
    }
}
