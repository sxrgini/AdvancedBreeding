package com.iiampanda.icbreed.mixin;

import com.iiampanda.icbreed.genetics.Effects;
import com.iiampanda.icbreed.genetics.Genetics;
import com.iiampanda.icbreed.genetics.Genome;
import com.iiampanda.icbreed.genetics.Sex;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.animal.Animal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Animal.class)
public abstract class AnimalMixin {
    @Inject(method = "spawnChildFromBreeding", at = @At("HEAD"), cancellable = true)
    private void icbreed$startPregnancy(ServerLevel level, Animal partner, CallbackInfo ci) {
        Animal self = (Animal) (Object) this;
        Genome a = Genetics.getOrCreate(self);
        Genome b = Genetics.getOrCreate(partner);
        if (a.sex == b.sex) {
            self.resetLove();
            partner.resetLove();
            ci.cancel();
            return;
        }
        Animal mother = a.sex == Sex.FEMALE ? self : partner;
        Animal father = mother == self ? partner : self;
        if (Genetics.isPregnant(mother)) return;
        Genetics.conceive(mother, father);
        self.finalizeSpawnChildFromBreeding(level, partner, null);
        ci.cancel();
    }

    @Inject(method = "finalizeSpawnChildFromBreeding", at = @At("HEAD"))
    private void icbreed$onBred(ServerLevel level, Animal partner, AgeableMob baby, CallbackInfo ci) {
        Animal self = (Animal) (Object) this;
        if (baby instanceof Animal child) Genetics.inherit(self, partner, child);

        Effects a = Genetics.getOrCreate(self).effects();
        Effects b = Genetics.getOrCreate(partner).effects();
        double xp = Math.max(a.xpPct, b.xpPct);
        if (xp > 0) {
            int extra = (int) Math.round((1 + self.getRandom().nextInt(7)) * xp);
            if (extra > 0) ExperienceOrb.award(level, self.position(), extra);
        }
    }

    @Inject(method = "canMate", at = @At("RETURN"), cancellable = true)
    private void icbreed$canMate(Animal partner, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ()) return;
        Animal self = (Animal) (Object) this;
        if (self.level().isClientSide()) return;
        if (!Genetics.canBreed(self, partner)) cir.setReturnValue(false);
    }

    @Inject(method = "canFallInLove", at = @At("RETURN"), cancellable = true)
    private void icbreed$noLoveWhilePregnant(CallbackInfoReturnable<Boolean> cir) {
        Animal self = (Animal) (Object) this;
        if (cir.getReturnValueZ() && !self.level().isClientSide() && (Genetics.isPregnant(self) || Genetics.isWild(self))) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "usePlayerItem", at = @At("HEAD"))
    private void icbreed$fed(net.minecraft.world.entity.player.Player player, net.minecraft.world.InteractionHand hand, net.minecraft.world.item.ItemStack stack, CallbackInfo ci) {
        Genetics.fed((Animal) (Object) this, player.getUUID());
    }
}
