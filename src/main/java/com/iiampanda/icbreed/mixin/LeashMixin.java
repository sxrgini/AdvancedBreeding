package com.iiampanda.icbreed.mixin;

import com.iiampanda.icbreed.genetics.Genetics;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Panda;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({Panda.class, Hoglin.class})
public abstract class LeashMixin {
    @Inject(method = "canBeLeashed", at = @At("RETURN"), cancellable = true)
    private void icbreed$babiesCanBeLeashed(CallbackInfoReturnable<Boolean> cir) {
        Mob self = (Mob) (Object) this;
        if (!cir.getReturnValueZ() && (self.isBaby() || Genetics.isTamed(self))) cir.setReturnValue(true);
    }
}
