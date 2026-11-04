package com.iiampanda.icbreed.traits;

import java.util.List;

import com.iiampanda.icbreed.genetics.Stat;
import net.minecraft.world.item.Item;

public record TraitDef(String id, int maxRank, String parent, String parent2, int col, int row, Item icon, List<Mod> mods, int cost, String name, String description) {
    public TraitDef(String id, int maxRank, String parent, String parent2, int col, int row, Item icon, List<Mod> mods, int cost) {
        this(id, maxRank, parent, parent2, col, row, icon, mods, cost, null, null);
    }

    public TraitDef(String id, int maxRank, String parent, String parent2, int col, int row, Item icon, List<Mod> mods) {
        this(id, maxRank, parent, parent2, col, row, icon, mods, 1, null, null);
    }

    public net.minecraft.network.chat.Component displayName() {
        return net.minecraft.network.chat.Component.translatableWithFallback(nameKey(), name != null ? name : id);
    }

    public net.minecraft.network.chat.Component descriptionComponent() {
        if (description != null) return net.minecraft.network.chat.Component.translatableWithFallback(nameKey() + ".desc", description);
        if (net.minecraft.locale.Language.getInstance().has(nameKey() + ".desc")) return net.minecraft.network.chat.Component.translatable(nameKey() + ".desc");
        return null;
    }

    public net.minecraft.world.item.ItemStack iconStack() {
        if (id.equals("rip_and_tear")) {
            return net.minecraft.world.item.alchemy.PotionUtils.setPotion(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.POTION), net.minecraft.world.item.alchemy.Potions.POISON);
        }
        if (id.equals("fire_resistance_aura")) {
            return net.minecraft.world.item.alchemy.PotionUtils.setPotion(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.POTION), net.minecraft.world.item.alchemy.Potions.FIRE_RESISTANCE);
        }
        if (id.equals("water_breather")) {
            return net.minecraft.world.item.alchemy.PotionUtils.setPotion(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.POTION), net.minecraft.world.item.alchemy.Potions.WATER_BREATHING);
        }
        if (id.equals("underwater_vision")) {
            return net.minecraft.world.item.alchemy.PotionUtils.setPotion(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.POTION), net.minecraft.world.item.alchemy.Potions.NIGHT_VISION);
        }
        if (id.equals("regen_aura")) {
            return net.minecraft.world.item.alchemy.PotionUtils.setPotion(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.POTION), net.minecraft.world.item.alchemy.Potions.REGENERATION);
        }
        return new net.minecraft.world.item.ItemStack(icon);
    }

    public String missing(java.util.function.ToIntFunction<String> rank) {
        if (parent != null && rank.applyAsInt(parent) <= 0) return parent;
        if (parent2 != null && rank.applyAsInt(parent2) <= 0) return parent2;
        return null;
    }

    public record Mod(Stat stat, double perRank) {
    }

    public String nameKey() {
        return "advbreeding.trait." + id;
    }
}
