package com.iiampanda.icbreed.platform;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@SuppressWarnings({"unchecked", "rawtypes"})
public final class Net {
    public record Registration(CustomPacketPayload.Type type, StreamCodec codec, boolean toServer) {
    }

    public interface ClientBridge {
        void handle(CustomPacketPayload payload, Object handler);
    }

    static final List<Registration> REGISTRATIONS = new CopyOnWriteArrayList<>();
    static final Map<CustomPacketPayload.Type, ServerPlayNetworking.PlayPayloadHandler> SERVER = new ConcurrentHashMap<>();
    static final Map<CustomPacketPayload.Type, Object> CLIENT = new ConcurrentHashMap<>();
    public static volatile ClientBridge clientBridge;

    private Net() {
    }

    public static void onRegister(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        for (Registration r : REGISTRATIONS) {
            if (r.toServer()) {
                registrar.playToServer(r.type(), r.codec(), (payload, ctx) -> {
                    ServerPlayNetworking.PlayPayloadHandler handler = SERVER.get(r.type());
                    if (handler == null || !(ctx.player() instanceof ServerPlayer player)) return;
                    handler.receive(payload, new ServerPlayNetworking.Context() {
                        @Override
                        public ServerPlayer player() {
                            return player;
                        }

                        @Override
                        public net.minecraft.server.MinecraftServer server() {
                            return player.level().getServer();
                        }
                    });
                });
            } else {
                registrar.playToClient(r.type(), r.codec(), (payload, ctx) -> {
                    Object handler = CLIENT.get(r.type());
                    ClientBridge bridge = clientBridge;
                    if (handler != null && bridge != null) bridge.handle(payload, handler);
                });
            }
        }
    }
}
