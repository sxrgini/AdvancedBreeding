package com.iiampanda.icbreed.block;

import com.iiampanda.icbreed.IcBreed;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CarpetBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public final class ModBlocks {
    private static final ResourceLocation HAY_CARPET_ID = ResourceLocation.fromNamespaceAndPath(IcBreed.MOD_ID, "hay_carpet");

    public static final Block HAY_CARPET = Registry.register(BuiltInRegistries.BLOCK, HAY_CARPET_ID,
            new CarpetBlock(BlockBehaviour.Properties.of()
                    .strength(0.1F)
                    .sound(SoundType.GRASS)
                    .ignitedByLava()));

    public static final Item HAY_CARPET_ITEM = Registry.register(BuiltInRegistries.ITEM, HAY_CARPET_ID,
            new BlockItem(HAY_CARPET, new Item.Properties()));

    private static final ResourceLocation HONEY_CAULDRON_ID = ResourceLocation.fromNamespaceAndPath(IcBreed.MOD_ID, "honey_cauldron");

    public static final Block HONEY_CAULDRON = Registry.register(BuiltInRegistries.BLOCK, HONEY_CAULDRON_ID,
            new HoneyCauldronBlock(BlockBehaviour.Properties.of()
                    .strength(2.0F)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()));

    private ModBlocks() {
    }

    public static void init() {
    }
}
