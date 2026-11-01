package com.iiampanda.icbreed.mixin;

import com.iiampanda.icbreed.traits.Rideable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Mob.class)
public abstract class RideMixin {
    @Inject(method = "getControllingPassenger", at = @At("RETURN"), cancellable = true)
    private void icbreed$rideable(CallbackInfoReturnable<LivingEntity> cir) {
        if (cir.getReturnValue() != null) return;
        Mob self = (Mob) (Object) this;
        if (self.getFirstPassenger() instanceof Player p && Rideable.is(self)) cir.setReturnValue(p);
    }
}
