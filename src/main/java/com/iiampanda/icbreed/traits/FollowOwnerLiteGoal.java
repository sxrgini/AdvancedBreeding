package com.iiampanda.icbreed.traits;

import java.util.EnumSet;

import com.iiampanda.icbreed.genetics.Genetics;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;

public class FollowOwnerLiteGoal extends Goal {
    private final PathfinderMob mob;
    private Player owner;
    private int recalc;

    private static final java.util.Set<PathfinderMob> ATTACHED = java.util.Collections.newSetFromMap(new java.util.WeakHashMap<>());

    public static void attach(PathfinderMob mob) {
        if (ATTACHED.add(mob)) ((com.iiampanda.icbreed.mixin.MobAccessor) (Object) mob).icbreed$goals().addGoal(5, new FollowOwnerLiteGoal(mob));
    }

    public FollowOwnerLiteGoal(PathfinderMob mob) {
        this.mob = mob;
        setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (!Genetics.isTamed(mob) || !StayPut.following(mob) || AttackTames.sitting(mob) || mob.getTarget() != null || mob.isLeashed() || mob.isVehicle()) return false;
        Player p = StayPut.ownerPlayer(mob);
        if (p == null || p.isSpectator() || mob.distanceToSqr(p) < 7.0 * 7.0) return false;
        owner = p;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return owner != null && owner.isAlive() && StayPut.following(mob) && !AttackTames.sitting(mob) && mob.getTarget() == null
                && mob.distanceToSqr(owner) > 3.0 * 3.0;
    }

    @Override
    public void start() {
        recalc = 0;
    }

    @Override
    public void stop() {
        mob.getNavigation().stop();
        owner = null;
    }

    @Override
    public void tick() {
        mob.getLookControl().setLookAt(owner, 10.0F, mob.getMaxHeadXRot());
        if (--recalc > 0) return;
        recalc = 10;
        if (mob.distanceToSqr(owner) > 32.0 * 32.0 && owner.onGround()) {
            mob.teleportTo(owner.getX(), owner.getY(), owner.getZ());
            mob.getNavigation().stop();
        } else {
            mob.getNavigation().moveTo(owner, 1.2);
        }
    }
}
