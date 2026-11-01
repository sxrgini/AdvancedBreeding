package com.iiampanda.icbreed.platform;

import java.util.List;
import java.util.Optional;

import net.minecraft.core.Holder;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

public class DropsModifier extends LootModifier {
    public static final MapCodec<DropsModifier> CODEC = RecordCodecBuilder.mapCodec(i -> codecStart(i).apply(i, DropsModifier::new));

    public DropsModifier(Optional<Holder<LootItemCondition>> conditions, int priority) {
        super(conditions, priority);
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        if (LootTableEvents.MODIFY_DROPS.handlers.isEmpty()) return generatedLoot;
        List<ItemStack> drops = generatedLoot;
        for (LootTableEvents.ModifyDrops cb : LootTableEvents.MODIFY_DROPS.handlers) cb.modifyLootTableDrops(null, context, drops);
        return generatedLoot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
