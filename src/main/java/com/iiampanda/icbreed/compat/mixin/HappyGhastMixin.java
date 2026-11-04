package com.iiampanda.icbreed.compat.mixin;

import com.blackgear.vanillabackport.common.level.entities.mob.animal.happy_ghast.HappyGhast;
import com.iiampanda.icbreed.compat.vb.GhastDeck;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HappyGhast.class)
public abstract class HappyGhastMixin {
    @ModifyConstant(method = "tick", constant = @Constant(intValue = 10))
    private int icbreed$longerStill(int original) {
        return 80;
    }

    @Inject(method = "canBeHarnessed", at = @At("HEAD"), cancellable = true, remap = false)
    private void icbreed$noHarness(CallbackInfoReturnable<Boolean> cir) {
        if (GhastDeck.hasBlocks((HappyGhast) (Object) this)) cir.setReturnValue(false);
    }

    @Inject(method = "getControllingPassenger", at = @At("RETURN"), cancellable = true)
    private void icbreed$seat(CallbackInfoReturnable<LivingEntity> cir) {
        if (cir.getReturnValue() != null) return;
        LivingEntity rider = GhastDeck.seated((HappyGhast) (Object) this);
        if (rider != null) cir.setReturnValue(rider);
    }

    @Inject(method = "aiStep", at = @At("TAIL"))
    private void icbreed$deckTick(CallbackInfo ci) {
        HappyGhast g = (HappyGhast) (Object) this;
        if (!g.level().isClientSide()) GhastDeck.tick(g);
    }
}
