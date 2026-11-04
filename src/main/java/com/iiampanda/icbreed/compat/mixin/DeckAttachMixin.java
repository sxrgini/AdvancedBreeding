package com.iiampanda.icbreed.compat.mixin;

import com.blackgear.vanillabackport.common.level.entities.mob.animal.happy_ghast.HappyGhast;
import com.iiampanda.icbreed.compat.vb.GhastDeck;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HappyGhast.class)
public abstract class DeckAttachMixin {
    @Inject(method = "positionRider", at = @At("HEAD"), cancellable = true)
    private void icbreed$deckAttach(Entity passenger, Entity.MoveFunction mover, CallbackInfo ci) {
        HappyGhast g = (HappyGhast) (Object) this;
        Vec3 v = GhastDeck.attach(g, passenger);
        if (v == null) return;
        Vec3 p = g.position().add(v.yRot(-g.getYRot() * ((float) Math.PI / 180.0F)));
        mover.accept(passenger, p.x, p.y, p.z);
        ci.cancel();
    }
}
