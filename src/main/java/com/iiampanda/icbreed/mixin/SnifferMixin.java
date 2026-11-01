package com.iiampanda.icbreed.mixin;

import com.iiampanda.icbreed.genetics.Effects;
import com.iiampanda.icbreed.genetics.Genetics;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.sniffer.Sniffer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Sniffer.class)
public abstract class SnifferMixin {
    @Inject(method = "dropSeed", at = @At("TAIL"))
    private void icbreed$nuggets(CallbackInfo ci) {
        Sniffer self = (Sniffer) (Object) this;
        if (!(self.level() instanceof ServerLevel level)) return;
        Effects fx = Genetics.effects(self);
        if (fx == null || fx.nuggets <= 0 || self.getRandom().nextFloat() >= 0.5F) return;
        var nugget = switch (self.getRandom().nextInt(3)) {
            case 0 -> Items.IRON_NUGGET;
            case 1 -> Items.GOLD_NUGGET;
            default -> Items.COPPER_NUGGET;
        };
        self.spawnAtLocation(level, new ItemStack(nugget));
    }
}
