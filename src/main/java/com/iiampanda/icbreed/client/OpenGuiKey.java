package com.iiampanda.icbreed.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;

public final class OpenGuiKey {
    public static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(net.minecraft.resources.Identifier.fromNamespaceAndPath(com.iiampanda.icbreed.IcBreed.MOD_ID, "keys"));
    public static final KeyMapping KEY = new KeyMapping("key.advbreeding.open_gui", InputConstants.KEY_G, CATEGORY);

    private OpenGuiKey() {
    }
}
