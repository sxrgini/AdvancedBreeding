package com.iiampanda.icbreed.mixin;

import java.util.List;

import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BeehiveBlockEntity.class)
public interface BeehiveAccessor {
    @Accessor("stored")
    List<?> icbreed$stored();
}
