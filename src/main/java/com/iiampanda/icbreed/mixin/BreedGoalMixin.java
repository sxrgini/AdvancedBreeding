package com.iiampanda.icbreed.mixin;

import java.util.List;

import com.iiampanda.icbreed.genetics.Genetics;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.Animal;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BreedGoal.class)
public abstract class BreedGoalMixin {
    @Shadow @Final protected Animal animal;
    @Shadow @Final protected net.minecraft.world.level.Level level;
    @Shadow @Final private Class<? extends Animal> partnerClass;
    @Shadow @Final private static TargetingConditions PARTNER_TARGETING;

    @Inject(method = "getFreePartner", at = @At("RETURN"), cancellable = true)
    private void icbreed$onlyAllowedPartners(CallbackInfoReturnable<Animal> cir) {
        Animal found = cir.getReturnValue();
        if (found == null || Genetics.canBreed(this.animal, found)) return;

        List<? extends Animal> nearby = this.level.getNearbyEntities(this.partnerClass, PARTNER_TARGETING,
                this.animal, this.animal.getBoundingBox().inflate(8.0));
        double best = Double.MAX_VALUE;
        Animal pick = null;
        for (Animal candidate : nearby) {
            if (this.animal.canMate(candidate) && Genetics.canBreed(this.animal, candidate)
                    && this.animal.distanceToSqr(candidate) < best) {
                pick = candidate;
                best = this.animal.distanceToSqr(candidate);
            }
        }
        cir.setReturnValue(pick);
    }
}
