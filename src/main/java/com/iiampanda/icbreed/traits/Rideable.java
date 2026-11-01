package com.iiampanda.icbreed.traits;

import com.iiampanda.icbreed.genetics.Effects;
import com.iiampanda.icbreed.genetics.Genetics;
import net.minecraft.world.entity.Entity;

public final class Rideable {
    private Rideable() {
    }

    public static boolean is(Entity e) {
        if (!(e instanceof net.minecraft.world.entity.Mob a) || !Genetics.supports(a) || !Genetics.isTamed(a)) return false;
        Effects fx = Genetics.effects(a);
        return fx != null && fx.rideable > 0;
    }

    public static boolean captain(Entity e) {
        if (!(e instanceof net.minecraft.world.entity.monster.Strider s) || !Genetics.isTamed(s)) return false;
        Effects fx = Genetics.effects(s);
        return fx != null && fx.captain > 0;
    }
}
