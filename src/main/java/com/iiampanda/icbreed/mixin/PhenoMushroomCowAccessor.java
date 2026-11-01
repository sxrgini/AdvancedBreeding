package com.iiampanda.icbreed.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(net.minecraft.world.entity.animal.cow.MushroomCow.class)
public interface PhenoMushroomCowAccessor {
    @Invoker("setVariant")
    void icbreed$set(net.minecraft.world.entity.animal.cow.MushroomCow.Variant v);
}
