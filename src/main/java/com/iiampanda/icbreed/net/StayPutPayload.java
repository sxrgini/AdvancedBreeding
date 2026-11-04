package com.iiampanda.icbreed.net;

import com.iiampanda.icbreed.IcBreed;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record StayPutPayload(int entityId) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<StayPutPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(IcBreed.MOD_ID, "stay_put"));

    public static final StreamCodec<RegistryFriendlyByteBuf, StayPutPayload> CODEC =
            StreamCodec.ofMember(StayPutPayload::write, StayPutPayload::read);

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(entityId);
    }

    private static StayPutPayload read(RegistryFriendlyByteBuf buf) {
        return new StayPutPayload(buf.readVarInt());
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
