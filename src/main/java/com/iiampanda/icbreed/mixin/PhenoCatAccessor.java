package com.iiampanda.icbreed.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(net.minecraft.world.entity.animal.feline.Cat.class)
public interface PhenoCatAccessor {
    @Invoker("setVariant")
    void icbreed$set(net.minecraft.core.Holder<net.minecraft.world.entity.animal.feline.CatVariant> v);
}
