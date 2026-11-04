package com.iiampanda.icbreed.traits;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.iiampanda.icbreed.IcBreed;
import com.iiampanda.icbreed.IcBreedConfig;
import com.iiampanda.icbreed.genetics.Effects;
import com.iiampanda.icbreed.genetics.Genetics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.Mob;

public final class AuraManager {
    private static final ResourceLocation AURA_ID = ResourceLocation.fromNamespaceAndPath(IcBreed.MOD_ID, "strength_aura");
    private static final Map<UUID, Double> THREAT = new ConcurrentHashMap<>();
    private static final Map<UUID, Double> SATURATION = new ConcurrentHashMap<>();
    private static final java.util.Set<UUID> NINE = ConcurrentHashMap.newKeySet();
    private static final double CAT_RANGE = com.iiampanda.icbreed.IcBreedConfig.get().catAuraRange;
    private static final Map<UUID, Effects> HIT = new ConcurrentHashMap<>();

    public static Effects hitAura(UUID player) {
        return HIT.get(player);
    }

    public static double saturation(UUID player) {
        return SATURATION.getOrDefault(player, 0.0);
    }

    public static boolean nineLives(UUID player) {
        return NINE.contains(player);
    }

    private static double best(double cur, double v) {
        return Math.abs(v) > Math.abs(cur) ? v : cur;
    }

    private static boolean passive(Mob a) {
        return a.getAttribute(Attributes.ATTACK_DAMAGE) == null;
    }

    private static boolean hasHitEffects(Effects fx) {
        return fx.leechChance != 0 || fx.bleedChance != 0 || fx.markSeconds != 0
                || fx.stunChance != 0 || fx.damagePct != 0 || fx.poisonBiteSeconds != 0;
    }

    private AuraManager() {
    }

    public static double threat(UUID player) {
        return THREAT.getOrDefault(player, 0.0);
    }

    public static void update(ServerLevel level, List<Mob> animals) {
        double range = IcBreedConfig.get().auraRange;
        double r2 = range * range;
        List<Mob> sources = new java.util.ArrayList<>();
        for (Mob a : animals) {
            if (!a.isAlive() || a.isBaby() || Genetics.isWild(a)) continue;
            Effects fx = Genetics.effects(a);
            if (fx != null && (fx.threatPct != 0 || fx.auraPct != 0 || fx.calmingPct != 0 || fx.regenAura > 0 || fx.fireAura > 0 || fx.satAura > 0 || fx.hopAura > 0 || fx.nineLives > 0 || fx.cling > 0 || (passive(a) && hasHitEffects(fx)))) sources.add(a);
        }
        for (ServerPlayer p : level.players()) {
            double threat = 0, aura = 0, sat = 0;
            boolean hop = false, regen = false, fire = false, food = false, nine = false, cling = false;
            Effects hit = new Effects();
            for (Mob a : sources) {
                double dx = a.getX() - p.getX(), dz = a.getZ() - p.getZ();
                double d2 = dx * dx + dz * dz;
                if (d2 <= CAT_RANGE * CAT_RANGE) {
                    Effects cf = Genetics.effects(a);
                    if (cf.nineLives > 0) nine = true;
                    if (cf.cling > 0) cling = true;
                }
                if (d2 > r2) continue;
                Effects fx = Genetics.effects(a);
                if (fx.regenAura > 0) regen = true;
                if (fx.hopAura > 0) hop = true;
                if (fx.fireAura > 0) fire = true;
                if (fx.satAura > 0) food = true;
                if (Math.abs(fx.threatPct) > Math.abs(threat)) threat = fx.threatPct;
                sat = best(sat, fx.calmingPct);
                double passiveDamage = passive(a) ? fx.damagePct : 0;
                aura = best(aura, fx.auraPct + passiveDamage);
                if (passive(a)) {
                    hit.leechChance = Math.max(hit.leechChance, fx.leechChance);
                    hit.bleedChance = Math.max(hit.bleedChance, fx.bleedChance);
                    hit.markSeconds = Math.max(hit.markSeconds, fx.markSeconds);
                    hit.stunChance = Math.max(hit.stunChance, fx.stunChance);
                    hit.poisonBiteSeconds = Math.max(hit.poisonBiteSeconds, fx.poisonBiteSeconds);
                }
            }
            if (hasHitEffects(hit) && hit.damagePct == 0) HIT.put(p.getUUID(), hit);
            else HIT.remove(p.getUUID());
            if (nine) NINE.add(p.getUUID()); else NINE.remove(p.getUUID());
            if (cling) p.setAttached(Genetics.CLING, true); else if (p.hasAttached(Genetics.CLING)) p.removeAttached(Genetics.CLING);
            if (fire) {
                p.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.FIRE_RESISTANCE, 100, 0, true, false, true));
            }
            if (hop) {
                p.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.JUMP, 100, 0, true, false, true));
            }
            if (regen) {
                p.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.REGENERATION, 100, 0, true, false, true));
            }
            if (food && level.getGameTime() % 100 < 20 && p.getFoodData().needsFood()) {
                p.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.SATURATION, 1, 0, true, false, true));
            }
            if (sat == 0) SATURATION.remove(p.getUUID());
            else SATURATION.put(p.getUUID(), sat);
            if (threat == 0) THREAT.remove(p.getUUID());
            else THREAT.put(p.getUUID(), threat);

            AttributeInstance dmg = p.getAttribute(Attributes.ATTACK_DAMAGE);
            if (dmg != null) {
                AttributeModifier cur = dmg.getModifier(AURA_ID);
                double want = Genetics.clamp(aura, -0.9, 5.0);
                if (cur == null ? want != 0 : cur.amount() != want) {
                    dmg.removeModifier(AURA_ID);
                    if (want != 0) {
                        dmg.addTransientModifier(new AttributeModifier(AURA_ID, want, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
                    }
                }
            }
        }
    }
}
