package com.iiampanda.icbreed.net;

import com.iiampanda.icbreed.IcBreed;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record RemoveHatPayload() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<RemoveHatPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(IcBreed.MOD_ID, "remove_hat"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RemoveHatPayload> CODEC =
            StreamCodec.unit(new RemoveHatPayload());

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
