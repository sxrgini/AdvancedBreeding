package com.iiampanda.icbreed.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(net.minecraft.world.entity.animal.pig.Pig.class)
public interface PhenoPigAccessor {
    @Invoker("setVariant")
    void icbreed$set(net.minecraft.core.Holder<net.minecraft.world.entity.animal.pig.PigVariant> v);
}
