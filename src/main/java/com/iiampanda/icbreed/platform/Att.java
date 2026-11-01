package com.iiampanda.icbreed.platform;

import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;

public final class Att {
    private Att() {
    }

    public static <T> T get(Object holder, AttachmentType<T> type) {
        return ((IAttachmentHolder) holder).getExistingDataOrNull(type);
    }

    public static <T> T getOrElse(Object holder, AttachmentType<T> type, T fallback) {
        T v = ((IAttachmentHolder) holder).getExistingDataOrNull(type);
        return v == null ? fallback : v;
    }

    public static <T> void set(Object holder, AttachmentType<T> type, T value) {
        IAttachmentHolder h = (IAttachmentHolder) holder;
        if (value == null) h.removeData(type);
        else h.setData(type, value);
    }

    public static boolean has(Object holder, AttachmentType<?> type) {
        return ((IAttachmentHolder) holder).hasData(type);
    }

    public static <T> T remove(Object holder, AttachmentType<T> type) {
        IAttachmentHolder h = (IAttachmentHolder) holder;
        if (!h.hasData(type)) return null;
        return h.removeData(type);
    }
}
