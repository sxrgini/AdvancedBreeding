package com.iiampanda.icbreed.mixin;

import com.iiampanda.icbreed.genetics.Genetics;
import com.iiampanda.icbreed.genetics.Traits;
import com.iiampanda.icbreed.traits.Shoulder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.entity.animal.ShoulderRidingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ShoulderRidingEntity.class)
public abstract class ShoulderRidingEntityMixin {
    @Inject(method = "canSitOnShoulder", at = @At("RETURN"), cancellable = true)
    private void icbreed$needsTrait(CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ() || !((Object) this instanceof Parrot parrot)) return;
        Traits t = parrot.getAttached(Genetics.TRAITS);
        if (t == null || t.rank("shoulder_pal") <= 0) cir.setReturnValue(false);
    }

    @Inject(method = "setEntityOnShoulder", at = @At("RETURN"))
    private void icbreed$perch(ServerPlayer player, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ() && (Object) this instanceof Parrot) Shoulder.refresh(player);
    }
}
