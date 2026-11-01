package com.iiampanda.icbreed.mixin;

import com.iiampanda.icbreed.traits.Rideable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Strider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Strider.class)
public abstract class StriderMixin {
    @Inject(method = "getControllingPassenger", at = @At("RETURN"), cancellable = true)
    private void icbreed$captain(CallbackInfoReturnable<LivingEntity> cir) {
        Strider self = (Strider) (Object) this;
        if (cir.getReturnValue() == null && self.isSaddled() && self.getFirstPassenger() instanceof Player p && Rideable.captain(self)) {
            cir.setReturnValue(p);
        }
    }

    @Inject(method = "tickRidden", at = @At("HEAD"), cancellable = true)
    private void icbreed$turn(Player player, Vec3 input, CallbackInfo ci) {
        Strider self = (Strider) (Object) this;
        if (!Rideable.captain(self)) return;
        self.setYRot(player.getYRot());
        self.setXRot(player.getXRot() * 0.5F);
        self.yRotO = self.yBodyRot = self.yHeadRot = self.getYRot();
        ci.cancel();
    }

    @Inject(method = "getRiddenInput", at = @At("RETURN"), cancellable = true)
    private void icbreed$input(Player player, Vec3 input, CallbackInfoReturnable<Vec3> cir) {
        if (!Rideable.captain((Strider) (Object) this)) return;
        float z = player.zza < 0.0F ? player.zza * 0.25F : player.zza;
        cir.setReturnValue(new Vec3(player.xxa * 0.5F, 0.0, z));
    }

    @Inject(method = "getRiddenSpeed", at = @At("RETURN"), cancellable = true)
    private void icbreed$speed(Player player, CallbackInfoReturnable<Float> cir) {
        Strider self = (Strider) (Object) this;
        if (Rideable.captain(self)) cir.setReturnValue((float) self.getAttributeValue(Attributes.MOVEMENT_SPEED) * 0.55F);
    }
}
