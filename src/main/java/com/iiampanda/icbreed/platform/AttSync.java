package com.iiampanda.icbreed.platform;

import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

@SuppressWarnings({"unchecked", "rawtypes"})
public final class AttSync {
    private AttSync() {
    }

    public static void init() {
        PayloadTypeRegistry.playS2C().register(AttSyncPayload.TYPE, AttSyncPayload.CODEC);
    }

    public static void initClient() {
        ClientPlayNetworking.registerGlobalReceiver(AttSyncPayload.TYPE, (payload, ctx) -> {
            Minecraft mc = ctx.client();
            if (mc.level == null) return;
            Entity entity = mc.level.getEntity(payload.entityId());
            if (!(entity instanceof AttStore store)) return;
            AttachmentType type = AttachmentRegistry.TYPES.get(payload.typeId());
            if (type == null || type.codec == null) return;
            if (payload.data() == null) {
                store.icbreed$att().remove(type);
                return;
            }
            Object value = AttPersist.decode(type, payload.data().get("v"));
            if (value != null) store.icbreed$att().put(type, value);
        });
    }

    public static void push(Object holder, AttachmentType type, Object value) {
        if (type.scope == AttachmentType.Scope.NONE || type.codec == null) return;
        if (!(holder instanceof Entity entity) || entity.level().isClientSide()) return;
        AttSyncPayload payload = payload(entity, type, value);
        if (payload == null) return;
        if (type.scope == AttachmentType.Scope.TARGET_ONLY) {
            if (entity instanceof ServerPlayer player) ServerPlayNetworking.send(player, payload);
        } else {
            Net.CHANNEL.send(net.minecraftforge.network.PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity), new Net.ToClient(payload));
        }
    }

    private static AttSyncPayload payload(Entity entity, AttachmentType type, Object value) {
        if (value == null) return new AttSyncPayload(entity.getId(), type.id, null);
        Tag tag = AttPersist.encode(type, value);
        if (tag == null) return null;
        CompoundTag wrapper = new CompoundTag();
        wrapper.put("v", tag);
        return new AttSyncPayload(entity.getId(), type.id, wrapper);
    }

    public static void sendTracked(ServerPlayer viewer, Entity target) {
        if (!(target instanceof AttStore store)) return;
        for (Map.Entry<AttachmentType<?>, Object> e : store.icbreed$att().entrySet()) {
            AttachmentType type = e.getKey();
            if (type.scope == AttachmentType.Scope.NONE) continue;
            if (type.scope == AttachmentType.Scope.TARGET_ONLY && target != viewer) continue;
            AttSyncPayload p = payload(target, type, e.getValue());
            if (p != null) ServerPlayNetworking.send(viewer, p);
        }
    }
}
