package com.iiampanda.icbreed.mixin;

import net.minecraft.world.entity.animal.fish.WaterAnimal;
import net.minecraft.world.entity.animal.frog.Tadpole;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WaterAnimal.class)
public abstract class WaterAnimalMixin {
    @Inject(method = "canBeLeashed", at = @At("RETURN"), cancellable = true)
    private void icbreed$tadpoleLead(CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ() && (Object) this instanceof Tadpole) cir.setReturnValue(true);
    }
}
