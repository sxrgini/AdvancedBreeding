package com.iiampanda.icbreed.platform;

import java.util.function.BiPredicate;
import java.util.function.Consumer;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class AttachmentRegistry {
    private static final String NAMESPACE = "advbreeding";
    private static final DeferredRegister<AttachmentType<?>> REGISTER = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, NAMESPACE);

    private AttachmentRegistry() {
    }

    public static void bind(IEventBus bus) {
        REGISTER.register(bus);
    }

    public static <A> AttachmentType<A> create(Identifier id) {
        return create(id, b -> { });
    }

    public static <A> AttachmentType<A> create(Identifier id, Consumer<Builder<A>> consumer) {
        Builder<A> builder = new Builder<>();
        consumer.accept(builder);
        AttachmentType<A> type = builder.inner.build();
        REGISTER.register(id.getPath(), () -> type);
        return type;
    }

    public static final class Builder<A> {
        private final AttachmentType.Builder<A> inner = AttachmentType.builder(() -> (A) null);

        public Builder<A> persistent(Codec<A> codec) {
            inner.serialize(codec.fieldOf("v"));
            return this;
        }

        public Builder<A> syncWith(StreamCodec<? super RegistryFriendlyByteBuf, A> codec, BiPredicate<IAttachmentHolder, ServerPlayer> predicate) {
            inner.sync(predicate, codec);
            return this;
        }
    }
}
