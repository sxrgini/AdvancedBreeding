package com.iiampanda.icbreed.platform;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

public interface UseEntityCallback {
    Ev<UseEntityCallback> EVENT = new Ev<>();

    InteractionResult interact(Player player, Level level, InteractionHand hand, Entity entity, EntityHitResult hit);
}
