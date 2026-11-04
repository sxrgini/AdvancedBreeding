package com.iiampanda.icbreed.platform;

import net.minecraft.resources.ResourceLocation;

public interface CustomPacketPayload {
    record Type<T extends CustomPacketPayload>(ResourceLocation id) {
    }

    Type<? extends CustomPacketPayload> type();
}
