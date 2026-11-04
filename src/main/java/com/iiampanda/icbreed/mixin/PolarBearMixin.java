package com.iiampanda.icbreed.mixin;

import com.iiampanda.icbreed.genetics.Ages;
import net.minecraft.world.entity.animal.PolarBear;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PolarBear.class)
public abstract class PolarBearMixin {
    @Inject(method = "isFood", at = @At("RETURN"), cancellable = true)
    private void icbreed$fishFood(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ() && Ages.isFish(stack)) cir.setReturnValue(true);
    }
}
