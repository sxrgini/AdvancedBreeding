package com.iiampanda.icbreed.net;

import com.iiampanda.icbreed.IcBreed;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record RideAttackPayload() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<RideAttackPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(IcBreed.MOD_ID, "ride_attack"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RideAttackPayload> CODEC =
            StreamCodec.unit(new RideAttackPayload());

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
