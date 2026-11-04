package com.iiampanda.icbreed.mixin;

import com.iiampanda.icbreed.genetics.Effects;
import com.iiampanda.icbreed.genetics.Genetics;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.armadillo.Armadillo;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Armadillo.class)
public abstract class ArmadilloMixin {
    @Inject(method = "brushOffScute", at = @At("RETURN"))
    private void icbreed$moreScutes(CallbackInfoReturnable<Boolean> cir) {
        Armadillo self = (Armadillo) (Object) this;
        if (!cir.getReturnValueZ() || !(self.level() instanceof ServerLevel level)) return;
        Effects fx = Genetics.effects(self);
        if (fx == null) return;
        int extra = (int) Math.min(3, fx.productionRank);
        for (int i = 0; i < extra; i++) self.spawnAtLocation(Items.ARMADILLO_SCUTE);
    }
}
