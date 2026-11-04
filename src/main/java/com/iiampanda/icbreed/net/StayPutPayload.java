package com.iiampanda.icbreed.net;

import com.iiampanda.icbreed.IcBreed;
import net.minecraft.network.FriendlyByteBuf;
import com.iiampanda.icbreed.platform.StreamCodec;
import com.iiampanda.icbreed.platform.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record StayPutPayload(int entityId) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<StayPutPayload> TYPE =
            new CustomPacketPayload.Type<>(new ResourceLocation(IcBreed.MOD_ID, "stay_put"));

    public static final StreamCodec<FriendlyByteBuf, StayPutPayload> CODEC =
            StreamCodec.ofMember(StayPutPayload::write, StayPutPayload::read);

    private void write(FriendlyByteBuf buf) {
        buf.writeVarInt(entityId);
    }

    private static StayPutPayload read(FriendlyByteBuf buf) {
        return new StayPutPayload(buf.readVarInt());
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
