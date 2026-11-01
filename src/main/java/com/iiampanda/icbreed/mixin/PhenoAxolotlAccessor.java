package com.iiampanda.icbreed.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(net.minecraft.world.entity.animal.axolotl.Axolotl.class)
public interface PhenoAxolotlAccessor {
    @Invoker("setVariant")
    void icbreed$set(net.minecraft.world.entity.animal.axolotl.Axolotl.Variant v);
}
