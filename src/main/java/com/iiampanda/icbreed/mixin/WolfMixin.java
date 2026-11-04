package com.iiampanda.icbreed.mixin;

import com.iiampanda.icbreed.genetics.Genetics;
import com.iiampanda.icbreed.taming.TamingLogic;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Wolf.class)
public abstract class WolfMixin {
    @Inject(method = "mobInteract", at = @At("HEAD"))
    private void icbreed$boneFed(Player player, net.minecraft.world.InteractionHand hand, CallbackInfoReturnable<net.minecraft.world.InteractionResult> cir) {
        Wolf wolf = (Wolf) (Object) this;
        if (wolf.level().isClientSide() || wolf.isTame() || wolf.isAngry() || !player.getItemInHand(hand).is(net.minecraft.world.item.Items.BONE)) return;
        var fx = Genetics.effects(wolf);
        Genetics.addXp(wolf, (int) Math.round(Genetics.XP_FEED * (1.0 + (fx == null ? 0 : Math.max(-0.9, fx.feedXpPct)))));
        TamingLogic.onFed(wolf, player.getUUID());
    }
}
