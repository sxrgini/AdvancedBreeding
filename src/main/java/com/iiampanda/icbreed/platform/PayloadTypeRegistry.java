package com.iiampanda.icbreed.platform;

import net.minecraft.network.FriendlyByteBuf;

public final class PayloadTypeRegistry {
    private static final PayloadTypeRegistry CLIENTBOUND = new PayloadTypeRegistry(false);
    private static final PayloadTypeRegistry SERVERBOUND = new PayloadTypeRegistry(true);

    private final boolean toServer;

    private PayloadTypeRegistry(boolean toServer) {
        this.toServer = toServer;
    }

    public static PayloadTypeRegistry playS2C() {
        return CLIENTBOUND;
    }

    public static PayloadTypeRegistry playC2S() {
        return SERVERBOUND;
    }

    public <T extends CustomPacketPayload> void register(CustomPacketPayload.Type<T> type, StreamCodec<? super FriendlyByteBuf, T> codec) {
        Net.CODECS.put(type.id(), codec);
        Net.TO_SERVER.put(type.id(), toServer);
    }
}
