package com.iiampanda.icbreed;

import com.iiampanda.icbreed.client.IcBreedClient;
import com.iiampanda.icbreed.platform.ForgeClientHooks;
import net.minecraftforge.eventbus.api.IEventBus;

public final class IcBreedForgeClient {
    private IcBreedForgeClient() {
    }

    public static void init(IEventBus modBus) {
        ForgeClientHooks.init(modBus);
        new IcBreedClient().onInitializeClient();
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener((net.minecraftforge.client.event.RenderLivingEvent.Pre<?, ?> e) -> {
            var mc = net.minecraft.client.Minecraft.getInstance();
            if (e.getEntity() instanceof net.minecraft.world.entity.animal.axolotl.Axolotl a && a.getVehicle() == mc.player
                    && mc.options.getCameraType().isFirstPerson()) e.setCanceled(true);
        });
    }
}
