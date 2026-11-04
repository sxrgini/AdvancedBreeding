package com.iiampanda.icbreed.net;

import com.iiampanda.icbreed.IcBreed;
import net.minecraft.network.FriendlyByteBuf;
import com.iiampanda.icbreed.platform.StreamCodec;
import com.iiampanda.icbreed.platform.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record OpenBagsPayload(int entityId) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<OpenBagsPayload> TYPE =
            new CustomPacketPayload.Type<>(new ResourceLocation(IcBreed.MOD_ID, "open_bags"));

    public static final StreamCodec<FriendlyByteBuf, OpenBagsPayload> CODEC =
            StreamCodec.ofMember(OpenBagsPayload::write, OpenBagsPayload::read);

    private void write(FriendlyByteBuf buf) {
        buf.writeVarInt(entityId);
    }

    private static OpenBagsPayload read(FriendlyByteBuf buf) {
        return new OpenBagsPayload(buf.readVarInt());
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
