package com.iiampanda.icbreed.mixin;

import com.iiampanda.icbreed.traits.Rideable;
import net.minecraft.world.entity.animal.equine.Llama;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(net.minecraft.world.entity.Mob.class)
public abstract class HorseSaddledMixin {
    @Inject(method = "isSaddled", at = @At("HEAD"), cancellable = true)
    private void icbreed$llamaRidden(CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof Llama llama && Rideable.is(llama)) cir.setReturnValue(true);
    }
}
