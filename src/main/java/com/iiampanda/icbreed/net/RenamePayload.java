package com.iiampanda.icbreed.net;

import com.iiampanda.icbreed.IcBreed;
import net.minecraft.network.FriendlyByteBuf;
import com.iiampanda.icbreed.platform.StreamCodec;
import com.iiampanda.icbreed.platform.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record RenamePayload(int entityId, String name) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<RenamePayload> TYPE =
            new CustomPacketPayload.Type<>(new ResourceLocation(IcBreed.MOD_ID, "rename"));

    public static final StreamCodec<FriendlyByteBuf, RenamePayload> CODEC =
            StreamCodec.ofMember(RenamePayload::write, RenamePayload::read);

    private void write(FriendlyByteBuf buf) {
        buf.writeVarInt(entityId);
        buf.writeUtf(name, 64);
    }

    private static RenamePayload read(FriendlyByteBuf buf) {
        return new RenamePayload(buf.readVarInt(), buf.readUtf(64));
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
