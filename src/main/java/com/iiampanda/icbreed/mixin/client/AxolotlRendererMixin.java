package com.iiampanda.icbreed.mixin.client;

import com.iiampanda.icbreed.genetics.AxolotlColors;
import com.iiampanda.icbreed.genetics.Genetics;
import net.minecraft.client.renderer.entity.AxolotlRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AxolotlRenderer.class)
public abstract class AxolotlRendererMixin {
    @Inject(method = "getTextureLocation(Lnet/minecraft/world/entity/animal/axolotl/Axolotl;)Lnet/minecraft/resources/ResourceLocation;", at = @At("HEAD"), cancellable = true)
    private void icbreed$texture(Axolotl axolotl, CallbackInfoReturnable<ResourceLocation> cir) {
        String id = com.iiampanda.icbreed.platform.Att.get(axolotl, Genetics.AXO_COLOR);
        if (id == null) return;
        cir.setReturnValue(AxolotlColors.texture(id, axolotl.isBaby()));
    }
}
