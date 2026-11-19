package com.iiampanda.icbreed.net;

import com.iiampanda.icbreed.IcBreed;
import net.minecraft.network.FriendlyByteBuf;
import com.iiampanda.icbreed.platform.StreamCodec;
import com.iiampanda.icbreed.platform.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record OpenGuiPayload(int entityId) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<OpenGuiPayload> TYPE =
            new CustomPacketPayload.Type<>(new ResourceLocation(IcBreed.MOD_ID, "open_gui"));

    public static final StreamCodec<FriendlyByteBuf, OpenGuiPayload> CODEC =
            StreamCodec.ofMember(OpenGuiPayload::write, OpenGuiPayload::read);

    private void write(FriendlyByteBuf buf) {
        buf.writeVarInt(entityId);
    }

    private static OpenGuiPayload read(FriendlyByteBuf buf) {
        return new OpenGuiPayload(buf.readVarInt());
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
