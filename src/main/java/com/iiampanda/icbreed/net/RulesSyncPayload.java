package com.iiampanda.icbreed.net;

import com.iiampanda.icbreed.IcBreed;
import net.minecraft.network.FriendlyByteBuf;
import com.iiampanda.icbreed.platform.StreamCodec;
import com.iiampanda.icbreed.platform.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record RulesSyncPayload(String json) implements CustomPacketPayload {
    private static final int MAX = 900000;

    public static final CustomPacketPayload.Type<RulesSyncPayload> TYPE =
            new CustomPacketPayload.Type<>(new ResourceLocation(IcBreed.MOD_ID, "rules_sync"));

    public static final StreamCodec<FriendlyByteBuf, RulesSyncPayload> CODEC =
            StreamCodec.ofMember(RulesSyncPayload::write, RulesSyncPayload::read);

    private void write(FriendlyByteBuf buf) {
        buf.writeUtf(json, MAX);
    }

    private static RulesSyncPayload read(FriendlyByteBuf buf) {
        return new RulesSyncPayload(buf.readUtf(MAX));
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
