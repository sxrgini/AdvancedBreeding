package com.iiampanda.icbreed.mixin;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Mob.class)
public interface MobAccessor {
    @Accessor("goalSelector")
    GoalSelector icbreed$goals();

    @Accessor("targetSelector")
    GoalSelector icbreed$targets();

    @Accessor("persistenceRequired")
    void icbreed$persistent(boolean value);
}
