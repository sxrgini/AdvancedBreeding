package com.iiampanda.icbreed.net;

import com.iiampanda.icbreed.IcBreed;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record RenamePayload(int entityId, String name) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<RenamePayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(IcBreed.MOD_ID, "rename"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RenamePayload> CODEC =
            StreamCodec.ofMember(RenamePayload::write, RenamePayload::read);

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(entityId);
        buf.writeUtf(name, 64);
    }

    private static RenamePayload read(RegistryFriendlyByteBuf buf) {
        return new RenamePayload(buf.readVarInt(), buf.readUtf(64));
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
