package com.iiampanda.icbreed.platform;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public final class ServerPlayConnectionEvents {
    public static final Ev<Disconnect> DISCONNECT = new Ev<>();

    private ServerPlayConnectionEvents() {
    }

    public interface Disconnect {
        void onPlayDisconnect(ServerGamePacketListenerImpl handler, MinecraftServer server);
    }
}
