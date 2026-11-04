package com.iiampanda.icbreed.mixin;

import com.iiampanda.icbreed.taming.TamingLogic;
import net.minecraft.world.entity.TamableAnimal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TamableAnimal.class)
public abstract class TamableAnimalMixin {
    @Inject(method = "setOrderedToSit", at = @At("HEAD"), cancellable = true)
    private void icbreed$noClickSit(boolean sit, CallbackInfo ci) {
        TamableAnimal self = (TamableAnimal) (Object) this;
        if (!self.level().isClientSide() && !com.iiampanda.icbreed.traits.StayPut.ALLOW.get() && com.iiampanda.icbreed.genetics.Genetics.isTamed(self)) ci.cancel();
    }

    @Inject(method = "setTame", at = @At("HEAD"), cancellable = true)
    private void icbreed$noVanillaTame(boolean tame, CallbackInfo ci) {
        if (tame && TamingLogic.blocksVanillaTaming(this)) ci.cancel();
    }
}
