package com.iiampanda.icbreed.genetics;

import java.util.Random;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public enum Bloodline {
    WILD("wild", 300, 0xB0B0B0,
            new Mod(Stat.FOOD_USE, 0.05, 0),
            new Mod(Stat.BLEED, 0, 0.01, 5)),
    BRAVE("brave", 50, 0xE05555,
            new Mod(Stat.JUMP_PCT, -0.10, 0),
            new Mod(Stat.DAMAGE_PCT, 0, 0.01, 2)),
    CAREFUL("careful", 50, 0x55C8B4,
            new Mod(Stat.SPEED_PCT, -0.10, 0),
            new Mod(Stat.DAMAGE_PCT, 0, 0.0005, 2)),
    TIMID("timid", 50, 0x8EC5FF,
            new Mod(Stat.DAMAGE_PCT, -0.10, 0),
            new Mod(Stat.SPEED_PCT, 0, 0.01)),
    BOLD("bold", 50, 0xFF9A3C,
            new Mod(Stat.PHYS_RES, -0.10, 0),
            new Mod(Stat.JUMP_PCT, 0, 0.005, 2),
            new Mod(Stat.SAT_RATE, 0, -0.01, 2)),
    HARDY("hardy", 50, 0x7FB05A,
            new Mod(Stat.FOOD_USE, 0.10, 0),
            new Mod(Stat.HEALTH_PCT, 0, 0.01, 2),
            new Mod(Stat.SAT_RATE, 0, -0.01, 2)),
    STOUT("stout", 50, 0xC9A26B,
            new Mod(Stat.THREAT, 0.10, 0),
            new Mod(Stat.PHYS_RES, 0, 0.01, 2)),
    AMBITIOUS("ambitious", 50, 0xF2E24A,
            new Mod(Stat.XP, 0.50, 0)),
    RESOLUTE("resolute", 25, 0xD8DDE6,
            new Mod(Stat.HEALTH_PCT, -0.10, 0),
            new Mod(Stat.SAT_RATE, 0, -0.01, 2),
            new Mod(Stat.FERTILITY, 0, 0.01)),
    UNSTABLE("unstable", 25, 0xB06BFF,
            new Mod(Stat.FERTILITY, -0.50, 0),
            new Mod(Stat.MUTATION, 0, 0.01, 2),
            new Mod(Stat.PHENO_MUTATION, 0, 0.01, 2)),
    SAVAGE("savage", 25, 0xA01818,
            new Mod(Stat.REGEN_PCT, -0.50, 0),
            new Mod(Stat.LEECH, 0, 0.01)),
    ALPHA("alpha", 10, 0xFFC93C,
            new Mod(Stat.DAMAGE_PCT, 0, 0.01),
            new Mod(Stat.HEALTH_PCT, 0, 0.01));

    public record Mod(Stat stat, double fixed, double perLevel, int every) {
        public Mod(Stat stat, double fixed, double perLevel) {
            this(stat, fixed, perLevel, 1);
        }

        public double at(int level) {
            return fixed + perLevel * (level / Math.max(1, every));
        }
    }

    public static final Bloodline[] ALL = values();
    public static int TOTAL_WEIGHT;

    static {
        recompute();
    }

    public static void recompute() {
        int t = 0;
        for (Bloodline b : ALL) t += b.weight;
        TOTAL_WEIGHT = Math.max(1, t);
    }

    public final String id;
    public int weight;
    public final int color;
    public Mod[] mods;
    public final int defaultWeight;
    public final Mod[] defaultMods;

    Bloodline(String id, int weight, int color, Mod... mods) {
        this.id = id;
        this.weight = weight;
        this.color = color;
        this.mods = mods;
        this.defaultWeight = weight;
        this.defaultMods = mods;
    }

    public void configure(int weight, Mod[] mods) {
        this.weight = weight;
        this.mods = mods;
    }

    public Component lineComponent(int index) {
        Mod m = mods[index];
        if (index < defaultMods.length && m.equals(defaultMods[index])) return Component.translatable(lineKey(index));
        MutableComponent c = Component.empty();
        boolean first = true;
        if (m.fixed() != 0) {
            c.append(Component.literal(m.stat().format(m.fixed()) + " ")).append(Component.translatable(m.stat().nameKey()));
            first = false;
        }
        if (m.perLevel() != 0) {
            if (!first) c.append(Component.literal(", "));
            c.append(Component.literal(m.stat().format(m.perLevel()) + " ")).append(Component.translatable(m.stat().nameKey())).append(Component.literal(" "));
            c.append(m.every() > 1 ? Component.translatable("advbreeding.gui.every_n", m.every()) : Component.translatable("advbreeding.gui.every_one"));
        }
        return c;
    }

    public String nameKey() {
        return "advbreeding.bloodline." + id;
    }

    public String lineKey(int index) {
        return "advbreeding.bloodline." + id + ".line" + index;
    }

    public void apply(Effects e, int level) {
        for (Mod m : mods) m.stat.apply(e, m.at(level));
    }

    public static Bloodline byId(String id) {
        for (Bloodline b : ALL) if (b.id.equals(id)) return b;
        return WILD;
    }

    public static Bloodline roll(Random random) {
        int r = random.nextInt(TOTAL_WEIGHT);
        for (Bloodline b : ALL) {
            r -= b.weight;
            if (r < 0) return b;
        }
        return WILD;
    }
}
