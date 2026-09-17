package com.github.mystery2099.voxlib.forge;

import com.github.mystery2099.voxlib.VoxLib;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;

@Mod("voxlib")
public final class VoxLibForge {
    public VoxLibForge() {
        VoxLib.INSTANCE.onInitialize();
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> VoxLibForgeClient::initialize);
    }
}
