package com.iiampanda.icbreed.genetics;

public enum Genotype {
    VIGOR("vigor", "VIG", 0.05),
    FITNESS("fitness", "FIT", 0.04),
    PHYSIQUE("physique", "PHY", 0.08),
    REFLEX("reflex", "REF", 0.04),
    TOUGHNESS("toughness", "TGH", -0.04),
    ADAPTATION("adaptation", "ADP", -0.06),
    INSTINCT("instinct", "INS", 0.08);

    public static final int MIN = 0;
    public static final int MAX = 10;
    public static final int BASELINE = 3;
    public static final Genotype[] ALL = values();

    public final String id;
    public final String abbr;

    public double perLevel;
    public final double defaultPerLevel;

    Genotype(String id, String abbr, double perLevel) {
        this.id = id;
        this.abbr = abbr;
        this.perLevel = perLevel;
        this.defaultPerLevel = perLevel;
    }

    public String nameKey() {
        return "advbreeding.genotype." + id;
    }

    public String descKey() {
        return "advbreeding.genotype." + id + ".desc";
    }
}
