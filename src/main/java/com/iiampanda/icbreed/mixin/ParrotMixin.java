package com.iiampanda.icbreed.mixin;

import com.iiampanda.icbreed.genetics.Genetics;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Parrot.class)
public abstract class ParrotMixin {
    @Inject(method = "registerGoals", at = @At("TAIL"))
    private void icbreed$breedGoal(CallbackInfo ci) {
        Parrot self = (Parrot) (Object) this;
        ((MobAccessor) (Object) self).icbreed$goals().addGoal(1, new BreedGoal(self, 1.0D));
    }

    @Inject(method = "isFood", at = @At("RETURN"), cancellable = true)
    private void icbreed$seeds(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (stack.is(ItemTags.PARROT_FOOD)) cir.setReturnValue(true);
    }

    @Inject(method = "canMate", at = @At("HEAD"), cancellable = true)
    private void icbreed$mate(Animal partner, CallbackInfoReturnable<Boolean> cir) {
        Parrot self = (Parrot) (Object) this;
        cir.setReturnValue(partner != self && partner instanceof Parrot && self.isInLove() && partner.isInLove()
                && (self.level().isClientSide() || Genetics.canBreed(self, partner)));
    }

    @Inject(method = "getBreedOffspring", at = @At("HEAD"), cancellable = true)
    private void icbreed$offspring(ServerLevel level, AgeableMob partner, CallbackInfoReturnable<AgeableMob> cir) {
        Parrot self = (Parrot) (Object) this;
        Parrot baby = EntityType.PARROT.create(level);
        if (baby == null) return;
        Parrot.Variant v = partner instanceof Parrot p && self.getRandom().nextBoolean() ? p.getVariant() : self.getVariant();
        ((ParrotAccessor) (Object) baby).icbreed$setVariant(v);
        cir.setReturnValue(baby);
    }

    @Inject(method = "mobInteract", at = @At("HEAD"), cancellable = true)
    private void icbreed$feedSeeds(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        Parrot self = (Parrot) (Object) this;
        ItemStack stack = player.getItemInHand(hand);
        if (!self.isTame() || !stack.is(ItemTags.PARROT_FOOD)) return;
        if (self.isBaby()) {
            if (com.iiampanda.icbreed.taming.TamingLogic.saturated(self)) return;
            if (!self.level().isClientSide()) {
                ((AnimalUseAccessor) (Object) self).icbreed$use(player, hand, stack);
            }
        } else if (self.getAge() == 0 && self.canFallInLove()) {
            if (!self.level().isClientSide()) {
                ((AnimalUseAccessor) (Object) self).icbreed$use(player, hand, stack);
                self.setInLove(player);
            }
        } else {
            return;
        }
        cir.setReturnValue(InteractionResult.SUCCESS);
    }
}
