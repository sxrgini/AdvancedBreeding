package com.iiampanda.icbreed.mixin;

import net.minecraft.world.entity.animal.happyghast.HappyGhast;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(HappyGhast.class)
public interface HappyGhastAccessor {
    @Invoker("setServerStillTimeout")
    void icbreed$setStill(int ticks);
}
