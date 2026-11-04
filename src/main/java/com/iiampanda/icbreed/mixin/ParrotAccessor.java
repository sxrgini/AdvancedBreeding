package com.iiampanda.icbreed.mixin;

import net.minecraft.world.entity.animal.Parrot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Parrot.class)
public interface ParrotAccessor {
    @Invoker("setVariant")
    void icbreed$setVariant(Parrot.Variant variant);
}
