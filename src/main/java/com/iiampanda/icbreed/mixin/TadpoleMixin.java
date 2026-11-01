package com.iiampanda.icbreed.mixin;

import com.iiampanda.icbreed.genetics.Genetics;
import com.iiampanda.icbreed.taming.TamingLogic;
import net.minecraft.world.entity.animal.frog.Tadpole;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Tadpole.class)
public abstract class TadpoleMixin {
    @Inject(method = "feed", at = @At("HEAD"))
    private void icbreed$fed(Player player, ItemStack stack, CallbackInfo ci) {
        Tadpole self = (Tadpole) (Object) this;
        if (self.level().isClientSide()) return;
        var fx = Genetics.effects(self);
        Genetics.addXp(self, (int) Math.round(Genetics.XP_FEED * (1.0 + (fx == null ? 0 : Math.max(-0.9, fx.feedXpPct)))));
        TamingLogic.onFed(self, player.getUUID());
    }

    @Inject(method = "ageUp()V", at = @At("HEAD"))
    private void icbreed$convertStart(CallbackInfo ci) {
        Genetics.CONVERTING.set((Tadpole) (Object) this);
    }

    @Inject(method = "ageUp()V", at = @At("RETURN"))
    private void icbreed$convertEnd(CallbackInfo ci) {
        Genetics.CONVERTING.remove();
    }
}
