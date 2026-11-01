package com.iiampanda.icbreed.mixin.client;

import com.iiampanda.icbreed.client.BagsLayer;
import com.iiampanda.icbreed.genetics.Genetics;
import net.minecraft.client.renderer.entity.HorseRenderer;
import net.minecraft.client.renderer.entity.state.HorseRenderState;
import net.minecraft.world.entity.animal.equine.Horse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HorseRenderer.class)
public abstract class HorseRendererMixin {
    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/animal/equine/Horse;Lnet/minecraft/client/renderer/entity/state/HorseRenderState;F)V",
            at = @At("TAIL"))
    private void icbreed$bags(Horse horse, HorseRenderState state, float partialTick, CallbackInfo ci) {
        Integer style = com.iiampanda.icbreed.platform.Att.get(horse, Genetics.BAG_STYLE);
        BagsLayer.BAG_STYLE.put(state, com.iiampanda.icbreed.platform.Att.get(horse, Genetics.BAGS) == null ? -1 : style == null ? 0 : style);
    }
}
