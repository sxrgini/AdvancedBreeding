package com.iiampanda.icbreed.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "net.minecraft.world.level.block.entity.BeehiveBlockEntity$BeeData")
public interface BeeDataAccessor {
    @Accessor("minOccupationTicks")
    int icbreed$getMin();

    @Mutable
    @Accessor("minOccupationTicks")
    void icbreed$setMin(int value);
}
