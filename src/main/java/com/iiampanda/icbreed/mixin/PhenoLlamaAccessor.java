package com.iiampanda.icbreed.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(net.minecraft.world.entity.animal.horse.Llama.class)
public interface PhenoLlamaAccessor {
    @Invoker("setVariant")
    void icbreed$set(net.minecraft.world.entity.animal.horse.Llama.Variant v);
}
