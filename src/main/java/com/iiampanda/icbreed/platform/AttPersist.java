package com.iiampanda.icbreed.platform;

import java.util.Map;

import com.mojang.serialization.DataResult;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

@SuppressWarnings({"unchecked", "rawtypes"})
public final class AttPersist {
    private static final String KEY = "advbreeding_att";

    private AttPersist() {
    }

    public static void write(Map<AttachmentType<?>, Object> map, CompoundTag out) {
        if (map.isEmpty()) return;
        CompoundTag tag = new CompoundTag();
        for (Map.Entry<AttachmentType<?>, Object> e : map.entrySet()) {
            AttachmentType type = e.getKey();
            if (!type.persistent || type.codec == null) continue;
            DataResult<Tag> r = type.codec.encodeStart(NbtOps.INSTANCE, e.getValue());
            r.result().ifPresent(t -> tag.put(type.id.toString(), t));
        }
        if (!tag.isEmpty()) out.put(KEY, tag);
    }

    public static void read(Map<AttachmentType<?>, Object> map, CompoundTag in) {
        if (!in.contains(KEY, 10)) return;
        CompoundTag tag = in.getCompound(KEY);
        for (String key : tag.getAllKeys()) {
            ResourceLocation id = ResourceLocation.tryParse(key);
            AttachmentType type = id == null ? null : AttachmentRegistry.TYPES.get(id);
            if (type == null || type.codec == null) continue;
            DataResult<?> r = type.codec.parse(NbtOps.INSTANCE, tag.get(key));
            r.result().ifPresent(v -> map.put(type, v));
        }
    }

    public static Tag encode(AttachmentType type, Object value) {
        return (Tag) type.codec.encodeStart(NbtOps.INSTANCE, value).result().orElse(null);
    }

    public static Object decode(AttachmentType type, Tag tag) {
        return type.codec.parse(NbtOps.INSTANCE, tag).result().orElse(null);
    }
}
