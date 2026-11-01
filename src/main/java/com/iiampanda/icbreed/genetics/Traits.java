package com.iiampanda.icbreed.genetics;

import java.util.HashMap;
import java.util.Map;

import com.iiampanda.icbreed.traits.TraitDef;
import com.iiampanda.icbreed.traits.TraitTrees;
import com.mojang.serialization.Codec;

public final class Traits {
    public static final Codec<Traits> CODEC = Codec.unboundedMap(Codec.STRING, Codec.INT).xmap(Traits::new, t -> t.ranks);
    public static final Traits NONE = new Traits(Map.of());

    private final Map<String, Integer> ranks;
    private Genome cachedFor;
    private Effects cached;

    public Traits(Map<String, Integer> ranks) {
        this.ranks = Map.copyOf(ranks);
    }

    public int rank(String id) {
        return ranks.getOrDefault(id, 0);
    }

    public int spent(TraitTrees.Tree tree) {
        int s = 0;
        for (var e : ranks.entrySet()) {
            TraitDef d = tree == null ? null : tree.get(e.getKey());
            if (tree != null && d == null) continue;
            s += e.getValue() * (d == null ? 1 : d.cost());
        }
        return s;
    }

    public int spent() {
        int s = 0;
        for (int r : ranks.values()) s += r;
        return s;
    }

    public Traits with(String id, int rank) {
        Map<String, Integer> m = new HashMap<>(ranks);
        m.put(id, rank);
        return new Traits(m);
    }

    public Effects effects(TraitTrees.Tree tree) {
        Effects e = new Effects();
        if (tree == null) return e;
        for (TraitDef d : tree.nodes) {
            int r = Math.min(rank(d.id()), d.maxRank());
            if (r <= 0) continue;
            for (TraitDef.Mod m : d.mods()) m.stat().apply(e, m.perRank() * r);
        }
        return e;
    }

    public Effects combined(Genome g, TraitTrees.Tree tree) {
        Effects c = cached;
        if (c != null && cachedFor == g) return c;
        Effects e = new Effects();
        e.add(g.effects());
        e.add(effects(tree));
        cachedFor = g;
        return cached = e;
    }
}
