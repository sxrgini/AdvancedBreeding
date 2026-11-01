package com.iiampanda.icbreed.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(net.minecraft.world.entity.animal.rabbit.Rabbit.class)
public interface PhenoRabbitAccessor {
    @Invoker("setVariant")
    void icbreed$set(net.minecraft.world.entity.animal.rabbit.Rabbit.Variant v);
}
