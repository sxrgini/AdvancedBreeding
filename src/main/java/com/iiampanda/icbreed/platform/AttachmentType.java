package com.iiampanda.icbreed.platform;

import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;

public final class AttachmentType<A> {
    public enum Scope {
        NONE,
        ALL,
        TARGET_ONLY
    }

    final ResourceLocation id;
    Codec<A> codec;
    boolean persistent;
    Scope scope = Scope.NONE;

    AttachmentType(ResourceLocation id) {
        this.id = id;
    }

    public ResourceLocation id() {
        return id;
    }
}
