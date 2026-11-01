package com.iiampanda.icbreed.net;

import com.iiampanda.icbreed.IcBreed;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record StayStatePayload(int entityId, int mode, boolean canChange) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<StayStatePayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(IcBreed.MOD_ID, "stay_state"));

    public static final StreamCodec<RegistryFriendlyByteBuf, StayStatePayload> CODEC =
            StreamCodec.ofMember(StayStatePayload::write, StayStatePayload::read);

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(entityId);
        buf.writeVarInt(mode);
        buf.writeBoolean(canChange);
    }

    private static StayStatePayload read(RegistryFriendlyByteBuf buf) {
        return new StayStatePayload(buf.readVarInt(), buf.readVarInt(), buf.readBoolean());
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
