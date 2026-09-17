package com.github.mystery2099.voxlib.forge;

import com.github.mystery2099.voxlib.config.VoxLibConfig;
import com.github.mystery2099.voxlib.debug.VoxelShapeDebugClient;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.RenderHighlightEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;

final class VoxLibForgeClient {
    static void initialize() {
        VoxLibConfig.Companion.initialize(FMLPaths.CONFIGDIR.get());
        VoxelShapeDebugClient.INSTANCE.initialize();
        ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
            () -> new ConfigScreenHandler.ConfigScreenFactory(
                (minecraft, parent) -> VoxLibConfig.Companion.createConfigScreen(parent)));
        MinecraftForge.EVENT_BUS.addListener(VoxLibForgeClient::renderBlockHighlight);
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
