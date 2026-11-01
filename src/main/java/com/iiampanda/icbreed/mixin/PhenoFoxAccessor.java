package com.iiampanda.icbreed.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(net.minecraft.world.entity.animal.fox.Fox.class)
public interface PhenoFoxAccessor {
    @Invoker("setVariant")
    void icbreed$set(net.minecraft.world.entity.animal.fox.Fox.Variant v);
}
