package com.iiampanda.icbreed.traits;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

import com.iiampanda.icbreed.genetics.Effects;
import com.iiampanda.icbreed.genetics.Genetics;
import com.iiampanda.icbreed.genetics.Taming;
import com.iiampanda.icbreed.mixin.MobAccessor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.fox.Fox;
import net.minecraft.world.entity.animal.panda.Panda;
import net.minecraft.world.entity.animal.polarbear.PolarBear;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.player.Player;

public final class AttackTames {
    private static final Map<UUID, UUID> COMMANDED = new ConcurrentHashMap<>();
    private static final Set<Animal> GOALED = Collections.newSetFromMap(new WeakHashMap<>());

    private AttackTames() {
    }

    public static boolean passive(Animal a) {
        return (a instanceof Hoglin || a instanceof PolarBear) && Genetics.isTamed(a);
    }

    public static boolean allowedVictim(Animal a, Player p) {
        return p.getUUID().equals(COMMANDED.get(a.getUUID()));
    }

    public static boolean is(Animal a) {
        if (!Genetics.isTamed(a)) return false;
        if (a instanceof Hoglin || a instanceof PolarBear) return true;
        if (a instanceof Fox || a instanceof Panda) {
            Effects fx = Genetics.effects(a);
            return fx != null && fx.feral > 0;
        }
        return false;
    }

    public static boolean owns(net.minecraft.world.entity.Mob a, Player p) {
        if (a instanceof net.minecraft.world.entity.TamableAnimal t && t.isTame() && t.getOwner() != null) return t.isOwnedBy(p);
        Taming t = a.getAttached(Genetics.TAMING);
        return t != null && (t.owner().isEmpty() || t.owner().get().equals(p.getUUID()));
    }

    private static boolean sameOwner(Animal a, LivingEntity other) {
        if (!(other instanceof Animal o)) return false;
        Taming ta = a.getAttached(Genetics.TAMING);
        Taming tb = o.getAttached(Genetics.TAMING);
        return ta != null && tb != null && tb.tamed() && ta.owner().isPresent() && ta.owner().equals(tb.owner());
    }

    public static void defend(ServerPlayer owner, LivingEntity attacker) {
        rally(owner, attacker);
    }

    public static void assist(ServerPlayer owner, LivingEntity victim) {
        rally(owner, victim);
    }

    private static void rally(ServerPlayer owner, LivingEntity enemy) {
        if (enemy == owner || !enemy.isAlive()) return;
        List<Animal> near = owner.level().getEntitiesOfClass(Animal.class, owner.getBoundingBox().inflate(24.0),
                a -> a.isAlive() && !a.isBaby() && a != enemy && is(a) && owns(a, owner));
        for (Animal a : near) {
            if (sameOwner(a, enemy)) continue;
            if (enemy instanceof Player p && owns(a, p)) continue;
            if (enemy instanceof Player p) COMMANDED.put(a.getUUID(), p.getUUID());
            else COMMANDED.remove(a.getUUID());
            ensureGoals(a);
            if (a instanceof Hoglin h) {
                @SuppressWarnings("unchecked")
                net.minecraft.world.entity.ai.Brain<Hoglin> brain = (net.minecraft.world.entity.ai.Brain<Hoglin>) h.getBrain();
                brain.setMemory(MemoryModuleType.ATTACK_TARGET, enemy);
            }
            a.setTarget(enemy);
        }
    }

    private static void ensureGoals(Animal a) {
        if (a instanceof Hoglin || !(a instanceof PathfinderMob pm)) return;
        if (GOALED.add(a)) {
            var goals = ((MobAccessor) (Object) a).icbreed$goals();
            goals.addGoal(2, new MeleeAttackGoal(pm, 1.3D, true));
            FollowOwnerLiteGoal.attach(pm);
        }
    }

    public static net.minecraft.world.entity.player.Player ownerOf(Animal a) {
        Taming t = a.getAttached(Genetics.TAMING);
        if (t == null || t.owner().isEmpty()) return null;
        return a.level().getPlayerByUUID(t.owner().get());
    }

    public static boolean sitting(net.minecraft.world.entity.Entity e) {
        return Boolean.TRUE.equals(e.getAttached(Genetics.SITTING));
    }

    public static boolean toggleSit(Animal a) {
        return StayPut.toggle(a);
    }

    public static void tick(net.minecraft.world.entity.Mob mob, net.minecraft.server.level.ServerLevel level) {
        if (!(mob instanceof Animal a)) return;
        if (a instanceof Hoglin || a instanceof PolarBear || a instanceof Fox || a instanceof Panda) {
            LivingEntity t = a.getTarget();
            if (t != null && (!t.isAlive() || t.isRemoved())) {
                a.setTarget(null);
                COMMANDED.remove(a.getUUID());
            }
            if (passive(a) && t instanceof Player p && !allowedVictim(a, p)) {
                a.setTarget(null);
            }
            if (a instanceof Hoglin hg && !hg.isConverting()) {
                Taming ht = hg.getAttached(Genetics.TAMING);
                if (hg.isBaby() || hg.isLeashed() || (ht != null && (ht.tamed() || ht.orphan()))) hg.setImmuneToZombification(true);
            } else if (a instanceof Hoglin hg2 && hg2.isConverting()) {
                Taming ht = hg2.getAttached(Genetics.TAMING);
                if (hg2.isBaby() || hg2.isLeashed() || (ht != null && (ht.tamed() || ht.orphan()))) hg2.setImmuneToZombification(true);
            }
            if (a instanceof Hoglin h && passive(h)) {
                @SuppressWarnings("unchecked")
                net.minecraft.world.entity.ai.Brain<Hoglin> brain = (net.minecraft.world.entity.ai.Brain<Hoglin>) h.getBrain();
                var mem = brain.getMemory(MemoryModuleType.ATTACK_TARGET);
                if (mem.isPresent() && mem.get() instanceof Player p && !allowedVictim(h, p)) {
                    brain.eraseMemory(MemoryModuleType.ATTACK_TARGET);
                }
                var cur = brain.getMemory(MemoryModuleType.ATTACK_TARGET);
                if (cur.isPresent() && (!cur.get().isAlive())) brain.eraseMemory(MemoryModuleType.ATTACK_TARGET);
            }
            if (is(a)) {
                ensureGoals(a);
            }
        }
    }
}
