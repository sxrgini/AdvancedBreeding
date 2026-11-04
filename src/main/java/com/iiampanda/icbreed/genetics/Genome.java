package com.iiampanda.icbreed.genetics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import com.iiampanda.icbreed.IcBreedConfig;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;

public final class Genome {
    public static final int MAX_LEVEL = 35;

    public static final Codec<Genome> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.INT.fieldOf("sex").forGetter(g -> g.sex.ordinal()),
            Codec.INT.listOf().fieldOf("genes").forGetter(g -> {
                List<Integer> l = new ArrayList<>();
                for (int v : g.genes) l.add(v);
                return l;
            }),
            Codec.STRING.fieldOf("bloodline").forGetter(g -> g.bloodline.id),
            Codec.INT.fieldOf("generation").forGetter(g -> g.generation),
            Codec.INT.optionalFieldOf("xp", 0).forGetter(g -> g.xp),
            Codec.STRING.optionalFieldOf("mother", "").forGetter(g -> g.motherName),
            Codec.STRING.optionalFieldOf("father", "").forGetter(g -> g.fatherName)
    ).apply(i, (sex, genes, bloodline, generation, xp, mother, father) -> {
        int[] arr = new int[Genotype.ALL.length];
        Arrays.fill(arr, Genotype.BASELINE);
        for (int k = 0; k < arr.length && k < genes.size(); k++) arr[k] = clampGene(genes.get(k));
        return new Genome(Sex.byOrdinal(sex), arr, Bloodline.byId(bloodline), Math.max(0, generation),
                Math.max(0, xp), mother, father);
    }));

    public final Sex sex;
    private final int[] genes;
    public final Bloodline bloodline;
    public final int generation;
    public final int xp;
    public final String motherName;
    public final String fatherName;
    private final int level;
    private Effects cachedEffects;

    public Genome(Sex sex, int[] genes, Bloodline bloodline, int generation, int xp, String motherName, String fatherName) {
        this.sex = sex;
        this.genes = genes.clone();
        this.bloodline = bloodline;
        this.generation = generation;
        this.xp = Math.min(xp, xpForLevel(MAX_LEVEL));
        this.motherName = motherName == null ? "" : motherName;
        this.fatherName = fatherName == null ? "" : fatherName;
        this.level = levelForXp(this.xp);
    }

    public int get(Genotype g) {
        return genes[g.ordinal()];
    }

    public int total() {
        int t = 0;
        for (int v : genes) t += v;
        return t;
    }

    public boolean hasParents() {
        return !motherName.isEmpty() || !fatherName.isEmpty();
    }

    public static int xpCostOfLevel(int level) {
        int cost = 0;
        for (int l = 1; l <= level; l++) cost += 50 * ((l - 1) / 3 + 1);
        return level <= 0 ? 0 : levelStep(level);
    }

    private static int levelStep(int level) {
        int cost = 0;
        for (int l = 1; l <= level; l++) cost += 50 * ((l - 1) / 3 + 1) * 0 + 0;
        return cost;
    }

    public static int[] LEVEL_COSTS = defaultCosts();

    public static int[] defaultCosts() {
        int[] c = new int[MAX_LEVEL + 1];
        int cost = 0;
        for (int l = 1; l <= MAX_LEVEL; l++) {
            cost = l == 1 ? 50 : cost + 50 * ((l - 1) / 3 + 1);
            c[l] = cost;
        }
        return c;
    }

    public static int xpForLevel(int level) {
        int[] costs = LEVEL_COSTS;
        int total = 0;
        for (int l = 1; l <= Math.min(level, MAX_LEVEL); l++) total += costs[l];
        return total;
    }

    public static int levelForXp(int xp) {
        int lvl = 0;
        while (lvl < MAX_LEVEL && xp >= xpForLevel(lvl + 1)) lvl++;
        return lvl;
    }

    public int level() {
        return level;
    }

    public Genome withXp(int newXp) {
        return new Genome(sex, genes, bloodline, generation, newXp, motherName, fatherName);
    }

    public Effects effects() {
        Effects cached = cachedEffects;
        if (cached == null) {
            cached = new Effects();
            cached.add(genotypeEffects());
            cached.add(bloodlineEffects());
            cachedEffects = cached;
        }
        return cached;
    }

    private Effects cachedGenotype;
    private Effects cachedBloodline;

    public Effects genotypeEffects() {
        Effects c = cachedGenotype;
        if (c != null) return c;
        Effects e = new Effects();
        for (Genotype g : Genotype.ALL) {
            double v = g.perLevel * (get(g) - Genotype.BASELINE);
            switch (g) {
                case VIGOR -> e.healthPct += v;
                case FITNESS -> e.jumpPct += v;
                case PHYSIQUE -> e.auraPct += v;
                case REFLEX -> e.speedPct += v;
                case TOUGHNESS -> e.damageTakenPct += v;
                case ADAPTATION -> e.elementTakenPct += v;
                case INSTINCT -> {
                    e.growthPct += v;
                    e.eggPct += v;
                }
            }
        }
        return cachedGenotype = e;
    }

    public Effects bloodlineEffects() {
        Effects c = cachedBloodline;
        if (c != null) return c;
        Effects e = new Effects();
        bloodline.apply(e, level);
        return cachedBloodline = e;
    }

    public static Genome wild(Random r) {
        int[] genes = new int[Genotype.ALL.length];
        for (int i = 0; i < genes.length; i++) {
            int roll = r.nextInt(100);
            int d = roll < 3 ? -2 : roll < 22 ? -1 : roll < 78 ? 0 : roll < 97 ? 1 : 2;
            genes[i] = clampGene(Genotype.BASELINE + d);
        }
        return new Genome(r.nextBoolean() ? Sex.MALE : Sex.FEMALE, genes, Bloodline.roll(r), 0, 0, "", "");
    }

    public static Genome breed(Genome mother, Genome father, Random r, boolean worse, String motherName, String fatherName) {
        IcBreedConfig cfg = IcBreedConfig.get();
        int n = Genotype.ALL.length;
        int[] genes = new int[n];
        for (int i = 0; i < n; i++) {
            genes[i] = worse ? Math.min(mother.genes[i], father.genes[i])
                    : (r.nextBoolean() ? mother.genes[i] : father.genes[i]);
        }

        double mutation = cfg.baseMutationChance + mother.effects().mutationBonus + father.effects().mutationBonus;
        for (int i = 0; i < n; i++) {
            if (mutation > 0 && r.nextDouble() < mutation) {
                int step = 1 + r.nextInt(2);
                genes[i] = clampGene(genes[i] + (r.nextBoolean() ? step : -step));
            }
        }

        for (int i = 0; i < n; i++) {
            if (worse) {
                if (r.nextDouble() < 0.5) genes[i] = clampGene(genes[i] - 1);
            } else if (r.nextDouble() < cfg.statIncreaseChance) {
                genes[i] = clampGene(genes[i] + 1);
            }
        }

        int cap = cfg.maxGenotypePoints;
        while (sum(genes) > cap) {
            int i = r.nextInt(n);
            if (genes[i] > Genotype.MIN) genes[i]--;
        }

        Bloodline bl = r.nextBoolean() ? mother.bloodline : father.bloodline;
        return new Genome(r.nextBoolean() ? Sex.MALE : Sex.FEMALE, genes, bl,
                Math.max(mother.generation, father.generation) + 1, 0, motherName, fatherName);
    }

    private static int sum(int[] g) {
        int t = 0;
        for (int v : g) t += v;
        return t;
    }

    private static int clampGene(int v) {
        return Math.max(Genotype.MIN, Math.min(Genotype.MAX, v));
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeByte(sex.ordinal());
        for (int v : genes) buf.writeByte(v);
        buf.writeByte(bloodline.ordinal());
        buf.writeVarInt(generation);
        buf.writeVarInt(xp);
        buf.writeUtf(motherName);
        buf.writeUtf(fatherName);
    }

    public static Genome read(FriendlyByteBuf buf) {
        Sex sex = Sex.byOrdinal(buf.readByte());
        int[] genes = new int[Genotype.ALL.length];
        for (int i = 0; i < genes.length; i++) genes[i] = clampGene(buf.readByte());
        Bloodline bl = Bloodline.ALL[Math.floorMod((int) buf.readByte(), Bloodline.ALL.length)];
        int generation = buf.readVarInt();
        int xp = buf.readVarInt();
        String mother = buf.readUtf();
        String father = buf.readUtf();
        return new Genome(sex, genes, bl, generation, xp, mother, father);
    }
}
