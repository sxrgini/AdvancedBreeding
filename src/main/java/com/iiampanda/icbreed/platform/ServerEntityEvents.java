package com.iiampanda.icbreed.platform;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

public final class ServerEntityEvents {
    public static final Ev<Load> ENTITY_LOAD = new Ev<>();

    private ServerEntityEvents() {
    }

    public interface Load {
        void onLoad(Entity entity, ServerLevel world);
    }
}
