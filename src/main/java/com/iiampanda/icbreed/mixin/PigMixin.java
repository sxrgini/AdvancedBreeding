package com.iiampanda.icbreed.mixin;

import com.iiampanda.icbreed.taming.PigGuard;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Pig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Pig.class)
public abstract class PigMixin {
    @Inject(method = "createAttributes", at = @At("RETURN"))
    private static void icbreed$bite(CallbackInfoReturnable<AttributeSupplier.Builder> cir) {
        cir.getReturnValue().add(Attributes.ATTACK_DAMAGE, 3.0);
    }

    @Inject(method = "registerGoals", at = @At("TAIL"))
    private void icbreed$guard(CallbackInfo ci) {
        Pig self = (Pig) (Object) this;
        ((MobAccessor) self).icbreed$goals().addGoal(0, new PigGuard.Attack(self));
        ((MobAccessor) self).icbreed$targets().addGoal(1, new PigGuard.OwnerHurtBy(self));
        ((MobAccessor) self).icbreed$targets().addGoal(2, new PigGuard.OwnerHurt(self));
        ((MobAccessor) self).icbreed$targets().addGoal(3, new PigGuard.Retaliate(self));
    }
}
