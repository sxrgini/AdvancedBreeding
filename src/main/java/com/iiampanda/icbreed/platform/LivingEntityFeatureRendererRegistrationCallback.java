package com.iiampanda.icbreed.platform;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.EntityType;

public interface LivingEntityFeatureRendererRegistrationCallback {
    Ev<LivingEntityFeatureRendererRegistrationCallback> EVENT = new Ev<>();

    void registerLayers(EntityType<?> entityType, EntityRenderer<?> entityRenderer, RegistrationHelper registrationHelper, EntityRendererProvider.Context context);

    interface RegistrationHelper {
        void register(RenderLayer<?, ?> layer);
    }
}
