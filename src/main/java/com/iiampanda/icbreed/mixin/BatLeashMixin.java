package com.iiampanda.icbreed.mixin;

import com.iiampanda.icbreed.genetics.Genetics;
import net.minecraft.world.entity.ambient.AmbientCreature;
import net.minecraft.world.entity.ambient.Bat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AmbientCreature.class)
public abstract class BatLeashMixin {
    @Inject(method = "canBeLeashed", at = @At("RETURN"), cancellable = true)
    private void icbreed$batsCanBeLeashed(CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ() && (Object) this instanceof Bat bat && (bat.isBaby() || Genetics.isTamed(bat))) cir.setReturnValue(true);
    }
}
