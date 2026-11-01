package com.iiampanda.icbreed.mixin;

import com.iiampanda.icbreed.genetics.Genetics;
import com.iiampanda.icbreed.traits.Shoulder;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin {
    @Inject(method = "removeEntitiesOnShoulder", at = @At("HEAD"), cancellable = true)
    private void icbreed$keepParrot(CallbackInfo ci) {
        ServerPlayer self = (ServerPlayer) (Object) this;
        if (Shoulder.FORCE.get()) return;
        Integer perks = com.iiampanda.icbreed.platform.Att.get(self, Genetics.PERKS);
        if (perks != null && (perks & 1) != 0 && self.isAlive()) ci.cancel();
    }
}
