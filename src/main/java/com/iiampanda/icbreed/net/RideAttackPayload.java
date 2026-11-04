package com.iiampanda.icbreed.net;

import com.iiampanda.icbreed.IcBreed;
import net.minecraft.network.FriendlyByteBuf;
import com.iiampanda.icbreed.platform.StreamCodec;
import com.iiampanda.icbreed.platform.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record RideAttackPayload() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<RideAttackPayload> TYPE =
            new CustomPacketPayload.Type<>(new ResourceLocation(IcBreed.MOD_ID, "ride_attack"));

    public static final StreamCodec<FriendlyByteBuf, RideAttackPayload> CODEC =
            StreamCodec.unit(new RideAttackPayload());

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
