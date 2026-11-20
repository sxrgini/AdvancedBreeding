package com.iiampanda.icbreed.compat;

import com.iiampanda.icbreed.genetics.Genetics;
import com.iiampanda.icbreed.genetics.Genome;
import com.iiampanda.icbreed.genetics.Sex;
import com.iiampanda.icbreed.genetics.Taming;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;

/** Reads the data exposed to EMF / ETF. Works on the client because the attachments are synced. */
public final class AnimalInfo {
    private AnimalInfo() {
    }

    /** The animal's sex, or null when the entity is not an Advanced Breeding animal. */
    public static Sex sex(Object entity) {
        if (!(entity instanceof Mob mob)) return null;
        Genome g = mob.getAttached(Genetics.GENOME);
        return g == null ? null : g.sex;
    }

    public static boolean tamed(Object entity) {
        if (!(entity instanceof Entity e)) return false;
        Taming t = e.getAttached(Genetics.TAMING);
        return t != null && t.tamed();
    }
}
