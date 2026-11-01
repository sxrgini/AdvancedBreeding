package com.iiampanda.icbreed.platform;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

public final class ServerTickEvents {
    public static final Ev<EndTick> END_SERVER_TICK = new Ev<>();
    public static final Ev<EndLevelTick> END_LEVEL_TICK = new Ev<>();

    private ServerTickEvents() {
    }

    public interface EndTick {
        void onEndTick(MinecraftServer server);
    }

    public interface EndLevelTick {
        void onEndTick(ServerLevel level);
    }
}
