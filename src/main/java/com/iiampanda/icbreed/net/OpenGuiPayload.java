package com.iiampanda.icbreed.net;

import com.iiampanda.icbreed.IcBreed;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record OpenGuiPayload(int entityId) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<OpenGuiPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(IcBreed.MOD_ID, "open_gui"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenGuiPayload> CODEC =
            StreamCodec.ofMember(OpenGuiPayload::write, OpenGuiPayload::read);

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(entityId);
    }

    private static OpenGuiPayload read(RegistryFriendlyByteBuf buf) {
        return new OpenGuiPayload(buf.readVarInt());
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
