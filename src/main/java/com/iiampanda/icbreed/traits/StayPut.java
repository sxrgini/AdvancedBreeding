package com.iiampanda.icbreed.traits;

import com.iiampanda.icbreed.genetics.Genetics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.camel.Camel;
import net.minecraft.world.entity.animal.fox.Fox;
import net.minecraft.world.entity.animal.panda.Panda;
import net.minecraft.world.entity.player.Player;
import java.util.UUID;

public final class StayPut {
    public static final ThreadLocal<Boolean> ALLOW = ThreadLocal.withInitial(() -> false);

    private StayPut() {
    }

    public static boolean holding(net.minecraft.world.entity.LivingEntity e) {
        return e instanceof Mob m && !m.isVehicle() && Boolean.TRUE.equals(m.getAttached(Genetics.SITTING));
    }

    public static boolean can(Mob mob, Player player) {
        return Genetics.supports(mob) && Genetics.isTamed(mob);
    }

    public enum Mode { WANDER, STAY, FOLLOW }

    private static final java.util.Set<Mob> GOALED = java.util.Collections.newSetFromMap(new java.util.WeakHashMap<>());

    public static boolean staying(Mob mob) {
        if (mob instanceof TamableAnimal t && t.isOrderedToSit()) return true;
        return AttackTames.sitting(mob);
    }

    public static boolean following(Mob mob) {
        return Boolean.TRUE.equals(mob.getAttached(Genetics.FOLLOWING));
    }

    public static Mode mode(Mob mob) {
        if (staying(mob)) return Mode.STAY;
        return following(mob) ? Mode.FOLLOW : Mode.WANDER;
    }

    public static boolean isOwner(Mob mob, Player player) {
        if (mob instanceof TamableAnimal t && t.isTame() && t.getOwnerReference() != null) return t.isOwnedBy(player);
        com.iiampanda.icbreed.genetics.Taming tm = mob.getAttached(Genetics.TAMING);
        if (tm == null) return false;
        return tm.owner().isEmpty() || tm.owner().get().equals(player.getUUID());
    }

    public static Player ownerPlayer(Mob mob) {
        if (mob instanceof TamableAnimal t && t.getOwner() instanceof Player p) return p;
        com.iiampanda.icbreed.genetics.Taming tm = mob.getAttached(Genetics.TAMING);
        if (tm == null || tm.owner().isEmpty()) return null;
        return mob.level().getPlayerByUUID(tm.owner().get());
    }

    public static void setMode(Mob mob, Mode mode, Player who) {
        if (who != null) {
            com.iiampanda.icbreed.genetics.Taming tm = mob.getAttached(Genetics.TAMING);
            if (tm != null && tm.owner().isEmpty()) mob.setAttached(Genetics.TAMING, tm.withOwner(who.getUUID()));
        }
        boolean wasStaying = staying(mob);
        mob.removeAttached(Genetics.FOLLOWING);
        if (mode == Mode.STAY) {
            mob.setAttached(Genetics.SITTING, true);
            if (mob instanceof PathfinderMob pm) pm.getNavigation().stop();
            pose(mob, true);
            return;
        }
        if (wasStaying) {
            mob.removeAttached(Genetics.SITTING);
            pose(mob, false);
        }
        if (mode == Mode.FOLLOW) {
            mob.setAttached(Genetics.FOLLOWING, true);
            if (mob instanceof PathfinderMob pm && !(mob instanceof TamableAnimal)) FollowOwnerLiteGoal.attach(pm);
        }
    }

    public static Mode cycle(Mob mob, Player who) {
        Mode next = Mode.values()[(mode(mob).ordinal() + 1) % Mode.values().length];
        setMode(mob, next, who);
        return next;
    }

    public static boolean toggle(Mob mob) {
        boolean now = !staying(mob);
        setMode(mob, now ? Mode.STAY : Mode.WANDER, null);
        return now;
    }

    public static void release(Mob mob) {
        if (staying(mob)) setMode(mob, Mode.WANDER, null);
    }

    public static void followUpkeep(Mob mob) {
        if (!following(mob) || mob instanceof TamableAnimal || mob.isLeashed() || mob.isVehicle()) return;
        Player owner = ownerPlayer(mob);
        if (owner == null || owner.isSpectator() || staying(mob) || mob.level() != owner.level()) return;
        if (mob instanceof PathfinderMob pm && !((com.iiampanda.icbreed.mixin.MobAccessor) (Object) mob).icbreed$goals().getAvailableGoals().isEmpty()) {
            FollowOwnerLiteGoal.attach(pm);
            return;
        }
        double d2 = mob.distanceToSqr(owner);
        if (d2 > 32.0 * 32.0 && owner.onGround()) {
            mob.teleportTo(owner.getX(), owner.getY(), owner.getZ());
        } else if (d2 > 7.0 * 7.0) {
            net.minecraft.world.entity.ai.behavior.BehaviorUtils.setWalkAndLookTargetMemories(mob, owner, 1.2F, 3);
        }
    }

    public static void sit(TamableAnimal t, boolean on) {
        ALLOW.set(true);
        try {
            t.setOrderedToSit(on);
        } finally {
            ALLOW.set(false);
        }
    }

    private static void pose(Mob mob, boolean on) {
        if (mob instanceof TamableAnimal t) {
            sit(t, on);
            t.setInSittingPose(on);
        } else if (mob instanceof Fox f) {
            f.setSitting(on);
        } else if (mob instanceof Panda p) {
            p.sit(on);
        } else if (mob instanceof Camel c) {
            if (on) c.sitDown();
            else c.standUp();
        }
    }

    public static void enforce(Mob mob) {
        if (!AttackTames.sitting(mob)) {
            if (mob instanceof TamableAnimal t && t.isOrderedToSit()) mob.setAttached(Genetics.SITTING, true);
            return;
        }
        if (!mob.isVehicle()) {
            if (mob instanceof PathfinderMob pm) pm.getNavigation().stop();
            mob.setTarget(null);
            net.minecraft.world.phys.Vec3 dm = mob.getDeltaMovement();
            if (dm.x != 0.0 || dm.z != 0.0) mob.setDeltaMovement(0.0, dm.y, 0.0);
        }
        if (mob.tickCount % 4 != 0) return;
        if (mob instanceof TamableAnimal t) {
            if (!t.isOrderedToSit()) mob.removeAttached(Genetics.SITTING);
        } else if (mob instanceof Fox f) {
            if (!f.isSitting()) f.setSitting(true);
        } else if (mob instanceof Panda p) {
            if (!p.isSitting()) p.sit(true);
        } else if (mob instanceof Camel c) {
            if (!c.isCamelSitting() && !c.isInPoseTransition() && !c.isVehicle()) c.sitDown();
        }
    }

    public static Component message(Mob mob, Mode mode) {
        String key = mode == Mode.STAY ? "advbreeding.sit.stay" : mode == Mode.FOLLOW ? "advbreeding.sit.follow" : "advbreeding.sit.wander";
        return Component.translatable(key, mob.getDisplayName());
    }
}
