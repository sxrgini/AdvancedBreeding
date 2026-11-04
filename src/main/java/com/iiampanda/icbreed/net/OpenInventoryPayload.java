package com.iiampanda.icbreed.net;

import com.iiampanda.icbreed.IcBreed;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record OpenInventoryPayload(int entityId) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<OpenInventoryPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(IcBreed.MOD_ID, "open_inventory"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenInventoryPayload> CODEC =
            StreamCodec.ofMember(OpenInventoryPayload::write, OpenInventoryPayload::read);

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(entityId);
    }

    private static OpenInventoryPayload read(RegistryFriendlyByteBuf buf) {
        return new OpenInventoryPayload(buf.readVarInt());
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
