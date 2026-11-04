package com.iiampanda.icbreed.compat.mixin;

import com.blackgear.vanillabackport.common.level.entities.mob.animal.happy_ghast.HappyGhast;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(HappyGhast.class)
public interface HappyGhastAccessor {
    @Invoker(value = "setServerStillTimeout", remap = false)
    void icbreed$setStill(int ticks);
}
