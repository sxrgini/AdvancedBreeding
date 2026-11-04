package com.iiampanda.icbreed.platform;

public final class AttachmentSyncPredicate {
    private AttachmentSyncPredicate() {
    }

    public static AttachmentType.Scope all() {
        return AttachmentType.Scope.ALL;
    }

    public static AttachmentType.Scope targetOnly() {
        return AttachmentType.Scope.TARGET_ONLY;
    }
}
