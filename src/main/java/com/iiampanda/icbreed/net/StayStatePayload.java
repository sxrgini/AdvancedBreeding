package com.iiampanda.icbreed.net;

import com.iiampanda.icbreed.IcBreed;
import net.minecraft.network.FriendlyByteBuf;
import com.iiampanda.icbreed.platform.StreamCodec;
import com.iiampanda.icbreed.platform.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record StayStatePayload(int entityId, int mode, boolean canChange) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<StayStatePayload> TYPE =
            new CustomPacketPayload.Type<>(new ResourceLocation(IcBreed.MOD_ID, "stay_state"));

    public static final StreamCodec<FriendlyByteBuf, StayStatePayload> CODEC =
            StreamCodec.ofMember(StayStatePayload::write, StayStatePayload::read);

    private void write(FriendlyByteBuf buf) {
        buf.writeVarInt(entityId);
        buf.writeVarInt(mode);
        buf.writeBoolean(canChange);
    }

    private static StayStatePayload read(FriendlyByteBuf buf) {
        return new StayStatePayload(buf.readVarInt(), buf.readVarInt(), buf.readBoolean());
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
