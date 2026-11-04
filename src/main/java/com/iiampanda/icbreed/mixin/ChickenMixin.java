package com.iiampanda.icbreed.mixin;

import com.iiampanda.icbreed.genetics.Effects;
import com.iiampanda.icbreed.genetics.Genetics;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Chicken.class)
public abstract class ChickenMixin {
    @Unique
    private int icbreed$prevEggTime;

    @Inject(method = "aiStep", at = @At("HEAD"))
    private void icbreed$before(CallbackInfo ci) {
        icbreed$prevEggTime = ((Chicken) (Object) this).eggTime;
    }

    @Inject(method = "aiStep", at = @At("RETURN"))
    private void icbreed$after(CallbackInfo ci) {
        Chicken self = (Chicken) (Object) this;

        if (self.eggTime <= icbreed$prevEggTime || !(self.level() instanceof ServerLevel level)) return;
        Genetics.addXp(self, Genetics.XP_PRODUCE);
        Effects fx = Genetics.effects(self);
        if (fx == null || fx.productionRank <= 0) return;
        if (self.getRandom().nextDouble() < com.iiampanda.icbreed.IcBreedConfig.get().productionChancePerRank * Math.min(3, fx.productionRank)) {
            self.spawnAtLocation(level, Items.EGG);
        }
    }
}
