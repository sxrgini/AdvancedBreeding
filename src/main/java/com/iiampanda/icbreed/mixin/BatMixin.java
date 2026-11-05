package com.iiampanda.icbreed.mixin;

import com.iiampanda.icbreed.traits.BatHang;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ambient.Bat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Bat.class)
public abstract class BatMixin {
    @Inject(method = "customServerAiStep", at = @At("HEAD"), cancellable = true)
    private void icbreed$control(ServerLevel level, CallbackInfo ci) {
        if (BatHang.control(level, (Bat) (Object) this)) ci.cancel();
    }
}
