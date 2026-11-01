package com.iiampanda.icbreed.platform;

import net.minecraft.server.MinecraftServer;

public final class ServerLifecycleEvents {
    public static final Ev<Stopping> SERVER_STOPPING = new Ev<>();

    private ServerLifecycleEvents() {
    }

    public interface Stopping {
        void onServerStopping(MinecraftServer server);
    }
}
