package com.iiampanda.icbreed.mixin;

import com.iiampanda.icbreed.genetics.Effects;
import com.iiampanda.icbreed.genetics.Genetics;
import net.minecraft.world.entity.animal.bee.Bee;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(targets = "net.minecraft.world.entity.animal.bee.Bee$BeePollinateGoal")
public abstract class BeePollinateMixin {
    @Shadow
    @Final
    Bee this$0;

    @ModifyConstant(method = "hasPollinatedLongEnough", constant = @Constant(intValue = 400))
    private int icbreed$pollination(int original) {
        Effects fx = Genetics.effects(this$0);
        if (fx == null) return original;
        return Math.max(20, original - (int) Math.round(Math.min(2, fx.pollination)) * 100);
    }
}
