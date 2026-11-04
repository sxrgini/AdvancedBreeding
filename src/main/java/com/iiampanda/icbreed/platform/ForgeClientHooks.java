package com.iiampanda.icbreed.platform;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;

@SuppressWarnings({"unchecked", "rawtypes"})
public final class ForgeClientHooks {
    private ForgeClientHooks() {
    }

    public static void init(IEventBus modBus) {
        modBus.addListener(ForgeClientHooks::onAddLayers);
        MinecraftForge.EVENT_BUS.addListener(ForgeClientHooks::onClientTick);
        AttSync.initClient();
        Net.clientBridge = (payload, handler) -> {
            ClientPlayNetworking.PlayPayloadHandler h = (ClientPlayNetworking.PlayPayloadHandler) handler;
            Minecraft mc = Minecraft.getInstance();
            h.receive(payload, new ClientPlayNetworking.Context() {
                @Override
                public Minecraft client() {
                    return mc;
                }

                @Override
                public net.minecraft.client.player.LocalPlayer player() {
                    return mc.player;
                }
            });
        };
    }

    private static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        for (ClientTickEvents.EndTick cb : ClientTickEvents.END_CLIENT_TICK.handlers) cb.onEndTick(mc);
    }

    private static void onAddLayers(EntityRenderersEvent.AddLayers event) {
        for (EntityType<?> type : net.minecraftforge.registries.ForgeRegistries.ENTITY_TYPES) {
            EntityRenderer<?> renderer = event.getRenderer((EntityType) type);
            if (renderer == null) continue;
            for (LivingEntityFeatureRendererRegistrationCallback cb : LivingEntityFeatureRendererRegistrationCallback.EVENT.handlers) {
                cb.registerLayers(type, renderer, layer -> {
                    if (renderer instanceof LivingEntityRenderer living) living.addLayer((RenderLayer) layer);
                }, event.getContext());
            }
        }
    }
}
