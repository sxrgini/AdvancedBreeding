package com.iiampanda.icbreed.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(net.minecraft.world.entity.animal.equine.Llama.class)
public interface PhenoLlamaAccessor {
    @Invoker("setVariant")
    void icbreed$set(net.minecraft.world.entity.animal.equine.Llama.Variant v);
}
