package com.iiampanda.icbreed.traits;

import com.iiampanda.icbreed.block.ModBlocks;
import com.iiampanda.icbreed.genetics.Genetics;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.PointedDripstoneBlock;

public final class HoneyDrip {
    public static final int SECONDS = 45;

    private HoneyDrip() {
    }

    private static boolean dripstone(BlockState s) {
        if (s.is(Blocks.DRIPSTONE_BLOCK)) return true;
        return s.is(Blocks.POINTED_DRIPSTONE) && s.getValue(PointedDripstoneBlock.TIP_DIRECTION) == Direction.DOWN;
    }

    public static void tick(Level level, BlockPos pos, BeehiveBlockEntity hive) {
        if (!(level instanceof ServerLevel sl)) return;
        BlockPos stone = pos.below();
        BlockPos cauldron = stone.below();
        if (!dripstone(level.getBlockState(stone)) || !level.getBlockState(cauldron).is(Blocks.CAULDRON)) {
            if (hive.hasAttached(Genetics.HONEY_PROGRESS)) hive.removeAttached(Genetics.HONEY_PROGRESS);
            return;
        }
        int progress = hive.getAttachedOrElse(Genetics.HONEY_PROGRESS, 0) + 1;
        sl.sendParticles(ParticleTypes.FALLING_HONEY, stone.getX() + 0.5, stone.getY() + 0.1, stone.getZ() + 0.5, 2, 0.1, 0.0, 0.1, 0.0);
        if (progress < SECONDS) {
            hive.setAttached(Genetics.HONEY_PROGRESS, progress);
            return;
        }
        hive.removeAttached(Genetics.HONEY_PROGRESS);
        level.setBlock(cauldron, ModBlocks.HONEY_CAULDRON.defaultBlockState(), 3);
        level.playSound(null, cauldron, SoundEvents.BEEHIVE_EXIT, SoundSource.BLOCKS, 1.0F, 1.0F);
    }
}
