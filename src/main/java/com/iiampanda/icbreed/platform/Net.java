package com.iiampanda.icbreed.platform;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

@SuppressWarnings({"unchecked", "rawtypes"})
public final class Net {
    public record ToServer(CustomPacketPayload payload) {
    }

    public record ToClient(CustomPacketPayload payload) {
    }

    public interface ClientBridge {
        void handle(CustomPacketPayload payload, Object handler);
    }

    static final Map<ResourceLocation, StreamCodec> CODECS = new ConcurrentHashMap<>();
    static final Map<ResourceLocation, Boolean> TO_SERVER = new ConcurrentHashMap<>();
    static final Map<ResourceLocation, ServerPlayNetworking.PlayPayloadHandler> SERVER = new ConcurrentHashMap<>();
    static final Map<ResourceLocation, Object> CLIENT = new ConcurrentHashMap<>();
    public static volatile ClientBridge clientBridge;

    static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation("advbreeding", "main"))
            .networkProtocolVersion(() -> "1")
            .clientAcceptedVersions(v -> true)
            .serverAcceptedVersions(v -> true)
            .simpleChannel();

    private Net() {
    }

    public static void init() {
        CHANNEL.registerMessage(0, ToServer.class, (p, b) -> encode(p.payload(), b), b -> new ToServer(decode(b)), Net::handleServer, java.util.Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(1, ToClient.class, (p, b) -> encode(p.payload(), b), b -> new ToClient(decode(b)), Net::handleClient, java.util.Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }

    private static void encode(CustomPacketPayload payload, FriendlyByteBuf buf) {
        ResourceLocation id = payload.type().id();
        buf.writeResourceLocation(id);
        CODECS.get(id).encode(buf, payload);
    }

    private static CustomPacketPayload decode(FriendlyByteBuf buf) {
        ResourceLocation id = buf.readResourceLocation();
        StreamCodec codec = CODECS.get(id);
        return (CustomPacketPayload) codec.decode(buf);
    }

    private static void handleServer(ToServer packet, java.util.function.Supplier<NetworkEvent.Context> sup) {
        NetworkEvent.Context ctx = sup.get();
        ServerPlayer player = ctx.getSender();
        ctx.enqueueWork(() -> {
            ServerPlayNetworking.PlayPayloadHandler handler = SERVER.get(packet.payload().type().id());
            if (handler == null || player == null) return;
            handler.receive(packet.payload(), new ServerPlayNetworking.Context() {
                @Override
                public ServerPlayer player() {
                    return player;
                }

                @Override
                public net.minecraft.server.MinecraftServer server() {
                    return player.getServer();
                }
            });
        });
        ctx.setPacketHandled(true);
    }

    private static void handleClient(ToClient packet, java.util.function.Supplier<NetworkEvent.Context> sup) {
        NetworkEvent.Context ctx = sup.get();
        ctx.enqueueWork(() -> {
            Object handler = CLIENT.get(packet.payload().type().id());
            ClientBridge bridge = clientBridge;
            if (handler != null && bridge != null) bridge.handle(packet.payload(), handler);
        });
        ctx.setPacketHandled(true);
    }
}
