package com.iiampanda.icbreed.genetics;

public enum Sex {
    MALE("male", "♂", 0x6FA8FF),
    FEMALE("female", "♀", 0xFF8FC8);

    public final String id;
    public final String symbol;
    public final int color;

    Sex(String id, String symbol, int color) {
        this.id = id;
        this.symbol = symbol;
        this.color = color;
    }

    public static Sex byOrdinal(int i) {
        Sex[] v = values();
        return v[Math.floorMod(i, v.length)];
    }
}
