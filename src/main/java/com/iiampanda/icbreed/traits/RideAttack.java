package com.iiampanda.icbreed.traits;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.iiampanda.icbreed.genetics.Effects;
import com.iiampanda.icbreed.genetics.Genetics;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public final class RideAttack {
    private static final Map<UUID, Long> READY = new HashMap<>();

    private RideAttack() {
    }

    public static void attack(ServerPlayer p) {
        if (!(p.getVehicle() instanceof Animal a) || a.getControllingPassenger() != p || !AttackTames.is(a)) return;
        Effects fx = Genetics.effects(a);
        if (fx == null || fx.rideable <= 0 || !AttackTames.owns(a, p)) return;
        ServerLevel level = p.level();
        long now = level.getGameTime();
        Long ready = READY.get(a.getUUID());
        if (ready != null && now < ready) return;
        READY.put(a.getUUID(), now + 20);
        a.swing(InteractionHand.MAIN_HAND, net.minecraft.world.item.component.SwingAnimation.DEFAULT);
        Vec3 look = p.getLookAngle();
        LivingEntity best = null;
        double bestDot = 0.3;
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, a.getBoundingBox().inflate(2.0, 1.0, 2.0),
                x -> x != a && x != p && x.isAlive() && !(x instanceof Player) && !x.isInvulnerable()
                        && !a.hasPassenger(x) && !x.isPassengerOfSameVehicle(a))) {
            if (e instanceof Animal o && Genetics.isTamed(o) && AttackTames.owns(o, p)) continue;
            Vec3 d = e.position().add(0, e.getBbHeight() * 0.5, 0).subtract(a.position().add(0, a.getBbHeight() * 0.5, 0));
            if (d.lengthSqr() < 1.0E-6) continue;
            double dot = d.normalize().dot(look);
            if (dot > bestDot) {
                bestDot = dot;
                best = e;
            }
        }
        if (best != null) a.doHurtTarget(level, best);
    }
}
