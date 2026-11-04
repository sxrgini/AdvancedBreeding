package com.iiampanda.icbreed.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(net.minecraft.world.entity.animal.Wolf.class)
public interface PhenoWolfAccessor {
    @Invoker("setVariant")
    void icbreed$set(net.minecraft.core.Holder<net.minecraft.world.entity.animal.WolfVariant> v);
}
