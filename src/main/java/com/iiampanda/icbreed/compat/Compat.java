package com.iiampanda.icbreed.compat;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;

public final class Compat {
    public interface Inheritor {
        boolean inherit(Mob baby, Mob a, Mob b, double mutationChance);
    }

    private static final List<Function<Mob, String>> TREES = new ArrayList<>();
    private static final List<Predicate<Mob>> UNTAMEABLE = new ArrayList<>();
    private static final List<Inheritor> INHERITORS = new ArrayList<>();
    private static final List<Function<Entity, String>> PHENOTYPES = new ArrayList<>();

    private Compat() {
    }

    public static void init() {
        if (Backport.present()) com.iiampanda.icbreed.compat.vb.VbCompat.init();
    }

    public static void tree(Function<Mob, String> resolver) {
        TREES.add(resolver);
    }

    public static void untameable(Predicate<Mob> predicate) {
        UNTAMEABLE.add(predicate);
    }

    public static void inheritor(Inheritor inheritor) {
        INHERITORS.add(inheritor);
    }

    public static void phenotype(Function<Entity, String> resolver) {
        PHENOTYPES.add(resolver);
    }

    public static String treeFor(Mob mob) {
        for (Function<Mob, String> f : TREES) {
            String key = f.apply(mob);
            if (key != null) return key;
        }
        return null;
    }

    public static boolean isUntameable(Mob mob) {
        for (Predicate<Mob> p : UNTAMEABLE) if (p.test(mob)) return true;
        return false;
    }

    public static boolean inherit(Mob baby, Mob a, Mob b, double chance) {
        for (Inheritor i : INHERITORS) if (i.inherit(baby, a, b, chance)) return true;
        return false;
    }

    public static String phenotype(Entity e) {
        for (Function<Entity, String> f : PHENOTYPES) {
            String s = f.apply(e);
            if (s != null) return s;
        }
        return null;
    }
}
