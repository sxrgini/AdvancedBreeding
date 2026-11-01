package com.iiampanda.icbreed.mixin;

import com.iiampanda.icbreed.traits.AuraManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Player.class)
public abstract class PlayerMixin {
    @ModifyVariable(method = "causeFoodExhaustion", at = @At("HEAD"), argsOnly = true)
    private float icbreed$saturation(float amount) {
        if (!((Object) this instanceof ServerPlayer p)) return amount;
        double s = AuraManager.saturation(p.getUUID());
        return s == 0 ? amount : amount * (float) Math.max(0.0, 1.0 + s);
    }
}
