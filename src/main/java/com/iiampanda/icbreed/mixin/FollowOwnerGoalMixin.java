package com.iiampanda.icbreed.mixin;

import com.iiampanda.icbreed.genetics.Genetics;
import com.iiampanda.icbreed.traits.StayPut;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FollowOwnerGoal.class)
public abstract class FollowOwnerGoalMixin {
    @Shadow @org.spongepowered.asm.mixin.Final private TamableAnimal tamable;

    @Inject(method = "canUse", at = @At("HEAD"), cancellable = true)
    private void icbreed$onlyWhenFollowing(CallbackInfoReturnable<Boolean> cir) {
        if (Genetics.supports(tamable) && Genetics.isTamed(tamable) && !StayPut.following(tamable)) cir.setReturnValue(false);
    }

    @Inject(method = "canContinueToUse", at = @At("HEAD"), cancellable = true)
    private void icbreed$stopWhenNotFollowing(CallbackInfoReturnable<Boolean> cir) {
        if (Genetics.supports(tamable) && Genetics.isTamed(tamable) && !StayPut.following(tamable)) cir.setReturnValue(false);
    }
}
