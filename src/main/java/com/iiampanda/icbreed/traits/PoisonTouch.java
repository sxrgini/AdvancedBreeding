package com.iiampanda.icbreed.traits;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.iiampanda.icbreed.genetics.Effects;
import com.iiampanda.icbreed.platform.ServerTickEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

public final class PoisonTouch {
    private static final Set<Mob> CARRIERS = ConcurrentHashMap.newKeySet();
    private static final int DURATION_TICKS = 60;

    private PoisonTouch() {
    }

    public static void track(Mob mob, Effects fx) {
        if (fx != null && fx.poisonTouch > 0 && mob.isAlive() && !mob.isBaby()) CARRIERS.add(mob);
        else CARRIERS.remove(mob);
    }

    public static void register() {
        ServerTickEvents.END_LEVEL_TICK.register(level -> {
            if (level.getGameTime() % 4 != 0 || CARRIERS.isEmpty()) return;
            for (Mob frog : CARRIERS) {
                if (frog.isRemoved() || !frog.isAlive()) {
                    CARRIERS.remove(frog);
                    continue;
                }
                if (frog.level() != level) continue;
                touch(level, frog);
            }
        });
    }

    private static void touch(ServerLevel level, Mob frog) {
        Effects fx = com.iiampanda.icbreed.genetics.Genetics.effects(frog);
        if (fx == null || fx.poisonTouch <= 0) {
            CARRIERS.remove(frog);
            return;
        }
        int amplifier = Math.max(0, (int) Math.round(fx.poisonTouch) - 1);
        Player owner = frog instanceof net.minecraft.world.entity.animal.Animal a ? AttackTames.ownerOf(a) : null;
        AABB box = frog.getBoundingBox().inflate(0.1);
        for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, box, e -> e != frog && e.isAlive())) {
            if (victim == owner || victim.getType() == frog.getType()) continue;
            if (victim instanceof Player p && (p.isSpectator() || p.isCreative())) continue;
            if (frog.isPassengerOfSameVehicle(victim)) continue;
            victim.addEffect(new MobEffectInstance(MobEffects.POISON, DURATION_TICKS, amplifier), frog);
        }
    }
}
