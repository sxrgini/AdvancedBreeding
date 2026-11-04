package com.iiampanda.icbreed.block;

import com.iiampanda.icbreed.IcBreed;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CarpetBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.RegisterEvent;

public final class ModBlocks {
    private static final ResourceLocation HAY_CARPET_ID = new ResourceLocation(IcBreed.MOD_ID, "hay_carpet");
    private static final ResourceLocation HONEY_CAULDRON_ID = new ResourceLocation(IcBreed.MOD_ID, "honey_cauldron");

    public static final Block HAY_CARPET = new CarpetBlock(BlockBehaviour.Properties.of()
            .strength(0.1F)
            .sound(SoundType.GRASS));

    public static final Item HAY_CARPET_ITEM = new BlockItem(HAY_CARPET, new Item.Properties());

    public static final Block HONEY_CAULDRON = new HoneyCauldronBlock(BlockBehaviour.Properties.of()
            .strength(2.0F)
            .requiresCorrectToolForDrops()
            .noOcclusion());

    private ModBlocks() {
    }

    public static void register(RegisterEvent event) {
        event.register(Registries.BLOCK, helper -> {
            helper.register(HAY_CARPET_ID, HAY_CARPET);
            helper.register(HONEY_CAULDRON_ID, HONEY_CAULDRON);
        });
        event.register(Registries.ITEM, helper -> helper.register(HAY_CARPET_ID, HAY_CARPET_ITEM));
    }

    public static void init() {
    }
}
