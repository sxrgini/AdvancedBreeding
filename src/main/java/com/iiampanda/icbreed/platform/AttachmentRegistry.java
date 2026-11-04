package com.iiampanda.icbreed.platform;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;

public final class AttachmentRegistry {
    static final Map<ResourceLocation, AttachmentType<?>> TYPES = new ConcurrentHashMap<>();

    private AttachmentRegistry() {
    }

    public static <A> AttachmentType<A> create(ResourceLocation id) {
        return create(id, b -> { });
    }

    public static <A> AttachmentType<A> create(ResourceLocation id, Consumer<Builder<A>> consumer) {
        AttachmentType<A> type = new AttachmentType<>(id);
        consumer.accept(new Builder<>(type));
        TYPES.put(id, type);
        return type;
    }

    public static final class Builder<A> {
        private final AttachmentType<A> type;

        private Builder(AttachmentType<A> type) {
            this.type = type;
        }

        public Builder<A> persistent(Codec<A> codec) {
            type.codec = codec;
            type.persistent = true;
            return this;
        }

        public Builder<A> syncWith(Codec<A> codec, AttachmentType.Scope scope) {
            if (type.codec == null) type.codec = codec;
            type.scope = scope;
            return this;
        }
    }
}
