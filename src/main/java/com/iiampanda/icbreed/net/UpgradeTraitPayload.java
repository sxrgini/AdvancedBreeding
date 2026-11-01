package com.iiampanda.icbreed.net;

import com.iiampanda.icbreed.IcBreed;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record UpgradeTraitPayload(int entityId, String traitId) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<UpgradeTraitPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(IcBreed.MOD_ID, "upgrade_trait"));

    public static final StreamCodec<RegistryFriendlyByteBuf, UpgradeTraitPayload> CODEC =
            StreamCodec.ofMember(UpgradeTraitPayload::write, UpgradeTraitPayload::read);

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(entityId);
        buf.writeUtf(traitId, 64);
    }

    private static UpgradeTraitPayload read(RegistryFriendlyByteBuf buf) {
        return new UpgradeTraitPayload(buf.readVarInt(), buf.readUtf(64));
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
