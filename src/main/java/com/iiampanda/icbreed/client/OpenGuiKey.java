package com.iiampanda.icbreed.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;

public final class OpenGuiKey {
    public static final KeyMapping KEY = new KeyMapping("key.advbreeding.open_gui", InputConstants.KEY_G, "key.categories.advbreeding");

    private OpenGuiKey() {
    }
}
