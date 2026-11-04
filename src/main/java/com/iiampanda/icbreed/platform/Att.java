package com.iiampanda.icbreed.platform;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

@SuppressWarnings("unchecked")
public final class Att {
    private Att() {
    }

    public static <T> T get(Object holder, AttachmentType<T> type) {
        return (T) ((AttStore) holder).icbreed$att().get(type);
    }

    public static <T> T getOrElse(Object holder, AttachmentType<T> type, T fallback) {
        T v = get(holder, type);
        return v == null ? fallback : v;
    }

    public static <T> void set(Object holder, AttachmentType<T> type, T value) {
        if (value == null) {
            remove(holder, type);
            return;
        }
        ((AttStore) holder).icbreed$att().put(type, value);
        AttSync.push(holder, type, value);
    }

    public static boolean has(Object holder, AttachmentType<?> type) {
        return ((AttStore) holder).icbreed$att().containsKey(type);
    }

    public static <T> T remove(Object holder, AttachmentType<T> type) {
        T old = (T) ((AttStore) holder).icbreed$att().remove(type);
        if (old != null) AttSync.push(holder, type, null);
        return old;
    }
}
