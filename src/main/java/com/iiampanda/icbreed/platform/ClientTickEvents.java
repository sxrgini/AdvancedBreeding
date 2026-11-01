package com.iiampanda.icbreed.platform;

import net.minecraft.client.Minecraft;

public final class ClientTickEvents {
    public static final Ev<EndTick> END_CLIENT_TICK = new Ev<>();

    private ClientTickEvents() {
    }

    public interface EndTick {
        void onEndTick(Minecraft client);
    }
}
