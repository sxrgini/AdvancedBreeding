package com.iiampanda.icbreed.mixin;

import com.iiampanda.icbreed.genetics.Effects;
import com.iiampanda.icbreed.genetics.Genetics;
import net.minecraft.world.entity.animal.Bee;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.world.entity.animal.Bee$BeePollinateGoal")
public abstract class BeePollinateMixin {
    @Unique
    private Bee icbreed$bee;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void icbreed$capture(Bee bee, CallbackInfo ci) {
        icbreed$bee = bee;
    }

    @ModifyConstant(method = "hasPollinatedLongEnough", constant = @Constant(intValue = 400))
    private int icbreed$pollination(int original) {
        if (icbreed$bee == null) return original;
        Effects fx = Genetics.effects(icbreed$bee);
        if (fx == null) return original;
        return Math.max(20, original - (int) Math.round(Math.min(2, fx.pollination)) * 100);
    }
}
