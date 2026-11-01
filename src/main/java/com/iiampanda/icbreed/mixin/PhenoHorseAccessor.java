package com.iiampanda.icbreed.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(net.minecraft.world.entity.animal.equine.Horse.class)
public interface PhenoHorseAccessor {
    @Invoker("setVariantAndMarkings")
    void icbreed$set(net.minecraft.world.entity.animal.equine.Variant v, net.minecraft.world.entity.animal.equine.Markings m);
}
