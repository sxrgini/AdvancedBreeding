package com.iiampanda.icbreed.net;

import com.iiampanda.icbreed.IcBreed;
import net.minecraft.network.FriendlyByteBuf;
import com.iiampanda.icbreed.platform.StreamCodec;
import com.iiampanda.icbreed.platform.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record UpgradeTraitPayload(int entityId, String traitId) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<UpgradeTraitPayload> TYPE =
            new CustomPacketPayload.Type<>(new ResourceLocation(IcBreed.MOD_ID, "upgrade_trait"));

    public static final StreamCodec<FriendlyByteBuf, UpgradeTraitPayload> CODEC =
            StreamCodec.ofMember(UpgradeTraitPayload::write, UpgradeTraitPayload::read);

    private void write(FriendlyByteBuf buf) {
        buf.writeVarInt(entityId);
        buf.writeUtf(traitId, 64);
    }

    private static UpgradeTraitPayload read(FriendlyByteBuf buf) {
        return new UpgradeTraitPayload(buf.readVarInt(), buf.readUtf(64));
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
