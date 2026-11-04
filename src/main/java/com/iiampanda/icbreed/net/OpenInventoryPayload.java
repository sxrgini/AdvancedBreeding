package com.iiampanda.icbreed.net;

import com.iiampanda.icbreed.IcBreed;
import net.minecraft.network.FriendlyByteBuf;
import com.iiampanda.icbreed.platform.StreamCodec;
import com.iiampanda.icbreed.platform.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record OpenInventoryPayload(int entityId) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<OpenInventoryPayload> TYPE =
            new CustomPacketPayload.Type<>(new ResourceLocation(IcBreed.MOD_ID, "open_inventory"));

    public static final StreamCodec<FriendlyByteBuf, OpenInventoryPayload> CODEC =
            StreamCodec.ofMember(OpenInventoryPayload::write, OpenInventoryPayload::read);

    private void write(FriendlyByteBuf buf) {
        buf.writeVarInt(entityId);
    }

    private static OpenInventoryPayload read(FriendlyByteBuf buf) {
        return new OpenInventoryPayload(buf.readVarInt());
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
