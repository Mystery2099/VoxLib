package com.github.mystery2099.voxlib.neoforge

import com.github.mystery2099.voxlib.VoxLib
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.ModContainer
import net.neoforged.fml.common.Mod
import net.neoforged.fml.loading.FMLEnvironment

@Mod(VoxLib.MOD_ID)
class VoxLibNeoForge(modEventBus: IEventBus, modContainer: ModContainer) {
    init {
        VoxLib.onInitialize()
        if (FMLEnvironment.dist.isClient) {
            VoxLibNeoForgeClient.initialize(modContainer)
        }
    }
}
