package com.iiampanda.icbreed.platform;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public final class ServerPlayNetworking {
    private ServerPlayNetworking() {
    }

    public interface PlayPayloadHandler<T extends CustomPacketPayload> {
        void receive(T payload, Context context);
    }

    public interface Context {
        ServerPlayer player();

        MinecraftServer server();
    }

    public static <T extends CustomPacketPayload> boolean registerGlobalReceiver(CustomPacketPayload.Type<T> type, PlayPayloadHandler<T> handler) {
        Net.SERVER.put(type.id(), handler);
        return true;
    }

    public static void send(ServerPlayer player, CustomPacketPayload payload) {
        Net.CHANNEL.send(net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> player), new Net.ToClient(payload));
    }
}
