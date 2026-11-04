package com.iiampanda.icbreed.net;

import com.iiampanda.icbreed.IcBreed;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record RulesSyncPayload(String json) implements CustomPacketPayload {
    private static final int MAX = 900000;

    public static final CustomPacketPayload.Type<RulesSyncPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(IcBreed.MOD_ID, "rules_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RulesSyncPayload> CODEC =
            StreamCodec.ofMember(RulesSyncPayload::write, RulesSyncPayload::read);

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeUtf(json, MAX);
    }

    private static RulesSyncPayload read(RegistryFriendlyByteBuf buf) {
        return new RulesSyncPayload(buf.readUtf(MAX));
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
