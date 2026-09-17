package com.github.mystery2099.voxlib.neoforge;

import com.github.mystery2099.voxlib.VoxLib;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;

@Mod(VoxLib.MOD_ID)
public final class VoxLibNeoForge {
    public VoxLibNeoForge(IEventBus modEventBus, ModContainer modContainer) {
        VoxLib.INSTANCE.onInitialize();
        if (FMLEnvironment.dist.isClient()) {
            VoxLibNeoForgeClient.initialize(modContainer);
        }
    }
}
