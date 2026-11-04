package com.iiampanda.icbreed.taming;

import java.util.UUID;

import com.iiampanda.icbreed.genetics.Genetics;
import com.iiampanda.icbreed.genetics.Taming;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.player.Player;

public final class PigGuard {
    private PigGuard() {
    }

    static boolean guarding(Pig pig) {
        Taming t = pig.getAttached(Genetics.TAMING);
        return t != null && t.tamed() && !pig.isBaby();
    }

    static Player owner(Pig pig) {
        Taming t = pig.getAttached(Genetics.TAMING);
        if (t == null || !t.tamed()) return null;
        UUID id = t.owner().orElse(null);
        return id == null ? null : pig.level().getPlayerByUUID(id);
    }

    static boolean validTarget(Pig pig, LivingEntity target) {
        if (target == null || !target.isAlive() || target == pig) return false;
        if (target instanceof Creeper || target instanceof Ghast || target instanceof Player) return false;
        return !(target instanceof Animal a && !Genetics.isWild(a));
    }

    public static class OwnerHurtBy extends TargetGoal {
        private final Pig pig;
        private LivingEntity victim;
        private int timestamp;

        public OwnerHurtBy(Pig pig) {
            super(pig, false);
            this.pig = pig;
        }

        @Override
        public boolean canUse() {
            if (!guarding(pig)) return false;
            Player owner = owner(pig);
            if (owner == null) return false;
            victim = owner.getLastHurtByMob();
            return owner.getLastHurtByMobTimestamp() != timestamp && validTarget(pig, victim)
                    && canAttack(victim, TargetingConditions.DEFAULT);
        }

        @Override
        public void start() {
            pig.setTarget(victim);
            Player owner = owner(pig);
            if (owner != null) timestamp = owner.getLastHurtByMobTimestamp();
            super.start();
        }
    }

    public static class OwnerHurt extends TargetGoal {
        private final Pig pig;
        private LivingEntity victim;
        private int timestamp;

        public OwnerHurt(Pig pig) {
            super(pig, false);
            this.pig = pig;
        }

        @Override
        public boolean canUse() {
            if (!guarding(pig)) return false;
            Player owner = owner(pig);
            if (owner == null) return false;
            victim = owner.getLastHurtMob();
            return owner.getLastHurtMobTimestamp() != timestamp && validTarget(pig, victim)
                    && canAttack(victim, TargetingConditions.DEFAULT);
        }

        @Override
        public void start() {
            pig.setTarget(victim);
            Player owner = owner(pig);
            if (owner != null) timestamp = owner.getLastHurtMobTimestamp();
            super.start();
        }
    }

    public static class Retaliate extends HurtByTargetGoal {
        private final Pig pig;

        public Retaliate(Pig pig) {
            super(pig);
            this.pig = pig;
        }

        @Override
        public boolean canUse() {
            return guarding(pig) && super.canUse();
        }
    }

    public static class Attack extends MeleeAttackGoal {
        private final Pig pig;

        public Attack(Pig pig) {
            super(pig, 1.25, true);
            this.pig = pig;
        }

        @Override
        public boolean canUse() {
            return guarding(pig) && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return guarding(pig) && super.canContinueToUse();
        }
    }
}
