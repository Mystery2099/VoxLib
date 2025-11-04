package com.github.mystery2099;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@EventBusSubscriber
@Mod("voxlib")
public class Voxlib {
    private static final String MODID = "voxlib";
    private static final Logger logger = LoggerFactory.getLogger(MODID);

    public Voxlib(IEventBus modEventBus, ModContainer modContainer) {}

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        logger.debug("Voxlib initialized!");
    }
}
