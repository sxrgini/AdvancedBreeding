package com.iiampanda.icbreed.net;

import com.iiampanda.icbreed.IcBreed;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record OpenBagsPayload(int entityId) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<OpenBagsPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(IcBreed.MOD_ID, "open_bags"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenBagsPayload> CODEC =
            StreamCodec.ofMember(OpenBagsPayload::write, OpenBagsPayload::read);

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(entityId);
    }

    private static OpenBagsPayload read(RegistryFriendlyByteBuf buf) {
        return new OpenBagsPayload(buf.readVarInt());
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
