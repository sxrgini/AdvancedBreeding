package com.iiampanda.icbreed.platform;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record AttSyncPayload(int entityId, ResourceLocation typeId, CompoundTag data) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<AttSyncPayload> TYPE =
            new CustomPacketPayload.Type<>(new ResourceLocation("advbreeding", "att_sync"));

    public static final StreamCodec<FriendlyByteBuf, AttSyncPayload> CODEC =
            StreamCodec.ofMember(AttSyncPayload::write, AttSyncPayload::read);

    private void write(FriendlyByteBuf buf) {
        buf.writeVarInt(entityId);
        buf.writeResourceLocation(typeId);
        buf.writeBoolean(data != null);
        if (data != null) buf.writeNbt(data);
    }

    private static AttSyncPayload read(FriendlyByteBuf buf) {
        int id = buf.readVarInt();
        ResourceLocation type = buf.readResourceLocation();
        CompoundTag data = buf.readBoolean() ? buf.readNbt() : null;
        return new AttSyncPayload(id, type, data);
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
