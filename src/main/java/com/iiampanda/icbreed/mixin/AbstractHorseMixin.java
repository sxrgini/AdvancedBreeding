package com.iiampanda.icbreed.mixin;

import com.iiampanda.icbreed.taming.TamingLogic;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractHorse.class)
public abstract class AbstractHorseMixin {
    @Inject(method = "setTamed", at = @At("HEAD"), cancellable = true)
    private void icbreed$noVanillaTame(boolean tamed, CallbackInfo ci) {
        if (tamed && TamingLogic.blocksVanillaTaming(this)) ci.cancel();
    }

    @Inject(method = "handleEating", at = @At("RETURN"))
    private void icbreed$fed(net.minecraft.world.entity.player.Player player, net.minecraft.world.item.ItemStack stack,
                             CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ()) return;
        AbstractHorse horse = (AbstractHorse) (Object) this;
        if (horse.level().isClientSide()) return;
        var fx = com.iiampanda.icbreed.genetics.Genetics.effects(horse);
        com.iiampanda.icbreed.genetics.Genetics.addXp(horse, (int) Math.round(com.iiampanda.icbreed.genetics.Genetics.XP_FEED
                * (1.0 + (fx == null ? 0 : Math.max(-0.9, fx.feedXpPct)))));
        TamingLogic.onFed(horse, player.getUUID());
    }
}
