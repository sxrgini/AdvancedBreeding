package com.iiampanda.icbreed;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;

public final class NameHelper {
    public static final ThreadLocal<Boolean> RAW = ThreadLocal.withInitial(() -> Boolean.FALSE);

    private NameHelper() {
    }

    public static Component rawName(Entity entity) {
        RAW.set(Boolean.TRUE);
        try {
            return entity.hasCustomName() ? entity.getCustomName() : entity.getType().getDescription();
        } finally {
            RAW.set(Boolean.FALSE);
        }
    }
}
