package com.iiampanda.icbreed.net;

import com.iiampanda.icbreed.IcBreed;
import net.minecraft.network.FriendlyByteBuf;
import com.iiampanda.icbreed.platform.StreamCodec;
import com.iiampanda.icbreed.platform.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record RemoveHatPayload() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<RemoveHatPayload> TYPE =
            new CustomPacketPayload.Type<>(new ResourceLocation(IcBreed.MOD_ID, "remove_hat"));

    public static final StreamCodec<FriendlyByteBuf, RemoveHatPayload> CODEC =
            StreamCodec.unit(new RemoveHatPayload());

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
