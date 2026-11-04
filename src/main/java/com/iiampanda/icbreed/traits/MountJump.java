package com.iiampanda.icbreed.traits;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import com.iiampanda.icbreed.genetics.Genetics;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public final class MountJump {
    private static final Map<Mob, Float> PENDING = Collections.synchronizedMap(new WeakHashMap<>());

    private MountJump() {
    }

    public static boolean species(Mob m) {
        return m instanceof net.minecraft.world.entity.monster.hoglin.Hoglin
                || m instanceof net.minecraft.world.entity.animal.PolarBear
                || m instanceof net.minecraft.world.entity.animal.Panda
                || m instanceof net.minecraft.world.entity.animal.sniffer.Sniffer;
    }

    public static boolean can(Mob m) {
        return Rideable.is(m) && m.getFirstPassenger() instanceof Player;
    }

    public static void charge(Mob m, int power) {
        if (!can(m)) return;
        float scale = power >= 90 ? 1.0F : 0.4F + 0.4F * power / 90.0F;
        PENDING.put(m, scale);
    }

    public static void tick(Mob m) {
        Float s = PENDING.get(m);
        if (s == null) return;
        if (!m.onGround()) return;
        PENDING.remove(m);
        double jump = m.getAttributeValue(Attributes.JUMP_STRENGTH) * s;
        var boost = m.getEffect(MobEffects.JUMP);
        if (boost != null) jump += 0.1 * (boost.getAmplifier() + 1);
        Vec3 v = m.getDeltaMovement();
        m.setDeltaMovement(v.x, jump, v.z);
        double yaw = m.getYRot() * (Math.PI / 180.0);
        m.setDeltaMovement(m.getDeltaMovement().add(-0.4 * Math.sin(yaw) * s, 0, 0.4 * Math.cos(yaw) * s));
    }

    public static void roll(Mob m) {
        if (!species(m) || com.iiampanda.icbreed.platform.Att.has(m, Genetics.JUMP_ROLLED)) return;
        var rnd = m.getRandom();
        double v = 0.4 + rnd.nextDouble() * 0.2 + rnd.nextDouble() * 0.2 + rnd.nextDouble() * 0.2;
        var attr = m.getAttribute(Attributes.JUMP_STRENGTH);
        if (attr != null) attr.setBaseValue(v);
        com.iiampanda.icbreed.platform.Att.set(m, Genetics.JUMP_ROLLED, true);
    }
}
