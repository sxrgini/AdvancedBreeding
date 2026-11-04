package com.iiampanda.icbreed.compat;

import net.neoforged.fml.ModList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public final class Backport {
    public static final String MOD_ID = "vanillabackport";

    private Backport() {
    }

    public static boolean present() {
        return ModList.get().isLoaded(MOD_ID);
    }

    public static TagKey<Item> spearTag() {
        return TagKey.create(Registries.ITEM, ResourceLocation.withDefaultNamespace("spears"));
    }

    public static boolean spears() {
        return BuiltInRegistries.ITEM.containsKey(ResourceLocation.withDefaultNamespace("iron_spear"));
    }

    public static Item spearIcon() {
        Item item = BuiltInRegistries.ITEM.get(ResourceLocation.withDefaultNamespace("iron_spear"));
        return item == Items.AIR ? Items.IRON_SWORD : item;
    }
}
