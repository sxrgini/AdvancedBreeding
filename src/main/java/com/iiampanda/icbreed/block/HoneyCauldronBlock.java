package com.iiampanda.icbreed.block;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class HoneyCauldronBlock extends Block {
    public HoneyCauldronBlock(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Blocks.CAULDRON.defaultBlockState().getShape(level, pos, context);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide()) {
            level.setBlock(pos, Blocks.CAULDRON.defaultBlockState(), 3);
            ItemStack stack = new ItemStack(Items.HONEY_BLOCK);
            if (!player.getInventory().add(stack)) player.drop(stack, false);
            level.playSound(null, pos, SoundEvents.HONEY_BLOCK_BREAK, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        return InteractionResult.SUCCESS;
    }
}
