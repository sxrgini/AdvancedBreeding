package com.iiampanda.icbreed.platform;

import java.util.function.BiPredicate;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.attachment.IAttachmentHolder;

public final class AttachmentSyncPredicate {
    private AttachmentSyncPredicate() {
    }

    public static BiPredicate<IAttachmentHolder, ServerPlayer> all() {
        return (holder, player) -> true;
    }

    public static BiPredicate<IAttachmentHolder, ServerPlayer> targetOnly() {
        return (holder, player) -> holder == player;
    }
}
