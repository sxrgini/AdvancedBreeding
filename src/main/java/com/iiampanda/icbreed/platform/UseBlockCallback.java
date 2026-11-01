package com.iiampanda.icbreed.platform;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

public interface UseBlockCallback {
    Ev<UseBlockCallback> EVENT = new Ev<>();

    InteractionResult interact(Player player, Level level, InteractionHand hand, BlockHitResult hit);
}
