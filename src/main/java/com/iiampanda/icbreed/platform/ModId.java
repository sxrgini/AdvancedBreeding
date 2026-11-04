package com.iiampanda.icbreed.platform;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public final class ModId {
    private ModId() {
    }

    public static UUID of(String name) {
        return UUID.nameUUIDFromBytes(("advbreeding:" + name).getBytes(StandardCharsets.UTF_8));
    }
}
