package com.iiampanda.icbreed.platform;

import java.util.List;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;

public final class LootTableEvents {
    public static final Ev<ModifyDrops> MODIFY_DROPS = new Ev<>();

    private LootTableEvents() {
    }

    public interface ModifyDrops {
        void modifyLootTableDrops(LootTable table, LootContext context, List<ItemStack> drops);
    }
}
