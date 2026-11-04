package com.iiampanda.icbreed.platform;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.common.NeoForge;

@SuppressWarnings({"unchecked", "rawtypes"})
public final class NeoClientHooks {
    private NeoClientHooks() {
    }

    public static void init(net.neoforged.bus.api.IEventBus modBus) {
        modBus.addListener(NeoClientHooks::onAddLayers);
        NeoForge.EVENT_BUS.addListener(NeoClientHooks::onClientTick);
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

    private static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        for (ClientTickEvents.EndTick cb : ClientTickEvents.END_CLIENT_TICK.handlers) cb.onEndTick(mc);
    }

    private static void onAddLayers(EntityRenderersEvent.AddLayers event) {
        for (EntityType<?> type : event.getEntityTypes()) {
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
