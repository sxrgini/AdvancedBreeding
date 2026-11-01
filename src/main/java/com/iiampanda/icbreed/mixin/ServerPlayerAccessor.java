package com.iiampanda.icbreed.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ServerPlayer.class)
public interface ServerPlayerAccessor {
    @Invoker("removeEntitiesOnShoulder")
    void icbreed$removeShoulder();

    @Invoker("setShoulderEntityLeft")
    void icbreed$setLeft(CompoundTag tag);

    @Invoker("setShoulderEntityRight")
    void icbreed$setRight(CompoundTag tag);
}
