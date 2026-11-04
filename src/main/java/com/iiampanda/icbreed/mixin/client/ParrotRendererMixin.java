package com.iiampanda.icbreed.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Parrot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class ParrotRendererMixin {
    @Inject(method = "scale", at = @At("HEAD"))
    private void icbreed$babyParrot(LivingEntity entity, PoseStack poseStack, float partialTick, CallbackInfo ci) {
        if (entity instanceof Parrot && entity.isBaby()) poseStack.scale(0.5F, 0.5F, 0.5F);
    }
}
