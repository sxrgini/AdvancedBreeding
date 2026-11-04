package com.iiampanda.icbreed.client;

import com.iiampanda.icbreed.net.OpenGeneticsPayload;
import com.iiampanda.icbreed.platform.ClientPlayNetworking;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class IcBreedClient {
    public void onInitializeClient() {
        com.iiampanda.icbreed.platform.LivingEntityRenderLayerRegistrationCallback.EVENT.register(
                (type, renderer, helper, context) -> {
                    if (renderer instanceof net.minecraft.client.renderer.entity.HorseRenderer horseRenderer) {
                        registerBags(helper, new BagsLayer(horseRenderer, context.getItemModelResolver()));
                    }
                });
        com.iiampanda.icbreed.platform.ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            var p = mc.player;
            if (p == null || !com.iiampanda.icbreed.platform.Att.has(p, com.iiampanda.icbreed.genetics.Genetics.CLING)) return;
            if (p.isShiftKeyDown() && p.horizontalCollision && !p.onGround() && !p.getAbilities().flying) {
                var dm = p.getDeltaMovement();
                if (dm.y < -0.06) p.setDeltaMovement(dm.x, -0.06, dm.z);
                p.resetFallDistance();
            }
        });
        final boolean[] wasUse = {false};
        com.iiampanda.icbreed.platform.ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            var p = mc.player;
            boolean down = mc.options.keyUse.isDown();
            if (p != null && down && !wasUse[0] && mc.gui.screen() == null && p.getXRot() < -50.0F && p.getMainHandItem().isEmpty()
                    && (p.getPassengers().stream().anyMatch(e -> e instanceof net.minecraft.world.entity.animal.axolotl.Axolotl)
                    || (com.iiampanda.icbreed.platform.Att.getOrElse(p, com.iiampanda.icbreed.genetics.Genetics.PERKS, 0) & 1) != 0)) {
                ClientPlayNetworking.send(new com.iiampanda.icbreed.net.RemoveHatPayload());
            }
            if (p != null && down && !wasUse[0] && mc.gui.screen() == null && p.getMainHandItem().isEmpty()
                    && (p.getVehicle() instanceof net.minecraft.world.entity.monster.hoglin.Hoglin
                    || p.getVehicle() instanceof net.minecraft.world.entity.animal.polarbear.PolarBear
                    || p.getVehicle() instanceof net.minecraft.world.entity.animal.panda.Panda
                    || p.getVehicle() instanceof net.minecraft.world.entity.animal.fox.Fox)) {
                ClientPlayNetworking.send(new com.iiampanda.icbreed.net.RideAttackPayload());
            }
            wasUse[0] = down;
        });
        ClientPlayNetworking.registerGlobalReceiver(com.iiampanda.icbreed.net.RulesSyncPayload.TYPE,
                (payload, context) -> context.client().execute(() -> com.iiampanda.icbreed.config.Rules.applyRemote(payload.json())));
        ClientPlayNetworking.registerGlobalReceiver(com.iiampanda.icbreed.net.StayStatePayload.TYPE,
                (payload, context) -> GeneticsScreen.STAY.put(payload.entityId(), new int[] {payload.mode(), payload.canChange() ? 1 : 0}));
        ClientPlayNetworking.registerGlobalReceiver(OpenGeneticsPayload.TYPE, (payload, context) -> {
            Entity e = context.client().level == null ? null : context.client().level.getEntity(payload.entityId());
            if (e instanceof LivingEntity living) {
                context.client().gui.setScreen(new GeneticsScreen(living, payload.genome(), payload.canOpenInventory(), payload.pregnantTicks()));
            }
        });
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void registerBags(com.iiampanda.icbreed.platform.LivingEntityRenderLayerRegistrationCallback.RegistrationHelper helper,
                                     BagsLayer layer) {
        helper.register((net.minecraft.client.renderer.entity.layers.RenderLayer) layer);
    }
}
