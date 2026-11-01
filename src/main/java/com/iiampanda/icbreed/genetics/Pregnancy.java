package com.iiampanda.icbreed.genetics;

import java.util.Optional;
import java.util.UUID;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;

public record Pregnancy(Genome father, String fatherName, Optional<UUID> fatherId, long dueTime, boolean worse) {
    public static final Codec<Pregnancy> CODEC = RecordCodecBuilder.create(i -> i.group(
            Genome.CODEC.fieldOf("father").forGetter(Pregnancy::father),
            Codec.STRING.optionalFieldOf("fatherName", "").forGetter(Pregnancy::fatherName),
            UUIDUtil.CODEC.optionalFieldOf("fatherId").forGetter(Pregnancy::fatherId),
            Codec.LONG.fieldOf("dueTime").forGetter(Pregnancy::dueTime),
            Codec.BOOL.optionalFieldOf("worse", false).forGetter(Pregnancy::worse)
    ).apply(i, Pregnancy::new));

    public Pregnancy withWorse() {
        return new Pregnancy(father, fatherName, fatherId, dueTime, true);
    }

    public long ticksLeft(long gameTime) {
        return Math.max(0L, dueTime - gameTime);
    }
}
