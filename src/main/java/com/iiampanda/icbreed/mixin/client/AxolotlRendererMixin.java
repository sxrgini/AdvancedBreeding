package com.iiampanda.icbreed.mixin.client;

import com.iiampanda.icbreed.client.AxolotlColorState;
import com.iiampanda.icbreed.genetics.Genetics;
import net.minecraft.client.renderer.entity.AxolotlRenderer;
import net.minecraft.client.renderer.entity.state.AxolotlRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AxolotlRenderer.class)
public abstract class AxolotlRendererMixin {
    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/animal/axolotl/Axolotl;Lnet/minecraft/client/renderer/entity/state/AxolotlRenderState;F)V", at = @At("TAIL"))
    private void icbreed$color(Axolotl axolotl, AxolotlRenderState state, float partialTick, CallbackInfo ci) {
        ((AxolotlColorState) state).icbreed$color(com.iiampanda.icbreed.platform.Att.get(axolotl, Genetics.AXO_COLOR));
    }

    @Inject(method = "getTextureLocation(Lnet/minecraft/client/renderer/entity/state/AxolotlRenderState;)Lnet/minecraft/resources/Identifier;", at = @At("HEAD"), cancellable = true)
    private void icbreed$texture(AxolotlRenderState state, CallbackInfoReturnable<Identifier> cir) {
        String id = ((AxolotlColorState) state).icbreed$color();
        if (id == null) return;
        cir.setReturnValue(com.iiampanda.icbreed.genetics.AxolotlColors.texture(id, state.isBaby));
    }
}
