package com.iiampanda.icbreed.platform;

import com.mojang.serialization.Codec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public final class Codecs {
    public static final Codec<ItemStack> ITEM_STACK = CompoundTag.CODEC.xmap(ItemStack::of, stack -> stack.save(new CompoundTag()));

    private Codecs() {
    }
}
