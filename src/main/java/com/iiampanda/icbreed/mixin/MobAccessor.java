package com.iiampanda.icbreed.mixin;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Mob.class)
public interface MobAccessor {
    @Accessor("goalSelector")
    GoalSelector icbreed$goals();

    @org.spongepowered.asm.mixin.gen.Invoker("usePlayerItem")
    void icbreed$use(net.minecraft.world.entity.player.Player player, net.minecraft.world.InteractionHand hand, net.minecraft.world.item.ItemStack stack);

    @Accessor("targetSelector")
    GoalSelector icbreed$targets();
}
