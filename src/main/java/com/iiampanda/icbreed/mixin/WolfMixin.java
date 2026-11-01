package com.iiampanda.icbreed.mixin;

import com.iiampanda.icbreed.genetics.Genetics;
import com.iiampanda.icbreed.taming.TamingLogic;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Wolf.class)
public abstract class WolfMixin {
    @Inject(method = "tryToTame", at = @At("HEAD"))
    private void icbreed$boneFed(Player player, CallbackInfo ci) {
        Wolf wolf = (Wolf) (Object) this;
        if (wolf.level().isClientSide()) return;
        var fx = Genetics.effects(wolf);
        Genetics.addXp(wolf, (int) Math.round(Genetics.XP_FEED * (1.0 + (fx == null ? 0 : Math.max(-0.9, fx.feedXpPct)))));
        TamingLogic.onFed(wolf, player.getUUID());
    }
}
